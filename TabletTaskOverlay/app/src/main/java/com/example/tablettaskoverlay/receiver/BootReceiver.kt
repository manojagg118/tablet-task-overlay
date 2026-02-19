package com.example.tablettaskoverlay.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tablettaskoverlay.BuildConfig
import com.example.tablettaskoverlay.worker.ReminderScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            ReminderScheduler.scheduleDailyReminders(context)
            if (BuildConfig.ENABLE_LOCKSCREEN_STRICT) {
                // Overlay service restart can be triggered by opening app once after reboot,
                // which avoids background launch restrictions on recent Android versions.
            }
        }
    }
}
