package com.medicare.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.medicare.app.models.Medicine;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "medicare.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";
    
    private static final String TABLE_MEDICINES = "medicines";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DOSAGE = "dosage";
    private static final String COLUMN_FREQUENCY = "frequency";
    private static final String COLUMN_TIMES = "times";
    private static final String COLUMN_MEDICINE_TYPE = "medicine_type";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_START_DATE = "start_date";
    private static final String COLUMN_END_DATE = "end_date";
    private static final String COLUMN_IS_ACTIVE = "is_active";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";

    private static final String CREATE_TABLE_MEDICINES = "CREATE TABLE " + TABLE_MEDICINES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT NOT NULL,"
            + COLUMN_DOSAGE + " TEXT NOT NULL,"
            + COLUMN_FREQUENCY + " TEXT NOT NULL,"
            + COLUMN_TIMES + " TEXT,"
            + COLUMN_MEDICINE_TYPE + " TEXT DEFAULT 'tablet',"
            + COLUMN_NOTES + " TEXT,"
            + COLUMN_START_DATE + " TEXT,"
            + COLUMN_END_DATE + " TEXT,"
            + COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_UPDATED_AT + " TEXT"
            + ")";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private Gson gson = new Gson();
    private Type listType = new TypeToken<List<String>>(){}.getType();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEDICINES);
        
        // Insert sample data
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        try {
            // Sample medicines
            List<Medicine> sampleMedicines = new ArrayList<>();
            
            List<String> aspirinTimes = new ArrayList<>();
            aspirinTimes.add("08:00");
            Medicine aspirin = new Medicine("Aspirin", "100mg", "once_daily", aspirinTimes, 
                    "tablet", "Take with food", new Date(), null);
            sampleMedicines.add(aspirin);
            
            List<String> vitaminTimes = new ArrayList<>();
            vitaminTimes.add("09:00");
            Medicine vitaminD = new Medicine("Vitamin D", "1000 IU", "once_daily", vitaminTimes, 
                    "capsule", "Take with breakfast", new Date(), null);
            sampleMedicines.add(vitaminD);
            
            List<String> omegaTimes = new ArrayList<>();
            omegaTimes.add("08:00");
            omegaTimes.add("20:00");
            Medicine omega3 = new Medicine("Omega-3", "1000mg", "twice_daily", omegaTimes, 
                    "capsule", "Take with meals", new Date(), null);
            sampleMedicines.add(omega3);
            
            for (Medicine medicine : sampleMedicines) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, medicine.getName());
                values.put(COLUMN_DOSAGE, medicine.getDosage());
                values.put(COLUMN_FREQUENCY, medicine.getFrequency());
                values.put(COLUMN_TIMES, gson.toJson(medicine.getTimes()));
                values.put(COLUMN_MEDICINE_TYPE, medicine.getMedicineType());
                values.put(COLUMN_NOTES, medicine.getNotes());
                values.put(COLUMN_START_DATE, dateFormat.format(medicine.getStartDate()));
                values.put(COLUMN_END_DATE, medicine.getEndDate() != null ? dateFormat.format(medicine.getEndDate()) : null);
                values.put(COLUMN_IS_ACTIVE, medicine.isActive() ? 1 : 0);
                values.put(COLUMN_CREATED_AT, dateFormat.format(medicine.getCreatedAt()));
                values.put(COLUMN_UPDATED_AT, dateFormat.format(medicine.getUpdatedAt()));
                
                db.insert(TABLE_MEDICINES, null, values);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting sample data", e);
        }
    }

    public long insertMedicine(Medicine medicine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_NAME, medicine.getName());
        values.put(COLUMN_DOSAGE, medicine.getDosage());
        values.put(COLUMN_FREQUENCY, medicine.getFrequency());
        values.put(COLUMN_TIMES, gson.toJson(medicine.getTimes()));
        values.put(COLUMN_MEDICINE_TYPE, medicine.getMedicineType());
        values.put(COLUMN_NOTES, medicine.getNotes());
        values.put(COLUMN_START_DATE, dateFormat.format(medicine.getStartDate()));
        values.put(COLUMN_END_DATE, medicine.getEndDate() != null ? dateFormat.format(medicine.getEndDate()) : null);
        values.put(COLUMN_IS_ACTIVE, medicine.isActive() ? 1 : 0);
        values.put(COLUMN_CREATED_AT, dateFormat.format(new Date()));
        values.put(COLUMN_UPDATED_AT, dateFormat.format(new Date()));
        
        long id = db.insert(TABLE_MEDICINES, null, values);
        db.close();
        return id;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> medicines = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDICINES + " ORDER BY " + COLUMN_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Medicine medicine = cursorToMedicine(cursor);
                medicines.add(medicine);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return medicines;
    }

    public List<Medicine> getActiveMedicines() {
        List<Medicine> medicines = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDICINES + " WHERE " + COLUMN_IS_ACTIVE + " = 1 ORDER BY " + COLUMN_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Medicine medicine = cursorToMedicine(cursor);
                medicines.add(medicine);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return medicines;
    }

    public Medicine getMedicine(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEDICINES, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        
        Medicine medicine = null;
        if (cursor.moveToFirst()) {
            medicine = cursorToMedicine(cursor);
        }
        
        cursor.close();
        db.close();
        return medicine;
    }

    public int updateMedicine(Medicine medicine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_NAME, medicine.getName());
        values.put(COLUMN_DOSAGE, medicine.getDosage());
        values.put(COLUMN_FREQUENCY, medicine.getFrequency());
        values.put(COLUMN_TIMES, gson.toJson(medicine.getTimes()));
        values.put(COLUMN_MEDICINE_TYPE, medicine.getMedicineType());
        values.put(COLUMN_NOTES, medicine.getNotes());
        values.put(COLUMN_START_DATE, dateFormat.format(medicine.getStartDate()));
        values.put(COLUMN_END_DATE, medicine.getEndDate() != null ? dateFormat.format(medicine.getEndDate()) : null);
        values.put(COLUMN_IS_ACTIVE, medicine.isActive() ? 1 : 0);
        values.put(COLUMN_UPDATED_AT, dateFormat.format(new Date()));
        
        int rowsAffected = db.update(TABLE_MEDICINES, values, COLUMN_ID + "=?", new String[]{String.valueOf(medicine.getId())});
        db.close();
        return rowsAffected;
    }

    public void deleteMedicine(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDICINES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    private Medicine cursorToMedicine(Cursor cursor) {
        Medicine medicine = new Medicine();
        medicine.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        medicine.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        medicine.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)));
        medicine.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)));
        
        String timesJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMES));
        if (timesJson != null) {
            List<String> times = gson.fromJson(timesJson, listType);
            medicine.setTimes(times);
        }
        
        medicine.setMedicineType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDICINE_TYPE)));
        medicine.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)));
        medicine.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1);
        
        try {
            String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE));
            if (startDateStr != null) {
                medicine.setStartDate(dateFormat.parse(startDateStr));
            }
            
            String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE));
            if (endDateStr != null) {
                medicine.setEndDate(dateFormat.parse(endDateStr));
            }
            
            String createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
            if (createdAtStr != null) {
                medicine.setCreatedAt(dateFormat.parse(createdAtStr));
            }
            
            String updatedAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT));
            if (updatedAtStr != null) {
                medicine.setUpdatedAt(dateFormat.parse(updatedAtStr));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e);
        }
        
        return medicine;
    }

    public int getMedicineCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_MEDICINES + " WHERE " + COLUMN_IS_ACTIVE + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }
}