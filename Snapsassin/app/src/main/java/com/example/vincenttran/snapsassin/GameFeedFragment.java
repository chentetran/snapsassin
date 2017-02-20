package com.example.vincenttran.snapsassin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincenttran on 2/20/17.
 */

public class GameFeedFragment extends Fragment {
    private String gameKey;
    View rootView;

    // TODO: redesign this so the whole feed isnt being reloaded on every open
    public GameFeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_game_feed, container, false);

//        // get arguments
//        Bundle bundle = this.getArguments();
//
//        gameKey = bundle.getString("gameKey");
//
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference feedRef = database.getReference("games").child(gameKey).child("feed");
//
//        feedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                final List<String> feedArrayList = new ArrayList<String>();
////                String thing = dataSnapshot.child("poop").getValue().toString();
////                Toast.makeText(getContext(), thing, Toast.LENGTH_SHORT).show();
////                Toast.makeText(getContext(), dataSnapshot.toString(), Toast.LENGTH_SHORT).show();
//
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    feedArrayList.add(child.getValue().toString());
//                }
//
//                final ListView feedList = (ListView) rootView.findViewById(R.id.list_feed);
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                        getContext(),
//                        android.R.layout.simple_list_item_1,
//                        android.R.id.text1,
//                        feedArrayList
//                );
//
//                feedList.setAdapter(adapter);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        return rootView;
    }
}
