package com.example.bloodbankt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Notification extends BaseActivity {
ListView notification_view;
ImageView backArrow;
NotificationAdapter notificationAdapter;
HashMap<String,String> hashMap;
ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupBottomNavigation(R.id.noti);
        notification_view=findViewById(R.id.notification_view);
        notificationAdapter = new NotificationAdapter();
        notification_view.setAdapter(notificationAdapter);
        backArrow = findViewById(R.id.backArrow);
        SharedPreferences prefs = getSharedPreferences("NOTIFICATION", MODE_PRIVATE);
        String json = prefs.getString("notifications", "[]");


        try {
            JSONArray array = new JSONArray(json);
            arrayList.clear();

            for(int i=0; i<array.length(); i++){

                JSONObject obj = array.getJSONObject(i);

                HashMap<String,String> map = new HashMap<>();
                map.put("title", obj.getString("title"));
                map.put("body", obj.getString("body"));

                long timeMillis = obj.getLong("time");
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        timeMillis,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                );
                map.put("time", timeAgo.toString());

                arrayList.add(map);
            }

            ((BaseAdapter) notification_view.getAdapter()).notifyDataSetChanged();

        } catch (JSONException e){
            e.printStackTrace();
        }

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Notification.this,MainActivity.class));
                finish();
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();

        // Listen for new notifications while activity is open
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                new IntentFilter("NEW_NOTIFICATION"));

        loadNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
    }

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNotifications(); // Reload the list
        }
    };

    private void loadNotifications() {
        SharedPreferences prefs = getSharedPreferences("NOTIFICATION", MODE_PRIVATE);
        String json = prefs.getString("notifications", "[]");

        try {
            JSONArray array = new JSONArray(json);
            arrayList.clear();

            for(int i=0; i<array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                HashMap<String,String> map = new HashMap<>();
                map.put("title", obj.getString("title"));
                map.put("body", obj.getString("body"));

                long timeMillis = obj.getLong("time");
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        timeMillis,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                );
                map.put("time", timeAgo.toString());

                arrayList.add(map);
            }

            notificationAdapter.notifyDataSetChanged();

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
    public class  NotificationAdapter extends BaseAdapter {

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
            View noti_view = inflater.inflate(R.layout.item_noti, parent, false);
            TextView title,message,time;

            title = noti_view.findViewById(R.id.title);
            message = noti_view.findViewById(R.id.message);
            time = noti_view.findViewById(R.id.time);

            hashMap = arrayList.get(position);
            String title_final = hashMap.get("title");
            String body_final = hashMap.get("body");
            String time_final = hashMap.get("time");

            title.setText(title_final);
            message.setText(body_final);
            time.setText(time_final);

            return noti_view;
        }
    }
}