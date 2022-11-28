package com.example.testlab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

public class MenuActivity extends Activity {

    private FirebaseAuth auth;
    private Snackbar snackbar;

    private static final String TAG = "EmailPassword";
    private LinearLayout root;

    ClasesAdapter adapter;
    Vector<Clase> vector;
    RecyclerView recyclerView;


    @Override
    protected void onResume() {
        super.onResume();

        //login ();
        //getClasesfromUser();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        vector = new Vector<> ();
        adapter = new ClasesAdapter (vector);

        root = findViewById (R.id.root);

        recyclerView = findViewById (R.id.menuContainer);
        recyclerView.addItemDecoration (new DividerItemDecoration (this, DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator (new DefaultItemAnimator ());
        recyclerView.setLayoutManager (new LinearLayoutManager (this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter (adapter);

        getClasesfromUser();

        Button btnNewClass = (Button) findViewById(R.id.class_new_btn);
        btnNewClass.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewClassActivity.class);
            startActivity(intent);
            //finish();
        });

    }

    public void getClasesfromUser () {
       //
        System.out.println("xddd");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String email = null;
        if (user != null) {
            email = user.getEmail();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        getClasesData(document);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }


    public void getClasesData(DocumentSnapshot document){
        ArrayList<String> clases = (ArrayList<String>) document.get("clases");

        if(clases != null){
            for (String clase : clases) {
                //System.out.println(clase);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection(clase).document("data");
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Clase clase = documentSnapshot.toObject(Clase.class);
                        System.out.println(clase.toString());
                        vector.add(clase);

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
        else{
            Snackbar.make (root, "Aun no formas parte de alguna clase", Snackbar.LENGTH_LONG).show ();
        }
    }

}

class ClasesAdapter extends RecyclerView.Adapter<ClasesAdapter.ClasesVH> {
    private final Vector<Clase> clases;

    public ClasesAdapter (Vector<Clase> clases) {
        this.clases = clases;
    }

    @NonNull
    @Override
    public ClasesAdapter.ClasesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.fragment_clase, parent, false);
        return new ClasesVH (view);
    }

    @Override
    public int getItemCount () {
        return clases.size ();
    }


    class ClasesVH extends RecyclerView.ViewHolder {
        private final ImageView logo;
        public TextView name, desc;

        public ClasesVH (@NonNull View itemView) {
            super (itemView);

            logo = (ImageView) itemView.findViewById (R.id.class_logo);
            name = (TextView) itemView.findViewById (R.id.class_name);
            desc = (TextView) itemView.findViewById (R.id.class_desc);
        }

        public void setPicture (Uri url) {
            Picasso.get()
                    .load (url)
                    .into (logo);
        }
    }


    @Override
    public void onBindViewHolder (@NonNull ClasesAdapter.ClasesVH holder, int position) {
        Clase clase = clases.get (position);

        holder.name.setText (String.valueOf (clase.name));
        holder.desc.setText (String.valueOf (clase.desc));

        holder.setPicture (Uri.parse (clase.logo));
    }
}