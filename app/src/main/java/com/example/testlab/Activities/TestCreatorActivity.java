package com.example.testlab.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testlab.Fragments.TestCreatorFragment;
import com.example.testlab.R;
import com.example.testlab.Test;


public class TestCreatorActivity extends AppCompatActivity {

    Test test;

    public Test getTest() {
        return this.test;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_creator);

        test = (Test) getIntent().getSerializableExtra("KEY_NAME");

        loadTestFragment();
    }

    public void loadTestFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.test_creator_container,
                new TestCreatorFragment()).commit();
    }
}