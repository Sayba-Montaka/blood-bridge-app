package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

public class Profile extends BaseActivity {
LinearLayout editProfile,donation_history,request_history;
Button P_A_logout;
TextView P_A_password,P_A_email,P_A_name;
ShapeableImageView P_A_image;
ImageView backArrow;
SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editProfile=findViewById(R.id.editProfile);
        P_A_password=findViewById(R.id.P_A_password);
        P_A_email=findViewById(R.id.P_A_email);
        P_A_name=findViewById(R.id.P_A_name);
        P_A_logout=findViewById(R.id.P_A_logout);
        P_A_image=findViewById(R.id.P_A_image);
        backArrow=findViewById(R.id.backArrow);
        donation_history=findViewById(R.id.donation_history);
        request_history=findViewById(R.id.request_history);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this,MainActivity.class));
            }
        });
        donation_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, Blood_Donation_history.class));
                finish();
            }
        });
        request_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this,Request_history.class));
            }
        });
        P_A_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", "");
                editor.apply();
                editor.clear();
                startActivity(new Intent(Profile.this, FirstPage.class));
                finish();
            }
        });
        sharedPreferences=
                getSharedPreferences("BloodBank", MODE_PRIVATE);

        String name  = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String image = sharedPreferences.getString("image", "");
        String password = sharedPreferences.getString("pass", "");

        P_A_name.setText(name);
        P_A_email.setText(email);
       P_A_password.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               P_A_password.setText(password);
           }
       });

        if (!image.isEmpty()) {
            Picasso.get().load(image).into(P_A_image);
        }

        /* --------BaseActvity on on bottom navigation--------*/
        setupBottomNavigation(R.id.profile);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, EditProfile.class));
            }
        });
    }
}