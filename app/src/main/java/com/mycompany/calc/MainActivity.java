package com.mycompany.calc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private EditText inputUsername, inputPassword;
    private Button btnAction;
    private TextView tvToggle;
    private boolean isSignupMode = false; // Default: Login mode
    private final String SERVER_URL = "https://messgram-k4vn.onrender.com";
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Agar pehle se logged-in hai toh direct MainActivity par bhej do
        SharedPreferences prefs = getSharedPreferences("MessgramPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        btnAction = findViewById(R.id.btnLogin);
        tvToggle = findViewById(R.id.tvToggle);

        // Toggle between Login and Signup mode
        tvToggle.setOnClickListener(v -> {
            isSignupMode = !isSignupMode;
            btnAction.setText(isSignupMode ? "Sign Up" : "Log In");
            tvToggle.setText(isSignupMode ? "Already have an account? Login" : "Don't have an account? Sign Up");
        });

        btnAction.setOnClickListener(v -> authenticateUser());
    }

    private void authenticateUser() {
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String endpoint = isSignupMode ? "/signup" : "/login";

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(
                jsonBody.toString(), 
                MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url(SERVER_URL + endpoint)
                .post(body)
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resStr = response.body().string();
                    try {
                        JSONObject resJson = new JSONObject(resStr);
                        if (response.isSuccessful()) {
                            if (!isSignupMode) {
                                // Login Success: Save session details & token
                                SharedPreferences.Editor editor = getSharedPreferences("MessgramPrefs", MODE_PRIVATE).edit();
                                editor.putString("username", username);
                                editor.putBoolean("isLoggedIn", true);
                                editor.putString("token", resJson.optString("token", ""));
                                editor.apply();

                                runOnUiThread(() -> {
                                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                });
                            } else {
                                // Signup Success: Switch back to Login mode automatically
                                runOnUiThread(() -> {
                                    Toast.makeText(LoginActivity.this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show();
                                    isSignupMode = false;
                                    btnAction.setText("Log In");
                                    tvToggle.setText("Don't have an account? Sign Up");
                                });
                            }
                        } else {
                            String err = resJson.optString("error", "Authentication failed");
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, err, Toast.LENGTH_LONG).show());
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Parsing Error", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
                                       }
