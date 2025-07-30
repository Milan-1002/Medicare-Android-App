package com.medicare.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.medicare.app.utils.ReminderScheduler;

public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // TODO: Reschedule all medicine reminders after device boot
            // This requires knowing which user is currently logged in
            // For now, reminders will be rescheduled when user opens the app
            // ReminderScheduler.rescheduleAllReminders(context, userId);
        }
    }
}