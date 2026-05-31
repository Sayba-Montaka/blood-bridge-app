package com.example.bloodbankt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

public class Search_Message extends AppCompatActivity {
    SearchView search_message;
    ProgressBar progressBar;
    SearchAdapter searchAdapter;
    RecyclerView messageRecycler;
    HashMap<String,String> hashMap;
    ArrayList<HashMap<String,String>> originalList = new ArrayList<>();
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_message);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        search_message = findViewById(R.id.search_message);
        messageRecycler = findViewById(R.id.messageRecycler);
        objectRequest();

        messageRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter();
        messageRecycler.setAdapter(searchAdapter);

        search_message.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });

    }

    private void filter(String text) {

        arrayList.clear();

        if (text.isEmpty()) {
            arrayList.addAll(originalList);
        } else {

            for (HashMap<String, String> item : originalList) {

                String name = item.get("donor_name");
                String email = item.get("email");

                if (name != null && email != null) {

                    if (name.toLowerCase().contains(text.toLowerCase()) ||
                            email.toLowerCase().contains(text.toLowerCase())) {

                        arrayList.add(item);
                    }
                }
            }
        }

        searchAdapter.notifyDataSetChanged();
    }

    public class SearchAdapter extends  RecyclerView.Adapter<SearchAdapter.myViewHolder>{

        public void filterList(ArrayList<HashMap<String, String>> filteredList) {
                arrayList.clear();
                arrayList.addAll(filteredList);
                notifyDataSetChanged();

        }
        @NonNull
        @Override
        public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.search_message_item, parent, false);
            return new myViewHolder(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
            hashMap = arrayList.get(position);
            String name = hashMap.get("donor_name");
            String email = hashMap.get("email");
            String image = hashMap.get("image");

            holder.M_name.setText(name);
            holder.M_email.setText(email);
            Glide.with(Search_Message.this)
                    .load("https://blood-bridge.org/blood_bridge/" + image)
                    .into(holder.M_shapeImage);

            // In Search_Message onBindViewHolder:
            holder.itemView.setOnClickListener(v -> {
                if (email == null || email.isEmpty()) return;
                Intent intent = new Intent(Search_Message.this, Chat.class);
                intent.putExtra("email", email);
                intent.putExtra("name",  name  != null ? name  : "Unknown");
                intent.putExtra("image", image != null ? image : "");
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class myViewHolder extends  RecyclerView.ViewHolder{
            ShapeableImageView M_shapeImage;
            TextView M_name,M_email;
            public myViewHolder(@NonNull View itemView) {
                super(itemView);
                M_shapeImage = itemView.findViewById(R.id.M_shapeImage);
                M_name = itemView.findViewById(R.id.M_name);
                M_email = itemView.findViewById(R.id.M_email);
            }
        }
    }

    private void objectRequest() {

        String url = "https://blood-bridge.org/blood_bridge/search_messanger.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, null,
                response -> {

                    originalList.clear();
                    arrayList.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<>();
                            map.put("donor_name", obj.getString("donor_name"));
                            map.put("image", obj.getString("image"));
                            map.put("email", obj.getString("email"));

                            arrayList.add(map);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    originalList.addAll(arrayList);
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
}