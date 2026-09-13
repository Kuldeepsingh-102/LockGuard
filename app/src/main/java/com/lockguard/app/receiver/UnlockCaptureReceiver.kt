package com.lockguard.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.ui.capture.IntruderCaptureActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * After the user unlocks the device ([Intent.ACTION_USER_PRESENT]), complete any
 * pending system lock-screen intruder events that still lack a photo.
 *
 * This is the policy-compliant fallback when CameraX cannot run while the device
 * is still locked / while only a DeviceAdminReceiver is active.
 */
class UnlockCaptureReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Entry {
        fun intruderDao(): IntruderDao
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_USER_PRESENT) return

        val pendingResult = goAsync()
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            Entry::class.java
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val since = System.currentTimeMillis() - 15 * 60 * 1000L
                val pending = entry.intruderDao().getPendingSystemCaptures(
                    source = DetectionSource.SYSTEM_LOCK_SCREEN.name,
                    sinceTimestamp = since,
                    limit = 1
                )
                val event = pending.firstOrNull() ?: return@launch

                val captureIntent = Intent(context, IntruderCaptureActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(IntruderCaptureActivity.EXTRA_EVENT_ID, event.id)
                }
                context.startActivity(captureIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
