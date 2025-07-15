package com.medicare.app.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.medicare.app.DashboardActivity;
import com.medicare.app.R;

public class MedicineReminderReceiver extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "MEDICINE_REMINDERS";
    private static final int NOTIFICATION_ID = 1001;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MedicineReminder", "Reminder received!");
        
        String medicineName = intent.getStringExtra("medicine_name");
        String dosage = intent.getStringExtra("dosage");
        String time = intent.getStringExtra("time");
        
        Log.d("MedicineReminder", "Medicine: " + medicineName + ", Dosage: " + dosage + ", Time: " + time);
        
        if (medicineName != null) {
            showNotification(context, medicineName, dosage, time);
        } else {
            Log.e("MedicineReminder", "Medicine name is null - cannot show notification");
        }
    }
    
    private void showNotification(Context context, String medicineName, String dosage, String time) {
        createNotificationChannel(context);
        
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        String title = "Medicine Reminder";
        String message = "Time to take " + medicineName;
        if (dosage != null) {
            message += " (" + dosage + ")";
        }
        if (time != null) {
            message += " at " + time;
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        
        Log.d("MedicineReminder", "Notification shown for: " + medicineName);
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Medicine Reminders";
            String description = "Notifications for medicine reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}