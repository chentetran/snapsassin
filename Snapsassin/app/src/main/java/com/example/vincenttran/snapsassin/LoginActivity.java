package com.example.vincenttran.snapsassin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

//    Just testing. This stuff doesn't have to be here

    private FirebaseDatabase store = FirebaseDatabase.getInstance();
    private DatabaseReference root = store.getReference("Users/vincenttran23");

    public void create_person(View v) {
        RequestQueue RQ = Volley.newRequestQueue(this);
        JSONObject request_body = new JSONObject();
        try {
            // TODO - Get name from somewhere
            request_body = new JSONObject("{\"name\": \"Leonardo DiCaprio\"}");
        } catch (JSONException e) {
            Toast.makeText(LoginActivity.this, "Failed to create person: Invalid name",
                    Toast.LENGTH_SHORT);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                getResources().getString(R.string.create_person_url),
                request_body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String name = "";
                        try {
                            name = response.getString("personId");
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(LoginActivity.this, "Player " + name + " created", Toast.LENGTH_SHORT).show();

                        // This needs to be Users/<UID>/personId, but we just don't have that yet
                        DatabaseReference playerid = root.child("/personId");
                        playerid.setValue(name);
                    }
                },
                // Store playerId into Firebase
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        NetworkResponse res = error.networkResponse;

                        Log.d("Request error",
                                "Error (" + statusCode + "): " + new String(res.data));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(getResources().getString(R.string.sub_id_key),
                        getResources().getString(R.string.sub_id));

                return params;
            }
        };
        RQ.add(request);
    }

    // Retrieve personId and photo URL from Firebase
    //      Construct a request to detect a face (?)
    //      Construct a request to add the face to the person
    //          Does this trigger a group retraining?
    String UIDPath = "Users/{+ UID}";

    public void attachphoto(View v) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference foo = database.getReference("Users/vincenttran23");

        foo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String url = dataSnapshot.child("photoUrl").getValue().toString();
                String personId = dataSnapshot.child("personId").getValue().toString();

                Toast.makeText(LoginActivity.this, url + "\n" + personId, Toast.LENGTH_SHORT).show();

                RequestQueue RQ = Volley.newRequestQueue(LoginActivity.this);
                JSONObject obj = new JSONObject();
                try {
                    obj = new JSONObject("{\"url\":" + url.replace("{personId}", personId) + "}");
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, "Your JSON is bad and you should feel bad",
                            Toast.LENGTH_SHORT).show();
                }

                JsonObjectRequest rq = new JsonObjectRequest(Request.Method.POST,
                        getResources().getString(R.string.add_face_url),
                        obj,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Nothing to do. We don't use the persistedFaceId now
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError e) {
                                Toast.makeText(LoginActivity.this, "Failed to add your photo",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put(getResources().getString(R.string.sub_id_key),
                                getResources().getString(R.string.sub_id));
                        return headers;
                    }
                };
                        RQ.add(rq);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        "Unfortunately something has broken. You weren't in the database!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
