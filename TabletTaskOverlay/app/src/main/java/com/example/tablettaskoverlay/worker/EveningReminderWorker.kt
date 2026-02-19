package com.example.tablettaskoverlay.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tablettaskoverlay.R
import com.example.tablettaskoverlay.TaskOverlayApp
import com.example.tablettaskoverlay.util.NotificationHelper

class EveningReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!canPostNotifications()) return Result.success()

        val repo = (applicationContext as TaskOverlayApp).repository
        val openTasks = repo.getOpenTasksSnapshot()
        val body = if (openTasks.isEmpty()) {
            "No open tasks left."
        } else {
            val tasks = openTasks.joinToString(separator = "\n") { "• ${it.id}. ${it.text}" }
            "Which of these should be closed tonight?\n$tasks"
        }

        val notification = NotificationHelper.buildReminderNotification(
            context = applicationContext,
            title = applicationContext.getString(R.string.evening_reminder_title),
            content = body,
            requestCode = NotificationHelper.EVENING_NOTIFICATION_ID
        )

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationHelper.EVENING_NOTIFICATION_ID, notification)

        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
