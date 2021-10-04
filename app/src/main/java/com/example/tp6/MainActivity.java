package com.example.tp6;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    ImageView cover;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cover = findViewById(R.id.coverImg);
        fab = findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(MainActivity.this)
                        /*
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        */
                        .start();

            }
        });


       // procesarImagenObtenida();


    }
/*
    private void procesarImagenObtenida() {

        Log.d("ProcesarImagen", "Armo el  Stream para el procesamiento");
        ByteArrayOutputStream streamSalida = new ByteArrayOutputStream();
        imagenAProcesar.compress(Bitmap.CompressFormat.JPEG, 100, streamSalida);
        ByteArrayInputStream streamEntrada=new ByteArrayInputStream(streamSalida.toByteArray());


        Log.d("ProcesarImagen", "Declaro la clase del AsyncTask");

        class procesarImagen extends AsyncTask<InputStream, String, Face[]> {




        }

        procesarImagen miTarea= new procesarImagen();
        miTarea.execute(streamEntrada);

    }



 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = data.getData();
        cover.setImageURI(uri);
    }
}