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
import com.medicare.app.models.User;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "medicare.db";
    private static final int DATABASE_VERSION = 5;
    private static final String TAG = "DatabaseHelper";
    
    private static final String TABLE_MEDICINES = "medicines";
    private static final String TABLE_USERS = "users";
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
    private static final String COLUMN_USER_ID = "user_id";

    // User table columns
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";

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
            + COLUMN_UPDATED_AT + " TEXT,"
            + COLUMN_USER_ID + " INTEGER NOT NULL"
            + ")";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_FIRST_NAME + " TEXT,"
            + COLUMN_LAST_NAME + " TEXT,"
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
        db.execSQL(CREATE_TABLE_USERS);
        
        // No sample data - each user starts with empty medicine list
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_USERS);
        }
        if (oldVersion < 3) {
            // Add user_id column to existing medicines table
            db.execSQL("ALTER TABLE " + TABLE_MEDICINES + " ADD COLUMN " + COLUMN_USER_ID + " INTEGER DEFAULT 1");
        }
        if (oldVersion < 4) {
            // Clear all existing medicine data to fix user isolation
            db.execSQL("DELETE FROM " + TABLE_MEDICINES);
            Log.d(TAG, "Cleared existing medicine data for proper user isolation");
        }
        if (oldVersion < 5) {
            // Force complete database reset to ensure user isolation
            db.execSQL("DELETE FROM " + TABLE_MEDICINES);
            Log.d(TAG, "Database version 5: Forced complete medicine data reset for user isolation");
        }
    }

    public long insertMedicine(Medicine medicine, long userId) {
        Log.d(TAG, "Inserting medicine: " + medicine.getName() + " for user_id: " + userId);
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
        values.put(COLUMN_USER_ID, userId);
        
        long id = db.insert(TABLE_MEDICINES, null, values);
        Log.d(TAG, "Medicine inserted with ID: " + id + " for user_id: " + userId);
        db.close();
        return id;
    }

    public List<Medicine> getAllMedicines(long userId) {
        List<Medicine> medicines = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDICINES + " WHERE " + COLUMN_USER_ID + " = ? ORDER BY " + COLUMN_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});
        
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

    public List<Medicine> getActiveMedicines(long userId) {
        Log.d(TAG, "Getting active medicines for user_id: " + userId);
        List<Medicine> medicines = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDICINES + " WHERE " + COLUMN_IS_ACTIVE + " = 1 AND " + COLUMN_USER_ID + " = ? ORDER BY " + COLUMN_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Debug: Check total medicines in database
        Cursor allCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MEDICINES, null);
        if (allCursor.moveToFirst()) {
            int totalCount = allCursor.getInt(0);
            Log.d(TAG, "Total medicines in database: " + totalCount);
        }
        allCursor.close();
        
        // Debug: Check medicines for this user
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                Medicine medicine = cursorToMedicine(cursor);
                medicines.add(medicine);
                Log.d(TAG, "Found medicine: " + medicine.getName() + " for user_id: " + userId);
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "No medicines found for user_id: " + userId);
        }
        
        Log.d(TAG, "Total medicines found for user_id " + userId + ": " + medicines.size());
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

    public int getMedicineCount(long userId) {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_MEDICINES + " WHERE " + COLUMN_IS_ACTIVE + " = 1 AND " + COLUMN_USER_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    // User management methods
    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_EMAIL, user.getEmail().toLowerCase());
        values.put(COLUMN_PASSWORD, hashPassword(user.getPassword()));
        values.put(COLUMN_FIRST_NAME, user.getFirstName());
        values.put(COLUMN_LAST_NAME, user.getLastName());
        values.put(COLUMN_CREATED_AT, dateFormat.format(new Date()));
        values.put(COLUMN_UPDATED_AT, dateFormat.format(new Date()));
        
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        
        Cursor cursor = db.query(TABLE_USERS, null, 
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?", 
                new String[]{email.toLowerCase(), hashedPassword}, 
                null, null, null);
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        
        cursor.close();
        db.close();
        return user;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, 
                COLUMN_EMAIL + "=?", new String[]{email.toLowerCase()}, 
                null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public User getUserById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        
        cursor.close();
        db.close();
        return user;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
        user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)));
        user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)));
        
        try {
            String createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
            if (createdAtStr != null) {
                user.setCreatedAt(dateFormat.parse(createdAtStr));
            }
            
            String updatedAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT));
            if (updatedAtStr != null) {
                user.setUpdatedAt(dateFormat.parse(updatedAtStr));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing user date", e);
        }
        
        return user;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return password; // Fallback, not secure
        }
    }
}