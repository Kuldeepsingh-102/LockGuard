package com.lockguard.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.data.security.CryptoManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

/**
 * Background maintenance worker for enforcing photo retention policies and storage limits.
 *
 * Privacy Feature:
 * Automatically purges encrypted intruder records older than the user's selected retention window
 * (e.g. 7 days, 14 days, 30 days) and prevents disk consumption beyond user limits.
 */
class PhotoRetentionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun intruderDao(): IntruderDao
        fun cryptoManager(): CryptoManager
        fun settingsRepository(): SettingsRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                WorkerEntryPoint::class.java
            )

            val intruderDao = entryPoint.intruderDao()
            val cryptoManager = entryPoint.cryptoManager()
            val settingsRepository = entryPoint.settingsRepository()

            val settings = settingsRepository.getSettings().firstOrNull() ?: return Result.success()

            // 1. Auto-delete photos older than X days
            if (settings.photoRetentionDays > 0) {
                val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(settings.photoRetentionDays.toLong())
                val oldEvents = intruderDao.getEventsOlderThan(cutoffTime)
                for (event in oldEvents) {
                    event.photoPath?.let { cryptoManager.deletePhotoFile(it) }
                    intruderDao.deleteById(event.id)
                }
            }

            // 2. Enforce maximum photo limit
            if (settings.maxStoredPhotos > 0) {
                val allEvents = intruderDao.getAllEvents().firstOrNull() ?: emptyList()
                val photosList = allEvents.filter { it.photoPath != null }
                if (photosList.size > settings.maxStoredPhotos) {
                    val excessCount = photosList.size - settings.maxStoredPhotos
                    // Take the oldest excess events
                    val toDelete = photosList.takeLast(excessCount)
                    for (event in toDelete) {
                        event.photoPath?.let { cryptoManager.deletePhotoFile(it) }
                        intruderDao.deleteById(event.id)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
