package com.example.bloodbankt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import android.util.Base64;
import android.widget.Toast;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Register extends AppCompatActivity {
SignInButton googleSignIn;
CredentialManager credentialManager;
GetCredentialRequest credentialRequest;
FirebaseAuth firebaseAuth;
DatabaseReference dbref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://blood-bank-t-default-rtdb.firebaseio.com/users");
TextView login;
Button signIn;
TextInputEditText PassEdit,EmailEdit,UserEdit;
ShapeableImageView imageView;
ProgressBar progressBar;
ImageView choose_photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        googleSignIn = findViewById(R.id.googleSignIn);
        login = findViewById(R.id.login);
        signIn = findViewById(R.id.signIn);
        PassEdit = findViewById(R.id.PassEdit);
        EmailEdit = findViewById(R.id.EmailEdit);
        UserEdit = findViewById(R.id.UserEdit);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        choose_photo = findViewById(R.id.choose_photo);
        firebaseAuth = FirebaseAuth.getInstance();
//------------------login on click------------------------------------------------
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
        //------------------image picker------------------------------------------------

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
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        choose_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(Register.this)
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
        //------------------SignIn on click------------------------------------------------
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = UserEdit.getText().toString();
                String email = EmailEdit.getText().toString();
                String password = PassEdit.getText().toString();

                if (password.length() <6 ) {
                    Toast.makeText(Register.this, "Password must contains 6-digit", Toast.LENGTH_LONG).show();
                    return;
                }

                if (imageView.getDrawable() != null) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    String image = Base64.encodeToString(imageBytes,Base64.DEFAULT);

                progressBar.setVisibility(View.VISIBLE);
                //------------------Volley String request------------------------------------------------


                String url ="https://blood-bridge.org/blood_bridge/signup.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("LOGIn_DEBUG",s);
                        progressBar.setVisibility(View.GONE);


                        if (s.contains("SignIn Successful")) {
                            Toast.makeText(Register.this, s, Toast.LENGTH_LONG).show();

                            String uid = email.replace(".", ",");

                            dbref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        HashMap<String, String> map = new HashMap<>();
                                        map.put("name",  name);
                                        map.put("email", email);
                                        map.put("image", image);
                                        dbref.child(uid).setValue(map);
                                    }

                                    SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
                                    prefs.edit()
                                            .putString("email", email)
                                            .putString("username", name)
                                            .putString("image", image)
                                            .apply();

                                    startActivity(new Intent(Register.this, MainActivity.class));
                                    finish();
                                }
                                @Override public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }else{
                            new  AlertDialog.Builder(Register.this)
                                    .setTitle("Sign In")
                                    .setMessage(s)
                                    .create()
                                    .show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressBar.setVisibility(View.GONE);
                        new  AlertDialog.Builder(Register.this)
                                .setTitle("Sign In")
                                .setMessage(volleyError.getMessage())
                                .create()
                                .show();
                    }
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map myMap = new HashMap<String,String>();
                        myMap.put("name",name);
                        try {
                            myMap.put("email",MyMethod.encryptedData(EmailEdit.getText().toString()));
                            myMap.put("password",MyMethod.encryptedData(PassEdit.getText().toString()));
                            myMap.put("key",MyMethod.encryptedData("sayba1122"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        myMap.put("image",image);
                        myMap.put("type", "manually");
                        return myMap;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(Register.this);
                requestQueue.add(stringRequest);
                } else {
                    Toast.makeText(Register.this, "Please select image", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        credentialManager = CredentialManager.create(Register.this);

        GetSignInWithGoogleOption googleOption = new GetSignInWithGoogleOption.Builder(getString(R.string.google_login_client_id))
                .setNonce(java.util.UUID.randomUUID().toString())
                .build();

        credentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                        .build();

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGoogleLogin();
            }
        });
    }
    public void requestGoogleLogin(){
        credentialManager.getCredentialAsync(Register.this, credentialRequest, null, Runnable::run, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                Credential credential = getCredentialResponse.getCredential();
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                String IdToken = googleIdTokenCredential.getIdToken();

                AuthCredential authCredential = GoogleAuthProvider.getCredential(IdToken,null);
                firebaseAuth.signInWithCredential(authCredential)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                String name_f = user.getDisplayName();
                                String email_f = user.getEmail();
                                Uri image_f = user.getPhotoUrl();

                                saveGoogleUserToServer(name_f, email_f, String.valueOf(image_f));
                            }
                        });
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {

            }
        });
    }
    private void saveGoogleUserToServer(String name, String email, String photo) {

        String url = "https://blood-bridge.org/blood_bridge/signup.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                   new AlertDialog.Builder(Register.this)
                           .setTitle("Alert")
                           .setMessage(response)
                           .create()
                           .show();
                    if (response.contains("SignIn Successful")) {
                        Toast.makeText(Register.this,response,Toast.LENGTH_LONG).show();

                        String uid = email.replace(".", ",");

                        HashMap<String, String> map = new HashMap<>();
                        map.put("name",  name);
                        map.put("email", email);
                        map.put("image", photo);

                        dbref.child(uid).setValue(map);

                        SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
                        prefs.edit()
                                .putString("email", email)
                                .putString("name",  name)
                                .putString("image", photo)
                                .apply();

                        startActivity(new Intent(Register.this, ResetPass.class));
                        finish();
                    }
                },
                error -> Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("type", "google");
                map.put("name", name);
                map.put("image", photo);
                try {
                    map.put("email", MyMethod.encryptedData(email));
                    map.put("key", MyMethod.encryptedData("sayba1122"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}