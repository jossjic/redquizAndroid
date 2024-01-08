package com.example.proyectoredquiz;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.DocumentSnapshot;

@IgnoreExtraProperties
public class Question {
    private String pregunta;
    private String categoria;

    private DocumentSnapshot documentSnapshot;
    private String id; // Nueva variable para almacenar el ID

    private boolean isVisible;

    public Question() {
    }

    public Question(String pregunta, String categoria, String id, DocumentSnapshot documentSnapshot) {
        this.id = id;
        this.pregunta = pregunta;
        this.categoria = categoria;
        this.documentSnapshot = documentSnapshot;

    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public DocumentSnapshot getDocumentSnapshot() {
        // Retorna el DocumentSnapshot asociado a la pregunta
        return documentSnapshot;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

}
