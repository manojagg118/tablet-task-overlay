package com.example.tablettaskoverlay.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val MORNING_WORK = "morning_open_tasks_work"
    private const val EVENING_WORK = "evening_close_prompt_work"

    fun scheduleDailyReminders(context: Context) {
        val manager = WorkManager.getInstance(context)

        // We schedule exactly one periodic work per reminder window and update it on app start.
        val morningDelay = computeInitialDelay(LocalTime.of(8, 0))
        val eveningDelay = computeInitialDelay(LocalTime.of(20, 0))

        val morningWork = PeriodicWorkRequestBuilder<MorningReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(morningDelay)
            .build()

        val eveningWork = PeriodicWorkRequestBuilder<EveningReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(eveningDelay)
            .build()

        manager.enqueueUniquePeriodicWork(MORNING_WORK, ExistingPeriodicWorkPolicy.UPDATE, morningWork)
        manager.enqueueUniquePeriodicWork(EVENING_WORK, ExistingPeriodicWorkPolicy.UPDATE, eveningWork)
    }

    private fun computeInitialDelay(targetTime: LocalTime): Duration {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        // If the target time already passed today, schedule for tomorrow.
        var target = now.withHour(targetTime.hour).withMinute(targetTime.minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target)
    }
}
