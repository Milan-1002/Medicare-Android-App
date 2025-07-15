package com.medicare.app.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings; // For custom sound fallback or default
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.medicare.app.MainActivity; // Assuming you want to open MainActivity on tap
import com.medicare.app.R; // For your drawable icon and raw sound

public class AlarmReceiver extends BroadcastReceiver {

    public static final String MEDICINE_ID_EXTRA = "MEDICINE_ID_EXTRA";
    public static final String MEDICINE_NAME_EXTRA = "MEDICINE_NAME_EXTRA";
    public static final String NOTIFICATION_TITLE_EXTRA = "NOTIFICATION_TITLE_EXTRA";
    public static final String NOTIFICATION_TEXT_EXTRA = "NOTIFICATION_TEXT_EXTRA";

    private static final String CHANNEL_ID = "MEDICINE_REMINDER_CHANNEL";
    private static final CharSequence CHANNEL_NAME = "Medicine Reminders";
    private static final String CHANNEL_DESCRIPTION = "Channel for Medicine Reminder Notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarm received!");

        long medicineId = intent.getLongExtra(MEDICINE_ID_EXTRA, -1);
        String medicineName = intent.getStringExtra(MEDICINE_NAME_EXTRA);
        String notificationTitle = intent.getStringExtra(NOTIFICATION_TITLE_EXTRA);
        String notificationText = intent.getStringExtra(NOTIFICATION_TEXT_EXTRA);

        if (medicineName == null) {
            medicineName = "Your Medicine"; // Fallback
        }
        if (notificationTitle == null) {
            notificationTitle = "Medicine Reminder";
        }
        if (notificationText == null) {
            notificationText = "It's time to take " + medicineName + ".";
        }


        createNotificationChannel(context); // Ensure channel is created

        // Intent to launch app when notification is tapped
        Intent tapIntent = new Intent(context, MainActivity.class); // Or a specific activity
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // You might want to pass data to MainActivity if needed, e.g., to open a specific medicine
        // tapIntent.putExtra("NAVIGATE_TO_MEDICINE_ID", medicineId);

        PendingIntent pendingTapIntent = PendingIntent.getActivity(
                context,
                (int) (medicineId > 0 ? medicineId : System.currentTimeMillis()), // Unique request code for PendingIntent
                tapIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Define a custom sound or use default
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Uncomment and ensure reminder_sound.mp3 is in res/raw for custom sound
        // try {
        //     soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.reminder_sound);
        // } catch (Exception e) {
        //     Log.e("AlarmReceiver", "Custom sound not found, using default.", e);
        //     soundUri = Settings.System.DEFAULT_NOTIFICATION_URI; // Fallback
        // }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon) // REPLACE with your notification icon
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // For heads-up display
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(soundUri) // Set the sound
                .setAutoCancel(true) // Dismiss notification when tapped
                .setContentIntent(pendingTapIntent) // Action on tap
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Show on lock screen


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Generate a unique notification ID (can use medicineId if only one notif per medicine at a time)
        int notificationId = (int) (medicineId > 0 ? medicineId : System.currentTimeMillis());


        // Check for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("AlarmReceiver", "POST_NOTIFICATIONS permission not granted. Cannot show notification.");
                // Potentially, you might want to inform the user or log this more formally
                // For this example, we just log and don't show the notification.
                return;
            }
        }
        notificationManager.notify(notificationId, builder.build());
        Log.d("AlarmReceiver", "Notification sent for medicine: " + medicineName);

        // If this is a one-time alarm, it's done.
        // If it's part of a recurring schedule, you might reschedule the next one here,
        // or have a separate mechanism for daily rescheduling.
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // High importance for sound and heads-up
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            // channel.setSound(soundUri, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_ALARM).build()); // More specific sound attributes

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("AlarmReceiver", "Notification channel created.");
            }
        }
    }
}

