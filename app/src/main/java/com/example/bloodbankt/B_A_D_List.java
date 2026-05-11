package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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

import java.util.HashMap;
import java.util.Map;

public class B_A_D_List extends AppCompatActivity {
ImageView backArrow;
Button editDonor,deleteDonor;
ProgressBar progressBar;
TextView location,gender,phone_number,bloodGroup,donorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ba_dlist);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backArrow = findViewById(R.id.backArrow);
        location = findViewById(R.id.location);
        gender = findViewById(R.id.gender);
        phone_number = findViewById(R.id.phone_number);
        bloodGroup = findViewById(R.id.bloodGroup);
        donorName = findViewById(R.id.donor_name);
        progressBar = findViewById(R.id.progressBar);
        deleteDonor = findViewById(R.id.deleteDonor);
        editDonor = findViewById(R.id.editDonor);
        SharedPreferences sharedPreferences = getSharedPreferences("DONOR_INFORMATION",MODE_PRIVATE);

        String name = sharedPreferences.getString("name","");
        String area = sharedPreferences.getString("area","");
        String phone = sharedPreferences.getString("phone","");
        String Gender = sharedPreferences.getString("gender","");
        String district = sharedPreferences.getString("district","");
        String division = sharedPreferences.getString("division","");
        String blood_group = sharedPreferences.getString("blood_group","");

        String location_final = division+ " , "+district+" , "+ area;
        donorName.setText(name);
        phone_number.setText(phone);
        gender.setText(Gender);
        bloodGroup.setText(blood_group);
        location.setText(location_final);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(B_A_D_List.this,MainActivity.class));
                finish();
            }
        });
        //=-----------------------------------edit donor----------------------------------
        editDonor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(B_A_D_List.this,EditDonor.class);
                intent.putExtra("name",name);
                intent.putExtra("area",area);
                intent.putExtra("phone",phone);
                intent.putExtra("gender",Gender);
                intent.putExtra("district",district);
                intent.putExtra("division",division);
                intent.putExtra("blood_group",blood_group);
                startActivity(intent);
                finish();
            }
        });
        //=-----------------------------------delete donor----------------------------------
        SharedPreferences sharedPreferences1 = getSharedPreferences("BLOOD_REQUEST",MODE_PRIVATE);
        String token = sharedPreferences1.getString("token","");
        deleteDonor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String url = "https://googix.xyz/blood_bridge/deleteDonor.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(B_A_D_List.this,s,Toast.LENGTH_LONG).show();
                        if(s.contains("deleted successfully")){
                            SharedPreferences sp = getSharedPreferences("DONOR_INFORMATION", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.clear();
                            editor.apply();
                            startActivity(new Intent(B_A_D_List.this, Questions.class));
                            finish();
                        }}
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(B_A_D_List.this,volleyError.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map map = new HashMap<String,String>();
                        try {
                            map.put("key",MyMethod.encryptedData("sayba1122"));
                            map.put("token",MyMethod.encryptedData(token));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return  map;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(B_A_D_List.this);
                requestQueue.add(stringRequest);
            }
        });
        getOnBackPressedDispatcher().addCallback(B_A_D_List.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(B_A_D_List.this,MainActivity.class));
            }
        });
    }
}