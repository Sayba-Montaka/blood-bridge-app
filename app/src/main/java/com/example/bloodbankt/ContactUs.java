package com.example.bloodbankt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContactUs extends AppCompatActivity {
    ImageView backArrow,email_to,call;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_us);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backArrow = findViewById(R.id.backArrow);
        email_to = findViewById(R.id.email_to);
        call = findViewById(R.id.call);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactUs.this,MainActivity.class));
                finish();
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = "01967139986";
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel: "+number));
                startActivity(intent);
            }
        });
        email_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriText = "mailto:bloodBank@googix.xyz" +
                        "?subject=" + Uri.encode("Blood Bank Support") +
                        "&body=" + Uri.encode("Hello Authority, \n");

                Uri uri = Uri.parse(uriText);
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(uri);

                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ContactUs.this, "No email app installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}