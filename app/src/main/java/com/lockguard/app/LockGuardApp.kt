package com.lockguard.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lockguard.app.service.NotificationHelper
import com.lockguard.app.workers.PhotoRetentionWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Main Application class for LockGuard.
 * Initializes Hilt, notification channels, and periodic maintenance tasks.
 */
@HiltAndroidApp
class LockGuardApp : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicMaintenance()
    }

    /**
     * Enqueues daily background worker to automatically purge photos older than
     * the configured retention window and enforce maximum photo limits.
     */
    private fun schedulePeriodicMaintenance() {
        val retentionWorkRequest = PeriodicWorkRequestBuilder<PhotoRetentionWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "lockguard_photo_retention",
            ExistingPeriodicWorkPolicy.KEEP,
            retentionWorkRequest
        )
    }
}
