package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Message extends BaseActivity {

    RecyclerView message_view;
    FloatingActionButton fab;
    ImageView backArrow;
    String loginEmail, safeLoginEmail;

    // Store all users from Firebase once, then match against chat rooms
    HashMap<String, HashMap<String, String>> usersMap = new HashMap<>();
    ArrayList<ConversationModel> conversationList = new ArrayList<>();
    ConversationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation(R.id.messenger);
        message_view = findViewById(R.id.message_view);
        fab          = findViewById(R.id.fab);
        backArrow    = findViewById(R.id.backArrow);

        backArrow.setOnClickListener(v ->
                startActivity(new Intent(Message.this, MainActivity.class)));
        fab.setOnClickListener(v ->
                startActivity(new Intent(Message.this, Search_Message.class)));

        SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
        loginEmail     = prefs.getString("email", "").trim();
        safeLoginEmail = loginEmail.replace(".", ",");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null && loginEmail.isEmpty()) {
            startActivity(new Intent(Message.this, FirstPage.class));
            finish();
            return;
        }

        message_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter();
        message_view.setAdapter(adapter);

        // Step 1: Load ALL users first, then load chat rooms
        loadAllUsersThenChats();
    }

    /**
     * STEP 1 — fetch every user from Firebase into usersMap (keyed by email).
     * STEP 2 — once we have users, scan chat rooms for this user.
     * This avoids nested async calls that caused race conditions before.
     */
    private void loadAllUsersThenChats() {
        if (loginEmail.isEmpty()) return;

        FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://blood-bank-t-default-rtdb.firebaseio.com/users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersMap.clear();

                        for (DataSnapshot userNode : snapshot.getChildren()) {

                            // ── CASE 1: node key looks like an email (hedayetullah123@gmail_com)
                            // and the value is a HashMap with name/email/image inside it
                            String nodeKey = userNode.getKey(); // e.g. "hedayetullah123@gmail_com"

                            String email = userNode.child("email").getValue(String.class);
                            String name  = userNode.child("name").getValue(String.class);
                            String image = userNode.child("image").getValue(String.class);

                            // ── CASE 2: data is nested one level deeper (users/users/key = {email,name,image})
                            // Check if this node itself has children that are user objects
                            if (email == null) {
                                for (DataSnapshot nested : userNode.getChildren()) {
                                    String nestedEmail = nested.child("email").getValue(String.class);
                                    String nestedName  = nested.child("name").getValue(String.class);
                                    String nestedImage = nested.child("image").getValue(String.class);

                                    // Also try: the nested key itself is the safe email
                                    if (nestedEmail == null) {
                                        // The object might be stored as value under the email key
                                        // Try getting email from the key
                                        String keyAsEmail = nested.getKey();
                                        if (keyAsEmail != null) {
                                            nestedEmail = keyAsEmail.replace("_", "."); // gmail_com → gmail.com
                                            // But this gives wrong result, so try getValue as map
                                            Object val = nested.getValue();
                                            if (val instanceof java.util.HashMap) {
                                                java.util.HashMap map = (java.util.HashMap) val;
                                                if (map.containsKey("email")) nestedEmail = (String) map.get("email");
                                                if (map.containsKey("name"))  nestedName  = (String) map.get("name");
                                                if (map.containsKey("image")) nestedImage = (String) map.get("image");
                                            }
                                        }
                                    }

                                    if (nestedEmail != null && !nestedEmail.trim().isEmpty()) {
                                        addToUsersMap(nestedEmail.trim(), nestedName, nestedImage);
                                    }
                                }
                            } else {
                                addToUsersMap(email.trim(), name, image);
                            }
                        }

                        Log.d("MSG_DEBUG", "usersMap final keys: " + usersMap.keySet());
                        listenToChatRooms();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MSG_DEBUG", "Failed: " + error.getMessage());
                    }
                });
    }

    // Helper to avoid duplicate code
    private void addToUsersMap(String email, String name, String image) {
        HashMap<String, String> info = new HashMap<>();
        info.put("name",  name  != null ? name  : "Unknown");
        info.put("image", image != null ? image : "");
        usersMap.put(email, info);                        // with dots
        usersMap.put(email.replace(".", ","), info);      // with commas
        Log.d("MSG_DEBUG", "Added to usersMap: " + email);
    }

    /**
     * STEP 2 — listen to chats/ in real time.
     * For each room that contains safeLoginEmail, find the other person,
     * grab last message, match with usersMap, build ConversationModel.
     */
    private void listenToChatRooms() {
        FirebaseDatabase.getInstance().getReference("chats")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // ✅ ADD THESE DEBUG LOGS FIRST
                        Log.d("MSG_DEBUG", "Total chat rooms found: " + snapshot.getChildrenCount());
                        Log.d("MSG_DEBUG", "My safe email: " + safeLoginEmail);


                        conversationList.clear();

                        for (DataSnapshot room : snapshot.getChildren()) {
                            Log.d("MSG_DEBUG", "Room ID: " + room.getKey());
                            String roomId = room.getKey();
                            if (roomId == null) continue;

                            // Only process rooms this user belongs to
                            if (!roomId.contains(safeLoginEmail)) continue;

                            // ── Find the other person's email ──────────────────
                            String[] parts = roomId.split("_");
                            if (parts.length != 2) continue;

                            String otherSafeEmail = parts[0].equals(safeLoginEmail)
                                    ? parts[1] : parts[0];
                            String otherEmail = otherSafeEmail.replace(",", ".");

                            // ── Get last message from this room ────────────────
                            String lastMsg  = "";
                            String lastTime = "";
                            long   latestTs = 0;

                            for (DataSnapshot msgSnap : room.getChildren()) {
                                Long ts = msgSnap.child("timestamp").getValue(Long.class);
                                if (ts != null && ts > latestTs) {
                                    latestTs = ts;
                                    String txt = msgSnap.child("message").getValue(String.class);
                                    lastMsg  = txt != null ? txt : "";
                                    lastTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                                            .format(new Date(ts));
                                }
                            }

                            // Replace the userInfo lookup line with this:
                            HashMap<String, String> userInfo = usersMap.get(otherEmail); // dots
                            if (userInfo == null) {
                                userInfo = usersMap.get(otherEmail.replace(".", ",")); // try commas
                            }
                            if (userInfo == null) {
                                Log.e("MSG_DEBUG", "STILL not found for: " + otherEmail);
                                Log.e("MSG_DEBUG", "Available keys: " + usersMap.keySet());
                                continue;
                            }

                            String name  = userInfo.get("name");
                            String image = userInfo.get("image");

                            conversationList.add(new ConversationModel(
                                    name, otherEmail, image, lastMsg, lastTime, latestTs));
                        }

                        // Sort by latest message (newest first, like WhatsApp)
                        conversationList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ── Model ──────────────────────────────────────────────────────────────────
    public static class ConversationModel {
        public String name, email, image, lastMessage, lastTime;
        public long   timestamp;

        public ConversationModel(String name, String email, String image,
                                 String lastMessage, String lastTime, long timestamp) {
            this.name        = name;
            this.email       = email;
            this.image       = image;
            this.lastMessage = lastMessage;
            this.lastTime    = lastTime;
            this.timestamp   = timestamp;
        }
    }

    // ── Adapter ────────────────────────────────────────────────────────────────
    public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ConversationModel conv = conversationList.get(position);

            holder.M_name.setText(conv.name);
            holder.last_message.setText(conv.lastMessage);
            holder.last_m_time.setText(conv.lastTime);

            Glide.with(Message.this)
                    .load(conv.image)
                    .placeholder(R.drawable.theme)
                    .into(holder.shapeImage);

            // ── Online dot ─────────────────────────────────────────────────
            String safeOther = conv.email.replace(".", ",");
            FirebaseDatabase.getInstance()
                    .getReference("presence").child(safeOther)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean isOnline = snapshot.child("online").getValue(Boolean.class);
                            holder.onlineDot.setVisibility(
                                    isOnline != null && isOnline ? View.VISIBLE : View.GONE);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });

            // ── Click to open chat ─────────────────────────────────────────
            holder.itemView.setOnClickListener(v -> {
                if (conv.email == null || conv.email.isEmpty()) return; // guard
                Intent intent = new Intent(Message.this, Chat.class);
                intent.putExtra("email", conv.email != null ? conv.email : "");
                intent.putExtra("name",  conv.name  != null ? conv.name  : "Unknown");
                intent.putExtra("image", conv.image != null ? conv.image : "");
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return conversationList.size(); }

        public class VH extends RecyclerView.ViewHolder {
            ShapeableImageView shapeImage;
            TextView M_name, last_message, last_m_time;
            View onlineDot;

            public VH(@NonNull View itemView) {
                super(itemView);
                shapeImage   = itemView.findViewById(R.id.shapeImage);
                M_name       = itemView.findViewById(R.id.M_name);
                last_message = itemView.findViewById(R.id.last_message);
                last_m_time  = itemView.findViewById(R.id.last_m_time);
                onlineDot    = itemView.findViewById(R.id.onlineDot);
            }
        }
    }
}