package com.medicare.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import com.medicare.app.database.DatabaseHelper;
import com.medicare.app.models.User;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvError;
    
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LoginActivity onCreate() called");
        
        // Initialize SharedPreferences first
        sharedPreferences = getSharedPreferences("MediCarePrefs", MODE_PRIVATE);
        
        // Always clear session on app start to force login every time
        clearSession();
        
        Log.d(TAG, "LoginActivity starting - user must login every time");
        
        Log.d(TAG, "Setting content view to login layout");
        setContentView(R.layout.activity_login);

        Log.d(TAG, "Initializing views");
        initializeViews();
        initializeDatabase();
        setupClickListeners();
        
        Log.d(TAG, "LoginActivity setup complete");
    }

    private void initializeViews() {
        Log.d(TAG, "Finding views by ID");
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);
        tvError = findViewById(R.id.tv_error);
        
        Log.d(TAG, "Views initialized - etEmail: " + (etEmail != null) + 
                   ", btnLogin: " + (btnLogin != null) + 
                   ", tvSignup: " + (tvSignup != null));
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void attemptLogin() {
        hideError();
        
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing In...");

        // Simulate network delay
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); // Simulate network delay
                    
                    User user = databaseHelper.authenticateUser(email, password);
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Sign In");
                            
                            if (user != null) {
                                Log.d(TAG, "Login successful for user: " + user.getEmail() + " with ID: " + user.getId());
                                
                                // Save minimal session data (just for current session, not persistent)
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong("user_id", user.getId());
                                editor.putString("user_email", user.getEmail());
                                editor.putString("user_name", user.getFullName());
                                // Don't save is_logged_in to force login every time
                                editor.apply();
                                
                                Log.d(TAG, "User session saved: user_id=" + user.getId() + ", email=" + user.getEmail());

                                Toast.makeText(LoginActivity.this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                                
                                navigateToMain();
                            } else {
                                showError("Invalid email or password");
                            }
                        }
                    });
                    
                } catch (InterruptedException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Sign In");
                            showError("Login failed. Please try again.");
                        }
                    });
                }
            }
        }).start();
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}