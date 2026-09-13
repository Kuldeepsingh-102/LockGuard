package com.lockguard.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.database.IntruderEventEntity
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.service.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Official Android Device Administration Receiver for LockGuard.
 *
 * Android Security & Privacy Implementation Details:
 * 1. Policy Declaration: Declares `<watch-login />` in res/xml/device_admin.xml.
 *    This allows the operating system to legitimately dispatch [ACTION_PASSWORD_FAILED]
 *    events when an unauthorized attempt occurs on the system lock screen.
 *
 * 2. Privacy Guarantee: This receiver NEVER receives, intercepts, reads, or stores
 *    the user's actual lock-screen PIN, pattern, or password. Android's API only transmits
 *    a binary signal that an incorrect attempt took place.
 *
 * 3. Modern Android Background Execution Compliance (Android 10, 11, 12, 13, 14, 15):
 *    Starting with Android 10, background services and broadcast receivers are strictly
 *    prohibited by Google Play and Android OS security policies from accessing the camera
 *    hardware without an active user-visible foreground activity or compliant foreground
 *    service type. Attempting to secretly open the camera from a broadcast receiver violates
 *    Android security boundaries and causes immediate system termination or SecurityException.
 *
 *    Compliant Strategy:
 *    - Records the timestamp and system-level failed attempt count immediately.
 *    - Fires a discreet high-priority security alert notification.
 *    - For instantaneous front-camera intruder photography, LockGuard provides its
 *      "In-App Vault Protection Mode" which operates legitimately within the foreground
 *      Compose lifecycle.
 */
class LockGuardDeviceAdminReceiver : DeviceAdminReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun intruderDao(): IntruderDao
        fun notificationHelper(): NotificationHelper
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)

        // Asynchronously record the system-level failed authentication event
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )

        val intruderDao = entryPoint.intruderDao()
        val notificationHelper = entryPoint.notificationHelper()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentAttempts = intruderDao.getTotalAttemptsCount().firstOrNull() ?: 0
                val entity = IntruderEventEntity(
                    timestamp = System.currentTimeMillis(),
                    attemptNumber = currentAttempts + 1,
                    photoPath = null, // System lock-screen event; camera access governed by foreground policy
                    source = DetectionSource.SYSTEM_LOCK_SCREEN,
                    notes = "Incorrect PIN/pattern entered on Android system lock screen.",
                    isEncrypted = false
                )
                intruderDao.insert(entity)

                // Dispatch privacy-compliant notification
                notificationHelper.showIntruderAlertNotification()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
        // Legitimate user unlocked device; no adverse action needed
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}
