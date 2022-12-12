package com.example.testlab.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.testlab.R;

public class AccountFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }


    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated (view, savedInstanceState);

        Button btnLogin = view.findViewById (R.id.login_btn);
        btnLogin.setOnClickListener (v -> {

            getActivity()
                    .getSupportFragmentManager ()
                    .beginTransaction ()
                    .replace (R.id.mainContainer, new LoginFragment())
                    .setTransition (FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit ();

        });

        Button btnRegister = view.findViewById (R.id.register_btn);
        btnRegister.setOnClickListener (v -> {

            getActivity()
                    .getSupportFragmentManager ()
                    .beginTransaction ()
                    .replace (R.id.mainContainer, new RegisterFragment())
                    .setTransition (FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit ();

        });

    }

}
