package com.example.bloodbankt;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {
    DrawerLayout drawerLayout;
    ImageButton circled_menu;
    NavigationView nav_view;
    FirebaseAuth firebaseAuth;
    LinearLayout  footer_container,next_donation;
    TextInputLayout dateInput,number_do_input;
    TextView text_donate_now,urgent,see_all,Username,wish,header_name,header_email,date_last,donation_num,leftDays,
    blood_urgent,location_urgent,hospital_urgent;
    ShapeableImageView shapeImage,header_image;
    BottomNavigationView bottom_nav;
    GridView grid_view;
    View header_view;
    SharedPreferences sharedPreferences;
    String nextDate;
    AdView adView;
    FrameLayout ad_view_container;
    MyAdapter myAdapter;
    HashMap<String,String> hashMap ;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation(R.id.Home);
        /* --------BaseActvity on on bottom navigation--------*/
        circled_menu = findViewById(R.id.circled_menu);
         nav_view = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.main);
        bottom_nav = findViewById(R.id.bottom_nav);
        text_donate_now = findViewById(R.id.text_donate_now);
        grid_view = findViewById(R.id.grid_view);
        next_donation = findViewById(R.id.next_donation);
        urgent = findViewById(R.id.urgent);
        location_urgent = findViewById(R.id.location_urgent);
        blood_urgent = findViewById(R.id.blood_urgent);
        hospital_urgent = findViewById(R.id.hospital_urgent);
        see_all = findViewById(R.id.see_all);
        shapeImage = findViewById(R.id.shapeImage);
        Username = findViewById(R.id.Username);
        wish = findViewById(R.id.wish);
        date_last = findViewById(R.id.date_last);
        donation_num = findViewById(R.id.donation_num);
        leftDays = findViewById(R.id.leftDays);
        ad_view_container = findViewById(R.id.ad_view_container);
        footer_container = nav_view.findViewById(R.id.footer_container);
        header_view = nav_view.getHeaderView(0);
        header_name = header_view.findViewById(R.id.header_name);
        header_image = header_view.findViewById(R.id.header_image);
        header_email = header_view.findViewById(R.id.header_email);
        firebaseAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences("BloodBank",MODE_PRIVATE);

        //=======================last donation date=========================================
        loadSavedDonationDate();
        array_request();
        askNotificationPermission();
        firebase_token();

                /* ---------------------------Login/Sign in----------------------------------*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        try {
            MyMethod.MY_KEY = MyMethod.encryptedData("sayba1122");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /*----------------notification message------------------*/
        Intent serviceIntent = new Intent(this, MessageListenerService.class);
        startService(serviceIntent);
        /* ---------------------------Logout nav----------------------------------*/
        if (footer_container != null) {
            // The include layout might be the last child
            View footerView = footer_container.getChildAt(footer_container.getChildCount() - 1);
            Button logoutBtn = footerView.findViewById(R.id.nav_footer_logout);
            if (logoutBtn != null) {
                logoutBtn.setOnClickListener(v -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", "");
                    editor.apply();
                    editor.clear();
                    startActivity(new Intent(MainActivity.this, FirstPage.class));
                    finish();
                });
            }
        }

        /* --------request see all text on click listener--------*/
        see_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Blood_Requests.class));
            }
        });
        /* --------badge on notification on bottom navigation--------*/
            updateNotificationBadge();



        /* --------drawer controller--------*/
       circled_menu.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               drawerLayout.openDrawer(GravityCompat.START);
           }
       });

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if(menuItem.getItemId() == R.id.MyProfile){
                    openActivity(Profile.class);
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (menuItem.getItemId()==R.id.Settings) {
                    openActivity(Settings.class);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }  else if (menuItem.getItemId()==R.id.Privacy) {
                    openActivity(PrivacyPolicy.class);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else if (menuItem.getItemId()==R.id.Feedback) {
                    openActivity(Feedback.class);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else if (menuItem.getItemId()==R.id.contact) {
                    openActivity(ContactUs.class);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else if (menuItem.getItemId()==R.id.About) {
                    openActivity(About.class);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return false;
            }
        });
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
        /* --------------------grid view -------------------------*/
        items();
   myAdapter = new MyAdapter();
       grid_view.setAdapter(myAdapter);
        //======================banner add===============================================
        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

// Create a new ad view.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_add_id));
// Request a large anchored adaptive banner with a width of 360.
        adView.setAdSize(new AdSize(320,48));

// Replace ad container with new ad view.
        ad_view_container.removeAllViews();
        ad_view_container.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    //======================banner add===============================================
    //-------------------------firebase authenticaion---------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        sharedPreferences = getSharedPreferences("BloodBank", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        if (email.isEmpty()) {
            startActivity(new Intent(MainActivity.this, FirstPage.class));
            finish();
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            Uri photoUri = firebaseUser.getPhotoUrl();

            String name   = firebaseUser.getDisplayName();
            String fEmail = firebaseUser.getEmail();

            if (name  != null) { Username.setText(name); header_name.setText(name); }
            if (fEmail != null) header_email.setText(fEmail);

            if (photoUri != null) {
                Glide.with(this).load(photoUri)
                        .placeholder(R.drawable.theme)
                        .error(R.drawable.theme)
                        .into(shapeImage);
                Glide.with(this).load(photoUri)
                        .placeholder(R.drawable.theme)
                        .error(R.drawable.theme)
                        .into(header_image);
            }
        }
        objectRequest();
        setGreeting();
    }
    private void loadImageFromString(String image, ImageView target) {
        if (image == null || image.isEmpty()) {
            Log.e("IMAGE_DEBUG", "Image is null or empty");
            target.setImageResource(R.drawable.theme);
            return;
        }

        if (image.startsWith("http") || image.startsWith("https")) {
            // ✅ URL image
            Log.d("IMAGE_DEBUG", "Loading URL image: " + image);
            Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.theme)
                    .error(R.drawable.theme)
                    .into(target);

        } else if (image.startsWith("/9j/") || image.startsWith("iVBOR")) {
            // ✅ Looks like Base64 — decode it
            Log.d("IMAGE_DEBUG", "Loading Base64 image");
            try {
                byte[] bytes = Base64.decode(image, Base64.DEFAULT);
                Glide.with(this)
                        .load(bytes)
                        .placeholder(R.drawable.theme)
                        .error(R.drawable.theme)
                        .into(target);
            } catch (Exception e) {
                Log.e("IMAGE_DEBUG", "Base64 decode failed: " + e.getMessage());
                target.setImageResource(R.drawable.theme);
            }

        } else {
            // ✅ Unknown format — try as URL first
            Log.w("IMAGE_DEBUG", "Unknown image format, trying as URL");
            Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.theme)
                    .error(R.drawable.theme)
                    .into(target);
        }
    }
    public class MyAdapter extends BaseAdapter{

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
            View myView = inflater.inflate(R.layout.grid_items,parent,false);

            ImageView image = myView.findViewById(R.id.image);
            TextView item_count = myView.findViewById(R.id.item_count);
            TextView title_of = myView.findViewById(R.id.title_of);
            LinearLayout grid_item = myView.findViewById(R.id.grid_item);

            hashMap = arrayList.get(position);
            String img= hashMap.get("image");
            String title= hashMap.get("title");
            String count= hashMap.get("item_count");

            item_count.setText(count);
            int imgRes = Integer.parseInt(img);
            image.setImageResource(imgRes);
            title_of.setText(title);


            /*==============grid view section===============*/
            grid_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (title.equals("Become a Donor")) {
                        SharedPreferences donor_sp = getSharedPreferences("DONOR_INFORMATION",MODE_PRIVATE);
                        String donor_phone = donor_sp.getString("phone","");

                        if(!donor_phone.isEmpty()){
                            Intent intent = new Intent(MainActivity.this, B_A_D_List.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Intent intent = new Intent(MainActivity.this, Questions.class);
                            startActivity(intent);
                            finish();
                        }
                    } else if (title.equals("Tips")) {
                        startActivity(new Intent(MainActivity.this, Tips.class));
                    } else if (title.equals("Update last date")) {
                        showNewTodoBottomSheet();
                    } else if (title.equals("Request for blood")) {
                        startActivity(new Intent(MainActivity.this, RequestForBlood.class));
                    } else if (title.contains("Search Donor")) {
                        startActivity(new Intent(MainActivity.this, SearchDonor.class));
                    } else if (title.equals("Blood Request")) {
                        startActivity(new Intent(MainActivity.this, Blood_Requests.class));
                    }
                }
            });
            return myView;
        }
    }

    /* --------grid view items method-------*/
    public void items(){
        arrayList.clear();
        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.becomeadonor));
        hashMap.put("title","Become a Donor");
        hashMap.put("item_count","");
        arrayList.add(hashMap);

        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.rquestforblood));
        hashMap.put("title","Request for blood");
        hashMap.put("item_count","");
        arrayList.add(hashMap);
        String dCount = sharedPreferences.getString("total_donors_count", "0");
        String rCount = sharedPreferences.getString("total_requests_count", "0");
        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.searchdonor));
        hashMap.put("title","Search Donor");
        hashMap.put("item_count",dCount);
        arrayList.add(hashMap);

        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.recentrequests));
        hashMap.put("title","Blood Request");
        hashMap.put("item_count",rCount);
        arrayList.add(hashMap);

        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.update));
        hashMap.put("title","Update last date");
        hashMap.put("item_count","");
        arrayList.add(hashMap);

        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.tips));
        hashMap.put("title","Tips");
        hashMap.put("item_count","");
        arrayList.add(hashMap);
    }
    //=======================update donation date=====================================
    private void showNewTodoBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_new_date, null);

        dialog.setContentView(view);
        dialog.show();
        TextInputEditText number_of_blood_given;
        MaterialAutoCompleteTextView set_date;
        set_date = view.findViewById(R.id.set_date);
        number_of_blood_given = view.findViewById(R.id.number_of_blood_given);
        number_do_input = view.findViewById(R.id.number_do_input);
        dateInput = view.findViewById(R.id.dateInput);

        dateInput.setEndIconOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select date")
                            .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                String date = datePicker.getHeaderText();
                set_date.setText(date);

                nextDate = getNextDonationDate(selection);


            });
        });

        ImageView close = view.findViewById(R.id.close);

        Button save = view.findViewById(R.id.save);


        close.setOnClickListener(v -> dialog.dismiss());

        save.setOnClickListener(v -> {
            String number = number_of_blood_given.getText().toString();
            String date = set_date.getText().toString();

            if (!date.isEmpty() && !number.isEmpty()) {
                date_last.setText(nextDate);
                donation_num.setText(number + "th");

                int daysLeft = getRemainingDays(nextDate);
                boolean isEligible = false;

                if (daysLeft <= 0) {
                    next_donation.setVisibility(View.GONE);
                    text_donate_now.setVisibility(View.VISIBLE);
                    isEligible = true;
                } else {
                    next_donation.setVisibility(View.VISIBLE);
                    text_donate_now.setVisibility(View.GONE);
                    leftDays.setText("Left " + daysLeft + " days");
                }

                sharedPreferences = getSharedPreferences("BloodBank", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("last_date", nextDate);
                editor.putString("total_donation", number);
                editor.putString("left_days", String.valueOf(daysLeft));
                editor.putString("dateInput", date);
                editor.putBoolean("is_eligible", isEligible);

                editor.apply();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //===============update date========================================
    private String getNextDonationDate(long selectedMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedMillis);
        calendar.add(Calendar.DAY_OF_MONTH, 90);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
    public int getRemainingDays(String nextDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        try {
            Date next = sdf.parse(nextDate);
            Date today = new Date();
            if(next == null) return  0;


            long diff = next.getTime() - today.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return (days > 0 ) ? (int) days : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    //=================bottom navigation activity controller=================================
    private void openActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
    //---------------------object request for getting data from php===========================
    private void objectRequest(){
        Log.d("API_TEST","Request Started");
        String url = "https://blood-bridge.org/blood_bridge/mainactivity.php";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key",MyMethod.encryptedData("sayba1122"));
            jsonObject.put("email",sharedPreferences.getString("email",""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {


                try {

                    String name = jsonObject.getString("Pname");
                    String image = jsonObject.getString("Pimage");
                    String email = jsonObject.getString("Pemail");
                    String password = jsonObject.getString("Ppassword");
                    String totalDonors = jsonObject.getString("total_donors");
                    String totalRequests = jsonObject.getString("total_requests");
                    Log.d("IMAGE_DEBUG", "Image value starts with: " +
                            (image.length() > 30 ? image.substring(0, 30) : image));

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.putString("email", email);
                    editor.putString("image", image);
                    editor.putString("pass",password);
                    editor.putString("total_donors_count", totalDonors);
                    editor.putString("total_requests_count", totalRequests);
                    editor.apply();

                    Username.setText(name);
                    header_name.setText(name);
                    header_email.setText(email);

                    items();

                    if (grid_view.getAdapter() != null) {
                        items();
                        myAdapter.notifyDataSetChanged();
                    }

                    loadImageFromString(image, shapeImage);
                    loadImageFromString(image, header_image);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("API_ERROR","Error: "+volleyError.toString());

                if (volleyError.networkResponse != null) {
                    String response = new String(volleyError.networkResponse.data);
                    Log.e("API_ERROR","Server response: "+response);
                }

                Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonObjectRequest);
    }
    // -----------------------set greetings---------------------------------------
    private void setGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good noon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }

        wish.setText(greeting);
    }
    //--------------------notificaion =========================================
    public void updateNotificationBadge() {

        boolean notiOn = sharedPreferences.getBoolean("notification", true);

        if (notiOn) {

            SharedPreferences prefs = getSharedPreferences("NOTIFICATION", MODE_PRIVATE);
            String json = prefs.getString("notifications", "[]");

            try {
                JSONArray array = new JSONArray(json);
                int count = array.length(); // number of notifications

                if (count > 0) {
                    BadgeDrawable badge = bottom_nav.getOrCreateBadge(R.id.noti);
                    badge.setVisible(true);
                    badge.setNumber(count);
                } else {
                    bottom_nav.removeBadge(R.id.noti);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            bottom_nav.removeBadge(R.id.noti);
        }
    }
    public void loadSavedDonationDate() {
        String lastDate = sharedPreferences.getString("last_date", "");
        String totalDonation = sharedPreferences.getString("total_donation", "0");
        boolean isEligible = sharedPreferences.getBoolean("is_eligible", false);

        if (!lastDate.isEmpty()) {
            date_last.setText(lastDate);
            donation_num.setText(totalDonation + "th");

            int daysLeft = getRemainingDays(lastDate);

            // Check if no days are left OR if the flag is true
            if (daysLeft <= 0 || isEligible) {
                next_donation.setVisibility(View.GONE);
                text_donate_now.setVisibility(View.VISIBLE);
            } else {
                next_donation.setVisibility(View.VISIBLE);
                text_donate_now.setVisibility(View.GONE);
                leftDays.setText("Left " + daysLeft + " days");
            }
        } else {
            // Default state if there is no donation date saved yet
            next_donation.setVisibility(View.VISIBLE);
            text_donate_now.setVisibility(View.GONE);
            leftDays.setText("No data");
        }
    }
    private Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            String lastDate = sharedPreferences.getString("last_date", "");
            if (!lastDate.isEmpty()) {
                int remainingDays = getRemainingDays(lastDate);

                if (remainingDays <= 0) {
                    next_donation.setVisibility(View.GONE);
                    text_donate_now.setVisibility(View.VISIBLE);

                    // Save the eligibility status
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_eligible", true);
                    editor.apply();
                } else {
                    next_donation.setVisibility(View.VISIBLE);
                    text_donate_now.setVisibility(View.GONE);
                    leftDays.setText("Left " + remainingDays + " days");
                }
            }

            // Schedule next update at midnight
            Calendar nextMidnight = Calendar.getInstance();
            nextMidnight.add(Calendar.DAY_OF_MONTH, 1);
            nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
            nextMidnight.set(Calendar.MINUTE, 0);
            nextMidnight.set(Calendar.SECOND, 0);
            nextMidnight.set(Calendar.MILLISECOND, 0);

            long delay = nextMidnight.getTimeInMillis() - System.currentTimeMillis();
            countdownHandler.postDelayed(this, delay);
        }
    };

 @Override
    protected void onResume() {
        super.onResume();
        countdownRunnable.run();
       // setSelectedNavItem(R.id.Home);
    }


    @Override
    protected void onPause() {
        super.onPause();
        countdownHandler.removeCallbacks(countdownRunnable);
    }
    public void array_request(){
        String url = "https://blood-bridge.org/blood_bridge/urgent.php";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.d("API_RESPONSE", jsonArray.toString());

                for(int i = 0; i < jsonArray.length(); i++){
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("request_name");
                        String blood_group = jsonObject.getString("bloodGroup");
                        String phone = jsonObject.getString("number");
                        String gender = jsonObject.getString("gender");
                        String location = jsonObject.getString("location");
                        String disease_type = jsonObject.getString("disease_type");
                        String unit = jsonObject.getString("unit");
                        String deadline = jsonObject.getString("deadline");
                        String hospital = jsonObject.getString("hospital");

                        SharedPreferences sp = getSharedPreferences("BLOOD_REQUEST", MODE_PRIVATE);
                        String lastHospital = sp.getString("last_hospital", "");

                        if(!hospital.equals(lastHospital)){
                            firebase_notification();

                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("last_hospital", hospital);
                            editor.apply();
                        }

                        blood_urgent.setText(blood_group);
                        location_urgent.setText("Address: " + location);
                        hospital_urgent.setText("Hospital: "+hospital);

                        Log.d("HOSPITAL","name of hos:"+hospital+location+blood_group);

                        SharedPreferences sharedPreferences = getSharedPreferences("BLOOD_REQUEST",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("request_name",name);
                        editor.putString("bloodGroup",blood_group);
                        editor.putString("number",phone);
                        editor.putString("gender",gender);
                        editor.putString("hospital",hospital);
                        editor.putString("location",location);
                        editor.putString("deadline",deadline);
                        editor.putString("disease_type",disease_type);
                        editor.putString("unit",unit);
                        editor.apply();
                        //firebase_notification();
                        urgent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(MainActivity.this, See_details.class));
                            }

                        });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }}}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){
            @Override
            public byte[] getBody() {
                JSONObject jsonObject = new JSONObject();
                SharedPreferences sp= getSharedPreferences("DONOR_INFORMATION",MODE_PRIVATE);
                try {
                    jsonObject.put("key",MyMethod.encryptedData("sayba1122"));
                    jsonObject.put("phone", MyMethod.encryptedData(sp.getString("phone","")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return jsonObject.toString().getBytes();
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonArrayRequest);
    }
    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                } else {
                }
            });

    private void askNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {

            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Notification")
                    .setMessage("Click 'OKAY' so that we can send you notifications")
                    .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    private void sendTokenToServer(String token){

        String url = "https://blood-bridge.org/blood_bridge/save_token.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {

            Log.d("server_response",response);

                },
                error -> {

                }){

            @Override
            protected Map<String,String> getParams(){

                Map<String,String> params = new HashMap<>();
                SharedPreferences sp= getSharedPreferences("DONOR_INFORMATION",MODE_PRIVATE);
                String phone = sp.getString("phone","");
                params.put("phone", phone);
                params.put("token", token);


                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
    public void firebase_token(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("firebase_token", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token

                        String token = task.getResult();
                        SharedPreferences sharedPreferences = getSharedPreferences("BLOOD_REQUEST",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token",token);
                        editor.apply();
                        // Log and toast
                        sendTokenToServer(token);
                    }
                });
    }
    public void firebase_notification(){
        String url = "https://blood-bridge.org/blood_bridge/firebase_notification.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map map = new HashMap<String, String>();

                SharedPreferences sharedPreferences = getSharedPreferences("BLOOD_REQUEST",MODE_PRIVATE);
                String group = sharedPreferences.getString("bloodGroup","");
                String hospital = sharedPreferences.getString("hospital","");
                String location = sharedPreferences.getString("location","");
                String token = sharedPreferences.getString("token","");

                if(token.isEmpty()){
                    Log.d("FCM","Token not ready yet");

                }

                map.put("blood_group",group);
                map.put("location",location);
                map.put("hospital",hospital);
                map.put("token",token );

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }
}


