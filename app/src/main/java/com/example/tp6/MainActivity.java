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
    FloatingActionButton boton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cover = findViewById(R.id.coverImg);
        boton = findViewById(R.id.floatingActionButton);

        boton.setOnClickListener(new View.OnClickListener() {
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




    }

    private void procesarImagenObtenida(Uri imageuri) {

        Log.d("ProcesarImagen", "Armo el  Stream para el procesamiento");
        ByteArrayOutputStream streamSalida = new ByteArrayOutputStream();
        ByteArrayInputStream streamEntrada=new ByteArrayInputStream(streamSalida.toByteArray());


        Log.d("ProcesarImagen", "Declaro la clase del AsyncTask");

        class procesarImagen extends AsyncTask<InputStream, String, Face[]> {

            @Override
            protected Face[] doInBackground(InputStream...imagenAProcesar){
                publishProgress("Detectando caras... ");

                Face[] resultado=null;
                try {

                    Log.d("ProcesarImagen", "Defino qué atributos quiero procesar");
                    FaceServiceClient.FaceAttributeType[] atributos;
                    atributos= new FaceServiceClient.FaceAttributeType[] {

                            FaceServiceClient.FaceAttributeType.Age,
                            FaceServiceClient.FaceAttributeType.Glasses,
                            FaceServiceClient.FaceAttributeType.Smile,
                            FaceServiceClient.FaceAttributeType.FacialHair,
                            FaceServiceClient.FaceAttributeType.Gender

                    };
                    Log.d("ProcesarImagen", "Llamo al procesamiento de la imagen");
                    resultado=servicioProcesamientoImagen.detect (imagenAProcesar[0], true, false, atributos);
                } catch (Exception error) {
                    Log.d("ProcesarImagen", "Error: "+error.getMessage());
                }
                return resultado;
            }

            @Override
            protected void onPreExecute(){
                super.onPreExecute();
                dialogoDeProgreso.show();
            }
            @Override
            protected void onProgressUpdate(String...mensajeProceso){
                super.onProgressUpdate(mensajeProceso);
                dialogoDeProceso.setMessage(mensajeProceso[0]);
            }
            @Override
            protected void onPostExecute(Face[] resultado){}
        }

        procesarImagen miTarea= new procesarImagen();
        miTarea.execute(streamEntrada);

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri imageuri = data.getData();
        cover.setImageURI(imageuri);
       procesarImagenObtenida(imageuri);


    }
}