package com.example.android.theocr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    Bitmap myImage;
    private String LANG="eng";
    final String TESSDATA="eng.traineddata";
    int resource_to_parse=R.drawable.image_full;
    final int REQUEST_WRITE_STORAGE=1;
    final int REQUEST_READ_STORAGE=2;
    File saving;
    File folder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead=ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck== PackageManager.PERMISSION_GRANTED ){
            setupOCR();
        }else
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
        if(permissionCheckRead==PackageManager.PERMISSION_GRANTED){
            parseImage();

        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        }



     }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    setupOCR();
                } else
                {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    parseImage();
                } else
                {
                    Toast.makeText(this, "The app was not allowed to read from storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void setupOCR(){

        folder = new File(Environment.getExternalStorageDirectory(), "classlinkp/tessdata");
        Log.v("Folder",folder.toString());
        if (!folder.exists()) {
            boolean result=folder.mkdirs();
            Log.v("Folder","created"+result);
        }else
        {
            Log.v("Folder","exists");
        }

        saving = new File(folder, TESSDATA);
        if (saving.exists()){

            Log.v("File","exists");

        }else{

            try {
                saving.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream stream = null;
            try {
                stream = getAssets().open(TESSDATA, AssetManager.ACCESS_STREAMING);
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (stream != null){
                copyInputStreamToFile(stream, saving);
            }

        }

    }

    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void parseImage(){

        String baseFolder=Environment.getExternalStorageDirectory()+ "/classlinkp";
        BitmapFactory.Options options = new BitmapFactory.Options();
        ImageView iv=(ImageView) findViewById(R.id.image_display);
        Picasso.with(this).load(resource_to_parse).resize(640,480)
                .into(iv);
       options.inSampleSize = 2;
        Bitmap myImage = BitmapFactory.decodeResource(getResources(),resource_to_parse,options);
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.init(baseFolder, LANG); // myDir + "/tessdata/eng.traineddata" must be present
        baseApi.setImage(myImage);

        String recognizedText = baseApi.getUTF8Text(); // Log or otherwise display this string...
        TextView tv=(TextView) findViewById(R.id.text_display);
        tv.setText(recognizedText);



        baseApi.end();
    }
}
