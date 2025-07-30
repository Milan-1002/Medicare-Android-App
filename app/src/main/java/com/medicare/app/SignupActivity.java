package com.medicare.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import com.medicare.app.database.DatabaseHelper;
import com.medicare.app.models.User;

public class SignupActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLogin, tvError;
    
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeViews();
        initializeDatabase();
        setupClickListeners();
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);
        tvError = findViewById(R.id.tv_error);
        
        sharedPreferences = getSharedPreferences("MediCarePrefs", MODE_PRIVATE);
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupClickListeners() {
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignup();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login
            }
        });
    }

    private void attemptSignup() {
        hideError();
        
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(firstName, lastName, email, password, confirmPassword)) {
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating Account...");

        // Simulate network delay
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); // Simulate network delay
                    
                    // Check if email already exists
                    boolean emailExists = databaseHelper.emailExists(email);
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (emailExists) {
                                btnSignup.setEnabled(true);
                                btnSignup.setText("Create Account");
                                showError("Email already exists. Please use a different email.");
                                return;
                            }
                            
                            // Create new user
                            User newUser = new User(email, password, firstName, lastName);
                            long userId = databaseHelper.insertUser(newUser);
                            
                            if (userId > 0) {
                                newUser.setId(userId);
                                
                                Toast.makeText(SignupActivity.this, "Account created successfully! Please login with your credentials.", Toast.LENGTH_LONG).show();
                                
                                // Redirect to login page instead of auto-login
                                finish(); // This will take user back to LoginActivity
                            } else {
                                btnSignup.setEnabled(true);
                                btnSignup.setText("Create Account");
                                showError("Failed to create account. Please try again.");
                            }
                        }
                    });
                    
                } catch (InterruptedException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSignup.setEnabled(true);
                            btnSignup.setText("Create Account");
                            showError("Signup failed. Please try again.");
                        }
                    });
                }
            }
        }).start();
    }

    private boolean validateInput(String firstName, String lastName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }

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

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
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

}