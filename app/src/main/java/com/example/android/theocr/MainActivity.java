package com.example.android.theocr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Bitmap mImage;
    final String TAG=MainActivity.class.getName();
    private String LANG="eng";
    final String TESSDATA="eng.traineddata";
    int resource_to_parse=R.drawable.input_image;
    final int REQUEST_WRITE_STORAGE=1;
    final int REQUEST_READ_STORAGE=2;
    final int REQUEST_IMAGE_CAPTURE = 3;
    final int REQUEST_TAKE_PHOTO = 200;
    ProgressBar mProgressbar;
    Button mButtonProcess,mButtonCapture;
    int mOrientation;
    File saving;
    File folder;
    String mCurrentPhotoPath;
    Context mContext;
    File mImageFile;
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
            //parseImage();

        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        }

     }
    public void captureCamera(View view){

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permissionCheck== PackageManager.PERMISSION_GRANTED ){
            dispatchTakePictureIntent();
            Log.v(TAG,"Permission granted");
        }else
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_IMAGE_CAPTURE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)) {

            Context context = getApplicationContext();
            CharSequence text = "Capture Not Complete";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }else{
            mImage = BitmapFactory.decodeFile(mImageFile.toString());
            ImageView iv=(ImageView) findViewById(R.id.image_display);
            iv.setImageBitmap(mImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupOCR();
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // parseImage();
                } else {
                    Toast.makeText(this, "The app was not allowed to read from storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }

            }
            case REQUEST_IMAGE_CAPTURE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "The app was not allowed to capture images. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }

        }
    }
    public void processParsing(View v){
        if (mImage!=null){
            mProgressbar=(ProgressBar)findViewById(R.id.progress_bar);
            mProgressbar.setVisibility(View.VISIBLE);
            mButtonProcess=(Button)findViewById(R.id.button_process);
            mButtonProcess.setEnabled(false);
            mButtonCapture=(Button)findViewById(R.id.button_capture);
            mButtonCapture.setEnabled(false);

        //    mOrientation = getRequestedOrientation();
    //        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            new ParseOcrAsync().execute(this);

        }else{
            Log.v(TAG,"NULL");
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




    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "THE_OCR_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.v(TAG,storageDir.toString());
        mImageFile=image;
        return image;
    }



    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG,"Exception while creating file on storage");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI=Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
