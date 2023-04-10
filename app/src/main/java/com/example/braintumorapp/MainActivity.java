package com.example.braintumorapp;
import org.tensorflow.lite.DataType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.braintumorapp.ml.Model;


import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {


    private Button gallery, camera, predict;
    private TextView result;
    private ImageView imageView;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getPermission();

        String[] labels=new String[3];
        int cnt=0;
        try {
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line=bufferedReader.readLine();
            while(line!=null){
                labels[cnt]=line;
                cnt++;
                line=bufferedReader.readLine();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        camera = (Button) findViewById(R.id.camera);
        gallery = (Button) findViewById(R.id.gallery);

        predict = findViewById(R.id.predict);
        result = (TextView) findViewById(R.id.result);
        imageView = (ImageView) findViewById((R.id.imageView));

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 100);

            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 12);
            }
        });
        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);


                try {
                    Model model = Model.newInstance(getApplicationContext());

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(bitmap);
                    ByteBuffer byteBuffer = tensorImage.getBuffer();

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    Model.Outputs outputs = model.process(inputFeature0);


                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();



                    // Releases model resources if no longer used.
                    model.close();
                    //
                    result.setText(labels[getMax(outputFeature0.getFloatArray())] + "");

                } catch (IOException e) {
                    // TODO Handle the exception
                }

            }
        });
    }

int getMax(float[] arr){
int max=0;
for (int i=0;i<arr.length;i++){
    if(arr[i]>arr[max])
        max=i;
}
return max;
}

   /* private void classifyImage (Bitmap image){

        try {

            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] Confidence = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < Confidence.length; i++) {
                if (Confidence[i] > maxConfidence) {
                    maxConfidence = Confidence[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Dosent exist", "Exists"};
            result.setText(classes[maxPos]);

            String s="";
            for(int i=0;i<classes.length;i++){
                s+=String.format("%s: %.1f%%\n",classes[i],Confidence[i]*100);
            }
            result.setText(s);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }*/

    void getPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},11);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==11){
            if(grantResults.length>0){
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 100)

        {
            imageView.setImageURI(data.getData());

            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);


    }
}




