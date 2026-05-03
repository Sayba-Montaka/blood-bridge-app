package com.example.bloodbankt;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MessageListenerService extends Service {

    String loginEmail, safeLoginEmail;
    HashMap<String, Long> lastSeenTimestamps = new HashMap<>();
    ChildEventListener chatRoomsListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
        loginEmail     = prefs.getString("email", "").trim();
        safeLoginEmail = loginEmail.replace(".", ",");

        if (loginEmail.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        listenForNewMessages();
        return START_STICKY; // restart if killed
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

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, String prev) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String prev) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void listenToRoom(String roomId) {

        // Find the other person's safe email from room ID
        String[] parts = roomId.split("_");
        if (parts.length != 2) return;
        String otherSafeEmail = parts[0].equals(safeLoginEmail) ? parts[1] : parts[0];
        String otherEmail     = otherSafeEmail.replace(",", ".");

        FirebaseDatabase.getInstance().getReference("chats").child(roomId)
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot msgSnap, String prev) {

                        String sender  = msgSnap.child("sender").getValue(String.class);
                        String message = msgSnap.child("message").getValue(String.class);
                        Long   ts      = msgSnap.child("timestamp").getValue(Long.class);

                        if (sender == null || message == null || ts == null) return;

                        // Only notify for messages FROM others, not our own
                        if (sender.trim().equals(loginEmail)) return;

                        // Avoid duplicate notifications on app start
                        // (onChildAdded fires for ALL existing messages at first)
                        Long lastSeen = lastSeenTimestamps.get(roomId);
                        if (lastSeen != null && ts <= lastSeen) return;
                        lastSeenTimestamps.put(roomId, ts);

                        // Fetch sender's name + image then show notification
                        FirebaseDatabase.getInstance().getReference("users")
                                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot usersSnap) {
                                        String senderName  = otherEmail; // fallback
                                        String senderImage = "";
                                        for (DataSnapshot user : usersSnap.getChildren()) {
                                            String email = user.child("email").getValue(String.class);
                                            if (email != null && email.trim().equals(otherEmail)) {
                                                senderName  = user.child("name").getValue(String.class);
                                                senderImage = user.child("image").getValue(String.class);
                                                if (senderName  == null) senderName  = otherEmail;
                                                if (senderImage == null) senderImage = "";
                                                break;
                                            }
                                        }
                                        NotificationHelper.showMessageNotification(
                                                MessageListenerService.this,
                                                senderName, message, otherEmail, senderImage);
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