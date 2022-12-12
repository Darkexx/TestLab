package com.example.testlab.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.testlab.BuildConfig;
import com.example.testlab.Fragments.MyTestsFragment;
import com.example.testlab.R;
import com.example.testlab.Test;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;

    ImageButton exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (!checkPermission()) {
            requestPermission();
        }

        exit = findViewById(R.id.settings);
        exit.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.loging_out)
                    .setMessage(R.string.want_to_leave)
                    .setIcon(R.drawable.icon)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(this, AccountActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(getContext(), "Yaay", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        });

        startMyTests();

    }

    public void startMyTests() {
        getSupportFragmentManager().beginTransaction().replace(R.id.my_classes_container,
                new MyTestsFragment()).commit();
    }

    public void generatePDF(Test test) {

        PdfDocument pdfDocument = new PdfDocument();

        Paint paint = new Paint();
        Paint title = new Paint();

        int pagewidth = 792;
        int pageHeight = 1120;
        int numPage=1;

        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, numPage).create();
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
        Canvas canvas = myPage.getCanvas();


        //Margen del 100 px a la izquierda
        int x = 50;
        //Margen de 100 px por arriba
        int y = 100;

        //VARIABLES PARA LETRAS Y ESPACIADO
        double charSize = 0.5;
        int fontSize = 15;
        int limit_x = pagewidth - x;
        int limit_y = pageHeight - x;
        int espaciado = 25;
        int esapciadoPreguntas = 180;

        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        //ENCABEZADO
        title.setTextAlign(Paint.Align.CENTER);
        title.setTextSize(25);
        title.setColor(ContextCompat.getColor(this, R.color.black));
        canvas.drawText(test.clase + " - " + test.name, pagewidth / 2, y, title);
        y += espaciado * 1.2;

        title.setColor(ContextCompat.getColor(this, R.color.grey));

        title.setTextSize(18);
        canvas.drawText(test.school, pagewidth / 2, y, title);
        y += espaciado * 2.5;

        //ALGUNOS OTROS DATOS
        title.setTextSize(18);
        title.setTextAlign(Paint.Align.LEFT);
        title.setColor(ContextCompat.getColor(this, R.color.black));

        String alumno = this.getResources().getString(R.string.test_student) +": ";
        canvas.drawText( alumno, x, y, title);
        y += espaciado;

        String fecha = this.getResources().getString(R.string.test_date) +": "+ test.date;
        canvas.drawText( fecha, x, y, title);
        y += espaciado;

        String indicaciones = this.getResources().getString(R.string.test_desc) +": "+ test.desc;
        canvas.drawText(indicaciones, x, y, title);
        y += espaciado * 2;

        //PREGUNTAS
        title.setTextSize(fontSize);


        //CÓDIGO PARA IMPRIMIR EL TEXTO EN UN NUEVO PARRAFO SI LA CADENA ES MUY LARGA
        double xD = x;
        int yD = y;
        int num_preguntas = 1;
        for (String pregunta : test.questions) {

            //SI SE LLEA LA PAGINA CREAMOS UNA NUEVA
            if(yD +espaciado >= limit_y-20){
                System.out.println("Nueva pagina :))");
                pdfDocument.finishPage(myPage);
                numPage++;
                mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, numPage).create();
                myPage = pdfDocument.startPage(mypageInfo);
                canvas = myPage.getCanvas();
                yD = x;
            }

            String parrafo = num_preguntas+".  ";
            String pregunta2[] = pregunta.split("\\s+");
            //System.out.println(pregunta);
            for (String palabra : pregunta2) {
                double sumaPalabra = palabra.length() * (charSize * fontSize);
                if (xD + sumaPalabra >= limit_x-20) {
                    canvas.drawText(parrafo, x, yD, title);
                    yD += espaciado;

                    xD = 0;
                    parrafo = " ";
                }

                parrafo = parrafo + palabra + " ";
                canvas.drawText(parrafo, x, yD, title);
                xD += sumaPalabra;
            }
            num_preguntas ++;
            yD += esapciadoPreguntas;
        }

        //canvas.drawText("test", x, yD, title);

        File fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        String fileName = test.clase + "_" + test.name + ".pdf";
        fileName = fileName.replaceAll(" ", "_");

        File file = new File(fullPath,fileName);

        try {
            FileOutputStream fOut = new FileOutputStream(file);

            pdfDocument.finishPage(myPage);
            pdfDocument.writeTo(fOut);
            pdfDocument.close();

            //Las otras apps marcan esta accion como documento no valido
            shareFile(file);

            Toast.makeText(MainActivity.this, R.string.test_created, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    public void shareFile(File file){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse(file.getAbsolutePath());
        sharingIntent.setType("appliaction/pdf");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(sharingIntent, "Share PDF using"));
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }


}

