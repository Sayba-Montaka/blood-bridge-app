package com.example.bloodbankt;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Blood_Requests extends AppCompatActivity {
    ListView search_list;
    LinearLayout blood_group_container;
    Button search;
    MaterialAutoCompleteTextView division,district,area;
    ProgressBar progressBar;
    SearchAdapter searchAdapter = new SearchAdapter();
    String selectedBloodGroup = null;
    CardView selectedCard = null;
    HashMap<String,String> hashMap;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        search_list=findViewById(R.id.search_list);
        division=findViewById(R.id.division);
        district=findViewById(R.id.district);
        area=findViewById(R.id.area);
        blood_group_container=findViewById(R.id.blood_group_container);
        search=findViewById(R.id.search);
        progressBar=findViewById(R.id.progressBar);

        objectRequest();

        search_list.setAdapter(searchAdapter);

        setupBloodGroupSelection(blood_group_container);

        search.setOnClickListener(new View.OnClickListener() {
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
                            new ArrayAdapter<>(Blood_Requests.this, android.R.layout.simple_list_item_1,district_Map.get(selectedDivison));
                    district.setText("");
                    district.setEnabled(true);
                    district.setAdapter(adapter_district);
                }
            }
        });

    //----------------------------------------------------------------------------------------------------------------
    }
    public class  SearchAdapter extends BaseAdapter {

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
            View search_view = inflater.inflate(R.layout.b_r_list, parent, false);

            TextView bloodGroup,deadline,location,hospital_name;
            ImageView pin;

            bloodGroup = search_view.findViewById(R.id.bloodGroup);
            hospital_name = search_view.findViewById(R.id.hospital_name);
            deadline = search_view.findViewById(R.id.deadline);
            pin = search_view.findViewById(R.id.pin);
            location = search_view.findViewById(R.id.location);

            hashMap = arrayList.get(position);
            String id = hashMap.get("id") != null ? hashMap.get("id") : "";
            String blood_group = hashMap.get("bloodGroup");
            String hospitalName = hashMap.get("hospital");
            String name = hashMap.get("request_name");
            String email = hashMap.get("email");
            String phone = hashMap.get("number");
            String gender = hashMap.get("gender");
            String location_final = hashMap.get("location");
            String deadline_final = hashMap.get("deadline");
            String disease_type = hashMap.get("disease_type");
            String unit = hashMap.get("unit");

            bloodGroup.setText(blood_group);
            hospital_name.setText(hospitalName);
            deadline.setText(deadline_final);
            location.setText(location_final);

            Button seeDetails_Btn = search_view.findViewById(R.id.seeDetails_Btn);

            seeDetails_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = getSharedPreferences("BLOOD_REQUEST",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("request_name",name);
                    editor.putString("email",email);
                    editor.putString("bloodGroup",blood_group);
                    editor.putString("number",phone);
                    editor.putString("gender",gender);
                    editor.putString("hospital",hospitalName);
                    editor.putString("location",location_final);
                    editor.putString("deadline",deadline_final);
                    editor.putString("disease_type",disease_type);
                    editor.putString("unit",unit);

                    editor.apply();
                    startActivity(new Intent(Blood_Requests.this, See_details.class));
                }});
            pin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    pin.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            pin.animate().scaleX(1f).scaleY(1f).setDuration(150);
                        }
                    });
                    SharedPreferences sharedPreferences = getSharedPreferences("PINNED_REQUEST", MODE_PRIVATE);

                    String oldData = sharedPreferences.getString("requests", "[]");

                    try {

                        JSONArray jsonArray = new JSONArray(oldData);
                        boolean alreadyPinned = false;
                        int removeIndex = -1;

                       for(int i =0 ;i < jsonArray.length(); i++){
                           JSONObject jsonObject = jsonArray.getJSONObject(i) ;
                           if(jsonObject.optString("id").equals(id)){
                               alreadyPinned = true;
                               removeIndex = i;
                               break;
                           }
                       }
                       if(alreadyPinned){

                           jsonArray.remove(removeIndex);
                           pin.setImageResource(R.drawable.pin);

                       }else{
                           JSONObject jsonObject = new JSONObject();
                           jsonObject.put("id",id);
                           jsonObject.put("bloodGroup", blood_group);
                           jsonObject.put("hospital", hospitalName);
                           jsonObject.put("location", location_final);
                           jsonObject.put("deadline", deadline_final);
                           jsonArray.put(jsonObject);

                           pin.setImageResource(R.drawable.unpin);
                       }
                       SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("requests", jsonArray.toString());
                        editor.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            return search_view;
        }
    }
    public void objectRequest(){
        String url = "https://googix.xyz/blood_bridge/search_request.php";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                arrayList.clear();
                progressBar.setVisibility(View.GONE);
                Log.d("REQ_RES", "onResponse: "+ jsonArray);

                for(int i = 0; i < jsonArray.length(); i++){
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("id");
                        String name = jsonObject.getString("request_name");
                        String email = jsonObject.getString("email");
                        String blood_group = jsonObject.getString("bloodGroup");
                        String phone = jsonObject.getString("number");
                        String gender = jsonObject.getString("gender");
                        String location = jsonObject.getString("location");
                        String disease_type = jsonObject.getString("disease_type");
                        String unit = jsonObject.getString("unit");
                        String deadline = jsonObject.getString("deadline");
                        String hospital = jsonObject.getString("hospital");

                        hashMap = new HashMap<>();
                        hashMap.put("id",id);
                        hashMap.put("request_name",name);
                        hashMap.put("email",email);
                        hashMap.put("bloodGroup",blood_group);
                        hashMap.put("number",phone);
                        hashMap.put("gender",gender);
                        hashMap.put("hospital",hospital);
                        hashMap.put("location",location);
                        hashMap.put("deadline",deadline);
                        hashMap.put("disease_type",disease_type);
                        hashMap.put("unit",unit);
                       arrayList.add(hashMap);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }}
                searchAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){
            @Override
            public byte[] getBody() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("key",MyMethod.encryptedData("sayba1122"));
                    return jsonObject.toString().getBytes();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }}};
        RequestQueue requestQueue = Volley.newRequestQueue(Blood_Requests.this);
        requestQueue.add(jsonArrayRequest);
    }
    public void objectRequest1(){
        String url = "https://googix.xyz/blood_bridge/request_blood_filter.php";
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                arrayList.clear();
                progressBar.setVisibility(View.GONE);

                for(int i = 0; i < jsonArray.length(); i++){
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.optString("id");
                        String name = jsonObject.getString("request_name");
                        String email = jsonObject.getString("email");
                        String blood_group = jsonObject.getString("bloodGroup");
                        String phone = jsonObject.getString("number");
                        String gender = jsonObject.getString("gender");
                        String location = jsonObject.getString("location");
                        String disease_type = jsonObject.getString("disease_type");
                        String unit = jsonObject.getString("unit");
                        String deadline = jsonObject.getString("deadline");
                        String hospital = jsonObject.getString("hospital");

                        hashMap = new HashMap<>();
                        hashMap.put("id", id);
                        hashMap.put("request_name",name);
                        hashMap.put("email",email);
                        hashMap.put("bloodGroup",blood_group);
                        hashMap.put("number",phone);
                        hashMap.put("gender",gender);
                        hashMap.put("hospital",hospital);
                        hashMap.put("location",location);
                        hashMap.put("deadline",deadline);
                        hashMap.put("disease_type",disease_type);
                        hashMap.put("unit",unit);
                        arrayList.add(hashMap);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }}
                searchAdapter.notifyDataSetChanged();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){
            @Override
            public byte[] getBody() {
                JSONObject json = new JSONObject();
                try {
                    json.put("district",district.getText().toString());
                    json.put("area",area.getText().toString());
                    json.put("division",division.getText().toString());
                    json.put("blood_group", selectedBloodGroup);
                    json.put("key", MyMethod.encryptedData("sayba1122"));
                    return  json.toString().getBytes();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Blood_Requests.this);
        requestQueue.add(arrayRequest);
    }
    public void setupBloodGroupSelection(LinearLayout container){
        for(int i = 0; i < container.getChildCount(); i++ ){
            View view = container.getChildAt(i);

            if(view instanceof  CardView){
                CardView card = (CardView) view;
                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(selectedCard == card){
                            card.setCardBackgroundColor(ContextCompat.getColor(Blood_Requests.this,R.color.blood_unselected));
                            selectedCard = null;
                            selectedBloodGroup = "";

                            return;
                        }
                        if(selectedCard != null){
                            selectedCard.setCardBackgroundColor(ContextCompat.getColor(Blood_Requests.this,R.color.blood_unselected));
                        }
                        selectedCard = card;
                        selectedBloodGroup = card.getTag().toString();

                        card.setCardBackgroundColor(ContextCompat.getColor(Blood_Requests.this,R.color.blood_selected));
                    }
                });
            }
        }
    }
}