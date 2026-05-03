package com.example.bloodbankt;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.OutputStream;

public class Blood_Donation_history extends AppCompatActivity {
TextView num_of_donation,days_left,next_donation_date,last_donation_date;
CardView for_download;
Button download_button;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_donation_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        last_donation_date = findViewById(R.id.last_donation_date);
        next_donation_date = findViewById(R.id.next_donation_date);
        days_left = findViewById(R.id.days_left);
        for_download = findViewById(R.id.for_download);
        download_button = findViewById(R.id.download_button);
        num_of_donation = findViewById(R.id.num_of_donation);
        sharedPreferences = getSharedPreferences("BloodBank",MODE_PRIVATE);

        String l_d_d = sharedPreferences.getString("dateInput","");
        String n_d_d = sharedPreferences.getString("last_date","");
        String days = sharedPreferences.getString("left_days","");
        String number= sharedPreferences.getString("total_donation","");
        Log.d("DONATION_INFO",l_d_d+n_d_d+days+number);
        last_donation_date.setText(l_d_d);
        next_donation_date.setText(n_d_d);
        days_left.setText(days +" days");
        num_of_donation.setText(number+" th");

        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download_button.setText("Congratulation, You have done a great work");
                captureAndSaveLayout();
            }
        });
    }
    private void captureAndSaveLayout() {

        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.SHUTTER_CLICK);

        Bitmap bitmap = Bitmap.createBitmap(for_download.getWidth(), for_download.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Drawable bgDrawable = for_download.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE); // fallback if no background
        }
        for_download.draw(canvas);
        saveImageToGallery(bitmap);
    }
    private void saveImageToGallery(Bitmap bitmap) {
        String filename = "donation_history_" + System.currentTimeMillis() + ".png";

        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                android.content.ContentResolver resolver = getContentResolver();
                android.content.ContentValues contentValues = new android.content.ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/DonationHistory");
                android.net.Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                // For Android 9 and below
                String imagesDir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DCIM
                ).toString();
                java.io.File image = new java.io.File(imagesDir, filename);
                fos = new java.io.FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "Saved to gallery!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}