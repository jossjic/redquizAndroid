package com.example.proyectoredquiz;

import static com.google.firebase.database.DatabaseKt.getSnapshots;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.logging.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.firestore.DocumentSnapshot;


import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    Context context;
    ArrayList<Question> list;
    private FirebaseFirestore mfirestore;
    DocumentSnapshot documentSnapshot;

    private static OnQuestionDeleteListener onQuestionDeleteListener;


    public MyAdapter( ArrayList<Question> list, OnQuestionDeleteListener onQuestionDeleteListener ) {
        this.list = list;
        this.onQuestionDeleteListener = onQuestionDeleteListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.questionentry,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Question question = list.get(position);
        holder.bind(question);
    }


    public interface OnQuestionDeleteListener {
        void onQuestionDelete(int position);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView pregunta, categoria;
        Button btn_eliminar;
        Button btn_actualizar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            pregunta = itemView.findViewById(R.id.textPregunta);
            categoria = itemView.findViewById(R.id.textcategoria);

            btn_eliminar = itemView.findViewById(R.id.btn_eliminar);
        }

        void bind(Question question){
            pregunta.setText(question.getPregunta());
            categoria.setText(question.getCategoria());

            btn_eliminar.setVisibility(question.isVisible() ? View.VISIBLE : View.INVISIBLE);
            btn_eliminar.setEnabled(question.isVisible());

            btn_eliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    onQuestionDeleteListener.onQuestionDelete(position);
                }
            });

        }
    }


}
