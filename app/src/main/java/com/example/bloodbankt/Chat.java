package com.example.bloodbankt;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.webkit.internal.ApiFeature;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    Uri cameraImageUri;
    ImageView    attachButton ;
    // Gallery picker
    ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) sendImage(uri);
            });

    // Camera picker
    ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) sendImage(cameraImageUri);
            });

    // Permission launcher
    ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean allGranted = true;
                for (boolean granted : result.values()) {
                    if (!granted) { allGranted = false; break; }
                }
                if (allGranted) showImageSourceDialog();
                else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            });

    // View types
    private static final int VIEW_TYPE_SENT          = 1;
    private static final int VIEW_TYPE_RECEIVED      = 2;
    private static final int VIEW_TYPE_SENT_IMAGE    = 3;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets  = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    Math.max(systemBars.bottom, imeInsets.bottom)
            );
            return insets;
        });

        chatName    = findViewById(R.id.chatName);
        imageOfM    = findViewById(R.id.imageOfM);
        sendButton  = findViewById(R.id.sendButton);
        backArrow   = findViewById(R.id.backArrow);
        messageInput = findViewById(R.id.messageInput);
        messageRecycler = findViewById(R.id.messageRecycler);
        onlineStatus = findViewById(R.id.online);
        attachButton    = findViewById(R.id.attachButton);


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

        listenToPresence(receiverEmail); //

        backArrow.setOnClickListener(v ->
                startActivity(new Intent(Chat.this, Message.class)));

        // ── Sender email from SharedPreferences ────────────────────────────────
        SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
        senderEmail = prefs.getString("email", "").trim();

        // ── One unique room ID for this pair, no matter the entry point ────────
        chatRoomId = ChatManager.getChatRoomId(senderEmail, receiverEmail);

        markMessagesAsRead(chatRoomId);


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
            map.put("status",    "sent");

            chatRef.push().setValue(map);
            messageInput.setText("");
        });

        // ── Attach image button ────────────────────────────────────────────────
        attachButton.setOnClickListener(v -> checkPermissionsAndOpenPicker());

        // ── Listen for messages ────────────────────────────────────────────────
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                long latestTs = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    String key    = data.getKey();
                    String sender = data.child("sender").getValue(String.class);
                    String status = data.child("status").getValue(String.class);
                    if (status == null) status = "sent"; // backward-compat for old messages

                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("sender",  sender);
                    msg.put("message", data.child("message").getValue(String.class));
                    msg.put("type",    data.child("type").getValue(String.class));
                    msg.put("status",  status);

                    Long ts = data.child("timestamp").getValue(Long.class);
                    if (ts != null && ts > latestTs) latestTs = ts;
                    msg.put("timestamp", ts != null ? formatTime(ts) : "");
                    messageList.add(msg);

                    // If this message isn't mine and hasn't been marked seen yet,
                    // mark it now — because I'm here, in this chat, looking at it.
                    boolean isMine = sender != null && sender.equals(senderEmail);
                    if (!isMine && !"seen".equals(status) && key != null) {
                        chatRef.child(key).child("status").setValue("seen");
                    }
                }

                if (latestTs > 0) {
                    getSharedPreferences("BloodBank", MODE_PRIVATE).edit()
                            .putLong("last_seen_" + chatRoomId, latestTs).apply();
                }

                chatAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty())
                    messageRecycler.scrollToPosition(messageList.size() - 1);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ── Permission check ───────────────────────────────────────────────────────
    private void checkPermissionsAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ — READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog();
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA});
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog();
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA});
            }
        }
    }

    // ── Show gallery or camera choice ──────────────────────────────────────────
    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Send Image")
                .setItems(new String[]{"Choose from Gallery", "Take Photo"}, (dialog, which) -> {
                    if (which == 0) {
                        // Gallery
                        galleryLauncher.launch("image/*");
                    } else {
                        // Camera
                        try {
                            File imageFile = File.createTempFile("IMG_", ".jpg", getCacheDir());
                            cameraImageUri = FileProvider.getUriForFile(
                                    this, getPackageName() + ".provider", imageFile);
                            cameraLauncher.launch(cameraImageUri);
                        } catch (IOException e) {
                            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    // ── Convert image URI to Base64 and send ───────────────────────────────────
    private void sendImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // Compress to reduce Firebase storage size
            Bitmap scaled = scaleBitmap(bitmap, 800);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            HashMap<String, Object> map = new HashMap<>();
            map.put("sender",    senderEmail);
            map.put("receiver",  receiverEmail);
            map.put("message",   base64Image);   // store image as base64
            map.put("type",      "image");        // type = image
            map.put("timestamp", System.currentTimeMillis());
            map.put("status",    "sent");

            chatRef.push().setValue(map);

        } catch (IOException e) {
            Toast.makeText(this, "Failed to send image", Toast.LENGTH_SHORT).show();
        }
    }

    // Scale bitmap to max width to save space
    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth) {
        if (bitmap.getWidth() <= maxWidth) return bitmap;
        float ratio = (float) maxWidth / bitmap.getWidth();
        int newHeight = (int) (bitmap.getHeight() * ratio);
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    // ── Chat Adapter ───────────────────────────────────────────────────────────
    public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            HashMap<String, String> msg = messageList.get(position);
            String sender = msg.get("sender");
            String type   = msg.get("type");
            boolean isSent = sender != null && sender.equals(senderEmail);

            if ("image".equals(type)) {
                return isSent ? VIEW_TYPE_SENT_IMAGE : VIEW_TYPE_RECEIVED_IMAGE;
            } else {
                return isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_SENT:
                    return new TextVH(inflater.inflate(R.layout.item_message_sent, parent, false));
                case VIEW_TYPE_RECEIVED:
                    return new TextVH(inflater.inflate(R.layout.item_message_received, parent, false));
                case VIEW_TYPE_SENT_IMAGE:
                    return new ImageVH(inflater.inflate(R.layout.item_image_sent, parent, false));
                case VIEW_TYPE_RECEIVED_IMAGE:
                    return new ImageVH(inflater.inflate(R.layout.item_image_received, parent, false));
                default:
                    return new TextVH(inflater.inflate(R.layout.item_message_sent, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            HashMap<String, String> msg = messageList.get(position);

            if (holder instanceof TextVH) {
                TextVH vh = (TextVH) holder;
                vh.messageText.setText(msg.get("message"));
                vh.timeText.setText(msg.get("timestamp"));
                bindTicks(vh.tick1, vh.tick2, msg.get("status"));

            } else if (holder instanceof ImageVH) {
                ImageVH vh = (ImageVH) holder;
                vh.timeText.setText(msg.get("timestamp"));
                bindTicks(vh.tick1, vh.tick2, msg.get("status"));
                String base64 = msg.get("message");

                if(vh.download_button != null){
                    String sender = msg.get("sender");
                    boolean isReceived = sender == null || !sender.equals(senderEmail);
                    vh.download_button.setVisibility(isReceived ? View.VISIBLE: View.GONE);
                }
                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Glide.with(Chat.this)
                                .load(bytes)
                                .placeholder(R.drawable.theme)
                                .into(vh.messageImage);
                    } catch (Exception e) {
                        vh.messageImage.setImageResource(R.drawable.theme);
                    }
                }

                vh.messageImage.setOnClickListener(v -> {
                    String b64 = msg.get("message");
                    if (b64 != null) {
                        Intent intent = new Intent(Chat.this, FullImageActivity.class);
                        intent.putExtra("base64", b64);
                        startActivity(intent);
                    }
                });
                if(vh.download_button != null){
                    vh.download_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(base64 != null && !base64.isEmpty()){
                                captureAndSaveLayout(vh.messageImage);
                            }else{
                                Toast.makeText(Chat.this, "No image to save", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() { return messageList.size(); }

        // Text message ViewHolder
        class TextVH extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            ImageView tick1, tick2;
            TextVH(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
                timeText    = itemView.findViewById(R.id.timeText);
                tick1 = itemView.findViewById(R.id.ivTick1);
                tick2 = itemView.findViewById(R.id.ivTick2);
            }
        }

        // Image message ViewHolder
        class ImageVH extends RecyclerView.ViewHolder {
            ImageView messageImage,tick1, tick2;

            TextView timeText,download_button;
            ImageVH(@NonNull View itemView) {
                super(itemView);
                messageImage = itemView.findViewById(R.id.messageImage);
                timeText     = itemView.findViewById(R.id.timeText);
                download_button     = itemView.findViewById(R.id.download_button);
                tick1 = itemView.findViewById(R.id.ivTick1); // null on received_image layout
                tick2 = itemView.findViewById(R.id.ivTick2);
            }
        }
    }

    private void listenToPresence(String email) {
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

    private void markMessagesAsRead(String roomId) {
        getSharedPreferences("BloodBank", MODE_PRIVATE).edit()
                .putLong("last_seen_" + roomId, System.currentTimeMillis()).apply();
    }
    private void captureAndSaveLayout(View view) {

        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);

        Drawable bgDrawable = view.getBackground();

        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }

        view.draw(canvas);

        saveBitmapToGallery(bitmap);
    }
    private void saveBitmapToGallery(Bitmap bitmap) {

        String filename = "chat_image_" + System.currentTimeMillis() + ".jpg";

        OutputStream fos = null;

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentResolver resolver = getContentResolver();

                ContentValues contentValues = new ContentValues();

                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/BloodBridge");

                Uri imageUri = resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                );

                fos = resolver.openOutputStream(imageUri);

            } else {

                File directory = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES),
                        "BloodBridge"
                );

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                File image = new File(directory, filename);

                fos = new FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            if (fos != null) {
                fos.flush();
                fos.close();
            }

            Toast.makeText(this, "Saved to gallery!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
    private void bindTicks(ImageView tick1, ImageView tick2, String status) {
        if (tick1 == null || tick2 == null) return; // received message, no ticks
        int gray = ContextCompat.getColor(this, R.color.tick_gray);
        int blue = ContextCompat.getColor(this, R.color.tick_blue);

        if ("seen".equals(status)) {
            tick1.setVisibility(View.VISIBLE);
            tick2.setVisibility(View.VISIBLE);
            tick1.setColorFilter(blue);
            tick2.setColorFilter(blue);
        } else if ("delivered".equals(status)) {
            tick1.setVisibility(View.VISIBLE);
            tick2.setVisibility(View.VISIBLE);
            tick1.setColorFilter(gray);
            tick2.setColorFilter(gray);
        } else { // "sent"
            tick1.setVisibility(View.VISIBLE);
            tick2.setVisibility(View.GONE);
            tick1.setColorFilter(gray);
        }
    }
}


