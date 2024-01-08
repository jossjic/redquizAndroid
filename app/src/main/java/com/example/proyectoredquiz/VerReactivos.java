package com.example.proyectoredquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class VerReactivos extends AppCompatActivity implements MyAdapter.OnQuestionDeleteListener {
    Button btn_agregar;
    RecyclerView recyclerView;
    ArrayList<Question> list;
    DatabaseReference databaseReference;
    MyAdapter adapter;
    Button btn_atras;
    Spinner categorias;
    private ArrayAdapter<CharSequence> categoriaAdapter;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(VerReactivos.this, VerReactivos.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_reactivos);

        // Verificar la conexión a Internet al inicio de la actividad
        if (!isInternetAvailable()) {
            showNoInternetDialogAndLogout();
        }


        btn_agregar = findViewById(R.id.btn_add);
        btn_atras = findViewById(R.id.btn_volverAdmin);
        categorias = findViewById(R.id.adminCategorias);
        categoriaAdapter = ArrayAdapter.createFromResource(this, R.array.categrias_array2, android.R.layout.simple_spinner_item);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorias.setAdapter(categoriaAdapter);

        categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedCategoria = (String) categoriaAdapter.getItem(i);
                //Toast.makeText(VerReactivos.this, "Categoria: " + selectedCategoria, Toast.LENGTH_SHORT).show();

                // Llamar a un método para cargar las preguntas con la categoría seleccionada
                loadQuestionsByCategory(selectedCategoria);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Puedes dejar esto vacío si no necesitas realizar ninguna acción cuando no se selecciona nada.
            }
        });


        btn_atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent index = new Intent(VerReactivos.this, MenuAdministrador.class);
                startActivities(new Intent[]{index});
            }
        });

        //RECYCLER VIEW
        recyclerView = findViewById(R.id.recyclerView);
        databaseReference = FirebaseDatabase.getInstance().getReference("preguntas");
        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(list, VerReactivos.this); //AQUI se agregó algo
        recyclerView.setAdapter(adapter);

        // Inicializa Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Configura la referencia a la colección "preguntas" en Firestore
        CollectionReference collectionRef = db.collection("preguntas");


        collectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                list = new ArrayList<>();

                // Iterate over the results of the query
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    // Convert the document to an "Ad" object
                    Question question = document.toObject(Question.class);
                    list.add(question);
                }
                //adapter.notifyDataSetChanged();
                // Create an adapter to display the ads in the RecyclerView
                adapter = new MyAdapter(list, VerReactivos.this);
                recyclerView.setAdapter(adapter);
            }
            // Handle error if the query fails
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NotNull Exception e) {
                // Notify user of error
                Toast.makeText(VerReactivos.this, "Error al visualizar los anuncios",
                        Toast.LENGTH_SHORT).show();
            }
        });


        btn_agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent index = new Intent(VerReactivos.this, IngresarPregunta.class);
                startActivities(new Intent[]{index});
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
        Intent intent = new Intent(VerReactivos.this, MenuAdministrador.class);
        startActivity(intent);
        finish();
    }


    private void loadQuestionsByCategory(String selectedCategoria) {
        // Configura la referencia a la colección "preguntas" en Firestore
        CollectionReference collectionRef = mFirestore.collection("preguntas");

        // Verifica si se seleccionó "Todos"
        if (selectedCategoria.equals("Todos")) {
            // Si es "Todos", obtén todas las preguntas sin filtrar por categoría
            collectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    list.clear(); // Limpiar la lista actual antes de agregar nuevas preguntas

                    // Iterate over the results of the query
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        // Convert the document to a Question object
                        Question question = document.toObject(Question.class);
                        question.setVisible(true);
                        list.add(question);
                    }

                    adapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NotNull Exception e) {
                    // Handle error if the query fails
                    Toast.makeText(VerReactivos.this, "Error al obtener todas las preguntas",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Si se selecciona una categoría específica, aplica el filtro por categoría a la consulta
            Query query = collectionRef.whereEqualTo("categoria", selectedCategoria);

            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    list.clear(); // Limpiar la lista actual antes de agregar nuevas preguntas

                    // Iterate over the results of the filtered query
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        // Convert the document to a Question object
                        Question question = document.toObject(Question.class);
                        question.setVisible(false);
                        list.add(question);
                    }

                    adapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NotNull Exception e) {
                    // Handle error if the query fails
                    Toast.makeText(VerReactivos.this, "Error al obtener preguntas por categoría",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @Override
    public void onQuestionDelete(int position) {
        Question question = list.get(position);

        // Crear un cuadro de diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(VerReactivos.this);
        builder.setTitle("Confirmación");
        builder.setMessage("¿Estás seguro de que deseas eliminar esta pregunta?");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener el nombre del documento en Firestore directamente desde la posición
                String documentId = mFirestore.collection("preguntas").document().getId();

                mFirestore.collection("preguntas").document(documentId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Actualizar la interfaz de usuario después de la eliminación exitosa
                                deletePosition(position);
                                list.remove(position);
                                adapter.notifyDataSetChanged(); // Notificar al adaptador sobre el cambio en los datos
                                Toast.makeText(VerReactivos.this, "Pregunta eliminada exitosamente", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Manejar la falla en la eliminación
                                Toast.makeText(VerReactivos.this, "Error al eliminar la pregunta", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // No hacer nada si se cancela la eliminación
            }
        });

        // Mostrar el cuadro de diálogo
        builder.show();
    }

    private void deletePosition(int position) {
        if (position >= 0 && position < list.size()) {
            mFirestore.collection("preguntas")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Verificar si hay algún documento en la posición especificada
                        if (position < queryDocumentSnapshots.size()) {
                            // Obtener el ID del documento en la posición especificada
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(position);
                            String documentIdToDelete = documentSnapshot.getId();

                            // Eliminar el documento
                            mFirestore.collection("preguntas")
                                    .document(documentIdToDelete)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Documento eliminado con éxito
                                        // Actualiza tu lista u realiza otras acciones necesarias
                                    })
                                    .addOnFailureListener(e -> {
                                        // Manejar errores
                                    });
                        } else {
                            // La posición especificada es mayor que el número de documentos
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Manejar errores al obtener la referencia al documento
                    });
        } else {
            // La posición especificada no es válida
        }
    }



}

