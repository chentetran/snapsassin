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

//    Just testing. This doesn't have to be here
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
                        Toast.makeText(LoginActivity.this, "Created", Toast.LENGTH_SHORT).show();
                    }
                },
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
}
