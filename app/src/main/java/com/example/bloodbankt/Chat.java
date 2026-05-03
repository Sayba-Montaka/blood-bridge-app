package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Chat extends AppCompatActivity {

    TextView chatName,onlineStatus;;
    ImageView sendButton, imageOfM, backArrow;
    TextInputEditText messageInput;
    RecyclerView messageRecycler;

    String senderEmail, receiverEmail, chatRoomId;
    ArrayList<HashMap<String, String>> messageList = new ArrayList<>();
    ChatAdapter chatAdapter;
    DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chatName    = findViewById(R.id.chatName);
        imageOfM    = findViewById(R.id.imageOfM);
        sendButton  = findViewById(R.id.sendButton);
        backArrow   = findViewById(R.id.backArrow);
        messageInput = findViewById(R.id.messageInput);
        messageRecycler = findViewById(R.id.messageRecycler);
        onlineStatus = findViewById(R.id.online);


        // ── Receiver info from Intent ──────────────────────────────────────────
        String receiverEmail = getIntent().getStringExtra("email") != null
                ? getIntent().getStringExtra("email").trim() : "";
        String receiverName  = getIntent().getStringExtra("name") != null
                ? getIntent().getStringExtra("name") : "Unknown";
        String receiverImage = getIntent().getStringExtra("image") != null
                ? getIntent().getStringExtra("image") : "";

        NotificationHelper.clearNotification(this, receiverEmail);

        chatName.setText(receiverName);
        Glide.with(this).load(receiverImage).placeholder(R.drawable.theme).into(imageOfM);

        onlineStatus = findViewById(R.id.online);
        listenToPresence(receiverEmail); //

        backArrow.setOnClickListener(v ->
                startActivity(new Intent(Chat.this, Message.class)));

        // ── Sender email from SharedPreferences ────────────────────────────────
        SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
        senderEmail = prefs.getString("email", "").trim();

        // ── One unique room ID for this pair, no matter the entry point ────────
        chatRoomId = ChatManager.getChatRoomId(senderEmail, receiverEmail);

        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);

        // ── RecyclerView ───────────────────────────────────────────────────────
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);          // newest messages at bottom
        messageRecycler.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter();
        messageRecycler.setAdapter(chatAdapter);

        // ── Send button ────────────────────────────────────────────────────────
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) return;

            HashMap<String, Object> map = new HashMap<>();
            map.put("sender",    senderEmail);
            map.put("receiver",  receiverEmail);
            map.put("message",   message);
            map.put("timestamp", System.currentTimeMillis());

            chatRef.push().setValue(map);
            messageInput.setText("");
        });

        // ── Listen for messages ────────────────────────────────────────────────
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("sender",    data.child("sender").getValue(String.class));
                    msg.put("message",   data.child("message").getValue(String.class));
                    Long ts = data.child("timestamp").getValue(Long.class);
                    msg.put("timestamp", ts != null ? formatTime(ts) : "");
                    messageList.add(msg);
                }
                chatAdapter.notifyDataSetChanged();
                // scroll to latest
                if (!messageList.isEmpty())
                    messageRecycler.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    // ── Chat Adapter ───────────────────────────────────────────────────────────
    private static final int VIEW_TYPE_SENT     = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MsgViewHolder> {

        @Override
        public int getItemViewType(int position) {
            String sender = messageList.get(position).get("sender");
            return (sender != null && sender.equals(senderEmail))
                    ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }

        @NonNull
        @Override
        public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = (viewType == VIEW_TYPE_SENT)
                    ? R.layout.item_message_sent
                    : R.layout.item_message_received;
            View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new MsgViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
            HashMap<String, String> msg = messageList.get(position);
            holder.messageText.setText(msg.get("message"));
            holder.timeText.setText(msg.get("timestamp"));
        }

        @Override
        public int getItemCount() { return messageList.size(); }

        public class MsgViewHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            MsgViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
                timeText    = itemView.findViewById(R.id.timeText);
            }
        }
    }


    private void listenToPresence(String email) {
        // ✅ Guard against null or empty email
        if (email == null || email.isEmpty()) return;

        String safeEmail = email.trim().replace(".", ",");
        FirebaseDatabase.getInstance()
                .getReference("presence").child(safeEmail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isOnline = snapshot.child("online").getValue(Boolean.class);
                        if (onlineStatus == null) return;
                        if (isOnline != null && isOnline) {
                            onlineStatus.setText("Online");
                            onlineStatus.setTextColor(Color.parseColor("#00E676"));
                        } else {
                            Long lastSeen = snapshot.child("lastSeen").getValue(Long.class);
                            if (lastSeen != null) {
                                String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                                        .format(new Date(lastSeen));
                                onlineStatus.setText("Last seen " + time);
                            } else {
                                onlineStatus.setText("Offline");
                            }
                            onlineStatus.setTextColor(Color.parseColor("#BBBBBB"));
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}