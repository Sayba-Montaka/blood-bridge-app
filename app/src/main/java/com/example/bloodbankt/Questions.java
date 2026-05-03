package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;

public class Questions extends AppCompatActivity {
    Button complete;
    MaterialCheckBox checkBox;
    TextView q1,q2,q3,q4,q5;

    MaterialRadioButton yes_1,yes_2,yes_3,yes_4,yes_5,no_1,no_2,no_3,no_4,no_5;
    RadioGroup  rg1,rg2,rg3,rg4,rg5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_questions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Direct the user back to MainActivity instead of exiting
                Intent intent = new Intent(Questions.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        complete = findViewById(R.id.complete);
        rg1 = findViewById(R.id.rg1);
        rg2 = findViewById(R.id.rg2);
        rg3 = findViewById(R.id.rg3);
        rg4 = findViewById(R.id.rg4);
        rg5 = findViewById(R.id.rg5);
        no_1 = findViewById(R.id.yes_1);
        no_2 = findViewById(R.id.yes_2);
        no_3 = findViewById(R.id.yes_3);
        no_4 = findViewById(R.id.yes_4);
        no_5 = findViewById(R.id.yes_5);
        checkBox = findViewById(R.id.checkbox);

        SharedPreferences sharedPreferences = getSharedPreferences("DONOR_INFORMATION",MODE_PRIVATE);

        if(sharedPreferences.getBoolean("isDonor",false)){
            startActivity(new Intent(Questions.this, B_A_D_List.class));
            finish();
        }
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rg1.getCheckedRadioButtonId() == -1 || rg2.getCheckedRadioButtonId() == -1
                || rg3.getCheckedRadioButtonId() == -1
                || rg4.getCheckedRadioButtonId()  == -1
                || rg5.getCheckedRadioButtonId() == -1){
                    Toast.makeText(Questions.this,"Please answer all questions",Toast.LENGTH_LONG).show();
                    return;
                }
                if(!checkBox.isChecked()){
                    Toast.makeText(Questions.this,"Please agree to terms and conditions",Toast.LENGTH_LONG).show();
                    return;
                }
                if(no_1.isChecked() ||
                no_2.isChecked() || no_3.isChecked() || no_4.isChecked() || no_5.isChecked()){

                    Toast.makeText(Questions.this,"You are not eligible to become a donor",Toast.LENGTH_LONG).show();
                    return;
                }
                startActivity(new Intent(Questions.this, Become_a_donor.class));
                finish();

            }
        });
    }
}