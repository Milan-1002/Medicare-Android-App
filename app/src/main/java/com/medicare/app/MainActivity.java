package com.medicare.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.medicare.app.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    
    private DatabaseHelper databaseHelper;
    private CardView dashboardCard, addMedicineCard;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MediCarePrefs", MODE_PRIVATE);
        
        setContentView(R.layout.activity_main);
        
        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MediCare");
        }
        
        initializeViews();
        initializeDatabase();
        setUpClickListeners();
    }

    private void initializeViews() {
        dashboardCard = findViewById(R.id.card_dashboard);
        addMedicineCard = findViewById(R.id.card_add_medicine);
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
        // Initialize database on first run
        databaseHelper.getReadableDatabase();
    }

    private void setUpClickListeners() {
        dashboardCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            }
        });

        addMedicineCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddMedicineActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh any data if needed
        updateMedicineCount();
    }

    private void updateMedicineCount() {
        try {
            long currentUserId = sharedPreferences.getLong("user_id", -1);
            if (currentUserId != -1) {
                int count = databaseHelper.getMedicineCount(currentUserId);
                // Update UI with medicine count if needed
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading medicine count", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear user session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

}