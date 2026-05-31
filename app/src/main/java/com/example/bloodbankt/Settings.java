package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Settings extends AppCompatActivity {
    Switch darkSwitch, notification_switch;
    SharedPreferences sharedPreferences;
    Button delete_account;
    ImageView backArrow;
    HashMap<String, String> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        darkSwitch = findViewById(R.id.dark_switch);
        delete_account = findViewById(R.id.delete_account);
        backArrow = findViewById(R.id.backArrow);
        notification_switch = findViewById(R.id.notification_switch);
        sharedPreferences = getSharedPreferences("BloodBank", MODE_PRIVATE);

        boolean isDark = sharedPreferences.getBoolean("dark_mode", false);
        darkSwitch.setChecked(isDark);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

// toggle
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this,MainActivity.class));
                finish();
            }
        });
        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ?
                            AppCompatDelegate.MODE_NIGHT_YES :
                            AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        boolean notification_on = sharedPreferences.getBoolean("notification", true);
        notification_switch.setChecked(notification_on);

        notification_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notification", isChecked).apply();

            if (isChecked) {
                sharedPreferences.edit().putBoolean("notification", true).apply();
            } else {
                sharedPreferences.edit().putBoolean("notification", false).apply();

            }
        });
        delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this)
                        .setTitle("Delete Account")
                        .setMessage("This action is permanent.Do you sure want to delete account?")
                        .setPositiveButton("Delete", ((dialog, which) -> {
                            deleteAccountFromServer();
                        }))
                        .setNegativeButton("Cencle", null)
                        .show();
            }
        });
    }

    private void deleteAccountFromServer() {
        String url = "https://blood-bridge.org/blood_bridge/delete_account.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.contains("successfully deleted")) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        new AlertDialog.Builder(Settings.this)
                                .setTitle("Server Response")
                                .setMessage(response)
                                .show();

                        startActivity(new Intent(this, FirstPage.class));
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                new AlertDialog.Builder(Settings.this)
                        .setTitle("Server Response")
                        .setMessage(volleyError.getMessage())
                        .show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", sharedPreferences.getString("email", ""));
                try {
                    map.put("key", MyMethod.encryptedData("sayba1122"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}