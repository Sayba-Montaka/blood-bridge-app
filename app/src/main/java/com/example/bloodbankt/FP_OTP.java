package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class FP_OTP extends AppCompatActivity {
    Button otpSendBtn;
    TextInputEditText OtpEdit;
    ImageView backArrow;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fp_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backArrow = findViewById(R.id.backArrow);
        OtpEdit = findViewById(R.id.OtpEdit);
        otpSendBtn = findViewById(R.id.otpSendBtn);
        progressBar = findViewById(R.id.progressBar);


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FP_OTP.this, Login.class));
            }});
        String emailFromIntent = getIntent().getStringExtra("email");
        if(emailFromIntent != null && !emailFromIntent.isEmpty()){
            SharedPreferences sharedPreferences =getSharedPreferences("BloodBank", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email",emailFromIntent);
            editor.apply();
        }
        otpSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String otp = OtpEdit.getText().toString();
                Log.d("OTP_Tapti","otp from emailedit "+otp);
                SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
                String email = prefs.getString("email", "");


                String url = "https://blood-bridge.org/blood_bridge/verifyOtp.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("OTP_DEBUG_RESPONSE", "Server raw response: '" + s + "'");
                        if(s.equals("OTP_VERIFIED")){
                            Toast.makeText(FP_OTP.this,s,Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(FP_OTP.this, ResetPass.class);
                            intent.putExtra("email",email);
                            startActivity(intent);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(FP_OTP.this,volleyError.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map map = new HashMap<String,String>();
                        try {
                            String encrypt_otp =MyMethod.encryptedData(OtpEdit.getText().toString());
                            map.put("otp",encrypt_otp);
                            map.put("key",MyMethod.encryptedData("sayba1122"));
                           map.put("email",MyMethod.encryptedData(email));
                           Log.d("OTP_EMAIL","check otp and msg: "+ email + "email"+ OtpEdit.getText().toString());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        return map;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(FP_OTP.this);
                requestQueue.add(stringRequest);
            }
        });
    }
}