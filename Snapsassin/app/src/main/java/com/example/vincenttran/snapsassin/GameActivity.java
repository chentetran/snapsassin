package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class GameActivity extends AppCompatActivity {
    private String key;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        String title = intent.getStringExtra("gameTitle");
        key = intent.getStringExtra("gameKey");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference gamesRef = database.getReference("Games");

        SharedPreferences prefs = getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
        id = prefs.getString("id", "No ID Error");

        setTitle(title);

        gamesRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("players/" + id + "/status").getValue().toString().equals("0")) {
                    LinearLayout readyBar = (LinearLayout) findViewById(R.id.readyBar);
                    readyBar.setVisibility(View.VISIBLE);
                    RelativeLayout playersReadyView = (RelativeLayout) findViewById(R.id.playersReadyCount);
                    playersReadyView.setVisibility(View.VISIBLE);
                }
                else {
                    String targetID = dataSnapshot.child("players/" + id + "/target").getValue().toString();
                    String targetName = dataSnapshot.child("players/" + targetID + "/name").getValue().toString();

                    TextView targetTextView = (TextView) findViewById(R.id.targetTextView);
                    targetTextView.setText(targetName);

                    TextView playersInGameTextView = (TextView) findViewById(R.id.playersInGameTextView);
                    int numPlayers = Integer.parseInt(dataSnapshot.child("numPlayers").getValue().toString());
                    int numDead = Integer.parseInt(dataSnapshot.child("numDead").getValue().toString());
                    String numAlive = String.valueOf(numPlayers - numDead);

                    playersInGameTextView.setText(numAlive + " / " + String.valueOf(numPlayers));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        // If player is ready, remove ready bar
//        final DatabaseReference playerRef = gamesRef.child(key + "/players/" + id);
//        playerRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue().equals("0")) {      // Player not ready
//                    LinearLayout readyBar = (LinearLayout) findViewById(R.id.readyBar);
//                    readyBar.setVisibility(View.VISIBLE);
//                    LinearLayout playersReadyView = (LinearLayout) findViewById(R.id.playersReadyCount);
//                    playersReadyView.setVisibility(View.VISIBLE);
//
//                } else {
//                    playerRef.child("target").addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            String targetID = dataSnapshot.getValue().toString();
//
//                            gamesRef.child(key + "/players/" + targetID).addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    TextView targetTextView = (TextView) findViewById(R.id.targetTextView);
//                                    targetTextView.setText(dataSnapshot.child("name").getValue().toString());
//
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
//
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
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
                            return;
                        }
                        Toast.makeText(GameActivity.this, "You're ready!", Toast.LENGTH_SHORT).show();
                    }
                });

        // TODO: set status as ready on firebase
    }
}
