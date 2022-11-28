package com.example.testlab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.protobuf.LazyStringArrayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewClassActivity extends Activity {

    private static final int SELECT_IMAGE_REQUEST_CODE = 2001;
    private static final String BASE_STORAGE_REFERENCE = "images";

    private LinearLayout root;
    private Snackbar snackbar;
    private ImageView classPic;
    private EditText className, classDesc;

    private FirebaseDatabase database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_class);

        database = FirebaseDatabase.getInstance ();
        storage = FirebaseStorage.getInstance ();

        root = findViewById (R.id.root);

        Button btnChangePic = findViewById (R.id.photo_btn);
        btnChangePic.setOnClickListener (view -> selectImage ());

        classPic = findViewById (R.id.class_logo);

        className = findViewById (R.id.class_name_edtxt);
        classDesc = findViewById (R.id.class_desc_edtxt);

        Button btnSave = findViewById (R.id.create_class_btn);
        btnSave.setOnClickListener (view -> {
            snackbar = Snackbar.make (root, "Guardando...", Snackbar.LENGTH_INDEFINITE);
            ViewGroup layer = (ViewGroup) snackbar.getView ().findViewById (com.google.android.material.R.id.snackbar_text).getParent ();
            ProgressBar bar = new ProgressBar (getBaseContext ());
            layer.addView (bar);
            snackbar.show ();

            saveInfo ();
        });

    }

    private void saveInfo () {

        String name = className.getText().toString ();
        String desc = classDesc.getText().toString ();

        Bitmap bitmap = getBitmapFromDrawable (classPic.getDrawable ());
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

        String fileReferece = String.format (Locale.US, "%s/%s_%d.jpg",
                BASE_STORAGE_REFERENCE, name, System.currentTimeMillis ());

        StorageReference images = storage.getReference (fileReferece);
        images.putBytes (data)
                .addOnCompleteListener (task -> {
                    if (task.isComplete ()) {
                        Task<Uri> dlUrlTask = images.getDownloadUrl ();

                        dlUrlTask.addOnCompleteListener (task1 -> {
                            Uri dlUrl = task1.getResult ();
                            if (dlUrl == null) return;

                            String foto = dlUrl.toString ();
                            doSave (name,desc,foto);
                        });
                    }
                })
                .addOnFailureListener (e -> {
                    Log.e ("TYAM", e.getMessage ());
                });
    }

    private void doSave (String name, String desc, String foto) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = null;
        if (user != null) {
            email = user.getEmail();
        }
        
        Map<String, Object> clase = new HashMap<>();
        clase.put("name", name);
        clase.put("desc", desc);
        clase.put("logo", foto);
        clase.put("owner", email);

        String classID = calculateStringHash (clase.toString ());

        //ArrayList<String> clases = new ArrayList<String>();
        //clases.add(classID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String finalEmail = email;

        String finalEmail1 = email;
        db.collection(classID).document( "data")
                .set(clase)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //Codigo para agregar la classID a la informacion del usuario

                        db.collection("users").document(finalEmail1)
                                .update("clases", FieldValue.arrayUnion(classID));

                        snackbar.dismiss ();
                        Snackbar.make (root, "Información almacenada!", Snackbar.LENGTH_LONG).show ();
                        finish();
                        //getBaseContext().getClasesfromUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (getBaseContext (), "datos no registrados!", Toast.LENGTH_LONG).show ();
                    }
                });
    }


    private Bitmap getBitmapFromDrawable (Drawable drble) {
        if (drble instanceof BitmapDrawable) {
            return  ((BitmapDrawable) drble).getBitmap ();
        }
        Bitmap bitmap = Bitmap.createBitmap (drble.getIntrinsicWidth (), drble.getIntrinsicHeight (), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drble.setBounds (0, 0, canvas.getWidth (), canvas.getHeight ());
        drble.draw (canvas);

        return bitmap;
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

    private void selectImage () {
        Intent intent = new Intent (Intent.ACTION_PICK);
        intent.setType ("image/*");

        String [] mimeTypes = { "image/jpeg", "image/png" };
        intent.putExtra (Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult (intent, SELECT_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null) return;

            Uri uri = data.getData ();
            classPic.setImageURI (uri);
        }

        super.onActivityResult (requestCode, resultCode, data);
    }
}
