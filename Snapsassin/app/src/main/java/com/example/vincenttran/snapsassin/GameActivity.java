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
import java.util.ArrayList;
import java.util.List;

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

                switch (status){
                    case "0":
                        readyBar.setVisibility(View.VISIBLE);
                        playersReadyView.setVisibility(View.VISIBLE);
                        break;
                    case "1":
                        break;
                    default:
                        RelativeLayout yourTargetLayout = (RelativeLayout) findViewById(R.id.yourTargetLayout);
                        yourTargetLayout.setVisibility(View.VISIBLE);
                        targetID = dataSnapshot.child("players/" + id + "/target").getValue().toString();
                        String targetName = dataSnapshot.child("players/" + targetID + "/name").getValue().toString();

                        targetTextView.setText(targetName);

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
                    // todo: upload to microsoft face
//                    Toast.makeText(GameActivity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                    // TODO: store in games

                    assassinationSuccessful();



                }
            });
        }
    }

    public void dispatchTakePictureIntent(View view) {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
        assassinationSuccessful();

    }

    private void assassinationSuccessful() {
        gamesRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Increment numDead
                int numDead = Integer.parseInt(dataSnapshot.child("numDead").getValue().toString());
                numDead++;
                Toast.makeText(GameActivity.this, String.valueOf(numDead), Toast.LENGTH_SHORT).show();
                gamesRef.child(key + "/numDead").setValue(numDead);

                // Change victim's status code
                gamesRef.child(key + "/players/" + targetID + "/status").setValue("3");

                // Assign victim's target to yourself
                String newTargetID = dataSnapshot.child("players/" + targetID + "/target").getValue().toString();
                gamesRef.child(key + "/players/" + id + "/target").setValue(newTargetID);

                // TODO: account for winner
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
