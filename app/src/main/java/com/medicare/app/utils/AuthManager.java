package com.medicare.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.medicare.app.models.User;

public class AuthManager {
    
    private static final String PREFS_NAME = "MediCarePrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private static AuthManager instance;
    private SharedPreferences sharedPreferences;
    private Context context;
    
    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }
    
    public void saveUserSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public long getCurrentUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }
    
    public String getCurrentUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }
    
    public String getCurrentUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }
    
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    public void clearSession() {
        logout();
    }
}