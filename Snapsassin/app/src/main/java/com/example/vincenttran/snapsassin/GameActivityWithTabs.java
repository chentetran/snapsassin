package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class GameActivityWithTabs extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 7331;
    private String gameKey;
    private Toolbar toolbar;
    private String targetID;
    private String title;
    private String id;
    private String key;
    private FirebaseDatabase database;
    private DatabaseReference gameRef;
    private DatabaseReference playerReadyRef;
    private LinearLayout readyLayout;
    private DatabaseReference gamesRef;
    private FirebaseStorage storage;
    private StorageReference storage_root;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private TabLayout tabs;
    private CharSequence Titles[]={"Info","Feed"};
    private int Numboftabs = 2;
    private String targetName;
    private String status;
    private boolean cameraDisabled = false;
    private LinearLayout readyBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_with_tabs);

        storage = FirebaseStorage.getInstance();
        storage_root = storage.getReferenceFromUrl("gs://snap-91990.appspot.com");

        Intent intent = getIntent();
        title = intent.getStringExtra("gameTitle");
        key = intent.getStringExtra("gameKey");

        SharedPreferences prefs = getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
        id = prefs.getString("id", "No ID Error");

        database = FirebaseDatabase.getInstance();
        gamesRef = database.getReference("Games");
        gamesRef.child(key + "/players/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("status").getValue().toString().equals("2")) {   // If player is alive
                    targetID = dataSnapshot.child("target").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setUpToolbar();

        // fragments and viewpager setup
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs,title,key);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        tabs = (TabLayout) findViewById(R.id.tabs);

        int color = getResources().getColor(R.color.tabsScrollColor);
        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setTabTextColors(color, Color.WHITE);
        tabs.setSelectedTabIndicatorHeight(10);
        tabs.setSelectedTabIndicatorColor(Color.WHITE);
        tabs.setupWithViewPager(pager);

        readyBar = (LinearLayout) findViewById(R.id.readyBar);


    }

    private void setUpToolbar() {
        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public void dispatchTakePictureIntent(View view) {
        if (!cameraDisabled) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "Sorry, you cannot assassinate now", Toast.LENGTH_SHORT).show();
        }
    }

    public void readyButton(View view) {
        Ion.with(this)
                .load("http://polysnap.herokuapp.com/vote")
                .setBodyParameter("userID", id)
                .setBodyParameter("gameID", key)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            // TODO: something went wrong
                            return;
                        }

                        int status = result.get("status").getAsInt();
                        if (status == 405) {
                            Toast.makeText(GameActivityWithTabs.this, result.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameActivityWithTabs.this, "You're ready!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        readyBar.setVisibility(View.GONE);
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
            if (targetID != null) {
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
                        Toast.makeText(GameActivityWithTabs.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                        // TODO: store in games

                        attemptAssassination(downloadUrl.toString());
                    }
                });
            }
        }
    }

    private void attemptAssassination(String url) {
        Ion.with(this)
                .load("http://polysnap.herokuapp.com/assassinate")
                .setBodyParameter("imgUrl", url)
                .setBodyParameter("userID", id)
                .setBodyParameter("gameKey", key)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        int status = result.get("status").getAsInt();

                        switch (status) {
                            case 202:
                                Toast.makeText(GameActivityWithTabs.this, "You won!", Toast.LENGTH_SHORT).show();
                                break;

                            case 200:
                                Toast.makeText(GameActivityWithTabs.this, "Assassination successful!", Toast.LENGTH_SHORT).show();
                                break;

                            case 201:
                                Toast.makeText(GameActivityWithTabs.this, result.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void disableCamera() {
        cameraDisabled = true;
    }

    public void enableCamera() {
        cameraDisabled = false;
    }

    public void showReadyBar() {
        readyBar.setVisibility(View.VISIBLE);
    }

}
