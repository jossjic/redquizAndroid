package com.example.proyectoredquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MenuUserActivity extends AppCompatActivity {

    Button btn_exit, btn_perfil, btn_jugar, btn_puntaje, btn_avatar;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private TextView nombreUsuario, vidas, mensajeTiempo;
    private String idUser;
    private TextView reloj;
    String TAG = "Main";
    private CountDownTimer countDownTimer;
    private int tiempoRestante;
    private int vidasDisponibles;
    ImageView profile, editarF;
    String generoU;
    boolean regenero = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_user);

        // Verificar la conexión a Internet al inicio de la actividad
        if (!isInternetAvailable()) {
            showNoInternetDialogAndLogout();
        }
            mAuth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();
            idUser = mAuth.getCurrentUser().getUid();

            editarF = findViewById(R.id.editarF);
            profile = findViewById(R.id.fotoPerfil);
            mensajeTiempo = findViewById(R.id.mensaje);
            btn_exit = findViewById(R.id.btn_cerrar);
            btn_perfil = findViewById(R.id.btn_perfil);
            btn_avatar = findViewById(R.id.btn_avatar);
            btn_jugar = findViewById(R.id.btn_jugar);
            btn_puntaje = findViewById(R.id.btn_puntaje);
            nombreUsuario = findViewById(R.id.nombreU);
            vidas = findViewById(R.id.vidasUsuario);

            reloj = findViewById(R.id.hora);
            long duration = TimeUnit.MINUTES.toMillis(1);

            //tiempoRestante = 15000;

            mensajeVidas();

            editarF.setVisibility(View.GONE);

            btn_perfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    countDownTimer.cancel();
                    Intent index = new Intent(MenuUserActivity.this, Perfil.class);
                    startActivities(new Intent[]{index});
                }
            });

            // OBTENER LAS VIDAS DEL USUARIO
            DocumentReference documentReference = fStore.collection("rqUsers").document(idUser);
            documentReference.addSnapshotListener(this, (documentSnapshot, e) -> {
                if (e != null) {
                    // Manejar el error, si es necesario
                    Log.e("ERROR", "Error al escuchar cambios en el documento", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Actualizar el valor de las vidas en el TextView
                    generoU = documentSnapshot.getString("genero");
                    if (generoU.equals("Masculino")){
                        profile.setImageResource(R.drawable.profilemen);
                    } else {
                        profile.setImageResource(R.drawable.profilewoman);
                    }
                    vidasDisponibles = documentSnapshot.getLong("vidas").intValue();
                    vidas.setText(String.valueOf(vidasDisponibles)+ " vidas");
                    nombreUsuario.setText(documentSnapshot.getString("nombre"));
                }
            });

            // ACCEDER A PUNTAJE
            btn_puntaje.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    countDownTimer.cancel();
                    Intent index = new Intent(MenuUserActivity.this, MiPuntaje.class);
                    startActivities(new Intent[]{index});
                }
            });

            // ACCEDER AL AVATAR
            btn_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    countDownTimer.cancel();
                    Intent index = new Intent(MenuUserActivity.this, miavatar.class);
                    startActivities(new Intent[]{index});
                }
            });

            // INICIAR JUEGO
            btn_jugar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Verificar si el usuario tiene vidas disponibles
                    int vidasDisponibles = Integer.parseInt(vidas.getText().toString().replaceAll("\\D+", ""));

                    if (vidasDisponibles > 0) {
                        countDownTimer.cancel();
                        // El usuario tiene vidas, iniciar el juego
                        Intent index = new Intent(MenuUserActivity.this, PreguntaActivity.class);
                        startActivities(new Intent[]{index});
                    } else {
                        // El usuario no tiene vidas, mostrar un mensaje
                        mostrarMensajeSinVidas();
                    }
                }
            });

            // CERRAR SESIÓN
            btn_exit.setOnClickListener(view -> {
                // Mostrar un cuadro de diálogo de confirmación
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuUserActivity.this);
                builder.setMessage("¿Estás seguro de que deseas cerrar la sesión?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            mAuth.signOut();
                            finish();
                            startActivity(new Intent(MenuUserActivity.this, MainActivity.class));
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // No hacer nada y cerrar el cuadro de diálogo
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            });

            // COUNT
            // Establecer el tiempo inicial en 1 minuto (60,000 milisegundos)
           tiempoRestante = 60000;

            // Inicializar el CountDownTimer con el nuevo tiempoRestante
            countDownTimer = new CountDownTimer(tiempoRestante, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tiempoRestante = (int) millisUntilFinished;

                    if (vidasDisponibles == 5) {
                        // Mostrar "00:00" si vidasDisponibles es igual a 5
                        reloj.setText("00:00");
                        reloj.setVisibility(View.GONE);
                    } else if (vidasDisponibles < 5 && regenero == false){
                        reloj.setVisibility(View.VISIBLE);
                        // Mostrar el tiempo en minutos y segundos
                        long minutos = TimeUnit.MILLISECONDS.toMinutes(tiempoRestante);
                        long segundos = TimeUnit.MILLISECONDS.toSeconds(tiempoRestante) -
                                TimeUnit.MINUTES.toSeconds(minutos);

                        String tiempoFormato = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);

                        reloj.setText(tiempoFormato);
                    } else if (vidasDisponibles < 5 && regenero == true) {
                        reloj.setText("00:00");
                    }
                }


                @Override
                public void onFinish() {
                    countDownTimer.cancel();
                    regenerarVida();
                    //countDownTimer.start();
                    //reiniciarTemporizador();
                }
            };
            // Iniciar el contador
            countDownTimer.start();
        // reloj.setVisibility(View.VISIBLE);

    }
    @Override
    protected void onResume() {
        super.onResume();

        // Reiniciar el CountDownTimer solo si aún hay vidas disponibles
        int vidasDisponibles = Integer.parseInt(vidas.getText().toString().replaceAll("\\D+", ""));
        if (vidasDisponibles < 0) {
            reiniciarTemporizador();
            countDownTimer.start();
        }
    }



    // Función para verificar la conexión a Internet
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

        // Redirigir al usuario al Main Activity
        //mAuth.signOut();
        Intent intent = new Intent(MenuUserActivity.this, MenuUserActivity.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual
    }

    private void mensajeVidas() {
        DocumentReference documentReference = fStore.collection("rqUsers").document(idUser);

        documentReference.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (e != null) {
                // Manejar el error, si es necesario
                Log.e("ERROR", "Error al escuchar cambios en el documento", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                int vidasActuales = documentSnapshot.getLong("vidas").intValue();

                // Verificar si el número de vidas es menor que 5 antes de regenerar
                if (vidasActuales == 5) {
                    mensajeTiempo.setText("¡Vidas Completas!");
                } else {
                    mensajeTiempo.setText("Tiempo aproximado para la próxima vida:");
                }
            }
        });
    }


    // Método para regenerar una vida y actualizar en la base de datos
    // Método para regenerar una vida y actualizar en la base de datos
    private void regenerarVida() {
        // Obtener las vidas actuales del usuario
        DocumentReference documentReference = fStore.collection("rqUsers").document(idUser);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    int vidasActuales = document.getLong("vidas").intValue();

                    // Verificar si el número de vidas es menor que 5 antes de regenerar
                    if (vidasActuales < 5) {
                        // Incrementar el número de vidas y actualizar en la base de datos
                        documentReference.update("vidas", vidasActuales + 1);

                        // Reiniciar el temporizador
                        reiniciarTemporizador();
                    } else {
                        // Detener el contador si el número de vidas es igual a 5
                        countDownTimer.cancel();
                    }
                }
            } else {
                Log.d("TAG", "Error obteniendo documento: ", task.getException());
            }
        });
    }

    // Método para reiniciar el temporizador
    private void reiniciarTemporizador() {
        // Cancelar el temporizador si está en ejecución
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Establecer el tiempo inicial en 1 minuto (60,000 milisegundos)
        tiempoRestante = 60000;

        // Inicializar el CountDownTimer con el nuevo tiempoRestante
        countDownTimer = new CountDownTimer(tiempoRestante, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestante = (int) millisUntilFinished;

                tiempoRestante = (int) millisUntilFinished;

                if (vidasDisponibles == 5) {
                    // Mostrar "00:00" si vidasDisponibles es igual a 5
                    reloj.setText("00:00");
                    reloj.setVisibility(View.GONE);
                } else if (vidasDisponibles < 5 && regenero == false){
                    reloj.setVisibility(View.VISIBLE);
                    // Mostrar el tiempo en minutos y segundos
                    long minutos = TimeUnit.MILLISECONDS.toMinutes(tiempoRestante);
                    long segundos = TimeUnit.MILLISECONDS.toSeconds(tiempoRestante) -
                            TimeUnit.MINUTES.toSeconds(minutos);

                    String tiempoFormato = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);

                    reloj.setText(tiempoFormato);
                } else if (vidasDisponibles < 5 && regenero == true) {
                    reloj.setText("00:00");
                }
            }

            @Override
            public void onFinish() {
                reloj.setText("00:00");
                countDownTimer.cancel();
                regenerarVida(); // Llama a la función para regenerar la vida
            }
        };

        // Iniciar el temporizador
        countDownTimer.start();
    }



    // Método para mostrar el mensaje cuando el usuario no tiene vidas
    private void mostrarMensajeSinVidas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuUserActivity.this);
        builder.setTitle("¡Vidas Agotadas!");
        builder.setMessage("Espera un tiempo para que se regeneren nuevamente.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Cerrar el cuadro de diálogo
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
