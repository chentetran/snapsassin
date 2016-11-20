package com.example.vincenttran.snapsassin;

import android.*;
import android.Manifest;
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


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1337;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private FirebaseDatabase database;
    FirebaseStorage storage;
    StorageReference storage_root;
    private String id;
    static final int REQUEST_IMAGE_CAPTURE = 7331;

    private Toolbar toolbar;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // init Facebook sdk
        setContentView(R.layout.activity_main);

        setUpToolbar();
        addDrawerItems();

        storage = FirebaseStorage.getInstance();
        storage_root = storage.getReferenceFromUrl("gs://snap-91990.appspot.com");

        database = FirebaseDatabase.getInstance();

        SharedPreferences prefs = getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
        id = prefs.getString("id", "No ID Error");
        final String name = prefs.getString("name", "No name error");

        final DatabaseReference gameRef = database.getReference("Games/-Kl32asdbfa9hnfa/");
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("players/" + id).exists()) { // User doesn't exist (in the game)
                    // hardcoded. fix later
                    DatabaseReference userRef = database.getReference("Users/" + id);

                    userRef.child("games").child("-Kl32asdbfa9hnfa").setValue("Polyhack");
                    userRef.child("name").setValue(name);


                    gameRef.child("players/" + id + "/status").setValue("0");
                    gameRef.child("players/" + id + "/name").setValue(name);
                    int numPlayers = Integer.parseInt(dataSnapshot.child("numPlayers").getValue().toString());
                    numPlayers++;
                    gameRef.child("numPlayers").setValue(numPlayers);


                    // Create a person on Microsoft Face
                    createPerson(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /******************/

        final ListView listView = (ListView) findViewById(R.id.gamesList);

        List<String> gamesList = new ArrayList<>();
        final List<String> keyList = new ArrayList<>();

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


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
                switch (position) {
                    case 0:             // Index 0: Join Game
//                        intent = new Intent(MainActivity.this, JoinGameActivity.class);
//                        startActivity(intent);
                        break;

                    case 1:             // Index 1: Create Game
                        intent = new Intent(MainActivity.this, CreateGameActivity.class);
                        startActivity(intent);
                        break;

                    case 2:             // Index 2: Calibrate Face
                        // First, check permissions for camera
                        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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
            Bitmap imageBitmap = (Bitmap) extras.get("data");
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
//                    Toast.makeText(MainActivity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                    String url = downloadUrl.toString();

                    Log.d("LINK", url);

                    database.getReference("Users/" + id + "/photoUrl").setValue(url);

                    attachPhoto(url);


                }
            });
        }
    }

    private void attachPhoto(String url) {
        DatabaseReference userRef = database.getReference("Users/" + id);

        final String mUrl = url;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String personId = dataSnapshot.child("personId").getValue().toString();

                Toast.makeText(MainActivity.this, mUrl + "\n" + personId, Toast.LENGTH_SHORT).show();

                RequestQueue RQ = Volley.newRequestQueue(MainActivity.this);
                JSONObject obj = new JSONObject();
                try {
                    obj = new JSONObject("{\"url\":" + "\"" + mUrl.replace("{personId}", personId) + "\"}");
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(MainActivity.this, getResources().getString(R.string.add_face_url).replace("{personId}", personId), Toast.LENGTH_LONG).show();
                JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST,
                        getResources().getString(R.string.add_face_url).replace("{personId}", personId),
                        obj,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Nothing to do. We don't use the persistedFaceId now
                                Toast.makeText(MainActivity.this, "Photo uploaded", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError e) {
                                int status = e.networkResponse.statusCode;
                                NetworkResponse res = e.networkResponse;

                                Toast.makeText(MainActivity.this, status + "\n" + new String(res.data),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put(getResources().getString(R.string.sub_id_key),
                                getResources().getString(R.string.sub_id));
                        return headers;
                    }
                };
                RQ.add(rq);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,
                        "Unfortunately something has broken. You weren't in the database!",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void createPerson(String name) {
        RequestQueue RQ = Volley.newRequestQueue(this);
        JSONObject request_body = new JSONObject();
        try {
            // TODO - Get name from somewhere
            request_body = new JSONObject("{\"name\": \"" + name + "\"}");
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this, "Failed to create person: Invalid name",
                    Toast.LENGTH_SHORT);
        }

        final String mName = name;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.create_person_url),
                request_body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String microsoft_name = "";
                        try {
                            microsoft_name = response.getString("personId");
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(MainActivity.this, "Player " + microsoft_name + " created", Toast.LENGTH_SHORT).show();

                        // This needs to be Users/<UID>/personId, but we just don't have that yet
                        DatabaseReference playerid = database.getReference("Users/" + id + "/personId");
                        playerid.setValue(microsoft_name);
                    }
                },
                // Store playerId into Firebase
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        NetworkResponse res = error.networkResponse;

                        Log.d("Request error",
                                "Error (" + statusCode + "): " + new String(res.data));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(getResources().getString(R.string.sub_id_key),
                        getResources().getString(R.string.sub_id));

                return params;
            }
        };
        RQ.add(request);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
