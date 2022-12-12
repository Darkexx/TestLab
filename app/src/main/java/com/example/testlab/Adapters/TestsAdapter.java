package com.example.testlab.Adapters;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testlab.Activities.MainActivity;
import com.example.testlab.Activities.TestCreatorActivity;
import com.example.testlab.R;
import com.example.testlab.Test;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Vector;

public class TestsAdapter extends RecyclerView.Adapter<TestsAdapter.RecipesVH> {
    private final Vector<Test> tests;

    public TestsAdapter(Vector<Test> tests) {
        this.tests = tests;
    }

    @NonNull
    @Override
    public RecipesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_test, parent, false);
        return new RecipesVH(view);
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }


    static class RecipesVH extends RecyclerView.ViewHolder {
        public TextView test_name;
        public CardView test_card;
        public ImageButton test_edit_btn, test_share_btn, test_delete_btn;

        public RecipesVH(@NonNull View itemView) {
            super(itemView);
            test_name = itemView.findViewById(R.id.test_name);
            test_card = itemView.findViewById(R.id.test_card);
            test_edit_btn = itemView.findViewById(R.id.edit_test_btn);
            test_delete_btn = itemView.findViewById(R.id.delete_test_btn);
            test_share_btn = itemView.findViewById(R.id.share_test_btn);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesVH holder, int position) {
        Test test = tests.get(position);
        holder.test_name.setText(test.clase + "\n"+ test.name);

        holder.test_edit_btn.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), TestCreatorActivity.class);
            intent.putExtra("KEY_NAME", test);
            view.getContext().startActivity(intent);
        });

        holder.test_delete_btn.setOnClickListener(view -> {
            new AlertDialog.Builder(view.getContext())
                    .setTitle(R.string.test_deleting)
                    .setMessage(R.string.want_to_delete)
                    .setIcon(R.drawable.icon)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        deleteTest(test.id,view);
                        ((MainActivity) view.getContext()).startMyTests();
                        //Toast.makeText(getContext(), "Yaay", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        });

        holder.test_share_btn.setOnClickListener(view -> {
            ((MainActivity) view.getContext()).generatePDF(test);
        });

    }

    private void deleteTest(String id,View view) {

        //Codigo para borrar datos de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tests").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.w(TAG, "Test deleted");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
    }
}
