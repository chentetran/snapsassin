package com.example.vincenttran.snapsassin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private String key;
    private String id;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        title = intent.getStringExtra("gameTitle");
        key = intent.getStringExtra("gameKey");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference gamesRef = database.getReference("Games");

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
                if (dataSnapshot.child("players/" + id + "/status").getValue().toString().equals("0")) {
                    readyBar.setVisibility(View.VISIBLE);
                    playersReadyView.setVisibility(View.VISIBLE);
                }
                else {
                    String targetID = dataSnapshot.child("players/" + id + "/target").getValue().toString();
                    String targetName = dataSnapshot.child("players/" + targetID + "/name").getValue().toString();

                    targetTextView.setText(targetName);

                    int numPlayers = Integer.parseInt(dataSnapshot.child("numPlayers").getValue().toString());
                    int numDead = Integer.parseInt(dataSnapshot.child("numDead").getValue().toString());
                    String numAlive = String.valueOf(numPlayers - numDead);

                    playersInGameTextView.setText(numAlive + " / " + String.valueOf(numPlayers));

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
                            return;
                        }
                        Toast.makeText(GameActivity.this, "You're ready!", Toast.LENGTH_SHORT).show();
                    }
                });

        // TODO: set status as ready on firebase
    }
}
