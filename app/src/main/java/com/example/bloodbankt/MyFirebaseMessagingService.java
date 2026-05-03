package com.example.bloodbankt;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String timeStr = remoteMessage.getData().get("time");
        long notificationTime = System.currentTimeMillis();

        Log.d("TITLE_BODY","title"+title+body+timeStr);

        if(timeStr != null){
            try { notificationTime = Long.parseLong(timeStr); }
            catch (NumberFormatException e){ e.printStackTrace(); }
        }

        saveNotification(title, body, notificationTime);

        // Show system notification
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notificationTime,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        sendNotification(title, body + " • " + timeAgo);

        // Send a broadcast so NotificationActivity updates
        Intent intent = new Intent("NEW_NOTIFICATION");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    // [END receive_message]

    // [START on_new_token]
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }
    // [END on_new_token]
    private void sendRegistrationToServer(String token){

        String url = "https://googix.xyz/blood_bridge/save_token.php";

        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        String phone = sp.getString("phone","");

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {

                    Log.d("TOKEN_SERVER", "Token saved : "+response);

                },
                error -> {

                    Log.d("TOKEN_SERVER", "Error saving token "+error.toString());

                }){

            @Override
            protected Map<String,String> getParams(){

                Map<String,String> params = new HashMap<>();

                params.put("phone", phone);
                params.put("token", token);

                Log.d("firebase_token", token);
                Log.d("firebase_phone", phone);

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    private void saveNotification(String title, String body, long time){
        SharedPreferences prefs = getSharedPreferences("NOTIFICATION", MODE_PRIVATE);
        String json = prefs.getString("notifications", "[]");

        try {
            JSONArray array = new JSONArray(json);

            JSONObject obj = new JSONObject();
            obj.put("title", title);
            obj.put("body", body);
            obj.put("time", time);

            // Create new array and add new notification first
            JSONArray newArray = new JSONArray();
            newArray.put(obj);

            for(int i=0; i<array.length(); i++){
                newArray.put(array.getJSONObject(i));
            }

            // Keep only latest 15 notifications
            if(newArray.length() > 15){
                JSONArray limited = new JSONArray();
                for(int i=0; i<15; i++){
                    limited.put(newArray.getJSONObject(i));
                }
                newArray = limited;
            }

            prefs.edit().putString("notifications", newArray.toString()).apply();

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}