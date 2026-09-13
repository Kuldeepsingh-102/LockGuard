package com.lockguard.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.ui.components.ShieldLogo
import com.lockguard.app.ui.navigation.Screen
import com.lockguard.app.ui.theme.ElectricBlueLight
import com.lockguard.app.ui.theme.Navy950
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SplashScreen(
    settingsRepository: SettingsRepository,
    pinManager: PinManager,
    onNavigate: (String) -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )

        delay(1200)

        val settings = settingsRepository.getSettings().firstOrNull()
        val isOnboardingDone = settings?.isOnboardingCompleted == true
        val isPinConfigured = pinManager.isPinSet()

        when {
            !isOnboardingDone -> {
                onNavigate(Screen.Onboarding.route)
            }
            isPinConfigured && pinManager.isAppLocked(settings.autoLockTimeoutSeconds) -> {
                onNavigate(Screen.AppLock.createRoute(returnRoute = Screen.Dashboard.route))
            }
            else -> {
                onNavigate(Screen.Dashboard.route)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            ShieldLogo(size = 120.dp, isAnimated = true)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LockGuard",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Intruder Alert & Privacy Protection",
                style = MaterialTheme.typography.bodyMedium,
                color = ElectricBlueLight
            )
        }
    }
}
