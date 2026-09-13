package com.lockguard.app.ui.screens.applock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.lockguard.app.ui.components.PinKeypad
import com.lockguard.app.ui.components.ShieldLogo
import com.lockguard.app.ui.theme.ElectricBlueLight
import com.lockguard.app.ui.theme.Navy950
import com.lockguard.app.ui.theme.StatusRed

@Composable
fun AppLockScreen(
    viewModel: AppLockViewModel,
    onUnlockSuccess: (returnRoute: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) {
            onUnlockSuccess(uiState.returnRoute)
        }
    }

    // Auto-launch biometric on start if in authenticate mode
    LaunchedEffect(Unit) {
        if (uiState.mode == PinScreenMode.AUTHENTICATE && uiState.isBiometricAvailable) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                viewModel.triggerBiometricAuthentication(activity)
            }
        }
    }

    val title = when (uiState.mode) {
        PinScreenMode.AUTHENTICATE -> "Unlock LockGuard"
        PinScreenMode.CREATE_PIN -> "Create 4-Digit PIN"
        PinScreenMode.CONFIRM_PIN -> "Confirm Your PIN"
    }

    val subtitle = when (uiState.mode) {
        PinScreenMode.AUTHENTICATE -> "Enter your in-app vault PIN or use biometric scan"
        PinScreenMode.CREATE_PIN -> "Establish a private PIN to encrypt and guard your evidence"
        PinScreenMode.CONFIRM_PIN -> "Re-enter your 4-digit PIN to finalize setup"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                ShieldLogo(size = 80.dp, isAnimated = true)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricBlueLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusRed,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Keypad Section
            PinKeypad(
                pinLength = 4,
                currentInputLength = uiState.enteredPin.length,
                onDigitClick = { digit -> viewModel.onDigitClick(digit, lifecycleOwner) },
                onBackspaceClick = { viewModel.onBackspaceClick() },
                showBiometric = uiState.mode == PinScreenMode.AUTHENTICATE && uiState.isBiometricAvailable,
                onBiometricClick = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        viewModel.triggerBiometricAuthentication(activity)
                    }
                },
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
