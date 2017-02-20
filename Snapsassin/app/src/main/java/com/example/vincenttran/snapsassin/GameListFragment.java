package com.example.vincenttran.snapsassin;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincenttran on 2/20/17.
 */

public class GameListFragment extends android.support.v4.app.Fragment {
    private String gameKey;
    private TextView playersReadyRatio;
    private TextView targetName;
    private String playersInGame;
    private String playersReady;
    View rootView;

    public GameListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_player_game, container, false);
//
//        // get arguments
//        Bundle bundle = this.getArguments();
//        gameKey = bundle.getString("gameKey");
//
//        // load info about game
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference gameRef = database.getReference("games").child(gameKey);
//
//        // Get player's id
//        SharedPreferences prefs = this.getActivity().getSharedPreferences("SnapsassinPrefs", Context.MODE_PRIVATE);
//        final String id = prefs.getString("id", "No ID Error");
//        targetName = (TextView) rootView.findViewById(R.id.targetName);
//
//        database.getReference("gamesVote").child(gameKey).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                playersReady = dataSnapshot.getValue().toString();
//
//                // Load list of players and their statuses
//                gameRef.child("players").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Load target name is available
//                        if (dataSnapshot.child(id).child("target").exists()) {
//                            targetName.setText(dataSnapshot.child(id).child("target/name").getValue().toString());
//                        }
//
//                        playersInGame = String.valueOf(dataSnapshot.getChildrenCount());
//
//                        // Populate players in gameActivity list
//                        final List<String> gamesList = new ArrayList<>();
//                        final List<Integer> playerStatusArray = new ArrayList<>();
//                        for (DataSnapshot child : dataSnapshot.getChildren()) {
//                            Log.d("this", dataSnapshot.toString());
//                            Log.d("that", String.valueOf(child.child("name").getValue()));
//                            gamesList.add(String.valueOf(child.child("name").getValue()));
//                            playerStatusArray.add(Integer.parseInt(child.child("status").getValue().toString()));
//                        }
//
//                        final ListView list = (ListView) rootView.findViewById(R.id.list_players);
//
//                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                                getContext(),
//                                R.layout.list_item_players,
//                                android.R.id.text1,
//                                gamesList
//                        ) {
//                            @Override
//                            public View getView(int position, View convertView, ViewGroup parent) {
//                                View view = super.getView(position, convertView, parent);
//
//                                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
//                                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
//
//                                text1.setText(gamesList.get(position));
//                                switch (playerStatusArray.get(position)) { // set status text and color
//                                    case 0: // waiting
//                                        text2.setText("waiting");
//                                        break;
//                                    case 1: // ready
//                                        text2.setText("ready");
//                                        text2.setTextColor(Color.BLUE);
//                                        break;
//                                    case 2: // alive
//                                        text2.setText("alive");
//                                        text2.setTextColor(Color.GREEN);
//                                        break;
//                                    case 3: // dead
//                                        text2.setText("dead");
//                                        text2.setTextColor(Color.RED);
//                                        break;
//                                    case 4: // winner
//                                        text2.setText("winner");
//                                        text2.setTextColor(Color.GREEN);
//                                        break;
//                                }
//                                return view;
//                            }
//                        };
//
//                        rootView.findViewById(R.id.loadingPanel).setVisibility(View.GONE); // set loading panel to invis
//
//                        list.setAdapter(adapter);
//
//                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                // ListView Clicked item index
//                                int itemPosition     = position;
//
//                                // ListView Clicked item value
//                                String  itemValue    = (String) list.getItemAtPosition(position);
//
//                                // Show Alert
//                                Toast.makeText(getContext(),
//                                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
//                                        .show();
//                            }
//                        });
//
//                        // load ready ratio
//                        playersReadyRatio = (TextView) rootView.findViewById(R.id.playersReadyRatio);
//                        String toDisplay = playersReady + " / " + playersInGame;
//                        playersReadyRatio.setText(toDisplay); // TODO: THIS SHOULD BE DYNAMIC BUT ITS NOT. MAYBE MOVE OUTSIDE OF THIS CALLBACK
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//        });
//

        return rootView;
    }
}
