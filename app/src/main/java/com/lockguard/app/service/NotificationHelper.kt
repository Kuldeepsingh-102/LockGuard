package com.lockguard.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lockguard.app.MainActivity
import com.lockguard.app.R
import com.lockguard.app.ui.capture.IntruderCaptureActivity

/**
 * Handles security alert notifications.
 *
 * Privacy Guarantees:
 * - Notification text is discreet: "Security alert: An unauthorized authentication attempt was detected."
 * - Intruder photo is NEVER displayed on the notification or lock screen shade.
 * - Full-screen / action intents open the foreground Capture Activity so CameraX can run legally.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "lockguard_security_alerts"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Issues a privacy-compliant security notification that can open the foreground
     * capture screen (full-screen intent + explicit action button).
     */
    fun showIntruderAlertNotification(eventId: Long? = null) {
        val galleryIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "gallery")
        }
        val galleryPending = PendingIntent.getActivity(
            context,
            1,
            galleryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val captureIntent = Intent(context, IntruderCaptureActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (eventId != null) {
                putExtra(IntruderCaptureActivity.EXTRA_EVENT_ID, eventId)
            }
        }
        val capturePending = PendingIntent.getActivity(
            context,
            2,
            captureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_alert_title))
            .setContentText(context.getString(R.string.notification_alert_body))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_alert_body_extended))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(galleryPending)
            .setFullScreenIntent(capturePending, true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.notification_capture_action),
                capturePending
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
