package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

public class See_details extends AppCompatActivity {
    TextView name_of_request,email_of_request,phone_of_request,group_of_request,gender_of_request,disease_of_request,
            deadline_of_request,unit_of_blood,location_of_request,hoapital_of_request;
    ImageView backArrow,select_person;
    Button call;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_see_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backArrow = findViewById(R.id.backArrow);
        location_of_request = findViewById(R.id.location_of_request);
        gender_of_request = findViewById(R.id.gender_of_request);
        select_person = findViewById(R.id.select_person);
        phone_of_request = findViewById(R.id.phone_of_request);
        email_of_request = findViewById(R.id.email_of_request);
        group_of_request = findViewById(R.id.group_of_request);
        name_of_request = findViewById(R.id.name_of_request);
        disease_of_request = findViewById(R.id.disease_of_request);
        deadline_of_request = findViewById(R.id.deadline_of_request);
        unit_of_blood = findViewById(R.id.unit_of_blood);
        hoapital_of_request = findViewById(R.id.hoapital_of_request);
        call = findViewById(R.id.call);

        SharedPreferences sharedPreferences = getSharedPreferences("BLOOD_REQUEST",MODE_PRIVATE);

        String name = sharedPreferences.getString("request_name","");
        String phone = sharedPreferences.getString("number","");
        String email = sharedPreferences.getString("email","");
        String Gender = sharedPreferences.getString("gender","");
        String blood_group = sharedPreferences.getString("bloodGroup","");
        String hospital = sharedPreferences.getString("hospital","");
        String disease = sharedPreferences.getString("disease_type","");
        String unit = sharedPreferences.getString("unit","");
        String deadline = sharedPreferences.getString("deadline","");
        String location = sharedPreferences.getString("location","");

        name_of_request.setText(name);
        phone_of_request.setText(phone);
        email_of_request.setText(email);
        gender_of_request.setText(Gender);
        group_of_request.setText(blood_group);
        location_of_request.setText(location);
        deadline_of_request.setText(deadline);
        hoapital_of_request.setText(hospital);
        disease_of_request.setText(disease);
        unit_of_blood.setText(unit);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(See_details.this,Blood_Requests.class));
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = phone;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel: "+number));
                startActivity(intent);
            }
        });
      SharedPreferences preferences = getSharedPreferences("BloodBank",MODE_PRIVATE);
      String image = preferences.getString("image","");
        select_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(See_details.this, Chat.class);
                intent.putExtra("email",email);
                intent.putExtra("name",name);
                intent.putExtra("image",image);
                startActivity(intent);
            }
        });
    }
}