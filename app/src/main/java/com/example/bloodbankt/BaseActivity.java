package com.example.bloodbankt;

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;

    protected void setupBottomNavigation(int selectedItemId) {

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == selectedItemId) {
                return true; // already on this screen
            } else if (item.getItemId()== R.id.Home) {
                openActivity(MainActivity.class);
            } else if (item.getItemId()==R.id.messenger) {
                openActivity(Message.class);
            }else if (item.getItemId()==R.id.noti) {
                openActivity(Notification.class);
            }else if (item.getItemId()==R.id.profile) {
                openActivity(Profile.class);
            }
            return true;
        });
    }

    private void openActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}

