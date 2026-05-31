package com.example.bloodbankt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchDonor extends AppCompatActivity {
ListView search_list;
Button    search_donor;
MaterialAutoCompleteTextView division,district;
TextInputEditText area;
String selectedBloodGroup = "";
CardView selectedCard = null;
ProgressBar progressBar;
SearchAdapter searchAdapter = new SearchAdapter();
HashMap<String,String> hashMap;
ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_donor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        search_list=findViewById(R.id.search_list);
        search_donor=findViewById(R.id.search_donor);
        division=findViewById(R.id.division);
        district=findViewById(R.id.district);
        area=findViewById(R.id.area);
        progressBar=findViewById(R.id.progressBar);

        objectRequest();
        search_list.setAdapter(searchAdapter);

        LinearLayout bloodGroupLayout = findViewById(R.id.blood_group_container);
        setupBloodGroupSelection(bloodGroupLayout);

        search_donor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                objectRequest1();
                search_list.setAdapter(searchAdapter);
            }
        });

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
                            new ArrayAdapter<>(SearchDonor.this, android.R.layout.simple_list_item_1,district_Map.get(selectedDivison));
                    district.setText("");
                    district.setEnabled(true);
                    district.setAdapter(adapter_district);
                }
            }
        });
    }
    //----------------------------------------------------------------------------------------------------------------
    public class  SearchAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View search_view = inflater.inflate(R.layout.search_donor_items,parent,false);
            TextView bloodGroup,name,phone_number,gender,location;

            bloodGroup = search_view.findViewById(R.id.bloodGroup);
            phone_number = search_view.findViewById(R.id.phone_number);
            gender = search_view.findViewById(R.id.gender);
            name = search_view.findViewById(R.id.name);
            location = search_view.findViewById(R.id.location);

            hashMap = arrayList.get(position);
            String donorName = hashMap.get("donor_name");
            String donorPhone = hashMap.get("number");
            String donorGender = hashMap.get("donor_gender");
            String donorBlood = hashMap.get("bloodGroup");
            String donorLocation = hashMap.get("location");

            name.setText(donorName);
            phone_number.setText(donorPhone);
            gender.setText(donorGender);
            bloodGroup.setText(donorBlood);
            location.setText(donorLocation);


            return search_view;
        }
    }
    private void objectRequest() {

        String url = "https://blood-bridge.org/blood_bridge/search_donor.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, null,
                response -> {

                    arrayList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<>();
                            map.put("donor_name", obj.getString("donor_name"));
                            map.put("number", obj.getString("number"));
                            map.put("donor_gender", obj.getString("donor_gender"));
                            map.put("bloodGroup", obj.getString("bloodGroup"));
                            map.put("location", obj.getString("location"));

                            arrayList.add(map);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    searchAdapter.notifyDataSetChanged();
                },
                error -> Log.e("VOLLEY_ERROR", error.toString())
        ) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("key", MyMethod.encryptedData("sayba1122"));
                    return json.toString().getBytes();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
    public void objectRequest1(){

        String url = "https://blood-bridge.org/blood_bridge/searchDonorByFiltering.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, null,
                response -> {
            progressBar.setVisibility(View.GONE);

                    arrayList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<>();
                            map.put("donor_name", obj.getString("donor_name"));
                            map.put("number", obj.getString("number"));
                            map.put("donor_gender", obj.getString("donor_gender"));
                            map.put("bloodGroup", obj.getString("bloodGroup"));
                            map.put("location", obj.getString("location"));

                            arrayList.add(map);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    searchAdapter.notifyDataSetChanged();
                },
                error -> Log.e("VOLLEY_ERROR", error.toString())
        ) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("district",district.getText().toString());
                    json.put("area",area.getText().toString());
                    json.put("division",division.getText().toString());
                    json.put("blood_group", selectedBloodGroup);
                    json.put("key", MyMethod.encryptedData("sayba1122"));
                    return json.toString().getBytes();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
    private void setupBloodGroupSelection(LinearLayout container) {

        for (int i = 0; i < container.getChildCount(); i++) {

            View view = container.getChildAt(i);

            if (view instanceof CardView) {

                CardView card = (CardView) view;

                card.setOnClickListener(v -> {
                    if(selectedCard == card){
                        card.setCardBackgroundColor(ContextCompat.getColor(SearchDonor.this,R.color.blood_unselected));

                        selectedCard = null;
                        selectedBloodGroup= "";

                        return;
                    }

                    if (selectedCard != null) {
                        selectedCard.setCardBackgroundColor(
                                ContextCompat.getColor(this, R.color.blood_unselected)
                        );
                    }
                    selectedCard = card;
                    selectedBloodGroup = card.getTag().toString();

                    card.setCardBackgroundColor(
                            ContextCompat.getColor(this, R.color.blood_selected)
                    );

                    Log.d("BLOOD", "Selected = " + selectedBloodGroup);
                });
            }
        }
    }
}
