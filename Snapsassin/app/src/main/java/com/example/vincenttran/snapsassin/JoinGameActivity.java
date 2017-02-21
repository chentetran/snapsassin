package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class JoinGameActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String id;
    private String name;
    private EditText gameName;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        setUpToolbar();

        final Button submitButton = (Button) findViewById(R.id.submitButton);
        gameName = (EditText) findViewById(R.id.gameName);

        // Get user ID and info from SharedPrefs
        SharedPreferences prefs = getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
        id = prefs.getString("id", "No ID error");
        name = prefs.getString("name", "No name error");

        database = FirebaseDatabase.getInstance();

        openKeyboard();     // Immediately open keyboard

        // GO action from the keyboard is equivalent to pressing the OK button
        gameName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    submitButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    private void openKeyboard() {
        gameName.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setUpToolbar() {
        // Set up Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Join a Game");
        toolbar.setTitleTextColor(Color.WHITE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void submitButtonClick(View view) {
        // Get gameName from EditText
        final String gameNameString = gameName.getText().toString();


        Ion.with(this)
                .load("http://polysnap.herokuapp.com/joinGame")
                .setBodyParameter("userID", id)
                .setBodyParameter("gameName", gameNameString)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            // TODO: something went wrong
                            return;
                        }
                        int status = result.get("status").getAsInt();

                        switch (status) {
                            case 401:           // No game with that name found
                                Toast.makeText(JoinGameActivity.this, "No game called " + gameNameString + " was found", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                            case 200:
                                String key = result.get("gameKey").getAsString();

                                // Go to this new game's GameActivity
                                Intent intent = new Intent(JoinGameActivity.this, GameActivityWithTabs.class);
                                intent.putExtra("gameTitle", gameNameString);
                                intent.putExtra("gameKey", key);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                Toast.makeText(JoinGameActivity.this, "There's been an error", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
