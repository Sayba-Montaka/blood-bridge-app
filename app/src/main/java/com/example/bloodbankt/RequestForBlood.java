package com.example.bloodbankt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class RequestForBlood extends AppCompatActivity {
MaterialAutoCompleteTextView dateInput,timeInput;
TextInputLayout dd_date,dd_time;
Button rfb_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_for_blood);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        dd_date =findViewById(R.id.dd_date);
        dd_time= findViewById(R.id.dd_time);
        rfb_button = findViewById(R.id.rfb_button);

        dd_date.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialDatePicker<Long> datePicker =
                        MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Select date")
                                .build();

                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

                datePicker.addOnPositiveButtonClickListener(selection -> {
                    String date = datePicker.getHeaderText(); // formatted date
                    dateInput.setText(date);
                });
            }
        });
        dd_time.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker timePicker =
                        new MaterialTimePicker.Builder()
                                .setTitleText("Select time")
                                .setTimeFormat(TimeFormat.CLOCK_12H) // or CLOCK_24H
                                .setHour(10)
                                .setMinute(30)
                                .build();

                timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String time = timePicker.getHour() + ":" + timePicker.getMinute();
                        timeInput.setText(time);
                    }
                });
            }
        });
        rfb_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RequestForBlood.this, R_B_List.class));
            }
        });


    }
}