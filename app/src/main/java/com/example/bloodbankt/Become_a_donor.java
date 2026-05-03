package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class Become_a_donor extends AppCompatActivity {
MaterialAutoCompleteTextView gender,bloodGroup,district,division;
TextInputEditText donor_area,donor_number,donor_name;
ProgressBar progressBar;

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
        division = findViewById(R.id.division);
        donor_area = findViewById(R.id.donor_area);
        donor_number = findViewById(R.id.donor_number);
        donor_name = findViewById(R.id.donor_name);
        bad_button = findViewById(R.id.bad_button);
        progressBar = findViewById(R.id.progressBar);

        SharedPreferences sharedPreferences = getSharedPreferences("DONOR_INFORMATION",MODE_PRIVATE);

        /* -----------gender----------------------------*/
        String[] options_gender = {"Male", "Female"};

        ArrayAdapter<String> adapter_gender =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_gender);
        gender.setAdapter(adapter_gender);
        /* -----------blood group----------------------------*/
        String[] options_blood = {"A+", "B+","AB+","O+","A-","B-","AB-","O-"};

        ArrayAdapter<String> adapter_blood =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_blood);
        bloodGroup.setAdapter(adapter_blood);
        /* -----------division----------------------------*/
        String[] options_division = {"Dhaka","Chattogram","Khulna","Rajshahi","Barishal","Sylhet","Rangpur","Mymensingh"};

        ArrayAdapter<String> adapter_division =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        options_division);
        division.setAdapter(adapter_division);
        /* -----------district----------------------------*/
        HashMap<String, String[]> district_Map = new HashMap<>();

        district_Map.put("Dhaka", new String[]{
                "Dhaka", "Gazipur", "Narayanganj", "Narsingdi","Faridpur","Gopalganj", "Kishoreganj",
                "Madaripur", "Manikganj", "Munshiganj","Rajbari","Shariatpur", "Tangail"
        });

        district_Map.put("Chattogram", new String[]{
                "Chattogram", "Cox's Bazar", "Cumilla", "Noakhali","Bandarban", "Brahmanbaria", "Chandpur","Feni", "Khagrachari", "Lakshmipur","Rangamati"
        });

        district_Map.put("Khulna", new String[]{
                "Khulna", "Jessore", "Satkhira","Bagerhat", "Chuadanga", "Jhenaidah","Kushtia", "Magura","Meherpur","Narail"
        });

        district_Map.put("Rajshahi", new String[]{
                "Rajshahi", "Bogura", "Pabna","Chapainawabganj","Joypurhat","Naogaon","Natore","Sirajganj"
        });

        district_Map.put("Barishal", new String[]{
                "Barguna", "Barishal", "Bhola", "Jhalokathi", "Patuakhali", "Pirojpur"
        });

        district_Map.put("Sylhet", new String[]{
                "Sylhet", "Moulvibazar", "Habiganj","Sunamganj"
        });

        district_Map.put("Rangpur", new String[]{
                "Rangpur", "Dinajpur", "Kurigram","Gaibandha","Lalmonirhat","Nilphamari","Panchagarh","Thakurgaon"
        });

        district_Map.put("Mymensingh", new String[]{
                "Mymensingh", "Jamalpur", "Netrokona","Sherpur"
        });
        district.setEnabled(false);
        //============================division selection-----------------------------
        division.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDivison = division.getText().toString();
                if(district_Map.containsKey(selectedDivison)){
                    ArrayAdapter<String> adapter_district =
                            new ArrayAdapter<>(Become_a_donor.this, android.R.layout.simple_list_item_1,district_Map.get(selectedDivison));
                    district.setText("");
                    district.setEnabled(true);
                    district.setAdapter(adapter_district);
                }
            }
        });
/*=================bad_button==============================*/

        bad_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_of_donor = donor_name.getText().toString().trim();
                String area_of_donor = donor_area.getText().toString().trim();
                String phone_of_donor = donor_number.getText().toString().trim();
                String gender_of_donor = gender.getText().toString().trim();
                String district_of_donor = district.getText().toString().trim();
                String division_of_donor = division.getText().toString().trim();
                String blood_group = bloodGroup.getText().toString().trim();

                if (name_of_donor.isEmpty() ||
                        area_of_donor.isEmpty() ||
                        phone_of_donor.isEmpty() ||
                        gender_of_donor.isEmpty() ||
                        district_of_donor.isEmpty() ||
                        division_of_donor.isEmpty() ||
                        blood_group.isEmpty()) {

                    Toast.makeText(Become_a_donor.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phone_of_donor.length() != 11) {
                    Toast.makeText(Become_a_donor.this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                String url = "https://googix.xyz/blood_bridge/becomeADonor.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if(s.contains("Congrats")){
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Become_a_donor.this,s,Toast.LENGTH_LONG).show();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isDonor",true);
                            editor.putString("name",name_of_donor);
                            editor.putString("area",area_of_donor);
                            editor.putString("phone",phone_of_donor);
                            editor.putString("gender",gender_of_donor);
                            editor.putString("district",district_of_donor);
                            editor.putString("division",division_of_donor);
                            editor.putString("blood_group",blood_group);
                            editor.apply();

                            startActivity(new Intent(Become_a_donor.this,B_A_D_List.class));
                            finish();

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        new  AlertDialog.Builder(Become_a_donor.this)
                                .setTitle("Sign In")
                                .setMessage(volleyError.getMessage())
                                .create()
                                .show();
                    }
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map map = new HashMap<String,String>();
                        map.put("name",name_of_donor);
                        map.put("gender",gender_of_donor);
                        map.put("blood_group",blood_group);
                        map.put("district",district_of_donor);
                        map.put("area",area_of_donor);
                        map.put("division",division_of_donor);
                        Log.d("POST_DIVISION", "division: " + division_of_donor);
                        try {
                            map.put("key",MyMethod.encryptedData("sayba1122"));
                            map.put("phone",MyMethod.encryptedData(phone_of_donor));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return map;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(Become_a_donor.this);
                requestQueue.add(stringRequest);
            }

        });
    }
}