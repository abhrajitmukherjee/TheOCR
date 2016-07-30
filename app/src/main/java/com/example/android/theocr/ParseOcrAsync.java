package com.example.android.theocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

/**
 * Created by abhrajit on 7/29/16.
 */
public class ParseOcrAsync extends AsyncTask<MainActivity,Void,String> {
    MainActivity mActivity;
    File mImageFile;
    private String LANG="eng";
    @Override
    protected String doInBackground(MainActivity... activity) {
        mActivity=activity[0];
        mImageFile=mActivity.mImageFile;
        return parseImage();
    }

    private String parseImage(){

        String baseFolder= Environment.getExternalStorageDirectory()+ "/classlinkp";
        BitmapFactory.Options options = new BitmapFactory.Options();
   //     options.inSampleSize = 2;
        Bitmap myImage = BitmapFactory.decodeFile(mImageFile.toString(),options);
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.init(baseFolder, LANG); // myDir + "/tessdata/eng.traineddata" must be present
        baseApi.setImage(myImage);

        String recognizedText = baseApi.getUTF8Text(); // Log or otherwise display this string...
        baseApi.end();
        return recognizedText;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        TextView tv=(TextView)mActivity.findViewById(R.id.text_display);
        tv.setText(result);
     //   mActivity.setRequestedOrientation(mActivity.mOrientation);
        mActivity.mProgressbar.setVisibility(View.GONE);
        mActivity.mButtonProcess.setEnabled(true);
        mActivity.mButtonCapture.setEnabled(true);

    }
}
