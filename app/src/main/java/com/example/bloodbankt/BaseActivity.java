package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;
    DatabaseReference presenceRef;

    protected void setupBottomNavigation(int selectedItemId) {
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.Home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), Profile.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.noti) {
                startActivity(new Intent(getApplicationContext(), Notification.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.messenger) {
                startActivity(new Intent(getApplicationContext(), Message.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create notification channel once
        NotificationHelper.createChannel(this);

        // Start background message listener
        Intent serviceIntent = new Intent(this, MessageListenerService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnlineStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setOnlineStatus(false);
    }

    private void setOnlineStatus(boolean isOnline) {
        SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
        String email = prefs.getString("email", "").trim();
        if (email.isEmpty()) return;

        String safeEmail = email.replace(".", ",");
        presenceRef = FirebaseDatabase.getInstance()
                .getReference("presence").child(safeEmail);

        HashMap<String, Object> map = new HashMap<>();
        map.put("online", isOnline);
        if (!isOnline) {
            map.put("lastSeen", ServerValue.TIMESTAMP);
        }
        presenceRef.updateChildren(map);

        if (isOnline) {
            HashMap<String, Object> offlineMap = new HashMap<>();
            offlineMap.put("online", false);
            offlineMap.put("lastSeen", ServerValue.TIMESTAMP);
            presenceRef.onDisconnect().updateChildren(offlineMap);
        }
    }
}