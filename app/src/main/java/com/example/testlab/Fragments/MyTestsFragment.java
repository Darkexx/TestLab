package com.example.testlab.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testlab.Activities.MainActivity;
import com.example.testlab.Activities.TestCreatorActivity;
import com.example.testlab.Adapters.TestsAdapter;
import com.example.testlab.R;
import com.example.testlab.Test;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Vector;

public class MyTestsFragment extends Fragment {

    private static final String TAG = "TAG";

    ImageButton add_test;
    TextView no_tests_text;

    TestsAdapter testsAdapter;
    Vector<Test> tests;
    RecyclerView recyclerView;

    LinearLayout root;

    boolean shouldRefreshOnResume = false;

    @Override
    public void onResume() {
        super.onResume();
        if (shouldRefreshOnResume) {
            ((MainActivity) getActivity()).startMyTests();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        shouldRefreshOnResume = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_tests, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.fragment_my_tests_root);

        add_test = view.findViewById(R.id.add_test);

        add_test.setOnClickListener(view1 -> {
            Intent intent = new Intent(view.getContext(), TestCreatorActivity.class);
            //intent.putExtra("KEY_NAME", clase);
            view.getContext().startActivity(intent);
        });

        tests = new Vector<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = null;
        if (user != null) {
            email = user.getEmail();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> docRef = db.collection("tests")
                .whereEqualTo("owner", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Test test = document.toObject(Test.class);
                            tests.add(test);
                            testsAdapter.notifyDataSetChanged();
                        }
                        no_tests_text = view.findViewById(R.id.no_tests_text);
                        if (tests.isEmpty()) {
                            no_tests_text.setVisibility(view.VISIBLE);
                        } else {
                            no_tests_text.setVisibility(view.GONE);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });


        testsAdapter = new TestsAdapter(tests);
        recyclerView = view.findViewById(R.id.tests_container);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(testsAdapter);

    }
}
