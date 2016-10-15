package com.example.vincenttran.snapsassin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by ngapham on 10/14/16.
 */

public class PhotoIntentActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 23450;

    public void dispatchPhotoIntent() {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
//        Log.v("Here", "Cameraaaaaaaaaaaaaaa");
        startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE);
    }

  
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);

            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }
    }


}
