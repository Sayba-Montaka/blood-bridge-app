package com.example.bloodbankt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EditProfile extends AppCompatActivity {
    TextInputEditText user_name_edit,email_edit,pass_edit;
    Button update;
    ShapeableImageView P_A_image;
    ImageView choose_photo;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        update = findViewById(R.id.update);
        user_name_edit= findViewById(R.id.user_name_edit);
        email_edit= findViewById(R.id.email_edit);
        pass_edit= findViewById(R.id.pass_edit);
        P_A_image= findViewById(R.id.P_A_image);
        choose_photo= findViewById(R.id.choose_photo);
        progressBar= findViewById(R.id.progressBar);


        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode() == Activity.RESULT_OK){
                            Intent intent = o.getData();
                            Uri uri = intent.getData();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                                P_A_image.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
        choose_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(EditProfile.this)
                        .maxResultSize(1000,1000)
                        .compress(1024)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                launcher.launch(intent);
                                return null;
                            }
                        });
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameEdit = user_name_edit.getText().toString();
                String emailEdit = email_edit.getText().toString();
                String passEdit = pass_edit.getText().toString();
                progressBar.setVisibility(View.VISIBLE);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) P_A_image.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                String image = Base64.encodeToString(imageBytes,Base64.DEFAULT);

                String url = "https://blood-bridge.org/blood_bridge/edit_profile.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditProfile.this,s,Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfile.this,Profile.class));
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(EditProfile.this,volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map map = new HashMap<String,String>();
                        map.put("username",usernameEdit);
                        map.put("image",image);

                        try {
                            map.put("email",MyMethod.encryptedData(emailEdit));
                            map.put("pass",MyMethod.encryptedData(passEdit));
                            map.put("key",MyMethod.encryptedData("sayba1122"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return map;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(EditProfile.this);
                requestQueue.add(stringRequest);
            }
        });

    }
}