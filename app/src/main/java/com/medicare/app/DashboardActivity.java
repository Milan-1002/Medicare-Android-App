package com.medicare.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.medicare.app.adapters.MedicineAdapter;
import com.medicare.app.database.DatabaseHelper;
import com.medicare.app.models.Medicine;
import com.medicare.app.utils.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements MedicineAdapter.OnMedicineClickListener {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private MedicineAdapter medicineAdapter;
    private List<Medicine> medicineList;
    private FloatingActionButton fabAddMedicine;
    
    private TextView tvGreeting, tvActiveMedicines, tvTodaysMedicines, tvRemindersToday, tvAdherenceRate;
    private TextView tvNoMedicines, tvNextReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        setupClickListeners();
        loadMedicines();
        updateStats();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_medicines);
        fabAddMedicine = findViewById(R.id.fab_add_medicine);
        tvGreeting = findViewById(R.id.tv_greeting);
        tvActiveMedicines = findViewById(R.id.tv_active_medicines);
        tvTodaysMedicines = findViewById(R.id.tv_todays_medicines);
        tvRemindersToday = findViewById(R.id.tv_reminders_today);
        tvAdherenceRate = findViewById(R.id.tv_adherence_rate);
        tvNoMedicines = findViewById(R.id.tv_no_medicines);
        tvNextReminder = findViewById(R.id.tv_next_reminder);
        
        // Set greeting based on time of day
        setGreeting();
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        medicineList = new ArrayList<>();
        medicineAdapter = new MedicineAdapter(this, medicineList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(medicineAdapter);
    }

    private void setupClickListeners() {
        fabAddMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, AddMedicineActivity.class));
            }
        });
    }

    private void setGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        
        tvGreeting.setText(greeting);
    }

    private void loadMedicines() {
        try {
            medicineList.clear();
            medicineList.addAll(databaseHelper.getActiveMedicines());
            medicineAdapter.notifyDataSetChanged();
            
            if (medicineList.isEmpty()) {
                tvNoMedicines.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoMedicines.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading medicines", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStats() {
        try {
            int activeMedicines = medicineList.size();
            int todaysMedicines = getTodaysMedicineCount();
            int remindersToday = getTodaysReminderCount();
            
            tvActiveMedicines.setText(String.valueOf(activeMedicines));
            tvTodaysMedicines.setText(String.valueOf(todaysMedicines));
            tvRemindersToday.setText(String.valueOf(remindersToday));
            tvAdherenceRate.setText("98%"); // Placeholder
            
            updateNextReminder();
        } catch (Exception e) {
            Toast.makeText(this, "Error updating stats", Toast.LENGTH_SHORT).show();
        }
    }

    private int getTodaysMedicineCount() {
        // Count medicines that have reminders today
        int count = 0;
        for (Medicine medicine : medicineList) {
            if (medicine.getTimes() != null && !medicine.getTimes().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private int getTodaysReminderCount() {
        // Count total reminders for today
        int count = 0;
        for (Medicine medicine : medicineList) {
            if (medicine.getTimes() != null) {
                count += medicine.getTimes().size();
            }
        }
        return count;
    }

    private void updateNextReminder() {
        String nextReminder = getNextReminderTime();
        if (nextReminder != null) {
            tvNextReminder.setText("Next reminder: " + nextReminder);
            tvNextReminder.setVisibility(View.VISIBLE);
        } else {
            tvNextReminder.setVisibility(View.GONE);
        }
    }

    private String getNextReminderTime() {
        // Find the next reminder time from all medicines
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String nextTime = null;
        
        for (Medicine medicine : medicineList) {
            if (medicine.getTimes() != null) {
                for (String time : medicine.getTimes()) {
                    if (time.compareTo(currentTime) > 0) {
                        if (nextTime == null || time.compareTo(nextTime) < 0) {
                            nextTime = time;
                        }
                    }
                }
            }
        }
        
        return nextTime;
    }

    @Override
    public void onMedicineClick(Medicine medicine) {
        // Handle medicine click - could open details or edit
        Toast.makeText(this, "Clicked: " + medicine.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Medicine medicine) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Are you sure you want to delete " + medicine.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        ReminderScheduler.cancelReminder(this, medicine);
                        databaseHelper.deleteMedicine(medicine.getId());
                        loadMedicines();
                        updateStats();
                        Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting medicine", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onInfoClick(Medicine medicine) {
        // Show medicine info dialog with AI integration
        showMedicineInfoDialog(medicine);
    }

    private void showMedicineInfoDialog(Medicine medicine) {
        // Create a simple info dialog for now
        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(medicine.getName()).append("\n");
        info.append("Dosage: ").append(medicine.getDosage()).append("\n");
        info.append("Frequency: ").append(medicine.getFrequencyDisplay()).append("\n");
        info.append("Times: ").append(medicine.getFormattedTimes()).append("\n");
        info.append("Type: ").append(medicine.getMedicineTypeDisplay()).append("\n");
        if (medicine.getNotes() != null && !medicine.getNotes().isEmpty()) {
            info.append("Notes: ").append(medicine.getNotes()).append("\n");
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Medicine Information")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicines();
        updateStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}