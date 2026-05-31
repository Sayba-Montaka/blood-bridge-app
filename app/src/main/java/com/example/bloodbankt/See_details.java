package com.example.bloodbankt;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class See_details extends AppCompatActivity implements OnMapReadyCallback {
    TextView name_of_request,email_of_request,phone_of_request,group_of_request,gender_of_request,disease_of_request,
            deadline_of_request,unit_of_blood,location_of_request,hoapital_of_request,call;
    ImageView backArrow,select_person;
    GoogleMap google_map;
    SupportMapFragment mapFragment;
    LatLng donorLatLng;
    FusedLocationProviderClient fusedLocationProviderClient;
    FrameLayout map;
    String location,hospital,name,blood_group;

    ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fine   = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                if ((fine != null && fine) || (coarse != null && coarse)) {
                    getDonorLocation();
                } else {
                    Toast.makeText(this,
                            "Location permission denied — showing seeker location only",
                            Toast.LENGTH_SHORT).show();
                    showSeekerLocation();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_see_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backArrow = findViewById(R.id.backArrow);
        location_of_request = findViewById(R.id.location_of_request);
        gender_of_request = findViewById(R.id.gender_of_request);
        select_person = findViewById(R.id.select_person);
        phone_of_request = findViewById(R.id.phone_of_request);
        email_of_request = findViewById(R.id.email_of_request);
        group_of_request = findViewById(R.id.group_of_request);
        name_of_request = findViewById(R.id.name_of_request);
        disease_of_request = findViewById(R.id.disease_of_request);
        deadline_of_request = findViewById(R.id.deadline_of_request);
        unit_of_blood = findViewById(R.id.unit_of_blood);
        hoapital_of_request = findViewById(R.id.hoapital_of_request);
        call = findViewById(R.id.call);

      mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(See_details.this);
        map = findViewById(R.id.map);

        SharedPreferences sharedPreferences = getSharedPreferences("BLOOD_REQUEST",MODE_PRIVATE);

        name = sharedPreferences.getString("request_name","");
        String phone = sharedPreferences.getString("number","");
        String email = sharedPreferences.getString("email","");
        String Gender = sharedPreferences.getString("gender","");
        blood_group = sharedPreferences.getString("bloodGroup","");
        hospital = sharedPreferences.getString("hospital","");
        String disease = sharedPreferences.getString("disease_type","");
        String unit = sharedPreferences.getString("unit","");
        String deadline = sharedPreferences.getString("deadline","");
        location = sharedPreferences.getString("location","");

        name_of_request.setText(name);
        phone_of_request.setText(phone);
        email_of_request.setText(email);
        gender_of_request.setText(Gender);
        group_of_request.setText(blood_group);
        location_of_request.setText(location);
        deadline_of_request.setText(deadline);
        hoapital_of_request.setText(hospital);
        disease_of_request.setText(disease);
        unit_of_blood.setText(unit);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(See_details.this,Blood_Requests.class));
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = phone;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel: "+number));
                startActivity(intent);
            }
        });
      SharedPreferences preferences = getSharedPreferences("BloodBank",MODE_PRIVATE);
      String image = preferences.getString("image","");
        select_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(See_details.this, Chat.class);
                intent.putExtra("email",email);
                intent.putExtra("name",name);
                intent.putExtra("image",image);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        google_map = googleMap;
        google_map.getUiSettings().setZoomControlsEnabled(true);
        google_map.getUiSettings().setMyLocationButtonEnabled(true);

        // ── Request donor location ─────────────────────────────────────────────
        if (hasLocationPermission()) {
            getDonorLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    // ── Check permission ───────────────────────────────────────────────────────
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    // ── Get donor's current GPS location ──────────────────────────────────────
    private void getDonorLocation() {
        if (!hasLocationPermission()) return;

        try {
            fusedLocationProviderClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            donorLatLng = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude());
                        }
                        showSeekerLocation();
                    })
                    .addOnFailureListener(e -> {
                        showSeekerLocation();
                    });
        } catch (SecurityException e) {
            showSeekerLocation();
        }
    }

    // ── Find seeker location by address ───────────────────────────────────────
    private void showSeekerLocation() {
        new Thread(() -> {
            LatLng seekerLatLng = null;
            String resolvedAddress = "";
            boolean exactMatch = false;

            String fullAddress = location + ", " + hospital + ", "+ ", Bangladesh";
            LatLng result = geocodeAddress(fullAddress);

            if (result != null) {
                seekerLatLng    = result;
                resolvedAddress = fullAddress;
                exactMatch      = true;
            }

            // ── Try 3: Just district + division (fallback) ────────────────────
            if (seekerLatLng == null) {
                String address3 = location + ", Bangladesh";
                result = geocodeAddress(address3);
                if (result != null) {
                    seekerLatLng    = result;
                    resolvedAddress = location + " area (hospital not found on map)";
                    exactMatch      = false;
                }
            }

            final LatLng finalSeekerLatLng = seekerLatLng;
            final String finalAddress      = resolvedAddress;
            final boolean finalExact       = exactMatch;

            runOnUiThread(() -> plotOnMap(finalSeekerLatLng, finalAddress, finalExact));

        }).start();
    }

    // ── Geocode an address string → LatLng ────────────────────────────────────
    private LatLng geocodeAddress(String address) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                return new LatLng(addr.getLatitude(), addr.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Plot seeker + donor on map ─────────────────────────────────────────────
      private void plotOnMap(LatLng seekerLatLng, String resolvedAddress, boolean exactMatch) {
        if (google_map == null) return;

        google_map.clear();

        if (seekerLatLng != null) {

            // ── Seeker marker ──────────────────────────────────────────────────
            String markerTitle = name + " needs " + blood_group;
            String markerSnippet;

            if (exactMatch) {
                markerSnippet = "📍 " + hospital;
            } else {
                markerSnippet = "⚠️ Hospital not on map. Showing: " + resolvedAddress;
            }

            google_map.addMarker(new MarkerOptions()
                    .position(seekerLatLng)
                    .title(markerTitle)
                    .snippet(markerSnippet)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // ── Donor marker (blue) ────────────────────────────────────────────
            if (donorLatLng != null) {
                google_map.addMarker(new MarkerOptions()
                        .position(donorLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                // ── Show both markers in view ──────────────────────────────────
                com.google.android.gms.maps.model.LatLngBounds.Builder builder =
                        new com.google.android.gms.maps.model.LatLngBounds.Builder();
                builder.include(seekerLatLng);
                builder.include(donorLatLng);
                google_map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        builder.build(), 150));

            } else {
                // Only seeker location available
                google_map.animateCamera(CameraUpdateFactory.newLatLngZoom(seekerLatLng, 14f));
            }

        } else {
            Toast.makeText(this,
                    "Location not found — try Google Maps",
                    Toast.LENGTH_LONG).show();
            }
        }
    }