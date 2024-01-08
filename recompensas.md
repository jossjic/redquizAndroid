package com.example.proyectoredquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class miavatar extends AppCompatActivity {

    Button btn_volver;

    private FirebaseFirestore mfirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miavatar);

        btn_volver = findViewById(R.id.btn_volverA);

        //FIRESTORE
        mfirestore = FirebaseFirestore.getInstance();

        obtenerPuntajeDesdeFirestore();

        // VOLVER A INICIO
        btn_volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent index = new Intent(miavatar.this, MenuUserActivity.class);
                startActivities(new Intent[]{index});
            }
        });
    }

    private void obtenerPuntajeDesdeFirestore() {
        // Obtener la referencia al documento del usuario en Firestore (deberías tener un ID de usuario único)
        String userId = "tu_id_de_usuario"; // Reemplaza con la lógica para obtener el ID del usuario
        String usuarioDocumentPath = "usuarios/" + userId;

        // Realizar la consulta para obtener el documento del usuario
        mfirestore.document(usuarioDocumentPath)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // El documento del usuario existe, obtener el puntaje
                            Long puntaje = documentSnapshot.getLong("puntaje"); // Asume que tienes un campo "puntaje" en tu documento de usuario

                            if (puntaje != null) {
                                // Actualizar la variable puntajeUsuario y realizar las acciones necesarias
                                puntajeUsuario = puntaje.intValue();
                                // Realizar otras acciones según el puntaje, como verificar y desbloquear recompensas
                                verificarDesbloqueoRecompensas();
                            }
                        } else {
                            // El documento del usuario no existe
                            Toast.makeText(miavatar.this, "Documento de usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error al obtener el documento del usuario
                        Toast.makeText(miavatar.this, "Error al obtener el puntaje desde Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarRecompensaEnFirestore(String nombreRecompensa, boolean desbloqueada) {
        // Obtener la referencia al documento del usuario en Firestore (deberías tener un ID de usuario único)
        String userId = "tu_id_de_usuario"; // Reemplaza con la lógica para obtener el ID del usuario
        String recompensaDocumentPath = "usuarios/" + userId + "/recompensas";

        // Crear un mapa para actualizar el campo de la recompensa en Firestore
        Map<String, Object> actualizacionRecompensa = new HashMap<>();
        actualizacionRecompensa.put(nombreRecompensa, desbloqueada);

        // Actualizar el documento en Firestore
        mfirestore.document(recompensaDocumentPath)
                .set(actualizacionRecompensa, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // La actualización se realizó con éxito
                        Toast.makeText(miavatar.this, "Recompensa actualizada en Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // La actualización falló
                        Toast.makeText(miavatar.this, "Error al actualizar la recompensa en Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarDesbloqueoRecompensas() {
        // Verificar y desbloquear recompensas según el puntaje del usuario
        if (puntajeUsuario >= 50 && !recompensa1Desbloqueada) {
            // Desbloquear recompensa 1
            recompensa1Desbloqueada = true;
            // Actualizar en Firestore la recompensa desbloqueada
            actualizarRecompensaEnFirestore("recompensa1", true);
        }

        if (puntajeUsuario >= 100 && !recompensa2Desbloqueada) {
            // Desbloquear recompensa 2
            recompensa2Desbloqueada = true;
            // Actualizar en Firestore la recompensa desbloqueada
            actualizarRecompensaEnFirestore("recompensa2", true);
        }

        if (puntajeUsuario >= 150 && !recompensa2Desbloqueada) {
            // Desbloquear recompensa 3
            recompensa2Desbloqueada = true;
            // Actualizar en Firestore la recompensa desbloqueada
            actualizarRecompensaEnFirestore("recompensa3", true);
        }

        if (puntajeUsuario >= 200 && !recompensa2Desbloqueada) {
            // Desbloquear recompensa 4
            recompensa2Desbloqueada = true;
            // Actualizar en Firestore la recompensa desbloqueada
            actualizarRecompensaEnFirestore("recompensa4", true);
        }
    }
}
