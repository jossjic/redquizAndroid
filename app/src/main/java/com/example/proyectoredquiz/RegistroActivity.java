package com.example.proyectoredquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistroActivity extends AppCompatActivity {

    Button btn_register, btn_index;
    EditText name, lastname, email, password, confPassword, date;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    private Spinner spinnerGender;
    private ArrayAdapter<CharSequence> genderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        // Verificar la conexión a Internet al inicio de la actividad
        if (!isInternetAvailable()) {
            showNoInternetDialogAndLogout();
        }

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.nombre);
        lastname = findViewById(R.id.apellidos);
        email = findViewById(R.id.correo);
        password = findViewById(R.id.contrasena);
        confPassword = findViewById(R.id.confContrasena);
        date = findViewById(R.id.fechaN);
        //curp = findViewById(R.id.curp);
        btn_register = findViewById(R.id.btn_registrar);
        btn_index = findViewById(R.id.btn_inicio);

        //SPINNER
        spinnerGender = findViewById(R.id.genero);
        genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedGender = (String) genderAdapter.getItem(i);
                Toast.makeText(RegistroActivity.this, "Género: " + selectedGender, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent index = new Intent(RegistroActivity.this, MainActivity.class);
                startActivities(new Intent[]{index});
            }
        });


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameUser = name.getText().toString().trim();
                String lastnameUser = lastname.getText().toString().trim();
                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();
                String confirmPassUser = confPassword.getText().toString().trim();
                String dateUser = date.getText().toString().trim();
                //String curpUser = curp.getText().toString().trim();
                String selectedGender = spinnerGender.getSelectedItem().toString();

                if (nameUser.isEmpty() || lastnameUser.isEmpty() || emailUser.isEmpty() || passUser.isEmpty() || confirmPassUser.isEmpty() || dateUser.isEmpty()){
                    Toast.makeText(RegistroActivity.this, "Complete los datos", Toast.LENGTH_SHORT).show();
                } else {
                    if (passUser.equals(confirmPassUser)) {
                        if (isValidDate(dateUser)) {
                            // La fecha tiene el formato correcto, proceder con el registro
                            registerUser(nameUser, lastnameUser, emailUser, passUser, dateUser, selectedGender);
                        } else {
                            Toast.makeText(RegistroActivity.this, "Formato de fecha no válido. Utilice dd/mm/aaaa", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    }
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
        Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // REGEX PARA LA FECHA
    // Método para validar el formato de la fecha
    private boolean isValidDate(String date) {
        String regex = "^(0[1-9]|[1-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/\\d{4}$";
        return date.matches(regex);
    }

    private void registerUser(String nameUser, String lastnameUser, String emailUser, String passUser, String dateUser, String selectedGender) {
        mAuth.fetchSignInMethodsForEmail(emailUser).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                        // Email is already in use
                        Toast.makeText(RegistroActivity.this, "El correo electrónico ya está en uso", Toast.LENGTH_SHORT).show();
                    } else {
                        // VERIFICAR CONTRASEÑA SEGURA
                        if (passUser.length() < 8 || !containsUpperCase(passUser) || !containsNumber(passUser)) {
                            Toast.makeText(RegistroActivity.this, "La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número", Toast.LENGTH_SHORT).show();
                            return; // Sale del método si la validación de la contraseña falla
                        }

                        // Email is not in use, proceed with registration
                        mAuth.createUserWithEmailAndPassword(emailUser, passUser)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Rest of your registration logic
                                            FirebaseUser user = mAuth.getCurrentUser();

                                            if (user != null) {
                                                // Enviar correo de verificación
                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> emailTask) {
                                                        if (emailTask.isSuccessful()) {
                                                            // Correo de verificación enviado exitosamente
                                                            Toast.makeText(RegistroActivity.this, "Se ha enviado un correo de verificación. Por favor, verifique su correo electrónico.", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            // Error al enviar el correo de verificación
                                                            Toast.makeText(RegistroActivity.this, "Error al enviar el correo de verificación: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }

                                            // Resto de tu lógica de registro
                                            String id = mAuth.getCurrentUser().getUid();
                                            String tipo = "usuario";
                                            createRecompensasDocument(id);
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("id", id);
                                            map.put("nombre", nameUser);
                                            map.put("apellidos", lastnameUser);
                                            map.put("email", emailUser);
                                            map.put("fechaNacimiento", dateUser);
                                            map.put("genero", selectedGender);
                                            map.put("vidas", 5);
                                            map.put("puntaje", 0);
                                            map.put("prendaI", 1);
                                            map.put("prendaS", 1);
                                            map.put("tipo", tipo);

                                            mFirestore.collection("rqUsers").document(id).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    finish();
                                                    startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                                                    Toast.makeText(RegistroActivity.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(RegistroActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(RegistroActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                } else {
                    Toast.makeText(RegistroActivity.this, "Error al verificar el correo electrónico", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean containsUpperCase(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNumber(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private void createRecompensasDocument(String id) {
        Map<String, Object> recompensasMap = new HashMap<>();

        recompensasMap.put("id", id);
        recompensasMap.put("recompensa1", false);
        recompensasMap.put("recompensa2", false);
        recompensasMap.put("recompensa3", false);
        recompensasMap.put("recompensa4", false);
        recompensasMap.put("recompensa5", false);

        // Crear el documento en la colección "rqRecompensas" con el ID del usuario
        mFirestore.collection("rqRecompensas").document(id).set(recompensasMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Documento creado con éxito
                        //Toast.makeText(RegistroActivity.this, "Documento de recompensas creado con éxito", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error al crear el documento
                        Toast.makeText(RegistroActivity.this, "Error al crear el documento de recompensas", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}