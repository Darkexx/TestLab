package com.example.testlab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.testlab.MenuActivity;

public class LoginFragment extends Fragment {
    private FirebaseAuth auth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance ();
        EditText edtEmail = view.findViewById (R.id.user_edtxt);
        EditText edtPassword = view.findViewById (R.id.pass_edtxt);

        Button btnRegister = view.findViewById (R.id.login_btn);
        btnRegister.setOnClickListener (v -> {
            login (edtEmail.getText().toString (), edtPassword.getText().toString ());
        });

    }

    private void login (String email, String password) {

        auth.signInWithEmailAndPassword (email, password)
                .addOnCompleteListener (task -> {
                    if (task.isSuccessful ()) {
                        FirebaseUser user = auth.getCurrentUser ();
                        String name = "";

                        System.out.println("logeado");

                        if (user != null) {
                            name = user.getDisplayName ();

                            ((MainActivity) getActivity()).openClasesMenu();
                        }

                        Toast.makeText (getActivity (), "Usuario " + name, Toast.LENGTH_LONG).show ();
                    } else {
                        Toast.makeText (getActivity (), "Usuario y/o contraseña no reconocida!", Toast.LENGTH_LONG).show ();
                    }
                });
    }

}