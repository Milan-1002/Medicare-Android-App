package com.medicare.app.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.medicare.app.database.DatabaseHelper;
import com.medicare.app.models.Medicine;
import com.medicare.app.receivers.MedicineReminderReceiver;

import java.util.Calendar;
import java.util.List;

public class ReminderScheduler {
    
    private static final String TAG = "ReminderScheduler";
    
    public static void scheduleReminder(Context context, Medicine medicine) {
        if (medicine.getTimes() == null || medicine.getTimes().isEmpty()) {
            Log.w(TAG, "No times set for medicine: " + medicine.getName());
            return;
        }
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Log.d(TAG, "Scheduling reminders for medicine: " + medicine.getName() + " with " + medicine.getTimes().size() + " times");
        
        for (int i = 0; i < medicine.getTimes().size(); i++) {
            String time = medicine.getTimes().get(i);
            scheduleReminderForTime(context, alarmManager, medicine, time, i);
        }
    }
    
    private static void scheduleReminderForTime(Context context, AlarmManager alarmManager, Medicine medicine, String time, int timeIndex) {
        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            // If the time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            Intent intent = new Intent(context, MedicineReminderReceiver.class);
            intent.putExtra("medicine_name", medicine.getName());
            intent.putExtra("dosage", medicine.getDosage());
            intent.putExtra("time", time);
            
            // Create unique request code for each reminder
            int requestCode = (int) (medicine.getId() * 1000 + timeIndex);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Schedule the alarm
            Log.d(TAG, "Attempting to schedule alarm for " + medicine.getName() + " at " + time + 
                  " (timestamp: " + calendar.getTimeInMillis() + ")");
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            }
            
            Log.d(TAG, "Successfully scheduled reminder for " + medicine.getName() + " at " + time + 
                  " with request code: " + requestCode);
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling reminder", e);
        }
    }
    
    public static void cancelReminder(Context context, Medicine medicine) {
        if (medicine.getTimes() == null || medicine.getTimes().isEmpty()) {
            return;
        }
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        for (int i = 0; i < medicine.getTimes().size(); i++) {
            Intent intent = new Intent(context, MedicineReminderReceiver.class);
            int requestCode = (int) (medicine.getId() * 1000 + i);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        
        Log.d(TAG, "Cancelled reminders for " + medicine.getName());
    }
    
    public static void rescheduleAllReminders(Context context, long userId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        List<Medicine> activeMedicines = databaseHelper.getActiveMedicines(userId);
        
        for (Medicine medicine : activeMedicines) {
            scheduleReminder(context, medicine);
        }
        
        databaseHelper.close();
        Log.d(TAG, "Rescheduled all reminders for user: " + userId);
    }
}