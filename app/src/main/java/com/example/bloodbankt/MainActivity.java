package com.example.bloodbankt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends BaseActivity {
    DrawerLayout drawerLayout;
    ImageButton circled_menu;
    NavigationView nav_view;
    TextView urgent,see_all;
    BottomNavigationView bottom_nav;
    GridView grid_view;
    SharedPreferences sharedPreferences;
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

        circled_menu = findViewById(R.id.circled_menu);
         nav_view = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.main);
        bottom_nav = findViewById(R.id.bottom_nav);
        grid_view = findViewById(R.id.grid_view);
        urgent = findViewById(R.id.urgent);
        see_all = findViewById(R.id.see_all);
        /* ---------------------------Login/Sign in----------------------------------*/
        sharedPreferences = getSharedPreferences("BloodBank",MODE_PRIVATE);
        String email =sharedPreferences.getString("email","");
        if(email.length() <= 0){
            startActivity(new Intent(MainActivity.this, FirstPage.class));
            finish();
        }
        /* --------request see all text on click listener--------*/
        see_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Blood_Requests.class));
            }
        });
        /* --------badge on notification on bottom navigation--------*/
       bottom_nav.getOrCreateBadge(R.id.noti).setNumber(100);
        /* --------BaseActvity on on bottom navigation--------*/
        setupBottomNavigation(R.id.Home);
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
       MyAdapter myAdapter = new MyAdapter();
       grid_view.setAdapter(myAdapter);

    }
    /* --------my Adapter--------*/

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
            TextView become_a_donor = myView.findViewById(R.id.become_a_donor);
            TextView title_of = myView.findViewById(R.id.title_of);
            LinearLayout grid_item = myView.findViewById(R.id.grid_item);

            hashMap = arrayList.get(position);
            String img= hashMap.get("image");
            String title= hashMap.get("title");
            String count= hashMap.get("item_count");

            int imgRes = Integer.parseInt(img);
            image.setImageResource(imgRes);
            title_of.setText(title);
            become_a_donor.setText(count);

            /*==============grid view section===============*/
            grid_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (title.equals("Become a Donor")) {
                        Intent intent = new Intent(MainActivity.this, Questions.class);
                        startActivity(intent);
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
        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.becomeadonor));
        hashMap.put("title","Become a Donor");
        hashMap.put("item_count","120");
        arrayList.add(hashMap);

        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.rquestforblood));
        hashMap.put("title","Request for blood");
        hashMap.put("item_count","120");
        arrayList.add(hashMap);

        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.searchdonor));
        hashMap.put("title","Search Donor");
        hashMap.put("item_count","120");
        arrayList.add(hashMap);

        hashMap = new HashMap<>();
        hashMap.put("image", String.valueOf(R.drawable.recentrequests));
        hashMap.put("title","Blood Request");
        hashMap.put("item_count","120");
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
    private void showNewTodoBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_new_date, null);

        dialog.setContentView(view);
        dialog.show();

        ImageView close = view.findViewById(R.id.close);
        Button save = view.findViewById(R.id.save);

        close.setOnClickListener(v -> dialog.dismiss());

        save.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }
    private void openActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
