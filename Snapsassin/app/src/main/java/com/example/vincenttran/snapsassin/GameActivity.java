package com.example.vincenttran.snapsassin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.games.Game;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {
    private String key;
    private String id;
    private String title;
    private String targetID;
    private FirebaseDatabase database;
    FirebaseStorage storage;
    StorageReference storage_root;
    DatabaseReference gamesRef;
    static final int REQUEST_IMAGE_CAPTURE = 7331;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        storage = FirebaseStorage.getInstance();
        storage_root = storage.getReferenceFromUrl("gs://snap-91990.appspot.com");

        Intent intent = getIntent();
        title = intent.getStringExtra("gameTitle");
        key = intent.getStringExtra("gameKey");

        database = FirebaseDatabase.getInstance();
        gamesRef = database.getReference("Games");

        SharedPreferences prefs = getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
        id = prefs.getString("id", "No ID Error");

        setUpToolbar();

        final LinearLayout readyBar = (LinearLayout) findViewById(R.id.readyBar);
        final RelativeLayout playersReadyView = (RelativeLayout) findViewById(R.id.playersReadyCount);
        final TextView playersInGameTextView = (TextView) findViewById(R.id.playersInGameTextView);
        final TextView targetTextView = (TextView) findViewById(R.id.targetTextView);
        final Context context = this;

        gamesRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Find out how many are ready
                String numReady = dataSnapshot.child("numReady").getValue().toString();
                int numPlayers = Integer.parseInt(dataSnapshot.child("numPlayers").getValue().toString());
                TextView playersReadyTextView = (TextView) findViewById(R.id.playersReadyTextView);
                playersReadyTextView.setText(numReady + " / " + String.valueOf(numPlayers));



                String status = dataSnapshot.child("players/" + id + "/status").getValue().toString();
                RelativeLayout yourTargetLayout = (RelativeLayout) findViewById(R.id.yourTargetLayout);

                switch (status){
                    case "0":           // Waiting
                        readyBar.setVisibility(View.VISIBLE);
                        playersReadyView.setVisibility(View.VISIBLE);
                        break;
                    case "1":           // Ready
                        break;
                    case "3":           // Dead
                        yourTargetLayout.setVisibility(View.GONE);
                        break;
                    default:            // Alive
                        targetID = dataSnapshot.child("players/" + id + "/target").getValue().toString();
                        String targetName = dataSnapshot.child("players/" + targetID + "/name").getValue().toString();

                        // If you are your own target, you've won the game.
                        if (!id.equals(targetID)) {
                            yourTargetLayout.setVisibility(View.VISIBLE);
                            targetTextView.setText(targetName);
                        }


                        int numDead = Integer.parseInt(dataSnapshot.child("numDead").getValue().toString());
                        String numAlive = String.valueOf(numPlayers - numDead);

                        playersInGameTextView.setText(numAlive + " / " + String.valueOf(numPlayers));
                }

                // Get every player in game
                final List<String> playerList = new ArrayList<String>();
                final List<Integer> playerStatusList = new ArrayList<Integer>();

                for (DataSnapshot child : dataSnapshot.child("players").getChildren()) {
                    playerList.add(String.valueOf(child.child("name").getValue()));
                    playerStatusList.add(Integer.parseInt(child.child("status").getValue().toString()));
                }

                ListView playerListView = (ListView) findViewById(R.id.list_players);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        context,
                        R.layout.list_item_players,
                        android.R.id.text1,
                        playerList
                ) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        text1.setText(playerList.get(position));
                        switch (playerStatusList.get(position)) { // set status text and color
                            case 0: // waiting
                                text2.setText("waiting");
                                break;
                            case 1: // ready
                                text2.setText("ready");
                                text2.setTextColor(Color.BLUE);
                                break;
                            case 2: // alive
                                text2.setText("alive");
                                text2.setTextColor(Color.GREEN);
                                break;
                            case 3: // dead
                                text2.setText("dead");
                                text2.setTextColor(Color.RED);
                                break;
                            case 4: // winner
                                text2.setText("winner");
                                text2.setTextColor(Color.GREEN);
                                break;
                        }
                        return view;
                    }
                };

                playerListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpToolbar() {
        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
//        toolbar.setTitle(title);

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
                        Toast.makeText(GameActivity.this, "You're ready!", Toast.LENGTH_SHORT).show();
                    }
                });

        // TODO: set status as ready on firebase
        LinearLayout readyBar = (LinearLayout) findViewById(R.id.readyBar);
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
                    Toast.makeText(GameActivity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                    // TODO: store in games

                    attemptAssassination(downloadUrl.toString());
                }
            });
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
                                Toast.makeText(GameActivity.this, "You won!", Toast.LENGTH_SHORT).show();
                                break;

                            case 200:
                                Toast.makeText(GameActivity.this, "Assassination successful!", Toast.LENGTH_SHORT).show();
                                break;

                            case 201:
                                Toast.makeText(GameActivity.this, result.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
