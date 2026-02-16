package com.example.bloodbankt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class Become_a_donor extends AppCompatActivity {
MaterialAutoCompleteTextView gender,bloodGroup,district;
Button bad_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_become_adonor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        gender = findViewById(R.id.gender);
        bloodGroup = findViewById(R.id.bloodGroup);
        district = findViewById(R.id.district);
        /* -----------gender----------------------------*/
        String[] options_gender = {"Male", "Female"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_gender);
        gender.setAdapter(adapter);
        /* -----------blood group----------------------------*/
        String[] options_blood = {"A+", "B+","AB+","O+","A-","B-","AB-","O-"};

        ArrayAdapter<String> adapter_blood =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_blood);
        bloodGroup.setAdapter(adapter_blood);
        /* -----------district----------------------------*/
        String[] options_district = {"Dhaka","Chattogram","Khulna","Rajshahi","Barishal","Sylhet","Rangpur","Mymensingh"};

        ArrayAdapter<String> adapter_district =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_district);
        district.setAdapter(adapter_district);
        /* -----------district----------------------------*/
/*=================bad_button==============================*/
        bad_button = findViewById(R.id.bad_button);
        bad_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Become_a_donor.this, B_A_D_List.class));
            }
        });
    }
}