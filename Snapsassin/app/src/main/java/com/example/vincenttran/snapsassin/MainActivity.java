package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1337;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    FirebaseStorage storage;
    StorageReference storage_root;
    private String id;
    static final int REQUEST_IMAGE_CAPTURE = 7331;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // init Facebook sdk
        setContentView(R.layout.activity_main);

        setUpToolbar();
        addDrawerItems();

        storage = FirebaseStorage.getInstance();
        storage_root = storage.getReferenceFromUrl("gs://snap-91990.appspot.com");

        final ListView listView = (ListView) findViewById(R.id.gamesList);

        List<String> gamesList = new ArrayList<>();
        final List<String> keyList   = new ArrayList<>();

        gamesList.add("Polyhack");
        keyList.add("-Kl32asdbfa9hnfa");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                gamesList
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("gameTitle", (String) listView.getItemAtPosition(position));
                intent.putExtra("gameKey", keyList.get(position));
                startActivity(intent);
            }
        });

        SharedPreferences prefs = getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
        id = prefs.getString("id", "No ID Error");
    }

    private void setUpToolbar() {
        // Set up Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("My Games");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // camera button
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.closed
        );
        drawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

    }

    private void addDrawerItems() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.closed) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        String[] drawerItems = {"Join a Game", "Create a Game", "Calibrate Face", "Log Out"};

        ArrayAdapter<String> navDrawerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(navDrawerAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch(position) {
                    case 0:             // Index 0: Join Game
//                        intent = new Intent(MainActivity.this, JoinGameActivity.class);
//                        startActivity(intent);
                        break;

                    case 1:             // Index 1: Create Game
//                        intent = new Intent(MainActivity.this, CreateGameActivity.class);
//                        startActivity(intent);
                        break;

                    case 2:             // Index 2: Calibrate Face
                        // First, check permissions for camera
                        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                                android.Manifest.permission.CAMERA);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        }
                        dispatchTakePictureIntent();
                        break;

                    case 3:             // Index 3: Settings
                        break;

                    case 4:             // Index 4: Log out
                        LoginManager.getInstance().logOut();
                        // clear sharedPref info
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.clear();
                        editor.apply();
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            // convert to bytes, base 64
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 1, bytes);
            byte[] imageBytes = bytes.toByteArray();
//            String imageFile = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            // upload to firebase
            StorageReference img_name = storage_root.child("Users/" + id + "/calibration.jpg");
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
                    Toast.makeText(MainActivity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("LINK", downloadUrl.toString());
                }
            });
        }
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
