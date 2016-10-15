package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.facebook.login.LoginManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1337;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();
        addDrawerItems();

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
//                        break;

                    case 1:             // Index 1: Create Game
//                        intent = new Intent(MainActivity.this, CreateGameActivity.class);
//                        startActivity(intent);
//                        break;

                    case 2:             // Index 2: Calibrate Face
                        // First, check permissions for camera
                        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                                android.Manifest.permission.CAMERA);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        }
//                        openCameraForCalibration();
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
}
