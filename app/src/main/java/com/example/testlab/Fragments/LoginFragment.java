package com.example.testlab.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.testlab.Activities.AccountActivity;
import com.example.testlab.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                Toast.makeText (getActivity (), "Proporciona un correo valido", Toast.LENGTH_LONG).show ();
            }
            else if(TextUtils.isEmpty(edtPassword.getText().toString())) {
                Toast.makeText (getActivity (), "Ingresa una contraseña", Toast.LENGTH_LONG).show ();
            }
            else {
                login(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

    }

    private void login (String email, String password) {

        auth.signInWithEmailAndPassword (email, password)
                .addOnCompleteListener (task -> {
                    if (task.isSuccessful ()) {
                        FirebaseUser user = auth.getCurrentUser ();
                        String name = "";

                        if (user != null) {
                            name = user.getDisplayName ();

                            ((AccountActivity) getActivity()).openClasesMenu();
                        }

                        Toast.makeText (getActivity (), "Bienvenido", Toast.LENGTH_LONG).show ();
                    } else {
                        Toast.makeText (getActivity (), "Usuario y/o contraseña no reconocida!", Toast.LENGTH_LONG).show ();
                    }
                });
    }

}