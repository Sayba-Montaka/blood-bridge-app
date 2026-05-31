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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
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

public class Message extends BaseActivity {

    RecyclerView message_view;
    FloatingActionButton fab;
    ImageView backArrow;
    String loginEmail, safeLoginEmail;
    HashMap<String, HashMap<String, String>> usersMap = new HashMap<>();

    //  Mixed list — holds both ConversationModel and NativeAd
    ArrayList<Object> displayList = new ArrayList<>();
    ArrayList<ConversationModel> conversationList = new ArrayList<>();

    ConversationAdapter adapter;
    NativeAd loadedNativeAd;

    private static final int AD_INTERVAL = 5; // show ad every 5 messages
    private static final int VIEW_TYPE_CONVERSATION = 1;
    private static final int VIEW_TYPE_AD = 2;

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

        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

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

        loadNativeAd();
        loadAllUsersThenChats();
    }
    private void loadNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(this,
                getString(R.string.native_add_id))
                .forNativeAd(nativeAd -> {
                    loadedNativeAd = nativeAd;
                    // Rebuild display list with ad injected
                    buildDisplayList();
                    adapter.notifyDataSetChanged();
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        Log.e("AD", "Failed to load: " + adError.getMessage());
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    // Build display list inserting ad every AD_INTERVAL conversations
    private void buildDisplayList() {
        displayList.clear();
        int convCount = 0;
        for (ConversationModel conv : conversationList) {
            displayList.add(conv);
            convCount++;
            // Insert ad after every AD_INTERVAL conversations
            if (convCount % AD_INTERVAL == 0 && loadedNativeAd != null) {
                displayList.add(loadedNativeAd);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadAllUsersThenChats() {
        if (loginEmail.isEmpty()) return;

        FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://blood-bank-t-default-rtdb.firebaseio.com/users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersMap.clear();
                        for (DataSnapshot userNode : snapshot.getChildren()) {
                            String email = userNode.child("email").getValue(String.class);
                            String name  = userNode.child("name").getValue(String.class);
                            String image = userNode.child("image").getValue(String.class);

                            if (email == null) {
                                for (DataSnapshot nested : userNode.getChildren()) {
                                    String nestedEmail = nested.child("email").getValue(String.class);
                                    String nestedName  = nested.child("name").getValue(String.class);
                                    String nestedImage = nested.child("image").getValue(String.class);
                                    if (nestedEmail == null) {
                                        Object val = nested.getValue();
                                        if (val instanceof HashMap) {
                                            HashMap map = (HashMap) val;
                                            if (map.containsKey("email")) nestedEmail = (String) map.get("email");
                                            if (map.containsKey("name"))  nestedName  = (String) map.get("name");
                                            if (map.containsKey("image")) nestedImage = (String) map.get("image");
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
                        listenToChatRooms();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void addToUsersMap(String email, String name, String image) {
        HashMap<String, String> info = new HashMap<>();
        info.put("name",  name  != null ? name  : "Unknown");
        info.put("image", image != null ? image : "");
        usersMap.put(email, info);
        usersMap.put(email.replace(".", ","), info);
    }

    private void listenToChatRooms() {
        FirebaseDatabase.getInstance().getReference("chats")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        conversationList.clear();

                        for (DataSnapshot room : snapshot.getChildren()) {
                            String roomId = room.getKey();
                            if (roomId == null || !roomId.contains(safeLoginEmail)) continue;

                            String[] parts = roomId.split("_");
                            if (parts.length != 2) continue;

                            String otherSafeEmail = parts[0].equals(safeLoginEmail)
                                    ? parts[1] : parts[0];
                            String otherEmail = otherSafeEmail.replace(",", ".");

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

                            HashMap<String, String> userInfo = usersMap.get(otherEmail);
                            if (userInfo == null) userInfo = usersMap.get(otherEmail.replace(".", ","));
                            if (userInfo == null) continue;

                            conversationList.add(new ConversationModel(
                                    userInfo.get("name"), otherEmail,
                                    userInfo.get("image"), lastMsg, lastTime, latestTs));
                        }

                        conversationList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

                        // Rebuild display list with ads injected
                        buildDisplayList();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ── Model ──────────────────────────────────────────────────────────────────
    public static class ConversationModel {
        public String name, email, image, lastMessage, lastTime;
        public long timestamp;
        public ConversationModel(String name, String email, String image,
                                 String lastMessage, String lastTime, long timestamp) {
            this.name = name; this.email = email; this.image = image;
            this.lastMessage = lastMessage; this.lastTime = lastTime;
            this.timestamp = timestamp;
        }
    }

    // ── Adapter ────────────────────────────────────────────────────────────────
    public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            return displayList.get(position) instanceof NativeAd
                    ? VIEW_TYPE_AD : VIEW_TYPE_CONVERSATION;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_AD) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_native_ad, parent, false);
                return new AdVH(v);
            } else {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                return new VH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_AD) {
                //  Bind native ad
                NativeAd nativeAd = (NativeAd) displayList.get(position);
                AdVH adVH = (AdVH) holder;
                populateNativeAdView(nativeAd, adVH.adView);
            } else {
                // Bind conversation
                ConversationModel conv = (ConversationModel) displayList.get(position);
                VH vh = (VH) holder;

                vh.M_name.setText(conv.name);
                vh.last_message.setText(conv.lastMessage);
                vh.last_m_time.setText(conv.lastTime);

                Glide.with(Message.this)
                        .load(conv.image)
                        .placeholder(R.drawable.theme)
                        .into(vh.shapeImage);

                String safeOther = conv.email.replace(".", ",");
                FirebaseDatabase.getInstance()
                        .getReference("presence").child(safeOther)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Boolean isOnline = snapshot.child("online").getValue(Boolean.class);
                                vh.onlineDot.setVisibility(
                                        isOnline != null && isOnline ? View.VISIBLE : View.GONE);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) {}
                        });

                vh.itemView.setOnClickListener(v -> {
                    if (conv.email == null || conv.email.isEmpty()) return;
                    Intent intent = new Intent(Message.this, Chat.class);
                    intent.putExtra("email", conv.email);
                    intent.putExtra("name",  conv.name  != null ? conv.name  : "Unknown");
                    intent.putExtra("image", conv.image != null ? conv.image : "");
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() { return displayList.size(); }

        // ── Conversation ViewHolder ────────────────────────────────────────
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

        // ── Ad ViewHolder ──────────────────────────────────────────────────
        public class AdVH extends RecyclerView.ViewHolder {
            NativeAdView adView;
            public AdVH(@NonNull View itemView) {
                super(itemView);
                adView = itemView.findViewById(R.id.native_ad_view);
            }
        }
    }

    //  Populate NativeAdView with ad assets
    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Headline
        TextView headlineView = adView.findViewById(R.id.ad_headline);
        headlineView.setText(nativeAd.getHeadline());
        adView.setHeadlineView(headlineView);

        // Body
        TextView bodyView = adView.findViewById(R.id.ad_body);
        if (nativeAd.getBody() != null) {
            bodyView.setText(nativeAd.getBody());
            bodyView.setVisibility(View.VISIBLE);
        } else {
            bodyView.setVisibility(View.GONE);
        }
        adView.setBodyView(bodyView);

        // Call to action
        TextView ctaView = adView.findViewById(R.id.ad_call_to_action);
        if (nativeAd.getCallToAction() != null) {
            ctaView.setText(nativeAd.getCallToAction());
            ctaView.setVisibility(View.VISIBLE);
        } else {
            ctaView.setVisibility(View.INVISIBLE);
        }
        adView.setCallToActionView(ctaView);

        // Register the native ad object
        adView.setNativeAd(nativeAd);
    }

    @Override
    protected void onDestroy() {
        //  Always destroy native ad to free memory
        if (loadedNativeAd != null) loadedNativeAd.destroy();
        super.onDestroy();
    }
}