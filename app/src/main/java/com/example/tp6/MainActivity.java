package com.example.tp6;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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


        dialogoDeProgreso = new ProgressDialog(this);
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

    //aunque no lo parezca esta bien copiado

    private void procesarImagenObtenida(Uri imageuri) {

        Log.d("ProcesarImagen", "Armo el  Stream para el procesamiento");
        ByteArrayOutputStream streamSalida = new ByteArrayOutputStream();
        ByteArrayInputStream streamEntrada = new ByteArrayInputStream(streamSalida.toByteArray());


        Log.d("ProcesarImagen", "Declaro la clase del AsyncTask");

        class procesarImagen extends AsyncTask<InputStream, String, Face[]> {
            @Override
            protected Face[] doInBackground(InputStream... imagenAProcesar) {
                publishProgress("Detectando caras... ");

                Face[] resultado = null;
                try {

                    Log.d("ProcesarImagen", "Defino qué atributos quiero procesar");
                    FaceServiceClient.FaceAttributeType[] atributos;
                    atributos = new FaceServiceClient.FaceAttributeType[]{

                            FaceServiceClient.FaceAttributeType.Age,
                            FaceServiceClient.FaceAttributeType.Glasses,
                            FaceServiceClient.FaceAttributeType.Smile,
                            FaceServiceClient.FaceAttributeType.FacialHair,
                            FaceServiceClient.FaceAttributeType.Gender

                    };
                    Log.d("ProcesarImagen", "Llamo al procesamiento de la imagen");
                    resultado = servicioProcesamientoImagen.detect(imagenAProcesar[0], true, false, atributos);
                } catch (Exception error) {
                    Log.d("ProcesarImagen", "Error: " + error.getMessage());
                }
                return resultado;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialogoDeProgreso.show();
            }

            @Override
            protected void onProgressUpdate(String... mensajeProceso) {
                super.onProgressUpdate(mensajeProceso);
                dialogoDeProceso.setMessage(mensajeProceso[0]);
            }

            @Override
            protected void onPostExecute(Face[] resultado) {
                super.onPostExecute(resultado);
                dialogoDeProgreso.dismiss();

                if (resultado == null) {
                    txtResultado.setText("Error en el procesamiento");
                } else {
                    if (resultado.length > 0) {
                        Log.d("ProcesarImagen", "Mando a recuadrar las caras");
                        recuadrarCaras(imageuri, resultado);  //Creo que esto es solo para bitmap

                        Log.d("ProcesarImagen", "Mando a procesar los resultados de las caras");
                        procesarResultadosDeCaras(resultado);
                    } else {
                        Log.d("ProcesarImagen", "No se detectó ninguna caripela");
                        txtResultado.setText("No se detectó ninguna caripela");
                    }
                }
            }
        }
            procesarImagen miTarea = new procesarImagen();
        miTarea.execute(streamEntrada);

        }


void recuadrarCaras (Bitmap imageOriginal,Face[] carasARecuadrar){

        Bitmap imagenADibujar;
        imagenADibujar=imageOriginal.copy(Bitmap.Config.ARGB_8888,true);

        Log.d("RecuadrarCaras", "Armo el canvas y el pincel");
        Canvas lienzo;
        lienzo=new Canvas(imagenADibujar);
    Paint pincel;
    pincel=new Paint();

        pincel.setAntiAlias(true);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setColor(Color.RED);
        pincel.setStrokeWidth(5);
    Log.d("RecuadrarCaras", "Para cada cara recibida dibujar su rectangulo");

    for (Face unaCara:carasARecuadrar){
        FaceRectangle rectanguloUnaCara;
        rectanguloUnaCara=unaCara.faceRectangle;

        lienzo.drawRect( rectanguloUnaCara.left,
        rectanguloUnaCara.top,
rectanguloUnaCara.left+rectanguloUnaCara.width,
                rectanguloUnaCara.top+rectanguloUnaCara.height,
                pincel);

    }

    Log.d("RecuadrarCaras", "Pongo la imagen resultante en el ImageView");
    imgResultado.setImageBitmap(imagenADibujar);

}

void procesarResultadosDeCaras(Face[] carasAProcesar){

    int cantidadHombres, cantidadMujeres;
    cantidadHombres=preferencias.getInt("cantidadHombre", 0);
    cantidadMujeres=preferencias.getInt("cantidadHombre", 0);

    Log.d("procesarImagen", "Armo el mensaje con información");
    String mensaje;
    mensaje="";
    for (int punteroCara=0; punteroCara<carasAProcesar.length;punteroCara++) {

mensaje+="Edad: " + carasAProcesar [punteroCara].faceAttributes.age;
        mensaje+=" - Sonrisa: " + carasAProcesar [punteroCara].faceAttributes.smile;
        mensaje+=" - Barba: " + carasAProcesar [punteroCara].faceAttributes.facialHair.beard;
        mensaje+=" - Genero: " + carasAProcesar [punteroCara].faceAttributes.gender;
        mensaje+=" - Anteojos: " + carasAProcesar [punteroCara].faceAttributes.glasses;

    if ( carasAProcesar[punteroCara]. faceAttributes.gender.equals("male") ) {
        cantidadHombres++;
    } else {
        cantidadMujeres++;
    }

        SharedPreferences.Editor editorDePreferencias;
        editorDePreferencias=preferencias.edit();
        editorDePreferencias.putInt("cantidadHombres", cantidadHombres);
        editorDePreferencias.putInt("cantidadMujeres", cantidadMujeres);
        editorDePreferencias.commit();


if (punteroCara<carasAProcesar.length-1){
    mensaje=""

    }


    }


}









        /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri imageuri = data.getData();
        cover.setImageURI(imageuri);
       procesarImagenObtenida(imageuri);


    }


 */
}