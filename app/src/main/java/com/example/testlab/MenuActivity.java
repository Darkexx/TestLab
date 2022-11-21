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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

public class MenuActivity extends Activity {
    private FirebaseAuth auth;
    private static final int SELECT_IMAGE_REQUEST_CODE = 2001;
    private static final String BASE_STORAGE_REFERENCE = "images";
    private static final String BASE_DATABASE_REFERENCE = "Clases";

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private LinearLayout root;
    private Snackbar snackbar;

    private Button btnChangePic;
    private ImageView class_logo;
    private TextView class_name;
    private TextView class_desc;

    private FirebaseStorage storage;
    private DatabaseReference clases;

    RecyclerView recyclerView;
    FirebaseDatabase database;
    ClassAdapter adapter;
    Vector<Clase> vector;


    @Override
    protected void onResume() {
        super.onResume ();

        //login ();
        getClases ();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        database = FirebaseDatabase.getInstance ();
        storage = FirebaseStorage.getInstance ();

        vector = new Vector<> ();
        adapter = new ClassAdapter (vector);

        recyclerView = findViewById (R.id.menuContainer);
        recyclerView.addItemDecoration (new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator (new DefaultItemAnimator());
        recyclerView.setLayoutManager (new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter (adapter);

        Button btnNewClass = (Button)findViewById (R.id.class_new_btn);
        btnNewClass.setOnClickListener (v -> {

            //Intent intent = new Intent(this, NewClassActivity.class);
            //startActivity(intent);
            //finish();

            CreatePopup();
        });

    }

    public  void  CreatePopup(){
        dialogBuilder = new AlertDialog. Builder( this);
        View contactPopupView = getLayoutInflater().inflate(R.layout.popup_new_class, null);

        btnChangePic = (Button) contactPopupView.findViewById (R.id.photo_btn);
        class_logo = (ImageView) contactPopupView.findViewById (R.id.class_logo);
        class_name = (TextView) contactPopupView.findViewById(R.id.class_name_edtxt);
        class_desc = (TextView) contactPopupView.findViewById (R.id.class_desc_edtxt);
        root = (LinearLayout) contactPopupView.findViewById(R.id.popup_root);

        dialogBuilder.setView(contactPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        btnChangePic.setOnClickListener (v -> {
                selectImage();

        });

        Button btnSave = (Button) contactPopupView.findViewById (R.id.create_class_btn);
        btnSave.setOnClickListener (view -> {
            snackbar = Snackbar.make (root, "Guardando...", Snackbar.LENGTH_INDEFINITE);
            ViewGroup layer = (ViewGroup) snackbar.getView ().findViewById (com.google.android.material.R.id.snackbar_text).getParent ();
            ProgressBar bar = new ProgressBar (getBaseContext ());
            layer.addView (bar);
            snackbar.show ();

            saveInfo ();
            dialog.dismiss();
            adapter.notifyDataSetChanged();
        });
    }

    private void getClases () {
        Snackbar snackbar = Snackbar.make (recyclerView, "Obteniendo información...", Snackbar.LENGTH_INDEFINITE);
        ViewGroup layer = (ViewGroup) snackbar.getView ().findViewById (com.google.android.material.R.id.snackbar_text).getParent ();
        ProgressBar bar = new ProgressBar (getBaseContext ());
        layer.addView (bar);
        snackbar.show ();

        DatabaseReference reference = database.getReference ("Clases");
        //Vector<User> users = new Vector<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap: snapshot.getChildren ()) {
                    Clase u = snap.getValue (Clase.class);
                    vector.add (u);
                }

                //recyclerView.setAdapter (new UsersAdapter (users));
                adapter.notifyDataSetChanged ();
                snackbar.dismiss ();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {
                Log.e ("TYAM", error.getDetails ());
                snackbar.dismiss ();
            }
        });
    }


