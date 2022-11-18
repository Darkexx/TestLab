package com.example.testlab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance ();
        if(auth.getCurrentUser() != null){
            System.out.println("logged in");

            openClasesMenu();
        }
        else{
            setContentView(R.layout.activity_main);
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
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Here you want to show the user a dialog box
        doMenu();

    }
}