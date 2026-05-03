package com.example.bloodbankt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
TextView signUp,forgot_pass;
TextInputEditText login_email,login_password;
Button login_button;
SignInButton googleSignIn;
CredentialManager credentialManager;
GetCredentialRequest credentialRequest;
FirebaseAuth firebaseAuth;
ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        signUp = findViewById(R.id.signUp);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);
        googleSignIn = findViewById(R.id.sign_google);
        progressBar = findViewById(R.id.progressBar);
        forgot_pass = findViewById(R.id.forgot_pass);
        firebaseAuth = FirebaseAuth.getInstance();
//-------------------forgot pass on click----------------------------------------------
        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,ForgotPassEmail.class));
            }
        });
//-------------------login manually--------------------------------------------------------
      login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                String url ="https://googix.xyz/blood_bridge/login.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressBar.setVisibility(View.GONE);
                        if(s.contains("Valid Login")){
                            SharedPreferences sharedPreferences = getSharedPreferences("BloodBank",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email",login_email.getText().toString());
                            editor.apply();

                            startActivity(new Intent(Login.this,MainActivity.class));
                            finish();
                        }else{
                            new  AlertDialog.Builder(Login.this)
                                    .setTitle("Login In")
                                    .setMessage(s)
                                    .create()
                                    .show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressBar.setVisibility(View.GONE);
                        new  AlertDialog.Builder(Login.this)
                                .setTitle("Login In")
                                .setMessage(volleyError.getMessage())
                                .create()
                                .show();
                    }
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map myMap = new HashMap<String,String>();
                        try {
                            myMap.put("email",MyMethod.encryptedData(login_email.getText().toString()));
                            myMap.put("password",MyMethod.encryptedData( login_password.getText().toString()));
                            myMap.put("key",MyMethod.encryptedData("sayba1122"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        return myMap;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
                requestQueue.add(stringRequest);
            }
        });
//---------------------------google sign in button----------------------------------------------
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
        credentialManager = CredentialManager.create(Login.this);

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
        credentialManager.getCredentialAsync(Login.this, credentialRequest, null, Runnable::run, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
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
                            }});}

            @Override
            public void onError(@NonNull GetCredentialException e) {
                Toast.makeText(Login.this, "Firebase Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void saveGoogleUserToServer(String name, String email, String photo) {

        String url = "https://googix.xyz/blood_bridge/signup.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.contains("SignIn Successful")) {
                        SharedPreferences prefs = getSharedPreferences("BloodBank", MODE_PRIVATE);
                        prefs.edit()
                                .putString("email", email)
                                .putString("name", name)
                                .putString("image", photo)
                                .apply();

                        startActivity(new Intent(Login.this, MainActivity.class));
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