package com.lockguard.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.database.IntruderEventEntity
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.service.NotificationHelper
import com.lockguard.app.ui.capture.IntruderCaptureActivity
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
 * 3. Modern Android Background Execution Compliance (Android 10+):
 *    Broadcast receivers MUST NOT open the camera in the background. LockGuard therefore:
 *    - Immediately logs the failed attempt metadata.
 *    - Posts a high-priority notification with a full-screen / action PendingIntent to
 *      [IntruderCaptureActivity], which is a visible foreground Activity where CameraX
 *      is allowed to run.
 *    - Relies on [UnlockCaptureReceiver] (USER_PRESENT) as a compliant fallback if
 *      capture could not complete while the device remained locked.
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
                    photoPath = null,
                    source = DetectionSource.SYSTEM_LOCK_SCREEN,
                    notes = "Incorrect PIN/pattern on Android lock screen. Waiting for foreground CameraX capture.",
                    isEncrypted = false
                )
                val eventId = intruderDao.insert(entity)

                // Discreet alert that can open the foreground capture Activity.
                notificationHelper.showIntruderAlertNotification(eventId)

                // Best-effort: also try launching the capture Activity directly.
                // On Android 10+ this may be blocked until the notification full-screen
                // intent or USER_PRESENT unlock path runs — that is expected and compliant.
                try {
                    val captureIntent = Intent(context, IntruderCaptureActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(IntruderCaptureActivity.EXTRA_EVENT_ID, eventId)
                    }
                    context.startActivity(captureIntent)
                } catch (e: Exception) {
                    // Background activity launch blocked — notification / unlock path will handle it.
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}
