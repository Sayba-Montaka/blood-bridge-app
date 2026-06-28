package com.example.bloodbankt;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MessageListenerService extends Service {

    String loginEmail, safeLoginEmail;
    SharedPreferences prefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationHelper.createChannel(this); // ensure channel exists

        prefs          = getSharedPreferences("BloodBank", MODE_PRIVATE);
        loginEmail     = prefs.getString("email", "").trim();
        safeLoginEmail = loginEmail.replace(".", ",");

        if (loginEmail.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        listenForNewMessages();
        return START_STICKY;
    }

    private void listenForNewMessages() {
        FirebaseDatabase.getInstance().getReference("chats")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot roomSnap, String prev) {
                        String roomId = roomSnap.getKey();
                        if (roomId == null || !roomId.contains(safeLoginEmail)) return;
                        listenToRoom(roomId);
                    }
                    @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void listenToRoom(String roomId) {
        String[] parts = roomId.split("_");
        if (parts.length != 2) return;

        String otherSafeEmail = parts[0].equals(safeLoginEmail) ? parts[1] : parts[0];
        String otherEmail     = otherSafeEmail.replace(",", ".");
        String prefKey        = "last_seen_" + roomId;

        // ✅ THE CORE FIX: only listen for messages newer than RIGHT NOW.
        // orderByChild + startAt means Firebase will ONLY deliver messages
        // with timestamp > now — no replay of existing messages at all.
        long listenFrom = System.currentTimeMillis();

        Query newMessagesQuery = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(roomId)
                .orderByChild("timestamp")
                .startAt(listenFrom);

        newMessagesQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot msgSnap, String prev) {
                String sender  = msgSnap.child("sender").getValue(String.class);
                String message = msgSnap.child("message").getValue(String.class);
                Long   ts      = msgSnap.child("timestamp").getValue(Long.class);
                String type    = msgSnap.child("type").getValue(String.class);

                if (sender == null || message == null || ts == null) return;

                // Skip my own messages
                if (sender.trim().equalsIgnoreCase(loginEmail)) return;

                // Skip if user has the chat open (last_seen updated by Chat.java)
                long lastSeen = prefs.getLong(prefKey, 0);
                if (ts <= lastSeen) return;

                // Don't notify for image messages with "image" as text
                String displayMessage = "image".equals(type) ? "📷 Image" : message;

                Log.d("NOTIF_SERVICE", "New message from " + otherEmail + ": " + displayMessage);

                // Fetch sender display name then notify
                FirebaseDatabase.getInstance().getReference("users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot usersSnap) {
                                String senderName  = otherEmail;
                                String senderImage = "";

                                for (DataSnapshot user : usersSnap.getChildren()) {
                                    String email = user.child("email").getValue(String.class);
                                    if (email != null && email.trim().equalsIgnoreCase(otherEmail)) {
                                        String n = user.child("name").getValue(String.class);
                                        String i = user.child("image").getValue(String.class);
                                        if (n != null) senderName  = n;
                                        if (i != null) senderImage = i;
                                        break;
                                    }
                                }

                                // Final guard: don't notify if chat was opened while we fetched name
                                if (ts <= prefs.getLong(prefKey, 0)) return;

                                NotificationHelper.showMessageNotification(
                                        MessageListenerService.this,
                                        senderName, displayMessage, otherEmail, senderImage);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) {}
                        });
            }

            @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}