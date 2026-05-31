package com.example.bloodbankt;

import android.content.Intent;
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

public class ForgotPassEmail extends AppCompatActivity {
Button otpSendBtn;
TextInputEditText EmailEdit;
ImageView backArrow;
ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_pass_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backArrow = findViewById(R.id.backArrow);
        EmailEdit = findViewById(R.id.EmailEdit);
        otpSendBtn = findViewById(R.id.otpSendBtn);
        progressBar = findViewById(R.id.progressBar);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPassEmail.this, Login.class));
            }
        });
           otpSendBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   String email = EmailEdit.getText().toString();
                   if (email.isEmpty()) {
                       Toast.makeText(ForgotPassEmail.this, "Enter your email first", Toast.LENGTH_LONG).show();
                   } else {
                       progressBar.setVisibility(View.VISIBLE);
                       String url = "https://blood-bridge.org/blood_bridge/sendOtp.php";

                       StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                           @Override
                           public void onResponse(String s) {
                               progressBar.setVisibility(View.GONE);
                               if (s.contains("Otp send")) {
                                   Toast.makeText(ForgotPassEmail.this, s, Toast.LENGTH_LONG).show();
                                   Intent intent = new Intent(ForgotPassEmail.this, FP_OTP.class);
                                   intent.putExtra("email", email);
                                   startActivity(intent);
                               }
                           }
                       }, new Response.ErrorListener() {
                           @Override
                           public void onErrorResponse(VolleyError volleyError) {
                               Toast.makeText(ForgotPassEmail.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                           }
                       }) {
                           @Nullable
                           @Override
                           protected Map<String, String> getParams() throws AuthFailureError {
                               Map map = new HashMap<String, String>();
                               try {
                                   map.put("email", MyMethod.encryptedData(EmailEdit.getText().toString()));
                                   map.put("key", MyMethod.encryptedData("sayba1122"));
                               } catch (Exception e) {
                                   throw new RuntimeException(e);
                               }

                               return map;
                           }
                       };
                       RequestQueue requestQueue = Volley.newRequestQueue(ForgotPassEmail.this);
                       requestQueue.add(stringRequest);
                   }
               }
           });
    }
}