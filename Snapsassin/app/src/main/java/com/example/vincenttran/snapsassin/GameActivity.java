package com.example.vincenttran.snapsassin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private String key;
    private String id;
    private String title;
    private String targetID;
    FirebaseStorage storage;
    StorageReference storage_root;
    static final int REQUEST_IMAGE_CAPTURE = 7331;

    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Info","Feed"};
    int Numboftabs =2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        title = intent.getStringExtra("gameTitle");
        key = intent.getStringExtra("gameKey");

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs,key);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        storage = FirebaseStorage.getInstance();
        storage_root = storage.getReferenceFromUrl("gs://snap-91990.appspot.com");





        SharedPreferences prefs = getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
        id = prefs.getString("id", "No ID Error");

        setUpToolbar();


    }

    private void setUpToolbar() {
        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void readyButton(View view) {
        Ion.with(this)
                .load("http://snap2016.herokuapp.com/vote")
                .setBodyParameter("userID", id)
                .setBodyParameter("gameID", key)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            // TODO: something went wrong
                            Log.d("ERROR", e.toString());
                            return;
                        }
                        Toast.makeText(GameActivity.this, "You're ready!", Toast.LENGTH_SHORT).show();
                    }
                });

//        LinearLayout readyBar = (LinearLayout) findViewById(R.id.readyBar);
//        readyBar.setVisibility(View.GONE);
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
            StorageReference img_name = storage_root.child("Games/" + key + "/" + targetID + "-assassinated.jpg");
            UploadTask uploadTask = img_name.putBytes(imageBytes);
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
//                    Toast.makeText(GameActivity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();

                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference ref = db.getReference("/Users/" + id + "/photoUrl");
                    ref.setValue(downloadUrl.toString());
                }
            });
        }
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
