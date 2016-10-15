package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        String title = intent.getStringExtra("gameTitle");
        String key = intent.getStringExtra("gameKey");

        setTitle(title);
    }
}
