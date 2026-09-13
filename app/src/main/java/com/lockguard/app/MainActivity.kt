package com.lockguard.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.ui.navigation.LockGuardNavHost
import com.lockguard.app.ui.navigation.Screen
import com.lockguard.app.ui.theme.LockGuardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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

    private var targetRouteFromNotification: String? = null

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
        // Check auto-lock timeout
        CoroutineScope(Dispatchers.Main).launch {
            val settings = settingsRepository.getSettings().firstOrNull() ?: return@launch
            if (settings.isAppLockEnabled && pinManager.isAppLocked(settings.autoLockTimeoutSeconds)) {
                // PinManager marks locked state; Splash/AppLock will intercept navigation
            }
        }
    }
}
