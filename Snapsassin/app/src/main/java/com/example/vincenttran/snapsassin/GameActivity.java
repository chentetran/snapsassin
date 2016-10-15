package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class GameActivity extends AppCompatActivity {
    FirebaseStorage storage;
    StorageReference storage_root;
    String target;
    String gameKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        String title = intent.getStringExtra("gameTitle");
//        String key = intent.getStringExtra("gameKey");

        setTitle(title);

        // SNAP!
        storage = FirebaseStorage.getInstance();
//        target = intent.getStringExtra("target"); // get target, image under this name
        target = "Vincent";
//        gameKey = intent.getStringExtra("gameKey"); // get gamekey, use for finding game storage
        gameKey = "GAAAMESSS";
        storage_root = storage.getReferenceFromUrl("gs://snap-91990.appspot.com");
        Button snap_button = (Button)findViewById(R.id.snap_button);
        snap_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            // convert to bytes, base 64
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            byte[] imageBytes = bytes.toByteArray();
//            String imageFile = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            // upload to firebase
            StorageReference img_name = storage_root.child("Games/" + gameKey + "/" + target + "-assassinated.jpg");
            UploadTask uploadTask = img_name.putBytes(imageBytes);
            Log.e("Uploaded?", "Continue on");
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("GAME FAIL", e.toString());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    // todo: upload to microsoft face
                    Toast.makeText(GameActivity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}


