package com.example.tablettaskoverlay

import android.app.Application
import com.example.tablettaskoverlay.db.AppDatabase
import com.example.tablettaskoverlay.repo.TaskRepository
import com.example.tablettaskoverlay.util.NotificationHelper
import com.example.tablettaskoverlay.worker.ReminderScheduler

class TaskOverlayApp : Application() {
    lateinit var repository: TaskRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = TaskRepository(db.taskDao())

        NotificationHelper.createChannels(this)
        ReminderScheduler.scheduleDailyReminders(this)
    }
}