    private void saveInfo () {
        clases = database.getReference (BASE_DATABASE_REFERENCE);

        Clase clase        = new Clase ();
        clase.nombre_clase = class_name.getText().toString ();
        clase.desc_clase   = class_desc.getText().toString ();

        Bitmap bitmap = getBitmapFromDrawable (class_logo.getDrawable ());
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        bitmap.compress (Bitmap.CompressFormat.JPEG, 100, bos);
        byte [] data = bos.toByteArray ();

        try {
            bos.close();
        } catch (IOException ex) {
            if (ex.getMessage () != null) {
                Log.e ("TYAM", ex.getMessage ());
                return;
            }

            Log.e ("TYAM", "Error getting bytearray...", ex);
        }

        String fileReferece = String.format (Locale.US, "%s/%s_%s_%d.jpg",
                BASE_STORAGE_REFERENCE, clase.nombre_clase, clase.desc_clase, System.currentTimeMillis ());

        StorageReference images = storage.getReference (fileReferece);
        images.putBytes (data)
                .addOnCompleteListener (task -> {
                    if (task.isComplete ()) {
                        Task<Uri> dlUrlTask = images.getDownloadUrl ();

                        dlUrlTask.addOnCompleteListener (task1 -> {
                            Uri dlUrl = task1.getResult ();
                            if (dlUrl == null) return;

                            clase.foto = dlUrl.toString ();
                            doSave (clase);
                        });
                    }
                })
                .addOnFailureListener (e -> {
                    Log.e ("TYAM", e.getMessage ());
                });
    }

    private void doSave (Clase clase) {
        String nodeId = calculateStringHash (clase.toString ());
        HashMap<String, Object> entry = new HashMap<> ();
        entry.put (nodeId, clase);

        clases.updateChildren (entry)
                .addOnSuccessListener (aVoid -> {
                    snackbar.dismiss ();
                    Snackbar.make (root, "Información almacenada!", Snackbar.LENGTH_LONG).show ();
                })
                .addOnFailureListener (e -> Toast.makeText (getBaseContext (),
                        "Error actualizando la BD: " + e.getMessage (),
                        Toast.LENGTH_LONG).show ());
    }


    private void selectImage () {
        Intent intent = new Intent (Intent.ACTION_PICK);
        intent.setType ("image/*");

        String [] mimeTypes = { "image/jpeg", "image/png" };
        intent.putExtra (Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult (intent, SELECT_IMAGE_REQUEST_CODE);
    }

    private String calculateStringHash (String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance ("MD5");
            md5.update(input.getBytes());
            byte[] digest = md5.digest();

            StringBuilder sb = new StringBuilder(digest.length * 2);

            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 8) & 0xf, 16));
                sb.append(Character.forDigit(b & 0xf, 16));
            }

            return sb.toString ();
        } catch (NoSuchAlgorithmException ex) {
            Log.e ("TYAM", ex.getMessage ());
        }

        return null;
    }

    private Bitmap getBitmapFromDrawable (Drawable drble) {
        // debido a la forma que el sistema dibuja una imagen en un el sistema gráfico
        // es necearios realzar comprobaciones para saber del tipo de objeto Drawable
        // con que se está trabajando.
        //
        // si el objeto recibido es del tipo BitmapDrawable no se requieren más conversiones
        if (drble instanceof BitmapDrawable) {
            return  ((BitmapDrawable) drble).getBitmap ();
        }

        // en caso contrario, se crea un nuevo objeto Bitmap a partir del contenido
        // del objeto Drawable
        Bitmap bitmap = Bitmap.createBitmap (drble.getIntrinsicWidth (), drble.getIntrinsicHeight (), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drble.setBounds (0, 0, canvas.getWidth (), canvas.getHeight ());
        drble.draw (canvas);

        return bitmap;
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null) return;

            Uri uri = data.getData ();
            class_logo.setImageURI (uri);
        }

        super.onActivityResult (requestCode, resultCode, data);
    }
}


class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.UsersVH> {
    private final Vector<Clase> clases;

    public ClassAdapter (Vector<Clase> clases) {
        this.clases = clases;
    }

    @NonNull
    @Override
    public ClassAdapter.UsersVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.fragment_clase, parent, false);
        return new UsersVH (view);
    }

    @Override
    public void onBindViewHolder (@NonNull ClassAdapter.UsersVH holder, int position) {
        Clase u = clases.get (position);

        //holder.tvNameListItem.setText (String.format (Locale.getDefault (), "%s %s", u.nombre, u.apellidos));
        //holder.tvAgeListItem.setText (String.valueOf (u.edad));
        holder.nombre_clase.setText (u.nombre_clase);
        holder.desc_clase.setText (u.desc_clase);

        holder.setPicture (Uri.parse (u.foto));
    }

    @Override
    public int getItemCount () {
        return clases.size ();
    }


    class UsersVH extends RecyclerView.ViewHolder {
        private final ImageView logo;
        public TextView nombre_clase, desc_clase;

        public UsersVH (@NonNull View itemView) {
            super (itemView);

            logo = itemView.findViewById (R.id.class_logo);
            nombre_clase = itemView.findViewById (R.id.class_name);
            desc_clase = itemView.findViewById (R.id.class_desc);
        }

        public void setPicture (Uri url) {
            Picasso.get()
                    .load (url)
                    .into (logo);
        }
    }
}