package com.medicare.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.medicare.app.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    
    private DatabaseHelper databaseHelper;
    private CardView dashboardCard, addMedicineCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
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
            int count = databaseHelper.getMedicineCount();
            // Update UI with medicine count if needed
        } catch (Exception e) {
            Toast.makeText(this, "Error loading medicine count", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}