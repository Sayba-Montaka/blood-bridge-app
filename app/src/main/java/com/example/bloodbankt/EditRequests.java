package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.bloodbankt.databinding.ActivityEditRequestsBinding;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.HashMap;
import java.util.Map;

public class EditRequests extends AppCompatActivity {
    MaterialAutoCompleteTextView dateInput, timeInput, district, division, bloodGroup, gender;
    TextInputEditText patient_email,hospital_name, area, unit_of_blood, type_of_diseases, patient_phone, patient_name;
    TextInputLayout dd_date, dd_time;
    ProgressBar progressBar;
    Button rfb_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_for_blood);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dateInput = findViewById(R.id.dateInput);
        patient_email = findViewById(R.id.patient_email);
        timeInput = findViewById(R.id.timeInput);
        dd_date = findViewById(R.id.dd_date);
        dd_time = findViewById(R.id.dd_time);
        rfb_button = findViewById(R.id.rfb_button);
        hospital_name = findViewById(R.id.hospital_name);
        area = findViewById(R.id.area);
        district = findViewById(R.id.district);
        division = findViewById(R.id.division);
        unit_of_blood = findViewById(R.id.unit_of_blood);
        bloodGroup = findViewById(R.id.bloodGroup);
        type_of_diseases = findViewById(R.id.type_of_diseases);
        gender = findViewById(R.id.gender);
        patient_phone = findViewById(R.id.patient_phone);
        patient_name = findViewById(R.id.patient_name);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String blood = intent.getStringExtra("blood_group");
        String phone = intent.getStringExtra("phone");
        String gender_1 = intent.getStringExtra("gender");
        String email = intent.getStringExtra("email");
        String area_1 = intent.getStringExtra("area");
        String division_1= intent.getStringExtra("division");
        String district_1 = intent.getStringExtra("district");
        String hospital = intent.getStringExtra("hospital");
        String disease = intent.getStringExtra("disease");
        String unit = intent.getStringExtra("unit");
        String time = intent.getStringExtra("time");
        String date = intent.getStringExtra("date");

        patient_email.setText(email);
        dateInput.setText(date);
        timeInput.setText(time);
        hospital_name.setText(hospital);
        area.setText(area_1);
        district.setText(district_1);
        division.setText(division_1);
        unit_of_blood.setText(unit);
        bloodGroup.setText(blood);
        type_of_diseases.setText(disease);
        gender.setText(gender_1);
        patient_phone.setText(phone);
        patient_name.setText(name);

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
        /* -----------gender----------------------------*/
        String[] options_gender = {"Male", "Female"};

        ArrayAdapter<String> adapter_gender =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_gender);
        gender.setAdapter(adapter_gender);
        /* -----------blood group----------------------------*/
        String[] options_blood = {"A+", "B+", "AB+", "O+", "A-", "B-", "AB-", "O-"};

        ArrayAdapter<String> adapter_blood =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_blood);
        bloodGroup.setAdapter(adapter_blood);
        /* -----------division----------------------------*/
        String[] options_division = {"Dhaka", "Chattogram", "Khulna", "Rajshahi", "Barishal", "Sylhet", "Rangpur", "Mymensingh"};

        ArrayAdapter<String> adapter_division =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_division);
        division.setAdapter(adapter_division);
        /* -----------district----------------------------*/
        HashMap<String, String[]> district_Map = new HashMap<>();

        district_Map.put("Dhaka", new String[]{
                "Dhaka", "Gazipur", "Narayanganj", "Narsingdi", "Faridpur", "Gopalganj", "Kishoreganj",
                "Madaripur", "Manikganj", "Munshiganj", "Rajbari", "Shariatpur", "Tangail"
        });

        district_Map.put("Chattogram", new String[]{
                "Chattogram", "Cox's Bazar", "Cumilla", "Noakhali", "Bandarban", "Brahmanbaria", "Chandpur", "Feni", "Khagrachari", "Lakshmipur", "Rangamati"
        });

        district_Map.put("Khulna", new String[]{
                "Khulna", "Jessore", "Satkhira", "Bagerhat", "Chuadanga", "Jhenaidah", "Kushtia", "Magura", "Meherpur", "Narail"
        });

        district_Map.put("Rajshahi", new String[]{
                "Rajshahi", "Bogura", "Pabna", "Chapainawabganj", "Joypurhat", "Naogaon", "Natore", "Sirajganj"
        });

        district_Map.put("Barishal", new String[]{
                "Barguna", "Barishal", "Bhola", "Jhalokathi", "Patuakhali", "Pirojpur"
        });

        district_Map.put("Sylhet", new String[]{
                "Sylhet", "Moulvibazar", "Habiganj", "Sunamganj"
        });

        district_Map.put("Rangpur", new String[]{
                "Rangpur", "Dinajpur", "Kurigram", "Gaibandha", "Lalmonirhat", "Nilphamari", "Panchagarh", "Thakurgaon"
        });

        district_Map.put("Mymensingh", new String[]{
                "Mymensingh", "Jamalpur", "Netrokona", "Sherpur"
        });
        district.setEnabled(false);
        //============================division selection-----------------------------
        division.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDivison = division.getText().toString();
                if (district_Map.containsKey(selectedDivison)) {
                    ArrayAdapter<String> adapter_district =
                            new ArrayAdapter<>(EditRequests.this, android.R.layout.simple_list_item_1, district_Map.get(selectedDivison));
                    district.setText("");
                    district.setEnabled(true);
                    district.setAdapter(adapter_district);
                }
            }
        });
        rfb_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_of_patient = patient_name.getText().toString().trim();
                String email_of_patient = patient_email.getText().toString().trim();
                String area_of_patient = area.getText().toString().trim();
                String phone_of_patient = patient_phone.getText().toString().trim();
                String gender_of_patient = gender.getText().toString().trim();
                String district_of_patient = district.getText().toString().trim();
                String division_of_patient = division.getText().toString().trim();
                String blood_group = bloodGroup.getText().toString().trim();
                String hospital = hospital_name.getText().toString().trim();
                String disease_type = type_of_diseases.getText().toString().trim();
                String unit = unit_of_blood.getText().toString().trim();
                String time = timeInput.getText().toString().trim();
                String date = dateInput.getText().toString().trim();

                SharedPreferences sharedPreferences = getSharedPreferences("REQUEST_BECOME", MODE_PRIVATE);

                if (name_of_patient.isEmpty() ||
                        phone_of_patient.isEmpty() ||
                        email_of_patient.isEmpty() ||
                        area_of_patient.isEmpty() ||
                        gender_of_patient.isEmpty() ||
                        district_of_patient.isEmpty() ||
                        division_of_patient.isEmpty() ||
                        hospital.isEmpty() ||
                        disease_type.isEmpty() ||
                        unit.isEmpty() ||
                        date.isEmpty() ||
                        time.isEmpty() ||
                        blood_group.isEmpty()) {
                    Toast.makeText(EditRequests.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone_of_patient.length() != 11) {
                    Toast.makeText(EditRequests.this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                String url = "https://blood-bridge.org/blood_bridge/edit_request.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditRequests.this, s, Toast.LENGTH_LONG).show();
                        if (s.contains("Updated successfully")) {
                            Toast.makeText(EditRequests.this, s, Toast.LENGTH_LONG).show();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isDonor", true);
                            editor.putString("name", name_of_patient);
                            editor.putString("email", email_of_patient);
                            editor.putString("area", area_of_patient);
                            editor.putString("phone", phone_of_patient);
                            editor.putString("gender", gender_of_patient);
                            editor.putString("district", district_of_patient);
                            editor.putString("division", division_of_patient);
                            editor.putString("hospital", hospital);
                            editor.putString("disease", disease_type);
                            editor.putString("unit", unit);
                            editor.putString("time", time);
                            editor.putString("date", date);
                            editor.putString("blood_group", blood_group);
                            editor.apply();
                            startActivity(new Intent(EditRequests.this, R_B_List.class));
                            finish();
                        }}}, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        new AlertDialog.Builder(EditRequests.this)
                                .setTitle("ERROR")
                                .setMessage(volleyError.getMessage())
                                .create()
                                .show();}}) {
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map map = new HashMap<String, String>();
                        map.put("name", name_of_patient);
                        map.put("email", email_of_patient);
                        map.put("gender", gender_of_patient);
                        map.put("blood_group", blood_group);
                        map.put("district", district_of_patient);
                        map.put("area", area_of_patient);
                        map.put("division", division_of_patient);
                        map.put("hospital", hospital);
                        map.put("disease_type", disease_type);
                        map.put("unit", unit);
                        map.put("time", time);
                        map.put("date", date);
                        Log.d("EMAILOFPATIENT",email_of_patient);

                        try {
                            map.put("key", MyMethod.encryptedData("sayba1122"));
                            map.put("phone", MyMethod.encryptedData(phone_of_patient));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return map;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(EditRequests.this);
                requestQueue.add(stringRequest);
            }

        });
    }
}