package com.example.vincenttran.snapsassin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Gameplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);
    }

    public void gameStart(View view) {
        // TODO: sends state of ready to server
    }
}
