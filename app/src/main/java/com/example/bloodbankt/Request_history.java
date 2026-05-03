package com.example.bloodbankt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Request_history extends AppCompatActivity {
ListView listView;
ImageView backArrow;
HashMap <String ,String> hashMap;
ArrayList<HashMap <String,String> >arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backArrow = findViewById(R.id.backArrow);
        listView = findViewById(R.id.listView);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Request_history.this,Profile.class));
                finish();
            }
        });


        SharedPreferences sharedPreferences = getSharedPreferences("PINNED_REQUEST", MODE_PRIVATE);

        String data = sharedPreferences.getString("requests", "[]");

        try {

            JSONArray jsonArray = new JSONArray(data);

            for(int i = 0; i < jsonArray.length(); i++){

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                HashMap<String,String> map = new HashMap<>();

                map.put("id",jsonObject.optString("id"));
                map.put("bloodGroup", jsonObject.getString("bloodGroup"));
                map.put("hospital", jsonObject.getString("hospital"));
                map.put("location", jsonObject.getString("location"));
                map.put("deadline", jsonObject.getString("deadline"));

                arrayList.add(map);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        RequestAdapter requestAdapter = new RequestAdapter();
        listView.setAdapter(requestAdapter);



    }
    public class RequestAdapter extends BaseAdapter{

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
            View pinned_view = inflater.inflate(R.layout.pinned_item,parent,false);

            TextView bloodGroup,deadline,location,hospital_name;
            Button seeDetails_Btn,done_Btn;
            bloodGroup = pinned_view.findViewById(R.id.bloodGroup);
            hospital_name = pinned_view.findViewById(R.id.hospital_name);
            deadline = pinned_view.findViewById(R.id.deadline);
            location = pinned_view.findViewById(R.id.location);
            seeDetails_Btn = pinned_view.findViewById(R.id.seeDetails_Btn);
            done_Btn = pinned_view.findViewById(R.id.done_Btn);

            hashMap = arrayList.get(position);
            String id = hashMap.get("id");
            String blood_group = hashMap.get("bloodGroup");
            String hospitalName = hashMap.get("hospital");
            String location_final = hashMap.get("location");
            String deadline_final = hashMap.get("deadline");

            bloodGroup.setText(blood_group);
            hospital_name.setText(hospitalName);
            deadline.setText(deadline_final);
            location.setText(location_final);



            seeDetails_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            done_Btn.setOnClickListener(v -> {

                SharedPreferences sharedPreferences =
                        getSharedPreferences("PINNED_REQUEST",MODE_PRIVATE);

                String data = sharedPreferences.getString("requests","[]");

                try {

                    JSONArray jsonArray = new JSONArray(data);

                    jsonArray.remove(position);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("requests",jsonArray.toString());
                    editor.apply();

                    arrayList.remove(position);
                    notifyDataSetChanged();

                }catch (Exception e){
                    e.printStackTrace();
                }

            });

            return pinned_view;
        }
    }
}