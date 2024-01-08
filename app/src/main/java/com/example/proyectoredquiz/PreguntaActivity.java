package com.example.proyectoredquiz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PreguntaActivity extends AppCompatActivity {
    Button btn_volver, boton1, boton2, boton3, boton4;
    TextView pregunta, categoria, vidas, color;
    FirebaseAuth mAuth;
    private String idUser;
    private FirebaseFirestore db;
    private CollectionReference preguntasCollection;
    private String idPreguntaActual;
    private ProgressBar duracion;
    int counter = 0;
    private Handler handler;
    private Timer progressBarTimer;
    private TimerTask progressBarTimerTask;
    private int preguntaActualIndex = 0;
    private List<DocumentSnapshot> preguntasList;
    private  int VIDAS;
    private int acSintomas = 0;
    private int acAnatomia = 0;
    private int acBonus = 0;
    private int acCuracion = 0;
    private int acSignos = 0;
    private int conteoT = 0;
    private int conteoC = 0;

    public static class SoundManager {
        private static MediaPlayer mediaPlayerCorrecta;
        private static MediaPlayer mediaPlayerIncorrecta;

        public static void reproducirSonidoFinal(Context context) {
            try {
                mediaPlayerCorrecta = MediaPlayer.create(context, R.raw.end);
                mediaPlayerCorrecta.start();
                mediaPlayerCorrecta.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        mediaPlayerCorrecta = null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void reproducirSonidoCorrecto(Context context) {
            try {
                mediaPlayerCorrecta = MediaPlayer.create(context, R.raw.success);
                mediaPlayerCorrecta.start();
                mediaPlayerCorrecta.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        mediaPlayerCorrecta = null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void reproducirSonidoIncorrecto(Context context) {
            try {
                mediaPlayerIncorrecta = MediaPlayer.create(context, R.raw.negative);
                mediaPlayerIncorrecta.start();
                mediaPlayerIncorrecta.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        mediaPlayerIncorrecta = null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (SoundManager.mediaPlayerCorrecta != null) {
            SoundManager.mediaPlayerCorrecta.release();
            SoundManager.mediaPlayerCorrecta = null;
        }
        if (SoundManager.mediaPlayerIncorrecta != null) {
            SoundManager.mediaPlayerIncorrecta.release();
            SoundManager.mediaPlayerIncorrecta = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregunta);

        // Verificar la conexión a Internet al inicio de la actividad
        if (!isInternetAvailable()) {
            showNoInternetDialogAndLogout();
        }

            mAuth = FirebaseAuth.getInstance();
            idUser = mAuth.getCurrentUser().getUid();

            btn_volver = findViewById(R.id.btn_volverJ);
            boton1 = findViewById(R.id.r1);
            boton2 = findViewById(R.id.r2);
            boton3 = findViewById(R.id.r3);
            boton4 = findViewById(R.id.r4);

            pregunta = findViewById(R.id.preguntaJ);
            categoria = findViewById(R.id.categoriaJ);
            color = findViewById(R.id.colorCaregoria);
            vidas = findViewById(R.id.vidasJ);

            duracion = findViewById(R.id.duracion);
            handler = new Handler();

            //FIREBASE
            db = FirebaseFirestore.getInstance();
            preguntasCollection = db.collection("preguntas");

            btn_volver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mostrarDialogoConfirmacion();
                }
            });

            // ASIGNAR RESPUESTAS Y PREGUNTA
            getQuestion();
            iniciarProgreso(); // Pasa la vista adecuada

            // ONCLICK PARA LOS BOTONES
            boton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(PreguntaActivity.this, "no se perdió", Toast.LENGTH_SHORT).show();
                    detenerProgreso();
                    verificarRespuesta(boton1);
                }
            });

            boton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detenerProgreso();
                    verificarRespuesta(boton2);
                }
            });

            boton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detenerProgreso();
                    verificarRespuesta(boton3);
                }
            });

            boton4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detenerProgreso();
                    verificarRespuesta(boton4);
                }
            });

            // OBTENER LAS VIDAS DEL USUARIO
            DocumentReference documentReference = db.collection("rqUsers").document(idUser);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        // Manejar el error, si es necesario
                        Log.e("ERROR", "Error al escuchar cambios en el documento", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Actualizar el valor de las vidas en el TextView
                        VIDAS = documentSnapshot.getLong("vidas").intValue();
                        vidas.setText("X " + String.valueOf(VIDAS));
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
        Intent intent = new Intent(PreguntaActivity.this, MenuUserActivity.class);
        startActivity(intent);
        finish();
    }



    // BOTÓN VOLVER
    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Estás por dejar el juego");
        builder.setMessage("¿Seguro de que quieres volver?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                almacenarConteoTConteoC();
                almacenarResultadoSintomas();
                almacenarResultadoAnatomia();
                almacenarResultadoBonus();
                almacenarResultadoCuracion();
                almacenarResultadoSignosVitales();
                onPause();
                // Acciones a realizar si el usuario hace clic en "Sí"
                volver();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Acciones a realizar si el usuario hace clic en "No" o cierra el diálogo
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void volver() {
        // AGREGUÉ ESO:
        finish();
        Intent index = new Intent(PreguntaActivity.this, MenuUserActivity.class);
        startActivities(new Intent[]{index});
    }

    // BARRA DE PROGRESO
    public void iniciarProgreso() {
        progressBarTimer = new Timer();
        progressBarTimerTask = new TimerTask() {
            @Override
            public void run() {
                counter++;
                duracion.setProgress(counter);

                if (counter == 100) {
                    detenerProgreso();
                    MediaPlayer mediaPlayer = MediaPlayer.create(PreguntaActivity.this, R.raw.negative);
                    mediaPlayer.start();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (VIDAS > 0) {
                                VIDAS = VIDAS - 1;

                                DocumentReference userDocumentRef = db.collection("rqUsers").document(idUser);
                                userDocumentRef.update("vidas", VIDAS)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("DEBUG", "Valor de vidas actualizado correctamente en Firestore");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("ERROR", "Error al actualizar el valor de vidas en Firestore", e);
                                            }
                                        });
                                // Mostrar la respuesta correcta antes de cargar la siguiente pregunta
                                mostrarRespuestaCorrecta();
                                mostrarRespuestasIncorrectas();

                                // Cargar la siguiente pregunta después de un breve retardo
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        preguntaActualIndex++;
                                        cargarSiguientePregunta();
                                    }
                                }, 2000);
                            } else {
                                //almacenarConteoTConteoC();
                                mostrarRespuestaCorrecta();
                                mostrarRespuestasIncorrectas();
                                mostrarMensajeSinVidas();
                            }

                        }
                    });
                }
            }
        };
        progressBarTimer.schedule(progressBarTimerTask, 0, 150);
    }



    // DETENER BARRA DE PROGRESO
    public void detenerProgreso() {
        if (progressBarTimer != null) {
            progressBarTimer.cancel();
            progressBarTimer = null;
            counter = 0; // Reiniciar el contador a cero
            duracion.setProgress(counter);
        }
    }

    private void getQuestion() {
        // Obtener la lista de preguntas de Firestore
        preguntasCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                preguntasList = task.getResult().getDocuments();
                // Realizar el shuffle de las preguntas
                Collections.shuffle(preguntasList);
                // Mostrar la primera pregunta
                mostrarPreguntaActual();
            } else {
                // Manejar el error si es necesario
            }
        });
    }


    // ASIGNAR VALORES A LOS BOTONES DE FORMA ALEATORIA
    private void mostrarPreguntaActual() {
        // SUMA CUANDO LA RESPUESTA ES CORRECTA
        conteoT = conteoT + 1;
        resetColoresBotones(); // Restablecer colores de los botones
        // Limpiar acciones previas de los botones
        boton1.setOnClickListener(null);
        boton2.setOnClickListener(null);
        boton3.setOnClickListener(null);
        boton4.setOnClickListener(null);


        DocumentSnapshot document = preguntasList.get(preguntaActualIndex);

        String Pregunta = document.getString("pregunta");
        String Categoria = document.getString("categoria");
        String correcta = document.getString("correcta");
        String incorrecta1 = document.getString("incorrecta1");
        String incorrecta2 = document.getString("incorrecta2");
        String incorrecta3 = document.getString("incorrecta3");
        //Long puntos = document.getLong("puntos");

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            numbers.add(i);
        }

        // Shuffle the list to get random order
        Collections.shuffle(numbers);

        List<String> respuestas = new ArrayList<>();
        for (int j = 0; j < numbers.size(); j++){
            if (numbers.size() > 0 && numbers.get(j) == 1){
                respuestas.add(correcta);
            }
            if (numbers.size() > 0 && numbers.get(j) == 2){
                respuestas.add(incorrecta1);
            }
            if (numbers.size() > 0 && numbers.get(j) == 3){
                respuestas.add(incorrecta2);
            }
            if (numbers.size() > 0 && numbers.get(j) == 4){
                respuestas.add(incorrecta3);
            }
        }

        // Assign each number to a button
        pregunta.setText(Pregunta);

        if (Categoria.equals("Signos Vitales")) {
            color.setBackgroundColor(ContextCompat.getColor(this, R.color.signosVitales));
            color.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.signosVitales));
        } else if (Categoria.equals("Curación")) {
            color.setBackgroundColor(ContextCompat.getColor(this, R.color.curacion));
            color.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.curacion));
        } else if (Categoria.equals("Síntomas")) {
            color.setBackgroundColor(ContextCompat.getColor(this, R.color.sintomas));
            color.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.sintomas));
        } else if (Categoria.equals("Anatomía")) {
            color.setBackgroundColor(ContextCompat.getColor(this, R.color.anatomia));
            color.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.anatomia));
        } else {
            color.setBackgroundColor(ContextCompat.getColor(this, R.color.bonus));
            color.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bonus));
        }

        categoria.setText(Categoria);
        boton1.setText(String.valueOf(respuestas.get(0)));
        boton2.setText(String.valueOf(respuestas.get(1)));
        boton3.setText(String.valueOf(respuestas.get(2)));
        boton4.setText(String.valueOf(respuestas.get(3)));

        // Restablecer la funcionalidad de los botones
        asignarFuncionalidadBotones();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerProgreso();
    }

    private void asignarFuncionalidadBotones() {
        boton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenerProgreso();
                verificarRespuesta(boton1);
            }
        });

        boton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenerProgreso();
                verificarRespuesta(boton2);
            }
        });

        boton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenerProgreso();
                verificarRespuesta(boton3);
            }
        });

        boton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenerProgreso();
                verificarRespuesta(boton4);
            }
        });

    }

    private void verificarRespuesta(Button boton) {
        String respuestaSeleccionada = boton.getText().toString();

        if (preguntaActualIndex < preguntasList.size()) {
            DocumentSnapshot document = preguntasList.get(preguntaActualIndex);
            String correcta = document.getString("correcta").trim();
            String catego = document.getString("categoria").trim();
            Log.d("DEBUG", "Valor de Categoria: " + catego);


            Log.d("DEBUG", "Respuesta seleccionada: " + respuestaSeleccionada);
            Log.d("DEBUG", "Respuesta correcta: " + correcta);

            // Verificar la respuesta seleccionada con la respuesta correcta
            if (respuestaSeleccionada.equals(correcta)) {
                // Respuesta correcta, cambiar color a verde
                // SUMA CUANDO LA RESPUESTA ES CORRECTA
                conteoC = conteoC + 1;

                boton.setBackgroundColor(Color.GREEN);
                SoundManager.reproducirSonidoCorrecto(this);

                // SUMAR EL PUNTAJE
                DocumentSnapshot preguntaActual = preguntasList.get(preguntaActualIndex);
                // OBTENER EL PUNTAJE DEL USUARIO
                DocumentReference documentReference = db.collection("rqUsers").document(idUser);
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Long puntaje = documentSnapshot.getLong("puntaje");
                            //Log.d("DEBUG", "Puntaje actual: " + String.valueOf(puntaje));

                            if (puntaje == null) {
                                puntaje = 0L;
                            }

                            // Sumar los puntos de la pregunta actual al puntaje
                            Long puntosPregunta = preguntaActual.getLong("puntos");
                            //Log.d("DEBUG", "Puntos de la pregunta: " + String.valueOf(puntosPregunta));

                            if (puntosPregunta != null) {
                                puntaje += puntosPregunta;
                                //Log.d("DEBUG", "Nuevo puntaje: " + String.valueOf(puntaje));

                                // Actualizar el valor de "puntaje" en Firestore
                                documentReference.update("puntaje", puntaje)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("DEBUG", "Valor de puntaje actualizado correctamente en Firestore");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("ERROR", "Error al actualizar el valor de puntaje en Firestore", e);
                                            }
                                        });
                            }
                        } else {
                            Log.e("ERROR", "El documento del usuario no existe");
                        }
                    }
                });

                // SUMAR DE ACUERDO A LA CATEGORÍA
                if (catego.equals("Curación")) { // CATEGORIA: CURACION
                    acCuracion = acCuracion + 1;
                } else if (catego.equals("Anatomía")) {
                    acAnatomia = acAnatomia + 1;
                } else if (catego.equals("Signos Vitales")) {
                    acSignos = acSignos + 1;
                } else if (catego.equals("Síntomas")) {
                    acSintomas = acSintomas + 1;
                } else {
                    acBonus = acBonus + 1;
                }

                // Desactivar todos los botones después de la respuesta
                boton1.setEnabled(false);
                boton2.setEnabled(false);
                boton3.setEnabled(false);
                boton4.setEnabled(false);

                // Realizar acciones relacionadas con la respuesta (por ejemplo, cargar la siguiente pregunta)
                preguntaActualIndex++;
                new Handler().postDelayed(this::cargarSiguientePregunta, 2000);

            } else {
                // Respuesta incorrecta, cambiar color a rojo
                boton.setBackgroundColor(Color.RED);
                boton.setTextColor(Color.WHITE);
                SoundManager.reproducirSonidoIncorrecto(this);

                // Encontrar el botón correcto y cambiar su color a verde
                Button botonCorrecto = encontrarBotonRespuestaCorrecta(correcta);

                if (botonCorrecto != null) {
                    botonCorrecto.setBackgroundColor(Color.GREEN);
                }

                if (VIDAS > 0) {
                    // Si el usuario aún tiene vidas, restar una vida
                    VIDAS = VIDAS - 1;

                    // Actualizar el valor de "vidas" en Firestore
                    DocumentReference userDocumentRef = db.collection("rqUsers").document(idUser);
                    userDocumentRef
                            .update("vidas", VIDAS)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("DEBUG", "Valor de vidas actualizado correctamente en Firestore");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("ERROR", "Error al actualizar el valor de vidas en Firestore", e);
                                }
                            });

                    // Desactivar todos los botones después de la respuesta
                    boton1.setEnabled(false);
                    boton2.setEnabled(false);
                    boton3.setEnabled(false);
                    boton4.setEnabled(false);

                    // Realizar acciones relacionadas con la respuesta (por ejemplo, cargar la siguiente pregunta)
                    preguntaActualIndex++;
                    new Handler().postDelayed(this::cargarSiguientePregunta, 2000);

                } else {
                    // El usuario ya no tiene vidas, mostrar mensaje
                    mostrarMensajeSinVidas();
                }
            }

        }
    }


    private Button encontrarBotonRespuestaCorrecta(String respuestaCorrecta) {
        // Convertir todas las respuestas a minúsculas para realizar una comparación sin distinción de mayúsculas
        //String respuestaCorrectaLower = respuestaCorrecta.toLowerCase();

        if (respuestaCorrecta.equals(boton1.getText().toString())) {
            return boton1;
        } else if (respuestaCorrecta.equals(boton2.getText().toString())) {
            return boton2;
        } else if (respuestaCorrecta.equals(boton3.getText().toString())) {
            return boton3;
        } else if (respuestaCorrecta.equals(boton4.getText().toString())) {
            return boton4;
        }

        return null; // No se encontró el botón correspondiente
    }



    private void cargarSiguientePregunta() {

        counter = 0;
        // Volver a habilitar todos los botones
        boton1.setEnabled(true);
        boton2.setEnabled(true);
        boton3.setEnabled(true);
        boton4.setEnabled(true);

        // Verificar si hay más preguntas disponibles
        if (preguntaActualIndex < preguntasList.size()) {
            // Si hay más preguntas, cargar la siguiente pregunta
            mostrarPreguntaActual();
            iniciarProgreso();
        } else {
            // Si no hay más preguntas, mostrar el mensaje de diálogo
            mostrarMensajeFinPreguntas();
        }
    }

    private void mostrarMensajeFinPreguntas() {
        almacenarConteoTConteoC();
        almacenarResultadoSintomas();
        almacenarResultadoAnatomia();
        almacenarResultadoBonus();
        almacenarResultadoCuracion();
        almacenarResultadoSignosVitales();
        SoundManager.reproducirSonidoFinal(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¡Has respondido todas las preguntas!")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    volverAMenuPrincipal();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para mostrar el mensaje cuando el usuario no tiene vidas
    private void mostrarMensajeSinVidas() {
        almacenarConteoTConteoC();
        almacenarResultadoSintomas();
        almacenarResultadoAnatomia();
        almacenarResultadoBonus();
        almacenarResultadoCuracion();
        almacenarResultadoSignosVitales();

        detenerJuego(); // Detener el juego

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¡Te has quedado sin vidas! Vuelve a la interfaz principal.")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    // Acciones a realizar si el usuario hace clic en "Aceptar"
                    volverAMenuPrincipal();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void detenerJuego() {
        detenerProgreso(); // Detener la barra de progreso
        // Deshabilitar los botones para evitar que el usuario continúe respondiendo
        boton1.setEnabled(false);
        boton2.setEnabled(false);
        boton3.setEnabled(false);
        boton4.setEnabled(false);
    }

    private void volverAMenuPrincipal() {
        Intent intent = new Intent(PreguntaActivity.this, MenuUserActivity.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual para que el usuario no pueda volver atrás desde el menú principal
    }

    // COLOR ORIGINAL DE LOS BOTONES
    private void resetColoresBotones() {
        boton1.setBackgroundColor(ContextCompat.getColor(this, R.color.botones));
        boton2.setBackgroundColor(ContextCompat.getColor(this, R.color.botones));
        boton3.setBackgroundColor(ContextCompat.getColor(this, R.color.botones));
        boton4.setBackgroundColor(ContextCompat.getColor(this, R.color.botones));
        boton1.setTextColor(Color.BLACK);
        boton2.setTextColor(Color.BLACK);
        boton3.setTextColor(Color.BLACK);
        boton4.setTextColor(Color.BLACK);
    }

    private void mostrarRespuestaCorrecta() {
        DocumentSnapshot document = preguntasList.get(preguntaActualIndex);
        String correcta = document.getString("correcta");

        // Encontrar el botón correcto y cambiar su color a verde
        Button botonCorrecto = encontrarBotonRespuestaCorrecta(correcta);
        if (botonCorrecto != null) {
            botonCorrecto.setBackgroundColor(Color.GREEN);
        }
    }

    private Button encontrarBotonRespuestaIncorrecta1(String respuestaIncorrecta) {
        if (respuestaIncorrecta.equals(boton1.getText().toString())) {
            return boton1;
        } else if (respuestaIncorrecta.equals(boton2.getText().toString())) {
            return boton2;
        } else if (respuestaIncorrecta.equals(boton3.getText().toString())) {
            return boton3;
        } else if (respuestaIncorrecta.equals(boton4.getText().toString())) {
            return boton4;
        }

        return null; // No se encontró el botón correspondiente
    }

    private Button encontrarBotonRespuestaIncorrecta2(String respuestaIncorrecta) {
        if (respuestaIncorrecta.equals(boton1.getText().toString())) {
            return boton1;
        } else if (respuestaIncorrecta.equals(boton2.getText().toString())) {
            return boton2;
        } else if (respuestaIncorrecta.equals(boton3.getText().toString())) {
            return boton3;
        } else if (respuestaIncorrecta.equals(boton4.getText().toString())) {
            return boton4;
        }

        return null; // No se encontró el botón correspondiente
    }

    private Button encontrarBotonRespuestaIncorrecta3(String respuestaIncorrecta) {
        if (respuestaIncorrecta.equals(boton1.getText().toString())) {
            return boton1;
        } else if (respuestaIncorrecta.equals(boton2.getText().toString())) {
            return boton2;
        } else if (respuestaIncorrecta.equals(boton3.getText().toString())) {
            return boton3;
        } else if (respuestaIncorrecta.equals(boton4.getText().toString())) {
            return boton4;
        }

        return null; // No se encontró el botón correspondiente
    }

    private void mostrarRespuestasIncorrectas() {
        DocumentSnapshot document = preguntasList.get(preguntaActualIndex);
        String incorrecta1 = document.getString("incorrecta1");
        String incorrecta2 = document.getString("incorrecta2");
        String incorrecta3 = document.getString("incorrecta3");

        // Encontrar los botones incorrecto y cambiar su color a rojo
        Button botonInorrecto1 = encontrarBotonRespuestaIncorrecta1(incorrecta1);
        if (botonInorrecto1 != null) {
            botonInorrecto1.setBackgroundColor(Color.RED);
            botonInorrecto1.setTextColor(Color.WHITE);
        }
        Button botonInorrecto2 = encontrarBotonRespuestaIncorrecta2(incorrecta2);
        if (botonInorrecto2 != null) {
            botonInorrecto2.setBackgroundColor(Color.RED);
            botonInorrecto2.setTextColor(Color.WHITE);
        }
        Button botonInorrecto3 = encontrarBotonRespuestaIncorrecta3(incorrecta3);
        if (botonInorrecto3 != null) {
            botonInorrecto3.setBackgroundColor(Color.RED);
            botonInorrecto3.setTextColor(Color.WHITE);
        }
    }

    private void almacenarResultadoSintomas() {
        // Obtén una referencia a la colección "rqSintomas" utilizando el ID del usuario
        DocumentReference sintomasDocumentRef = db.collection("rqSintomas").document(idUser);

        // Verifica si el documento ya existe en la colección
        sintomasDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // El documento ya existe, obtén el valor actual de acumulado
                        Long acumuladoActual = document.getLong("acumulado");

                        // Suma el valor actual de acumulado con acSintomas
                        if (acumuladoActual != null) {
                            acumuladoActual += acSintomas;
                        } else {
                            acumuladoActual = (long) acSintomas;
                        }

                        // Actualiza el valor de acumulado en Firestore
                        sintomasDocumentRef.update("acumulado", acumuladoActual)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Valor de acumulado actualizado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al actualizar el valor de acumulado en Firestore", e);
                                    }
                                });
                    } else {
                        // El documento no existe, crea uno nuevo con los atributos idUsuario y acumulado
                        Map<String, Object> sintomasData = new HashMap<>();
                        sintomasData.put("idUsuario", idUser);
                        sintomasData.put("acumulado", (long) acSintomas);

                        sintomasDocumentRef.set(sintomasData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Documento creado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al crear el documento en Firestore", e);
                                    }
                                });
                    }
                } else {
                    Log.e("ERROR", "Error al obtener el documento en Firestore", task.getException());
                }
            }
        });
    }

    private void almacenarResultadoCuracion() {
        // Obtén una referencia a la colección "rqSintomas" utilizando el ID del usuario
        DocumentReference sintomasDocumentRef = db.collection("rqCuracion").document(idUser);

        // Verifica si el documento ya existe en la colección
        sintomasDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // El documento ya existe, obtén el valor actual de acumulado
                        Long acumuladoActual = document.getLong("acumulado");

                        // Suma el valor actual de acumulado con acSintomas
                        if (acumuladoActual != null) {
                            acumuladoActual += acCuracion;
                        } else {
                            acumuladoActual = (long) acCuracion;
                        }

                        // Actualiza el valor de acumulado en Firestore
                        sintomasDocumentRef.update("acumulado", acumuladoActual)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Valor de acumulado actualizado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al actualizar el valor de acumulado en Firestore", e);
                                    }
                                });
                    } else {
                        // El documento no existe, crea uno nuevo con los atributos idUsuario y acumulado
                        Map<String, Object> sintomasData = new HashMap<>();
                        sintomasData.put("idUsuario", idUser);
                        sintomasData.put("acumulado", (long) acCuracion);

                        sintomasDocumentRef.set(sintomasData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Documento creado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al crear el documento en Firestore", e);
                                    }
                                });
                    }
                } else {
                    Log.e("ERROR", "Error al obtener el documento en Firestore", task.getException());
                }
            }
        });
    }

    private void almacenarResultadoSignosVitales() {
        // Obtén una referencia a la colección "rqSignosVitales" utilizando el ID del usuario
        DocumentReference signosVitalesDocumentRef = db.collection("rqSignosVitales").document(idUser);

        // Verifica si el documento ya existe en la colección
        signosVitalesDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // El documento ya existe, obtén el valor actual de acumulado
                        Long acumuladoActual = document.getLong("acumulado");

                        // Suma el valor actual de acumulado con acSignos
                        if (acumuladoActual != null) {
                            acumuladoActual += acSignos;
                        } else {
                            acumuladoActual = (long) acSignos;
                        }

                        // Actualiza el valor de acumulado en Firestore
                        signosVitalesDocumentRef.update("acumulado", acumuladoActual)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Valor de acumulado actualizado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al actualizar el valor de acumulado en Firestore", e);
                                    }
                                });
                    } else {
                        // El documento no existe, crea uno nuevo con los atributos idUsuario y acumulado
                        Map<String, Object> signosVitalesData = new HashMap<>();
                        signosVitalesData.put("idUsuario", idUser);
                        signosVitalesData.put("acumulado", (long) acSignos);

                        signosVitalesDocumentRef.set(signosVitalesData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Documento creado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al crear el documento en Firestore", e);
                                    }
                                });
                    }
                } else {
                    Log.e("ERROR", "Error al obtener el documento en Firestore", task.getException());
                }
            }
        });
    }

    private void almacenarResultadoBonus() {
        // Obtén una referencia a la colección "rqBonus" utilizando el ID del usuario
        DocumentReference bonusDocumentRef = db.collection("rqBonus").document(idUser);

        // Verifica si el documento ya existe en la colección
        bonusDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // El documento ya existe, obtén el valor actual de acumulado
                        Long acumuladoActual = document.getLong("acumulado");

                        // Suma el valor actual de acumulado con acBonus
                        if (acumuladoActual != null) {
                            acumuladoActual += acBonus;
                        } else {
                            acumuladoActual = (long) acBonus;
                        }

                        // Actualiza el valor de acumulado en Firestore
                        bonusDocumentRef.update("acumulado", acumuladoActual)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Valor de acumulado actualizado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al actualizar el valor de acumulado en Firestore", e);
                                    }
                                });
                    } else {
                        // El documento no existe, crea uno nuevo con los atributos idUsuario y acumulado
                        Map<String, Object> bonusData = new HashMap<>();
                        bonusData.put("idUsuario", idUser);
                        bonusData.put("acumulado", (long) acBonus);

                        bonusDocumentRef.set(bonusData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Documento creado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al crear el documento en Firestore", e);
                                    }
                                });
                    }
                } else {
                    Log.e("ERROR", "Error al obtener el documento en Firestore", task.getException());
                }
            }
        });
    }

    private void almacenarResultadoAnatomia() {
        // Obtén una referencia a la colección "rqAnatomia" utilizando el ID del usuario
        DocumentReference anatomiaDocumentRef = db.collection("rqAnatomia").document(idUser);

        // Verifica si el documento ya existe en la colección
        anatomiaDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // El documento ya existe, obtén el valor actual de acumulado
                        Long acumuladoActual = document.getLong("acumulado");

                        // Suma el valor actual de acumulado con acAnatomia
                        if (acumuladoActual != null) {
                            acumuladoActual += acAnatomia;
                        } else {
                            acumuladoActual = (long) acAnatomia;
                        }

                        // Actualiza el valor de acumulado en Firestore
                        anatomiaDocumentRef.update("acumulado", acumuladoActual)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Valor de acumulado actualizado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al actualizar el valor de acumulado en Firestore", e);
                                    }
                                });
                    } else {
                        // El documento no existe, crea uno nuevo con los atributos idUsuario y acumulado
                        Map<String, Object> anatomiaData = new HashMap<>();
                        anatomiaData.put("idUsuario", idUser);
                        anatomiaData.put("acumulado", (long) acAnatomia);

                        anatomiaDocumentRef.set(anatomiaData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Documento creado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al crear el documento en Firestore", e);
                                    }
                                });
                    }
                } else {
                    Log.e("ERROR", "Error al obtener el documento en Firestore", task.getException());
                }
            }
        });
    }

    private void almacenarConteoTConteoC() {
        // Obtén una referencia a la colección "rqConteo" utilizando el ID del usuario
        DocumentReference conteoDocumentRef = db.collection("rqConteo").document(idUser);

        // Verifica si el documento ya existe en la colección
        conteoDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // El documento ya existe, obtén los valores actuales de conteoT y conteoC
                        Long conteoTActual = document.getLong("conteoT");
                        Long conteoCActual = document.getLong("conteoC");

                        // Incrementa los valores actuales de conteoT y conteoC
                        if (conteoTActual != null) {
                            conteoTActual = conteoTActual + conteoT;
                        } else {
                            conteoTActual = 1L;  // Si es la primera vez, inicializa en 1
                        }

                        if (conteoCActual != null) {
                            conteoCActual = conteoCActual + conteoC;
                        } else {
                            conteoCActual = 1L;  // Si es la primera vez, inicializa en 1
                        }

                        // Actualiza los valores en Firestore
                        Map<String, Object> conteoData = new HashMap<>();
                        conteoData.put("conteoT", conteoTActual);
                        conteoData.put("conteoC", conteoCActual);

                        conteoDocumentRef.update(conteoData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Valores de conteoT y conteoC actualizados correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al actualizar los valores de conteoT y conteoC en Firestore", e);
                                    }
                                });
                    } else {
                        // El documento no existe, crea uno nuevo con los atributos idUsuario, conteoT y conteoC
                        Map<String, Object> conteoData = new HashMap<>();
                        conteoData.put("idUsuario", idUser);
                        conteoData.put("conteoT", conteoT);  // Inicializa en 1 si es la primera vez
                        conteoData.put("conteoC", conteoC);  // Inicializa en 1 si es la primera vez

                        conteoDocumentRef.set(conteoData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DEBUG", "Documento creado correctamente en Firestore");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ERROR", "Error al crear el documento en Firestore", e);
                                    }
                                });
                    }
                } else {
                    Log.e("ERROR", "Error al obtener el documento en Firestore", task.getException());
                }
            }
        });
    }


}