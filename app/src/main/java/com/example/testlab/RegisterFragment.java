package com.example.testlab;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance ();

        EditText edtUser = view.findViewById (R.id.user_name_edtxt);
        EditText edtEmail = view.findViewById (R.id.user_edtxt);
        EditText edtPassword = view.findViewById (R.id.pass_edtxt);


        Button btnRegister = view.findViewById (R.id.register_btn);
        btnRegister.setOnClickListener (v -> {
            registerUser (edtEmail.getText().toString (), edtPassword.getText().toString (),edtUser.getText().toString ());
        });

    }

    private void registerUser (String email, String password, String userName) {

        auth.createUserWithEmailAndPassword (email, password)
                .addOnCompleteListener (task -> {
                    if (task.isSuccessful ()) {

                        saveNewUser(email, userName);

                        Toast.makeText (getContext (), "Register completed!", Toast.LENGTH_LONG).show ();
                        ((MainActivity) getActivity()).openClasesMenu();

                    } else {
                        if (task.getException () != null) {
                            Log.e("TYAM", task.getException().getMessage());
                        }
                        Toast.makeText (getContext (), "Register failed!", Toast.LENGTH_LONG).show ();
                    }
                });


    }

    private void saveNewUser (String userID, String userName) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", userName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document( userID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText (getContext (), "datos registrados!", Toast.LENGTH_LONG).show ();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (getContext (), "datos no registrados!", Toast.LENGTH_LONG).show ();
                    }
                });
    }

}


class User {
    public String name;
}