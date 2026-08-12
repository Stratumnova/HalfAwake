package com.halfwake.pocket

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.halfwake.pocket.widget.HalfwakeWidgetProvider
import java.util.concurrent.TimeUnit

class TickWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val metrics = UsageRepository.currentMetrics(applicationContext)
        val result = computeMood(metrics)
        val last = DiaryStore.latest(applicationContext)
        val line = DiaryLines.pick(result.mood, last?.line)

        DiaryStore.append(
            applicationContext,
            DiaryEntry(
                atMillis = System.currentTimeMillis(),
                mood = result.mood.id,
                reason = result.reason,
                line = line,
            )
        )

        // Fallback in case the instant BatteryLowReceiver didn't catch it
        // (e.g. it dropped below the floor while the app was force-stopped).
        if (result.mood == Mood.CRITICAL) {
            NotificationHelper.notifyCritical(applicationContext, metrics.batteryPercent)
        }

        HalfwakeWidgetProvider.updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "halfwake_tick"

        fun ensureScheduled(context: Context, hours: Long = 3) {
            val request = PeriodicWorkRequestBuilder<TickWorker>(hours, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** Runs a tick immediately — used on first launch so the diary isn't empty. */
        fun runOnce(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<TickWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
