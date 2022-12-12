package com.example.testlab.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testlab.Fragments.TestCreatorFragment;
import com.example.testlab.R;

import java.util.ArrayList;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.RecipesVH>{
    private final ArrayList<String> questions;
    private Fragment fragment;

    public QuestionsAdapter(ArrayList<String> questions, Fragment fragment) {
        this.questions = questions;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public QuestionsAdapter.RecipesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_question, parent, false);
        return new QuestionsAdapter.RecipesVH(view);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }


    static class RecipesVH extends RecyclerView.ViewHolder {
        public TextView question_name, question_number;
        public CardView question_card;
        public ImageButton quit_question;

        public RecipesVH(@NonNull View itemView) {
            super(itemView);
            question_name = itemView.findViewById(R.id.question_name);
            question_number = itemView.findViewById(R.id.question_number);
            question_card = itemView.findViewById(R.id.question_card);
            quit_question = itemView.findViewById(R.id.remove_question);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionsAdapter.RecipesVH holder, int position) {
        String question = questions.get(position);
        String numba = "Pregunta #"+String.valueOf(position+1);
        holder.question_number.setText(numba);
        holder.question_name.setText(question);

        holder.quit_question.setOnClickListener(view -> {
                ((TestCreatorFragment) fragment).removeQuestion(question);
        });

    }
}

