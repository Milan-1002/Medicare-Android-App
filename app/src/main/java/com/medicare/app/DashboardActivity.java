package com.medicare.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.medicare.app.adapters.MedicineAdapter;
import com.medicare.app.database.DatabaseHelper;
import com.medicare.app.models.Medicine;
import com.medicare.app.services.MedicineInfoService;
import com.medicare.app.utils.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements MedicineAdapter.OnMedicineClickListener {

    private DatabaseHelper databaseHelper;
    private MedicineInfoService medicineInfoService;
    private RecyclerView recyclerView;
    private MedicineAdapter medicineAdapter;
    private List<Medicine> medicineList;
    private FloatingActionButton fabAddMedicine;
    
    private TextView tvGreeting, tvActiveMedicines, tvTodaysMedicines, tvRemindersToday, tvAdherenceRate;
    private TextView tvNoMedicines, tvNextReminder;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_dashboard);
            
            // Setup Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Dashboard");
            }
            
            initializeViews();
            initializeDatabase();
            setupRecyclerView();
            setupClickListeners();
            loadMedicines();
            updateStats();
        } catch (Exception e) {
            Toast.makeText(this, "Dashboard failed to load: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Navigate back to MainActivity instead of crashing
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initializeViews() {
        try {
            recyclerView = findViewById(R.id.recycler_medicines);
            fabAddMedicine = findViewById(R.id.fab_add_medicine);
            tvGreeting = findViewById(R.id.tv_greeting);
            tvActiveMedicines = findViewById(R.id.tv_active_medicines);
            tvTodaysMedicines = findViewById(R.id.tv_todays_medicines);
            tvRemindersToday = findViewById(R.id.tv_reminders_today);
            tvAdherenceRate = findViewById(R.id.tv_adherence_rate);
            tvNoMedicines = findViewById(R.id.tv_no_medicines);
            tvNextReminder = findViewById(R.id.tv_next_reminder);
            
            // Check for null views
            if (recyclerView == null || fabAddMedicine == null || tvGreeting == null) {
                Toast.makeText(this, "Layout initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Set greeting based on time of day
            setGreeting();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
        medicineInfoService = new MedicineInfoService();
        sharedPreferences = getSharedPreferences("MediCarePrefs", MODE_PRIVATE);
    }

    private void setupRecyclerView() {
        try {
            if (recyclerView == null) {
                Toast.makeText(this, "RecyclerView not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            medicineList = new ArrayList<>();
            medicineAdapter = new MedicineAdapter(this, medicineList, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(medicineAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up medicine list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            if (fabAddMedicine != null) {
                fabAddMedicine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(DashboardActivity.this, AddMedicineActivity.class));
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up click listeners", Toast.LENGTH_SHORT).show();
        }
    }

    private void setGreeting() {
        try {
            if (tvGreeting == null) return;
            
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
            
            // Add user name if available
            if (sharedPreferences != null) {
                String userName = sharedPreferences.getString("user_name", "");
                if (!userName.isEmpty()) {
                    String[] nameParts = userName.split(" ");
                    if (nameParts.length > 0) {
                        greeting += ", " + nameParts[0]; // Use first name only
                    }
                }
            }
            
            tvGreeting.setText(greeting);
        } catch (Exception e) {
            // Fallback to default greeting
            if (tvGreeting != null) {
                tvGreeting.setText("Welcome");
            }
        }
    }

    private void loadMedicines() {
        try {
            if (sharedPreferences == null) {
                Toast.makeText(this, "Error: User session not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            long currentUserId = sharedPreferences.getLong("user_id", -1);
            Log.d("DashboardActivity", "Loading medicines for user_id: " + currentUserId);
            
            if (currentUserId == -1) {
                Log.e("DashboardActivity", "No user_id found in SharedPreferences");
                Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            
            medicineList.clear();
            List<Medicine> userMedicines = databaseHelper.getActiveMedicines(currentUserId);
            medicineList.addAll(userMedicines);
            Log.d("DashboardActivity", "Loaded " + userMedicines.size() + " medicines for user_id: " + currentUserId);
            if (medicineAdapter != null) {
                medicineAdapter.notifyDataSetChanged();
            }
            
            if (tvNoMedicines != null && recyclerView != null) {
                if (medicineList.isEmpty()) {
                    tvNoMedicines.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoMedicines.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading medicines: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStats() {
        try {
            if (medicineList == null) {
                medicineList = new ArrayList<>();
            }
            
            Log.d("DashboardActivity", "Updating stats with " + medicineList.size() + " medicines");
            
            int activeMedicines = medicineList.size();
            int todaysMedicines = getTodaysMedicineCount();
            int remindersToday = getTodaysReminderCount();
            String adherenceRate = calculateAdherenceRate();
            
            Log.d("DashboardActivity", "Stats calculated - Active: " + activeMedicines + 
                  ", Today's: " + todaysMedicines + ", Reminders: " + remindersToday + 
                  ", Adherence: " + adherenceRate);
            
            if (tvActiveMedicines != null) {
                tvActiveMedicines.setText(String.valueOf(activeMedicines));
            }
            if (tvTodaysMedicines != null) {
                tvTodaysMedicines.setText(String.valueOf(todaysMedicines));
            }
            if (tvRemindersToday != null) {
                tvRemindersToday.setText(String.valueOf(remindersToday));
            }
            if (tvAdherenceRate != null) {
                tvAdherenceRate.setText(adherenceRate);
            }
            
            updateNextReminder();
        } catch (Exception e) {
            Log.e("DashboardActivity", "Error updating stats", e);
            Toast.makeText(this, "Error updating stats", Toast.LENGTH_SHORT).show();
        }
    }

    private int getTodaysMedicineCount() {
        // Count medicines that are active and have reminders scheduled
        int count = 0;
        
        for (Medicine medicine : medicineList) {
            if (medicine.isActive() && medicine.getTimes() != null && !medicine.getTimes().isEmpty()) {
                count++;
            }
        }
        Log.d("DashboardActivity", "Today's medicines count: " + count);
        return count;
    }

    private int getTodaysReminderCount() {
        // Count total reminders for active medicines
        int count = 0;
        for (Medicine medicine : medicineList) {
            if (medicine.isActive() && medicine.getTimes() != null) {
                count += medicine.getTimes().size();
            }
        }
        Log.d("DashboardActivity", "Today's reminders count: " + count);
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
        // Find the next reminder time from all active medicines
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String nextTime = null;
        
        for (Medicine medicine : medicineList) {
            if (medicine.isActive() && medicine.getTimes() != null) {
                for (String time : medicine.getTimes()) {
                    if (time.compareTo(currentTime) > 0) {
                        if (nextTime == null || time.compareTo(nextTime) < 0) {
                            nextTime = time;
                        }
                    }
                }
            }
        }
        
        // If no reminders found for today, find the earliest reminder for tomorrow
        if (nextTime == null) {
            for (Medicine medicine : medicineList) {
                if (medicine.isActive() && medicine.getTimes() != null) {
                    for (String time : medicine.getTimes()) {
                        if (nextTime == null || time.compareTo(nextTime) < 0) {
                            nextTime = time + " (Tomorrow)";
                        }
                    }
                }
            }
        }
        
        Log.d("DashboardActivity", "Next reminder time: " + nextTime);
        return nextTime;
    }

    private boolean isMedicineActiveToday(Medicine medicine) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Date todayDate = today.getTime();
        
        // Check if medicine start date is before or equal to today
        if (medicine.getStartDate() != null && medicine.getStartDate().after(todayDate)) {
            return false;
        }
        
        // Check if medicine end date is after or equal to today (if end date is set)
        if (medicine.getEndDate() != null) {
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(medicine.getEndDate());
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            endCalendar.set(Calendar.MILLISECOND, 999);
            
            if (endCalendar.getTime().before(todayDate)) {
                return false;
            }
        }
        
        return true;
    }

    private String calculateAdherenceRate() {
        if (medicineList.isEmpty()) {
            return "N/A";
        }
        
        // Calculate adherence rate based on active vs inactive medicines
        int totalMedicines = medicineList.size();
        int activeMedicines = 0;
        
        for (Medicine medicine : medicineList) {
            if (medicine.isActive()) {
                activeMedicines++;
            }
        }
        
        if (totalMedicines == 0) {
            return "N/A";
        }
        
        // Calculate percentage: active medicines / total medicines * 100
        double adherencePercentage = ((double) activeMedicines / totalMedicines) * 100;
        int adherenceRate = (int) Math.round(adherencePercentage);
        
        Log.d("DashboardActivity", "Adherence calculation: " + activeMedicines + "/" + totalMedicines + " = " + adherenceRate + "%");
        
        return adherenceRate + "%";
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

    @Override
    public void onLearnMoreClick(Medicine medicine) {
        // Fetch medicine information from FDA API
        showMedicineAPIInfo(medicine);
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

    private void showMedicineAPIInfo(Medicine medicine) {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching medicine information...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Fetch medicine info from FDA API
        medicineInfoService.getMedicineInfo(medicine.getName(), new MedicineInfoService.MedicineInfoCallback() {
            @Override
            public void onSuccess(MedicineInfoService.MedicineInfo medicineInfo) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showMedicineInfoFromAPI(medicineInfo);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(DashboardActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showMedicineInfoFromAPI(MedicineInfoService.MedicineInfo medicineInfo) {
        StringBuilder info = new StringBuilder();
        
        if (!medicineInfo.drugName.isEmpty()) {
            info.append("Drug Name: ").append(medicineInfo.drugName).append("\n\n");
        }

        if (!medicineInfo.activeIngredient.isEmpty()) {
            info.append("Active Ingredient:\n").append(cleanText(medicineInfo.activeIngredient)).append("\n\n");
        }

        if (!medicineInfo.manufacturer.isEmpty()) {
            info.append("Manufacturer: ").append(medicineInfo.manufacturer).append("\n\n");
        }

        if (!medicineInfo.description.isEmpty()) {
            info.append("Description/Uses:\n").append(cleanText(medicineInfo.description)).append("\n\n");
        }

        if (!medicineInfo.dosageAndAdministration.isEmpty()) {
            info.append("Dosage & Administration:\n").append(cleanText(medicineInfo.dosageAndAdministration)).append("\n\n");
        }

        if (!medicineInfo.warnings.isEmpty()) {
            info.append("⚠️ Warnings:\n").append(cleanText(medicineInfo.warnings)).append("\n\n");
        }

        if (!medicineInfo.adverseReactions.isEmpty()) {
            info.append("⚠️ Side Effects:\n").append(cleanText(medicineInfo.adverseReactions)).append("\n\n");
        }

        if (!medicineInfo.contraindications.isEmpty()) {
            info.append("❌ Contraindications:\n").append(cleanText(medicineInfo.contraindications)).append("\n\n");
        }

        if (info.length() == 0) {
            info.append("No detailed information found for this medicine in the FDA database.\n\n");
            info.append("Please consult your healthcare provider or pharmacist for more information.");
        } else {
            info.append("⚕️ This information is from the FDA database. Always consult your healthcare provider for personalized medical advice.");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Learn More: " + (medicineInfo.drugName.isEmpty() ? "Medicine Info" : medicineInfo.drugName))
               .setMessage(info.toString())
               .setPositiveButton("OK", null)
               .setNegativeButton("Share", (dialog, which) -> {
                   shareInformation(info.toString());
               });

        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Make text scrollable for long content
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setMaxLines(20);
            messageView.setVerticalScrollBarEnabled(true);
        }
    }

    private String cleanText(String text) {
        if (text == null) return "";
        // Remove extra whitespace and format nicely
        return text.replaceAll("\\s+", " ").trim();
    }

    private void shareInformation(String information) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, information);
        startActivity(Intent.createChooser(shareIntent, "Share Medicine Information"));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.dashboard_menu, menu);
            return true;
        } catch (Exception e) {
            // If menu inflation fails, just continue without menu
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item != null && item.getItemId() == R.id.action_logout) {
                showLogoutDialog();
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Menu error", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
}