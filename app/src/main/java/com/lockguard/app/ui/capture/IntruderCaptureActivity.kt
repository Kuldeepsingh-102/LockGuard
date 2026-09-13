package com.lockguard.app.ui.capture

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lockguard.app.data.camera.CameraCaptureManager
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.ui.theme.LockGuardTheme
import com.lockguard.app.ui.theme.StatusRed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Foreground CameraX capture with a discreet "Wrong PIN" style UI.
 *
 * Android still requires a visible Activity to open the camera. This screen
 * intentionally looks like a normal authentication error popup (no camera preview,
 * no "capturing photo" wording) so the attempt feels like a failed unlock message
 * while evidence is secured in the foreground.
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
                WrongPinCaptureUi(
                    onCapture = { captureAndAttach(eventId) },
                    onDone = { finish() }
                )
            }
        }
    }

    private suspend fun captureAndAttach(eventId: Long): Boolean {
        if (!cameraCaptureManager.hasCameraPermission()) {
            return false
        }

        val targetId = if (eventId > 0) {
            eventId
        } else {
            val since = System.currentTimeMillis() - 15 * 60 * 1000L
            intruderDao.getPendingSystemCaptures(
                source = DetectionSource.SYSTEM_LOCK_SCREEN.name,
                sinceTimestamp = since,
                limit = 1
            ).firstOrNull()?.id
        }

        if (targetId == null || targetId <= 0) {
            return false
        }

        val existing = intruderDao.getEventById(targetId)
        if (existing?.photoPath != null) {
            return true
        }

        val capture = cameraCaptureManager.captureIntruderPhoto(this)
        return if (capture.isSuccess) {
            val path = capture.getOrNull()!!
            intruderDao.attachPhoto(
                id = targetId,
                photoPath = path,
                isEncrypted = true,
                notes = "Incorrect PIN/pattern on Android lock screen. Photo captured during discreet Wrong PIN foreground screen."
            )
            true
        } else {
            false
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }
}

/**
 * Looks like a system "Wrong PIN / Try again" dialog.
 * Camera runs with no preview; screen auto-dismisses quickly.
 */
@Composable
private fun WrongPinCaptureUi(
    onCapture: suspend () -> Boolean,
    onDone: () -> Unit
) {
    var subtitle by remember { mutableStateOf("Try again") }

    LaunchedEffect(Unit) {
        // Start capture immediately while showing Wrong PIN UI (no camera preview).
        val ok = onCapture()
        if (!ok) {
            subtitle = "Try again"
        }
        // Brief pause so the popup feels like a normal unlock error flash.
        delay(1100)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = StatusRed,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Wrong PIN",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
