package com.example.testlab.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.testlab.Fragments.AccountFragment;
import com.example.testlab.R;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance ();
        if(auth.getCurrentUser() != null){
            openClasesMenu();
        }
        else{
            setContentView(R.layout.activity_account);
            doMenu();
        }

    }

    public void doMenu(){
        getSupportFragmentManager ()
                .beginTransaction ()
                .replace (R.id.mainContainer, new AccountFragment())
                .setTransition (FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit ();
    }

    public void openClasesMenu(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Here you want to show the user a dialog box
        doMenu();

    }
}