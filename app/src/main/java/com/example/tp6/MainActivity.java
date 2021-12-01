package com.example.tp6;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
    FloatingActionButton buscar;
    TextView txtResultado;
    FaceServiceClient servicioProcesamientoImagenes;
    SharedPreferences preferencias;
    final int REQUEST_CODE_TAKE_BROWSE_PHOTO_PERMISSION = 1169;
    final int REQUEST_CODE_TAKE_PHOTO = 1142;
    final int REQUEST_CODE_BROWSE_PHOTO = 1150;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //dialogoDeProgreso = new ProcessDialog(this);
        txtResultado=findViewById(R.id.textViewResultados);
        cover = findViewById(R.id.coverImg);
        boton = findViewById(R.id.floatingActionButton);
        buscar = findViewById(R.id.buscarfto);

        Log.d("Inicio","inicializo el SharedPreferences");
        preferencias = getSharedPreferences("Naza", Context.MODE_PRIVATE);

        Log.d("Inicio","Defino credenciales para usar la API");
        String apiEndpoint = "https://visionservicedai.cognitiveservices.azure.com/face/v1.0";
        String subscriptionKey = "0634b45d88ba439e80d05964716fa4c4";

        try {
            Log.d("Inicio","Voy instanciar el servicio");
            servicioProcesamientoImagenes =new FaceServiceRestClient(apiEndpoint,subscriptionKey);
            Log.d("Inicio","Servicio instanciado exitosamente");
        } catch (Exception error){
            Log.d("Inicio","Error en inicializacion"+ error.getMessage());
        }

/*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Inicio", "NO PERMITIDO");
            boton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, codigoPedirPermiso);
        } else {
            Log.d("Inicio", "TIENE PERMISO");
boton.setEnabled(true);

        }
        */

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Camera", "NO PERMITIDO");
            boton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE_TAKE_BROWSE_PHOTO_PERMISSION);
        }




/*

//SELECCIONAR IMAGEN VIEJO
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(MainActivity.this)

                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();

            }
        });

        */

    }

/*
    @Override
    public void onRequestPermissionsResult(int codigoRespuesta, @NonNull String[] nombresPermisos, @NonNull int[] resultadosPermisos) {
        if (codigoRespuesta == codigoPedirPermiso)
            for (int PunteroPermiso = 0; PunteroPermiso < nombresPermisos.length; PunteroPermiso++) {
                Log.d("Permisos pedidos", "Permiso" + PunteroPermiso + " - Nombre " + nombresPermisos[PunteroPermiso]+"-" + (resultadosPermisos[PunteroPermiso]==PackageManager.PERMISSION_GRANTED));
            }
    }
*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_TAKE_BROWSE_PHOTO_PERMISSION)
            for (int i = 0; i < permissions.length; i++) {
                int visibility = grantResults[i] == PackageManager.PERMISSION_GRANTED ? View.VISIBLE : View.INVISIBLE;
                if (permissions[i].equals(Manifest.permission.CAMERA))
                    boton.setVisibility(visibility);
                else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    buscar.setVisibility(visibility);
                Log.d("PERMISSION", "" + permissions[i] + ": " + (grantResults[i] == PackageManager.PERMISSION_GRANTED));
            }
    }





    private void procesarImagenObtenida(final Bitmap imagenaProcesar) {

        Log.d("ProcesarImagen", "Armo el  Stream para el procesamiento");

        ByteArrayOutputStream streamSalida = new ByteArrayOutputStream();
        imagenaProcesar.compress(Bitmap.CompressFormat.JPEG, 100, streamSalida);
        ByteArrayInputStream streamEntrada = new ByteArrayInputStream(streamSalida.toByteArray());


        Log.d("ProcesarImagen", "Declaro la clase del AsyncTask");

        class procesarImagen extends AsyncTask<InputStream, String, Face[]> {
            @Override
            protected Face[] doInBackground(InputStream... imagenAProcesar) {
                //publishProgress("Detectando caras... ");

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
                    resultado = servicioProcesamientoImagenes.detect(imagenAProcesar[0], true, false, atributos);
                } catch (Exception error) {
                    Log.d("ProcesarImagen", "Error: " + error.getMessage());
                }
                return resultado;
            }
/*
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
*/
            @Override
            protected void onPostExecute(Face[] resultado) {
                super.onPostExecute(resultado);
               // dialogoDeProgreso.dismiss();

                if (resultado == null) {
                    txtResultado.setText("ERROR EN EL PROCESAMIENTO");
                } else {
                    if (resultado.length > 0) {
                        Log.d("ProcesarImagen", "Mando a recuadrar las caras");
                        recuadrarCaras(imagenaProcesar, resultado);

                        Log.d("ProcesarImagen", "Mando a procesar los resultados de las caras");
                        procesarResultadosDeCaras(resultado);
                    } else {
                        Log.d("ProcesarImagen", "No se detectó ninguna caripela");
                        txtResultado.setText("No se detectó ninguna cara");
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
    cover.setImageBitmap(imagenADibujar);

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
    mensaje="\n";

    }


    }

    mensaje +=" -H:" +cantidadHombres+" -M:" +cantidadMujeres;
    txtResultado.setText(mensaje);

}



    //ON ACTIVITY RESULT SIRVE PARA QUE SE EJECUTEN COSAS DESPUES
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            Bitmap photo = (Bitmap)data.getExtras().get("data");
            cover.setImageBitmap(photo);
            procesarImagenObtenida(photo);
        }
        else if (requestCode == REQUEST_CODE_BROWSE_PHOTO && data != null) {
            Uri imgPath = data.getData();
            Bitmap imgBitmap = null;
            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgPath);
                Log.d("fotoElegida", "SUCCESS");
            }
            catch (Exception e) {
                Log.d("fotoElegida", "Error obteniendo " + imgPath);
            }
            if (imgBitmap != null) {
                cover.setImageBitmap(imgBitmap);
                procesarImagenObtenida(imgBitmap);
            }
        }

    }


    public void TakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }
    public void BrowsePhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar foto"), REQUEST_CODE_BROWSE_PHOTO);
    }


}