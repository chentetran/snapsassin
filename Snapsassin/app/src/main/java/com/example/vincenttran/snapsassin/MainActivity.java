package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
