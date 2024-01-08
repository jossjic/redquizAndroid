package com.example.proyectoredquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IngresarPregunta extends AppCompatActivity {

    private Spinner spinnerCategory;
    private ArrayAdapter<CharSequence> categoyAdapter;
    Button btn_add, btn_back;
    EditText question, rate, correct, incorrect1, incorrect2, incorrect3;
    private FirebaseFirestore mfirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresar_pregunta);

        // Verificar la conexión a Internet al inicio de la actividad
        if (!isInternetAvailable()) {
            showNoInternetDialogAndLogout();
        }

        //FIRESTORE
        mfirestore = FirebaseFirestore.getInstance();

        // SPINNER
        spinnerCategory = findViewById(R.id.categoria);
        categoyAdapter = ArrayAdapter.createFromResource(this, R.array.categrias_array, android.R.layout.simple_spinner_item);
        categoyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoyAdapter);

        //INSTANCIAS
        btn_back = findViewById(R.id.btn_back);
        btn_add = findViewById(R.id.btn_agregar);
        question = findViewById(R.id.pregunta);
        rate = findViewById(R.id.rating);
        correct = findViewById(R.id.correcta);
        incorrect1 = findViewById(R.id.incorrecta1);
        incorrect2 = findViewById(R.id.incorrecta2);
        incorrect3 = findViewById(R.id.incorrecta3);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent index = new Intent(IngresarPregunta.this, VerReactivos.class);
                startActivities(new Intent[]{index});
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedCategory = (String) categoyAdapter.getItem(i);
                //Toast.makeText(IngresarPregunta.this, "Categoría: " + selectedCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // AGREGAR
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String preguntai = question.getText().toString();
                String puntajei = rate.getText().toString();
                String correctai = correct.getText().toString();
                String incorrecta1i = incorrect1.getText().toString();
                String incorrecta2i = incorrect2.getText().toString();
                String incorrecta3i = incorrect3.getText().toString();
                String categoriai = spinnerCategory.getSelectedItem().toString();

                if (isEmptyOrWhitespace(preguntai) || isEmptyOrWhitespace(puntajei) || isEmptyOrWhitespace(correctai) || isEmptyOrWhitespace(incorrecta1i) || isEmptyOrWhitespace(incorrecta2i) || isEmptyOrWhitespace(incorrecta3i)) {
                    Toast.makeText(IngresarPregunta.this, "Ingrese todos los datos", Toast.LENGTH_SHORT).show();
                } else {
                    postPregunta(preguntai, puntajei, correctai, incorrecta1i, incorrecta2i, incorrecta3i, categoriai);
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
        Intent intent = new Intent(IngresarPregunta.this, MenuAdministrador.class);
        startActivity(intent);
        finish();
    }

    private boolean isEmptyOrWhitespace(String input) {
        return input == null || input.trim().isEmpty();
    }

    private void postPregunta(String preguntai, String puntajei, String correctai, String incorrecta1i, String incorrecta2i, String incorrecta3i, String categoriai) {
        String questionUid  = UUID.randomUUID().toString();
        Map<String, Object> map = new HashMap<>();
        map.put("pregunta", preguntai);
        map.put("categoria", categoriai);
        map.put("puntos", puntajei);
        map.put("correcta", correctai);
        map.put("incorrecta1", incorrecta1i);
        map.put("incorrecta2", incorrecta2i);
        map.put("incorrecta3", incorrecta3i);
        map.put("id",questionUid);

        mfirestore.collection("preguntas").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(new IngresarPregunta(), "Pregunta agregada exitosamente", Toast.LENGTH_SHORT).show();
                finish();
                //startActivity(new Intent(IngresarPregunta.this, ));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(new IngresarPregunta(), "Error al ingresar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}