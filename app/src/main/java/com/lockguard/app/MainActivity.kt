package com.lockguard.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.ui.capture.IntruderCaptureActivity
import com.lockguard.app.ui.navigation.LockGuardNavHost
import com.lockguard.app.ui.navigation.Screen
import com.lockguard.app.ui.theme.LockGuardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Root Activity for LockGuard.
 * Uses Single-Activity Compose architecture and integrates lifecycle-aware auto-lock.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var pinManager: PinManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var intruderDao: IntruderDao

    private var targetRouteFromNotification: String? = null
    private var pendingCaptureChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)

        setContent {
            LockGuardTheme {
                val navController = rememberNavController()

                LockGuardNavHost(
                    navController = navController,
                    settingsRepository = settingsRepository,
                    pinManager = pinManager,
                    initialTargetRoute = targetRouteFromNotification
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getStringExtra("navigate_to") == "gallery") {
            targetRouteFromNotification = Screen.Gallery.route
        }
    }

    override fun onStop() {
        super.onStop()
        pinManager.recordUserActivity()
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            val settings = settingsRepository.getSettings().firstOrNull() ?: return@launch
            if (settings.isAppLockEnabled && pinManager.isAppLocked(settings.autoLockTimeoutSeconds)) {
                // PinManager marks locked state; Splash/AppLock will intercept navigation
            }

            // Compliant fallback: if a lock-screen attempt is still missing a photo, capture now.
            if (!pendingCaptureChecked) {
                pendingCaptureChecked = true
                launchPendingCaptureIfNeeded()
            }
        }
    }

    private suspend fun launchPendingCaptureIfNeeded() {
        val pending = withContext(Dispatchers.IO) {
            val since = System.currentTimeMillis() - 15 * 60 * 1000L
            intruderDao.getPendingSystemCaptures(
                source = DetectionSource.SYSTEM_LOCK_SCREEN.name,
                sinceTimestamp = since,
                limit = 1
            ).firstOrNull()
        } ?: return

        startActivity(
            Intent(this, IntruderCaptureActivity::class.java).apply {
                putExtra(IntruderCaptureActivity.EXTRA_EVENT_ID, pending.id)
            }
        )
    }
}
