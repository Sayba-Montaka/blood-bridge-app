package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class ResetPass extends AppCompatActivity {
TextInputEditText password,confirm_pass;
Button reset_button;
ProgressBar progressBar;
ImageView backArrow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        reset_button = findViewById(R.id.reset_button);
        backArrow = findViewById(R.id.backArrow);
        password = findViewById(R.id.password);
        confirm_pass = findViewById(R.id.confirm_pass);
        progressBar = findViewById(R.id.progressBar);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResetPass.this,FP_OTP.class));
            }
        });
        String emailFromIntent = getIntent().getStringExtra("email");
        if(emailFromIntent != null && !emailFromIntent.isEmpty()){
            SharedPreferences sharedPreferences =getSharedPreferences("BloodBank", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email",emailFromIntent);
            editor.apply();
        }
        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass1 = password.getText().toString().trim();
                String pass2 = confirm_pass.getText().toString().trim();


                if(pass1.isEmpty() || pass2.isEmpty()){
                    Toast.makeText(ResetPass.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!pass1.equals(pass2)){
                    Toast.makeText(ResetPass.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
                String email = prefs.getString("email", "");

                String url = "https://googix.xyz/blood_bridge/resetpass.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressBar.setVisibility(View.GONE);

                        if(s.trim().contains("PASSWORD_UPDATED")){
                            Toast.makeText(ResetPass.this, "Password Changed Successfully!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ResetPass.this, Login.class); // Send to Login, not MainActivity
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ResetPass.this, "Error: " + s, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ResetPass.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        try {
                            map.put("email", MyMethod.encryptedData(email));
                            map.put("password", MyMethod.encryptedData(pass1)); // Send the validated password
                            map.put("key", MyMethod.encryptedData("sayba1122"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return map;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(ResetPass.this);
                requestQueue.add(stringRequest);
            }
        });
    }
}