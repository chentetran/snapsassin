package com.example.vincenttran.snapsassin;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.key;


public class GameFragment extends Fragment {
    String key;
    View rootView;
    String targetID;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game, container, false);

        // get arguments
        Bundle bundle = this.getArguments();
        key = bundle.getString("key");

        SharedPreferences prefs = this.getActivity().getSharedPreferences("SnapsassinPrefs", Context.MODE_PRIVATE);
        final String id = prefs.getString("id", "No ID Error");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference gamesRef = database.getReference("Games");

        final LinearLayout readyBar = (LinearLayout) rootView.findViewById(R.id.readyBar);
        final RelativeLayout playersReadyView = (RelativeLayout) rootView.findViewById(R.id.playersReadyCount);
        final TextView playersInGameTextView = (TextView) rootView.findViewById(R.id.playersInGameTextView);
        final TextView targetTextView = (TextView) rootView.findViewById(R.id.targetTextView);
        final LinearLayout infoLayout = (LinearLayout) rootView.findViewById(R.id.infoLayout);
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

                switch (status){
                    case "0":
                        readyBar.setVisibility(View.VISIBLE);
                        playersReadyView.setVisibility(View.VISIBLE);
                        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        p.addRule(RelativeLayout.BELOW, R.id.readyBar);
                        infoLayout.setLayoutParams(p);
                        break;
                    case "1":
                        break;
                    default:
                        RelativeLayout yourTargetLayout = (RelativeLayout) rootView.findViewById(R.id.yourTargetLayout);
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
}
