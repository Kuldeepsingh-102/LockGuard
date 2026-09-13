package com.lockguard.app.ui.capture

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lockguard.app.data.camera.CameraCaptureManager
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.LockGuardTheme
import com.lockguard.app.ui.theme.Navy950
import com.lockguard.app.ui.theme.StatusGreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Foreground-only CameraX capture screen.
 *
 * Android compliance:
 * - Camera is opened only while this visible Activity is in the foreground.
 * - No silent/background camera from DeviceAdminReceiver.
 * - May appear over the lock screen via showWhenLocked when launched from a
 *   high-priority full-screen notification intent (official Alarm-category path).
 * - If the OS still blocks camera while locked, capture retries after unlock
 *   via [com.lockguard.app.receiver.UnlockCaptureReceiver].
 */
@AndroidEntryPoint
class IntruderCaptureActivity : ComponentActivity() {

    @Inject
    lateinit var cameraCaptureManager: CameraCaptureManager

    @Inject
    lateinit var intruderDao: IntruderDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)

        setContent {
            LockGuardTheme {
                CaptureUi(
                    onCapture = { captureAndAttach(eventId) },
                    onDone = { finish() }
                )
            }
        }
    }

    private suspend fun captureAndAttach(eventId: Long): CaptureResult {
        if (!cameraCaptureManager.hasCameraPermission()) {
            return CaptureResult.Failed(
                "Camera permission is required. Open LockGuard Settings and grant Camera access."
            )
        }

        val targetId = if (eventId > 0) {
            eventId
        } else {
            // Fallback: attach to newest pending system lock-screen event (last 15 minutes)
            val since = System.currentTimeMillis() - 15 * 60 * 1000L
            intruderDao.getPendingSystemCaptures(
                source = DetectionSource.SYSTEM_LOCK_SCREEN.name,
                sinceTimestamp = since,
                limit = 1
            ).firstOrNull()?.id
        }

        if (targetId == null || targetId <= 0) {
            return CaptureResult.Failed("No pending lock-screen event found to attach a photo.")
        }

        val existing = intruderDao.getEventById(targetId)
        if (existing?.photoPath != null) {
            return CaptureResult.Success("Evidence already secured for this attempt.")
        }

        val capture = cameraCaptureManager.captureIntruderPhoto(this)
        return if (capture.isSuccess) {
            val path = capture.getOrNull()!!
            intruderDao.attachPhoto(
                id = targetId,
                photoPath = path,
                isEncrypted = true,
                notes = "Incorrect PIN/pattern on Android lock screen. Photo captured in foreground Capture Activity (CameraX)."
            )
            CaptureResult.Success("Intruder photo encrypted and saved.")
        } else {
            val reason = capture.exceptionOrNull()?.localizedMessage
                ?: "Camera unavailable while device is locked."
            CaptureResult.Failed(
                "Could not capture yet: $reason. Unlock the phone or tap the security notification to retry."
            )
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }
}

private sealed class CaptureResult {
    data class Success(val message: String) : CaptureResult()
    data class Failed(val message: String) : CaptureResult()
}

@Composable
private fun CaptureUi(
    onCapture: suspend () -> CaptureResult,
    onDone: () -> Unit
) {
    var status by remember { mutableStateOf("Securing evidence…") }
    var isWorking by remember { mutableStateOf(true) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = onCapture()
        when (result) {
            is CaptureResult.Success -> {
                status = result.message
                isSuccess = true
            }
            is CaptureResult.Failed -> {
                status = result.message
                isSuccess = false
            }
        }
        isWorking = false
        delay(1800)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isWorking) {
                CircularProgressIndicator(
                    color = ElectricBlue,
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isWorking) {
                    "Capturing front-camera evidence"
                } else if (isSuccess) {
                    "Evidence secured"
                } else {
                    "Capture pending"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (isSuccess) StatusGreen else Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    }
}
