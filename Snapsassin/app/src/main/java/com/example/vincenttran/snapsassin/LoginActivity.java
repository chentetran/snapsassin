package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference dbref;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ProfileTracker profileTracker;
    private AccessTokenTracker tracker;
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // init facebook sdk
        setContentView(R.layout.activity_login);


        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile");

        // trackers
        tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                updateWithToken(currentAccessToken);
            }
        };
        tracker.startTracking();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

            }
        };
        profileTracker.startTracking();

        accessToken = AccessToken.getCurrentAccessToken();
        updateWithToken(accessToken);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();

        tracker.stopTracking();
        profileTracker.stopTracking();
    }

    private void updateWithToken(AccessToken currentAccessToken) {
        if (currentAccessToken != null) { // not logged in
            Profile profile = Profile.getCurrentProfile();
            startMainActivity(profile);

        }
        else  { // already logged in
            callbackManager = CallbackManager.Factory.create();

            // Facebook button
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    accessToken = loginResult.getAccessToken();
                    Profile profile = Profile.getCurrentProfile();
                    startMainActivity(profile);



                }

                @Override
                public void onCancel() {
                    // TODO: Nothing?
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startMainActivity(Profile profile) {
        if (profile != null) {
            String id = profile.getId();
            String name = profile.getName();


            // store user data in sharedpref
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("SnapsassinPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("id", id);
            editor.putString("name", name);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

            Toast.makeText(LoginActivity.this, "Welcome to Snapsassin", Toast.LENGTH_SHORT).show();
        }
    }
}
