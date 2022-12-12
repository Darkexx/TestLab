package com.example.testlab.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testlab.Activities.TestCreatorActivity;
import com.example.testlab.Adapters.QuestionsAdapter;
import com.example.testlab.R;
import com.example.testlab.Test;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestCreatorFragment extends Fragment {

    Test test;
    Snackbar snackbar;

    boolean showedWarning, editorMode;

    ArrayList<String> questions;
    QuestionsAdapter adapter;
    RecyclerView recyclerView;

    NestedScrollView root;

    Button addQuestionBtn;
    FloatingActionButton saveTestBtn;
    EditText name, desc, schoool, clase, date, input;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_creator, container, false);

        test = ((TestCreatorActivity) this.getActivity()).getTest();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.fragment_test_creator_root);

        showedWarning = false;
        editorMode = false;

        name = view.findViewById(R.id.test_name);
        desc = view.findViewById(R.id.test_desc);
        schoool = view.findViewById(R.id.test_institution);
        clase = view.findViewById(R.id.test_class);
        date = view.findViewById(R.id.test_date);

        questions = new ArrayList<>();

        if (test != null) {
            editorMode = true;
            name.setText(test.name);
            desc.setText(test.desc);
            schoool.setText(test.school);
            clase.setText(test.clase);
            date.setText(test.date);
            questions = test.questions;
        }

        adapter = new QuestionsAdapter(questions, this);

        recyclerView = view.findViewById(R.id.questions_container);
        //recyclerView.addItemDecoration (new DividerItemDecoration (this, DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        addQuestionBtn = view.findViewById(R.id.add_question);
        input = view.findViewById(R.id.question_input);

        addQuestionBtn.setOnClickListener(view13 -> {
            String inputtext = String.valueOf(input.getText());
            if (TextUtils.isEmpty(inputtext)) {
                Snackbar.make(view13, "Empty", Snackbar.LENGTH_SHORT).show();
            } else {
                questions.add(inputtext);
                input.setText("");
                //RecipeEditorFragment.this.hideKeyboard(view13);
                adapter.notifyDataSetChanged();
            }
        });

        saveTestBtn = view.findViewById(R.id.save_test);
        saveTestBtn.setOnClickListener(view13 -> {
            snackbar = Snackbar.make(view13, R.string.saving, Snackbar.LENGTH_INDEFINITE);
            ViewGroup layer = (ViewGroup) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();
            ProgressBar bar = new ProgressBar(TestCreatorFragment.this.getContext());
            layer.addView(bar);
            snackbar.show();

            saveData(editorMode);
        });


        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!showedWarning) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.warning)
                            .setMessage(R.string.want_to_leave)
                            .setIcon(R.drawable.icon)
                            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                showedWarning = true;
                                requireActivity().onBackPressed();
                                //Toast.makeText(getContext(), "Yaay", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                    // Handle back press
                } else {
                    // If you want to get default implementation of onBackPressed, use this
                    this.remove();
                    requireActivity().onBackPressed();

                }
            }
        });


    }

    public void removeQuestion(String question) {
        questions.remove(question);
        adapter.notifyDataSetChanged();
    }

    public void saveData(boolean editorMode) {

        if (fieldsEmpty()) {
            Snackbar.make(root, R.string.empty_data, Snackbar.LENGTH_LONG).show();
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String email = null;
            if (user != null) {
                email = user.getEmail();
            }

            Map<String, Object> examen = new HashMap<>();

            String n = name.getText().toString();
            String d = desc.getText().toString();
            String sch = schoool.getText().toString();
            String c = clase.getText().toString();
            String da = date.getText().toString();

            examen.put("name", n);
            examen.put("desc", d);
            examen.put("school", sch);
            examen.put("clase", c);
            examen.put("date", da);
            examen.put("owner", email);
            examen.put("questions", questions);


            String id;
            if (editorMode) {
                id = test.id;
            }
            else{
                id = n + "_" + System.currentTimeMillis();
            }

            examen.put("id", id);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("tests").document(id)
                    .set(examen)
                    .addOnSuccessListener(aVoid -> {
                        snackbar.dismiss();
                        Snackbar.make(root, R.string.data_saved, Snackbar.LENGTH_LONG).show();
                        getActivity().finish();

                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "datos no registrados!", Toast.LENGTH_LONG).show());
        }
    }

    public boolean fieldsEmpty() {
        if (TextUtils.isEmpty(name.getText().toString()) || TextUtils.isEmpty(desc.getText().toString()) || TextUtils.isEmpty(schoool.getText().toString()) || TextUtils.isEmpty(clase.getText().toString()) || TextUtils.isEmpty(date.getText().toString()) || questions.isEmpty()) {
            return true;
        }
        return false;
    }
}
