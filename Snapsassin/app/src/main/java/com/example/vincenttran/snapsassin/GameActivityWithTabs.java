package com.example.vincenttran.snapsassin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;

public class GameActivityWithTabs extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1337;
    private String gameKey;
    private Toolbar toolbar;
    private String title;
    private String id;
    private FirebaseDatabase database;
    private DatabaseReference gameRef;
    private DatabaseReference playerReadyRef;
    private LinearLayout readyLayout;
    private File image;
    private String imgPath;
    private String imageFileName;
    private String api_key = "94268d2c6049471283eb781d34391c16";
    private String api_secret = "2cf82e0f29c44dd0b4649a9d8f4469f6";
    ViewPager pager;
    ViewPagerAdapter adapter;
    TabLayout tabs;
    CharSequence Titles[]={"Info","Feed"};
    int Numboftabs =2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_with_tabs);

        Intent intent = getIntent();
        title = intent.getStringExtra("gameTitle");
        gameKey = intent.getStringExtra("gameKey");

        setUpToolbar();

        // fragments and viewpager setup
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs,title,gameKey);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        tabs = (TabLayout) findViewById(R.id.tabs);

        int color = getResources().getColor(R.color.tabsScrollColor);
        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setTabTextColors(color, Color.WHITE);
        tabs.setSelectedTabIndicatorHeight(10);
        tabs.setSelectedTabIndicatorColor(Color.WHITE);
        tabs.setupWithViewPager(pager);




    }

    private void setUpToolbar() {
        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
