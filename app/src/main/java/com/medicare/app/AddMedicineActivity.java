package com.medicare.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView; // Keep this import
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.medicare.app.adapters.TimeAdapter;
import com.medicare.app.database.DatabaseHelper;
import com.medicare.app.models.Medicine;
import com.medicare.app.utils.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap; // Keep this import
import java.util.List;
import java.util.Locale;
import java.util.Map; // Keep this import
// import java.text.ParseException; // Not strictly needed here if TimeAdapter handles its own parsing

public class AddMedicineActivity extends AppCompatActivity {

    private EditText etMedicineName, etDosage, etNotes, etCustomTime;
    private Spinner spinnerFrequency, spinnerMedicineType;
    private Button btnStartDate, btnEndDate, btnAddTime, btnSave, btnCancel;
    private TextView tvStartDate, tvEndDate, tvTimeCount;
    private RecyclerView recyclerTimes;

    private DatabaseHelper databaseHelper;
    private Date startDate, endDate;
    private List<String> timesList; // Stores time as "HH:mm" (24-hour format)
    private TimeAdapter timeAdapter;

    // Date format for storing/displaying full dates
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    // Time formats for AM/PM display and 24-hour storage
    private final SimpleDateFormat displayTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final SimpleDateFormat storageTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());


    private Map<String, Integer> frequencyMaxTimes;
    private String[] frequencyValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        try {
            // Setup Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Medicine");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            
            initializeFrequencyMappings();
            initializeBasicViews();
            initializeDatabase();
            initializeTimesListBasedOnFrequency();
            setupBasicClickListeners();
            setInitialValues();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading form: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeFrequencyMappings() {
        frequencyMaxTimes = new HashMap<>();
        frequencyValues = new String[]{
                "once_daily", "twice_daily", "three_times_daily", "four_times_daily",
                "every_6_hours", "every_8_hours", "every_12_hours", "as_needed"
        };

        frequencyMaxTimes.put(frequencyValues[0], 1);  // Once daily
        frequencyMaxTimes.put(frequencyValues[1], 2);  // Twice daily
        frequencyMaxTimes.put(frequencyValues[2], 3);  // Three times daily
        frequencyMaxTimes.put(frequencyValues[3], 4);  // Four times daily
        frequencyMaxTimes.put(frequencyValues[4], 4);  // Every 6 hours
        frequencyMaxTimes.put(frequencyValues[5], 3);  // Every 8 hours
        frequencyMaxTimes.put(frequencyValues[6], 2);  // Every 12 hours
        frequencyMaxTimes.put(frequencyValues[7], 100); // As needed (high limit)
    }

    private void initializeTimesListBasedOnFrequency() {
        timesList = new ArrayList<>();
    }

    private void initializeBasicViews() {
        etMedicineName = findViewById(R.id.et_medicine_name);
        etDosage = findViewById(R.id.et_dosage);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        etNotes = findViewById(R.id.et_notes);
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        spinnerMedicineType = findViewById(R.id.spinner_medicine_type);
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        btnAddTime = findViewById(R.id.btn_add_time);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        tvTimeCount = findViewById(R.id.tv_time_count);
        recyclerTimes = findViewById(R.id.recycler_times);
        etCustomTime = findViewById(R.id.et_custom_time);

        setupSpinners();
        setupTimesRecyclerView();
        setupAdvancedClickListeners();
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupSpinners() {
        if (spinnerFrequency != null) {
            String[] frequencyDisplays = {
                    "Once daily", "Twice daily", "Three times daily", "Four times daily",
                    "Every 6 hours", "Every 8 hours", "Every 12 hours", "As needed"
            };
            ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, frequencyDisplays);
            frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFrequency.setAdapter(frequencyAdapter);
            spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    handleFrequencyChange();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (spinnerMedicineType != null) {
            String[] medicineTypeDisplays = {
                    "Tablet", "Capsule", "Liquid", "Injection", "Topical", "Inhaler", "Drops", "Other"
            };
            ArrayAdapter<String> medicineTypeAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, medicineTypeDisplays);
            medicineTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMedicineType.setAdapter(medicineTypeAdapter);
        }
    }

    private void setupTimesRecyclerView() {
        if (recyclerTimes != null && timesList != null) {
            timeAdapter = new TimeAdapter(timesList, position -> {
                if (position >= 0 && position < timesList.size()) {
                    timesList.remove(position);
                    timeAdapter.notifyItemRemoved(position);
                    timeAdapter.notifyItemRangeChanged(position, timesList.size());
                    updateTimeCount();
                    updateAddTimeControls();
                }
            });
            recyclerTimes.setLayoutManager(new LinearLayoutManager(this));
            recyclerTimes.setAdapter(timeAdapter);
            updateTimeCount();
        }
    }

    private void setupBasicClickListeners() {
        if (btnSave != null) btnSave.setOnClickListener(v -> saveMedicine());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());
    }

    private void setupAdvancedClickListeners() {
        if (btnStartDate != null) btnStartDate.setOnClickListener(v -> showDatePicker(true));
        if (btnEndDate != null) btnEndDate.setOnClickListener(v -> showDatePicker(false));
        if (btnAddTime != null) btnAddTime.setOnClickListener(v -> showTimePicker());
        if (etCustomTime != null) {
            etCustomTime.setFocusable(false);
            etCustomTime.setClickable(true);
            etCustomTime.setOnClickListener(v -> showTimePicker());
        }
    }

    private void setInitialValues() {
        startDate = new Date();
        if (tvStartDate != null) {
            tvStartDate.setText(displayDateFormat.format(startDate));
        }
        if (spinnerFrequency != null && spinnerFrequency.getAdapter() != null && spinnerFrequency.getAdapter().getCount() > 0) {
            if (spinnerFrequency.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                updateAddTimeControls();
            } else {
                handleFrequencyChange();
            }
        } else {
            updateAddTimeControls();
        }
    }

    private void handleFrequencyChange() {
        if (spinnerFrequency == null || timesList == null || frequencyValues == null || frequencyMaxTimes == null) {
            updateAddTimeControls();
            return;
        }
        int selectedPosition = spinnerFrequency.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= frequencyValues.length) {
            updateAddTimeControls();
            return;
        }

        String selectedFrequencyValue = frequencyValues[selectedPosition];
        Integer maxTimes = frequencyMaxTimes.getOrDefault(selectedFrequencyValue, 1);
        boolean isAsNeeded = "as_needed".equals(selectedFrequencyValue);

        if (!isAsNeeded) {
            if (timesList.size() > maxTimes) {
                Toast.makeText(this, "Reducing number of times to match: " + spinnerFrequency.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                while (timesList.size() > maxTimes) {
                    timesList.remove(timesList.size() - 1);
                }
                if (timeAdapter != null) timeAdapter.notifyDataSetChanged();
                updateTimeCount();
            }
        }
        updateAddTimeControls();
    }

    private void updateAddTimeControls() {
        if (btnAddTime == null || etCustomTime == null) return;

        Integer maxTimes = 1;
        boolean isAsNeeded = false;
        String currentFrequencyHint = "Select frequency";

        if (spinnerFrequency != null && frequencyValues != null && frequencyMaxTimes != null &&
                spinnerFrequency.getSelectedItemPosition() >= 0 &&
                spinnerFrequency.getSelectedItemPosition() < frequencyValues.length) {
            String selectedFrequencyValue = frequencyValues[spinnerFrequency.getSelectedItemPosition()];
            maxTimes = frequencyMaxTimes.getOrDefault(selectedFrequencyValue, 1);
            isAsNeeded = "as_needed".equals(selectedFrequencyValue);
            currentFrequencyHint = spinnerFrequency.getSelectedItem().toString();
        } else {
            btnAddTime.setEnabled(false);
            etCustomTime.setEnabled(false);
            etCustomTime.setHint(currentFrequencyHint);
            return;
        }

        boolean canAddMore = timesList.size() < maxTimes;
        if (isAsNeeded) {
            btnAddTime.setEnabled(true);
            etCustomTime.setEnabled(true);
            etCustomTime.setHint("Tap to add time (As needed)");
        } else {
            btnAddTime.setEnabled(canAddMore);
            etCustomTime.setEnabled(canAddMore);
            if (canAddMore) {
                etCustomTime.setHint("Tap to add (" + (maxTimes - timesList.size()) + " left for " + currentFrequencyHint + ")");
            } else {
                etCustomTime.setHint("Max times for " + currentFrequencyHint);
            }
        }
    }

    private void showDatePicker(boolean isStartDatePicker) {
        Calendar calendar = Calendar.getInstance();
        Date initialDate = isStartDatePicker ? startDate : endDate;
        if (initialDate != null) calendar.setTime(initialDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    if (isStartDatePicker) {
                        startDate = selectedDate.getTime();
                        if (tvStartDate != null) tvStartDate.setText(displayDateFormat.format(startDate));
                    } else {
                        endDate = selectedDate.getTime();
                        if (tvEndDate != null) tvEndDate.setText(displayDateFormat.format(endDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        if (spinnerFrequency == null || timesList == null || frequencyValues == null || frequencyMaxTimes == null ||
                spinnerFrequency.getSelectedItemPosition() < 0 ||
                spinnerFrequency.getSelectedItemPosition() >= frequencyValues.length) {
            Toast.makeText(this, "Please select a valid frequency first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedFrequencyValue = frequencyValues[spinnerFrequency.getSelectedItemPosition()];
        Integer maxTimes = frequencyMaxTimes.getOrDefault(selectedFrequencyValue, 1);
        boolean isAsNeeded = "as_needed".equals(selectedFrequencyValue);

        if (!isAsNeeded && timesList.size() >= maxTimes) {
            Toast.makeText(this, "Maximum times reached for " + spinnerFrequency.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar selectedTimeCalendar = Calendar.getInstance();
                    selectedTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTimeCalendar.set(Calendar.MINUTE, minute);

                    String timeToStore = storageTimeFormat.format(selectedTimeCalendar.getTime()); // "HH:mm"

                    if (!timesList.contains(timeToStore)) {
                        timesList.add(timeToStore);
                        if (timeAdapter != null) {
                            timeAdapter.notifyItemInserted(timesList.size() - 1);
                        }
                        updateTimeCount();
                        updateAddTimeControls();
                        if (etCustomTime != null) etCustomTime.setText("");
                        // Show AM/PM in Toast
                        Toast.makeText(this, "Time '" + displayTimeFormat.format(selectedTimeCalendar.getTime()) + "' added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Time already added", Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // Use 12-hour format with AM/PM selector
        );
        timePickerDialog.show();
    }

    private void updateTimeCount() {
        if (tvTimeCount != null && timesList != null) {
            tvTimeCount.setText(String.format(Locale.getDefault(), "%d times set", timesList.size()));
        }
    }

    private void saveMedicine() {
        if (!validateForm()) {
            return;
        }

        Medicine medicine = new Medicine();
        medicine.setName(etMedicineName.getText().toString().trim());
        medicine.setDosage(etDosage.getText().toString().trim());

        String frequency = "once_daily";
        if (spinnerFrequency != null && spinnerFrequency.getSelectedItemPosition() >= 0 &&
                spinnerFrequency.getSelectedItemPosition() < frequencyValues.length) {
            frequency = frequencyValues[spinnerFrequency.getSelectedItemPosition()];
        }
        medicine.setFrequency(frequency);

        String medicineType = "Tablet";
        if (spinnerMedicineType != null && spinnerMedicineType.getSelectedItem() != null) {
            medicineType = spinnerMedicineType.getSelectedItem().toString();
        }
        medicine.setMedicineType(medicineType);

        medicine.setTimes(new ArrayList<>(timesList)); // timesList contains "HH:mm"

        if (etNotes != null) {
            medicine.setNotes(etNotes.getText().toString().trim());
        }

        medicine.setStartDate(startDate != null ? startDate : new Date());
        medicine.setEndDate(endDate);
        medicine.setActive(true);

        // Get current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MediCarePrefs", MODE_PRIVATE);
        long currentUserId = sharedPreferences.getLong("user_id", -1);
        
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        long id = databaseHelper.insertMedicine(medicine, currentUserId);
        if (id > 0) {
            medicine.setId(id);
            ReminderScheduler.scheduleReminder(this, medicine); // Assumes ReminderScheduler uses "HH:mm"
            Toast.makeText(this, "Medicine added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error adding medicine to database", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm() {
        if (etMedicineName == null || TextUtils.isEmpty(etMedicineName.getText().toString().trim())) {
            if (etMedicineName != null) etMedicineName.setError("Medicine name is required");
            else Toast.makeText(this, "Medicine name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDosage == null || TextUtils.isEmpty(etDosage.getText().toString().trim())) {
            if (etDosage != null) etDosage.setError("Dosage is required");
            else Toast.makeText(this, "Dosage is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (timesList == null || timesList.isEmpty()) {
            Toast.makeText(this, "Please add at least one reminder time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_medicine_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear user session
        SharedPreferences sharedPreferences = getSharedPreferences("MediCarePrefs", MODE_PRIVATE);
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
