package com.example.proyectoredquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    Button bnt_regist, btn_login, btn_restaurar;
    EditText emailI, passwordI;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar la conexión a Internet al inicio de la actividad
        if (!isInternetAvailable()) {
            showNoInternetDialogAndLogout();
        }

        mAuth= FirebaseAuth.getInstance();

        bnt_regist = findViewById(R.id.btn_registro);
        emailI = findViewById(R.id.correoI);
        passwordI = findViewById(R.id.contrasenaI);
        btn_login = findViewById(R.id.btn_iniciar);
        btn_restaurar = findViewById(R.id.olvidar);

        bnt_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iregistro = new Intent(MainActivity.this, RegistroActivity.class);
                startActivities(new Intent[]{iregistro});
            }
        });

        btn_restaurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iregistro = new Intent(MainActivity.this, RecuperarActivity.class);
                startActivities(new Intent[]{iregistro});
            }
        });


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailUser = emailI.getText().toString().trim();
                String passUser = passwordI.getText().toString().trim();
                    // No son credenciales de administrador, realiza el inicio de sesión normal
                    if (TextUtils.isEmpty(emailUser) || TextUtils.isEmpty(passUser)) {
                        Toast.makeText(MainActivity.this, "Ingrese los datos", Toast.LENGTH_SHORT).show();
                    } else {
                        loginUser(emailUser, passUser);
                    }
            }
        });

    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return false;
    }

    // Función para mostrar el cuadro de diálogo cuando no hay conexión a Internet y cerrar sesión
    private void showNoInternetDialogAndLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sin conexión a Internet")
                .setMessage("Por favor, verifica tu conexión a Internet.")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar sesión y redirigir al usuario al Main Activity
                        logoutAndRedirectToMainActivity();
                    }
                })
                .setCancelable(false) // Impide que el usuario cierre el diálogo haciendo clic fuera de él
                .show();
    }

    // Función para cerrar sesión y redirigir al usuario al Main Activity
    private void logoutAndRedirectToMainActivity() {
        // Aquí puedes agregar la lógica para cerrar la sesión, por ejemplo, limpiar las preferencias de usuario
        // o realizar cualquier acción necesaria para cerrar la sesión.

        // Redirigir al usuario al Main Activity
        //mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean esCuentaDeAdmin(String email, String contraseña) {
        // Verifica aquí si las credenciales coinciden con las de una cuenta de administrador
        return email.equals("admin@gmail.com") && contraseña.equals("admin123#");  //checar si se puden poner mas cuentas de admin
    }

    private void loginUser(String emailUser, String passUser) {
        mAuth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Verificar el tipo de usuario
                        String userId = user.getUid();
                        verificarTipoUsuario(userId);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void verificarTipoUsuario(String userId) {
        // Obtener referencia a la colección "rqUsers"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("rqUsers").document(userId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Obtener el atributo "tipo" del documento
                        String tipoUsuario = document.getString("tipo");

                        // Verificar el tipo de usuario
                        if ("administrador".equals(tipoUsuario)) {
                            // Es un administrador, redirige a la interfaz del administrador
                            finish();
                            startActivity(new Intent(MainActivity.this, MenuAdministrador.class));
                            Toast.makeText(MainActivity.this, "¡Bienvenido Administrador!", Toast.LENGTH_SHORT).show();
                        } else {
                            // No es un administrador, verifica si el correo electrónico está verificado
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // El correo electrónico está verificado, redirige a la interfaz de usuario normal
                                finish();
                                startActivity(new Intent(MainActivity.this, MenuUserActivity.class));
                                Toast.makeText(MainActivity.this, "¡Bienvenid@!", Toast.LENGTH_SHORT).show();
                            } else {
                                // El correo electrónico no está verificado, muestra un mensaje o realiza otras acciones si es necesario
                                Toast.makeText(MainActivity.this, "Verifique su correo electrónico antes de iniciar sesión.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Cerrar sesión para evitar intentos de inicio de sesión no verificados
                            }
                        }
                    } else {
                        // El documento no existe, trata este caso según tus necesidades
                        Toast.makeText(MainActivity.this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()){
            // Usuario autenticado y correo electrónico verificado
            verificarTipoUsuario(user.getUid());
        }
    }

}