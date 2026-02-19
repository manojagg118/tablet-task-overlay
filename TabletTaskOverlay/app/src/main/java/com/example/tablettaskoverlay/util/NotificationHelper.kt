package com.example.tablettaskoverlay.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tablettaskoverlay.R
import com.example.tablettaskoverlay.ui.MainActivity

object NotificationHelper {
    const val OVERLAY_CHANNEL_ID = "overlay_channel"
    const val REMINDER_CHANNEL_ID = "reminder_channel"
    const val OVERLAY_NOTIFICATION_ID = 1101
    const val MORNING_NOTIFICATION_ID = 2101
    const val EVENING_NOTIFICATION_ID = 2102

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val overlayChannel = NotificationChannel(
            OVERLAY_CHANNEL_ID,
            context.getString(R.string.overlay_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )

        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )

        manager.createNotificationChannel(overlayChannel)
        manager.createNotificationChannel(reminderChannel)
    }

    fun buildOverlayServiceNotification(context: Context): Notification {
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, OVERLAY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.overlay_notification_text))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun buildReminderNotification(
        context: Context,
        title: String,
        content: String,
        requestCode: Int
    ): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
