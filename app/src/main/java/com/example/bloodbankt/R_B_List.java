package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class R_B_List extends AppCompatActivity {
    TextView email_of_request,name_of_request,phone_of_request,group_of_request,gender_of_request,disease_of_request,
            deadline_of_request,unit_of_blood,location_of_request,hoapital_of_request;
    ImageView backArrow;
    Button edit_request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rb_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backArrow = findViewById(R.id.backArrow);
        location_of_request = findViewById(R.id.location_of_request);
        gender_of_request = findViewById(R.id.gender_of_request);
        phone_of_request = findViewById(R.id.phone_of_request);
        group_of_request = findViewById(R.id.group_of_request);
        name_of_request = findViewById(R.id.name_of_request);
        email_of_request = findViewById(R.id.email_of_request);
        disease_of_request = findViewById(R.id.disease_of_request);
        deadline_of_request = findViewById(R.id.deadline_of_request);
        unit_of_blood = findViewById(R.id.unit_of_blood);
        edit_request = findViewById(R.id.edit_request);
        hoapital_of_request = findViewById(R.id.hoapital_of_request);

        SharedPreferences sharedPreferences = getSharedPreferences("REQUEST_BECOME",MODE_PRIVATE);

        String name = sharedPreferences.getString("name","");
        String email = sharedPreferences.getString("email","");
        String area = sharedPreferences.getString("area","");
        String phone = sharedPreferences.getString("phone","");
        String Gender = sharedPreferences.getString("gender","");
        String district = sharedPreferences.getString("district","");
        String division = sharedPreferences.getString("division","");
        String blood_group = sharedPreferences.getString("blood_group","");
        String hospital = sharedPreferences.getString("hospital","");
        String disease = sharedPreferences.getString("disease","");
        String unit = sharedPreferences.getString("unit","");
        String time = sharedPreferences.getString("time","");
        String date = sharedPreferences.getString("date","");

        String location_final = division+ " , "+district+" , "+ area;
        String deadline = date+ "  |  "+ time;
        name_of_request.setText(name);
        email_of_request.setText(email);
        phone_of_request.setText(phone);
        gender_of_request.setText(Gender);
        group_of_request.setText(blood_group);
        location_of_request.setText(location_final);
        deadline_of_request.setText(deadline);
        hoapital_of_request.setText(hospital);
        disease_of_request.setText(disease);
        unit_of_blood.setText(unit);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(R_B_List.this,MainActivity.class));
            }
        });
        edit_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(R_B_List.this,EditRequests.class);
                intent.putExtra("name",name);
                intent.putExtra("blood_group",blood_group);
                intent.putExtra("phone", phone);
                intent.putExtra("gender",Gender);
                intent.putExtra("email", email);
                intent.putExtra("area", area);
                intent.putExtra("division", division);
                intent.putExtra("district", district);
                intent.putExtra("hospital",hospital);
                intent.putExtra("disease", disease);
                intent.putExtra("unit", unit);
                intent.putExtra("time", time);
                intent.putExtra("date",date);
                startActivity(intent);
                finish();
            }
        });
    }
}