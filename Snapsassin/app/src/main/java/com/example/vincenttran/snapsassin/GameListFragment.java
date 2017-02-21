package com.example.vincenttran.snapsassin;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincenttran on 2/20/17.
 */

public class GameListFragment extends android.support.v4.app.Fragment {
    private String key;
    private String status;
    private String id;
    private String title;
    private String targetID;
    private int numPlayers;
    private int numReady;
    private int numDead;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storage_root;
    private  DatabaseReference gamesRef;
    static final int REQUEST_IMAGE_CAPTURE = 7331;
    private View rootView;
    private Activity mActivity;

    public GameListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_player_game, container, false);

        storage = FirebaseStorage.getInstance();
        storage_root = storage.getReferenceFromUrl("gs://snap-91990.appspot.com");

        // get arguments
        Bundle bundle = this.getArguments();
        key = bundle.getString("gameKey");
        status = bundle.getString("status");
        numPlayers = bundle.getInt("numPlayers");
        numReady = bundle.getInt("numReady");
        numDead = bundle.getInt("numDead");
        targetID = bundle.getString("targetID");


        database = FirebaseDatabase.getInstance();
        gamesRef = database.getReference("Games");

        SharedPreferences prefs = this.getActivity().getSharedPreferences("SnapsassinPrefs", Context.MODE_PRIVATE);
        id = prefs.getString("id", "No ID Error");

        final RelativeLayout playersReadyView = (RelativeLayout) rootView.findViewById(R.id.playersReadyCount);
        final TextView playersInGameTextView = (TextView) rootView.findViewById(R.id.playersInGameTextView);
        final TextView targetTextView = (TextView) rootView.findViewById(R.id.targetTextView);
        final Context context = getContext();

        gamesRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Find out how many are ready
                String numReady = dataSnapshot.child("numReady").getValue().toString();
                int numPlayers = Integer.parseInt(dataSnapshot.child("numPlayers").getValue().toString());
                TextView playersReadyTextView = (TextView) rootView.findViewById(R.id.playersReadyTextView);
                playersReadyTextView.setText(numReady + " / " + String.valueOf(numPlayers));

                String status = dataSnapshot.child("players/" + id + "/status").getValue().toString();
                String targetName;
                RelativeLayout yourTargetLayout = (RelativeLayout) rootView.findViewById(R.id.yourTargetLayout);
                GameActivityWithTabs gameActivity = (GameActivityWithTabs) mActivity;

                switch (status){
                    case "0":           // Waiting
                        gameActivity.showReadyBar();
                        playersReadyView.setVisibility(View.VISIBLE);
                        gameActivity.disableCamera(); // Disable camera
                        break;
                    case "1":           // Ready
                        playersReadyView.setVisibility(View.VISIBLE);
                        break;
                    case "3":           // Dead
                        yourTargetLayout.setVisibility(View.GONE);
                        gameActivity.disableCamera(); // Disable camera
                        break;
                    case "4":           // Winner
                        targetID = dataSnapshot.child("players/" + id + "/target").getValue().toString();
                        targetName = dataSnapshot.child("players/" + targetID + "/name").getValue().toString();
                        yourTargetLayout.setVisibility(View.VISIBLE);
                        targetTextView.setText(targetName);

                        playersReadyView.setVisibility(View.GONE);
                        gameActivity.disableCamera(); // Disable camera
                        break;
                    default:            // Alive
                        targetID = dataSnapshot.child("players/" + id + "/target").getValue().toString();
                        playersReadyView.setVisibility(View.GONE);
                        gameActivity.enableCamera();  // Enable camera


//                        // If you are your own target, you've won the game.
//                        if (!id.equals(targetID)) {
//                            yourTargetLayout.setVisibility(View.VISIBLE);
//                            targetTextView.setText(targetName);
//                        }


                }
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

                ListView playerListView = (ListView) rootView.findViewById(R.id.list_players);

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

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (Activity) context;
    }
}
