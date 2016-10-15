package com.example.vincenttran.snapsassin;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.vincenttran.snapsassin.R;
import com.example.vincenttran.snapsassin.SampleFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarBadge;
import com.roughike.bottombar.BottomBarFragment;
import com.roughike.bottombar.OnMenuTabSelectedListener;

public class BottomMenuBarActivity extends AppCompatActivity {
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_menu_bar);

        Toast.makeText(this, "HERE!!!", Toast.LENGTH_SHORT).show();
        bottomBar = BottomBar.attach(this, savedInstanceState);

        bottomBar.setFragmentItems(getSupportFragmentManager(), R.id.fragmentContainer,
                new BottomBarFragment(SampleFragment.newInstance("Game"), R.drawable.icon_dashboard, "Game"),
                new BottomBarFragment(SampleFragment.newInstance("Go to camera"), R.drawable.icon_face, "Snap"),
                new BottomBarFragment(SampleFragment.newInstance("Settings"), R.drawable.icon_menu, "Settings"));

        bottomBar.setItemsFromMenu(R.menu.bottom_bar, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int position) {
                switch (position) {
                    case 0:
                        // Item 1 Selected
                        break;
                    case 1:
                        // Snap
                        break;
                    case 2:
                        // Settings page
                        break;
                }
            }
        });

        // Setting colors for different tabs when there's more than three of them.
        bottomBar.mapColorForTab(0, "#3B494C");
        bottomBar.mapColorForTab(1, "#00796B");
        bottomBar.mapColorForTab(2, "#7B1FA2");

        // Make a Badge for the first tab, with red background color and a value of "4".
        BottomBarBadge unreadMessages = bottomBar.makeBadgeForTabAt(1, "#E91E63", 4);

        // Control the badge's visibility
        unreadMessages.show();
        //unreadMessages.hide();

        // Change the displayed count for this badge.
        //unreadMessages.setCount(4);

        // Change the show / hide animation duration.
        unreadMessages.setAnimationDuration(200);

        // If you want the badge be shown always after unselecting the tab that contains it.
        //unreadMessages.setAutoShowAfterUnSelection(true);
    }
}