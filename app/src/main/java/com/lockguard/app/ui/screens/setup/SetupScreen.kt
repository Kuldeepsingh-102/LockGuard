package com.lockguard.app.ui.screens.setup

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockguard.app.R
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.domain.model.SecurityStatus
import com.lockguard.app.receiver.LockGuardDeviceAdminReceiver
import com.lockguard.app.ui.components.LockGuardTopBar
import com.lockguard.app.ui.navigation.Screen
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy900
import com.lockguard.app.ui.theme.Navy950
import com.lockguard.app.ui.theme.StatusGreen
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    settingsRepository: SettingsRepository,
    onNavigateToPinSetup: () -> Unit,
    onFinishSetup: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val securityStatus by settingsRepository.getSecurityStatus().collectAsState(initial = SecurityStatus())

    // Activity launcher for Device Admin prompt
    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Will reflect in dynamic status
    }

    // Activity launcher for Camera Permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Trigger status update
    }

    // Activity launcher for Notification Permission (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Trigger status update
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LockGuardTopBar(
                title = "Security Permissions Setup"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Configure Protection Features",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "To detect unauthorized access and safely store evidence, LockGuard requires specific permissions. We never collect or transmit your personal data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 1. Camera Permission
                PermissionCard(
                    title = "Camera Access",
                    description = "Used solely to capture an encrypted photo when an unauthorized authentication attempt takes place. Photos are never uploaded or shared.",
                    icon = Icons.Rounded.CameraAlt,
                    isGranted = securityStatus.isCameraPermissionGranted,
                    onRequest = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Device Administration (watch-login)
                PermissionCard(
                    title = "Screen Unlock Monitor",
                    description = "Enables Android's official Device Administrator watch-login policy to receive signals when an incorrect PIN or pattern is entered on the lock screen.",
                    icon = Icons.Rounded.Security,
                    isGranted = securityStatus.isDeviceAdminActive,
                    onRequest = {
                        val adminComponent = ComponentName(context, LockGuardDeviceAdminReceiver::class.java)
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                context.getString(R.string.device_admin_description)
                            )
                        }
                        deviceAdminLauncher.launch(intent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Security Notifications
                PermissionCard(
                    title = "Security Alert Notifications",
                    description = "Delivers discreet, private security alerts whenever an unauthorized attempt is detected, without exposing photos on the lock screen.",
                    icon = Icons.Rounded.Notifications,
                    isGranted = securityStatus.isNotificationPermissionGranted,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. App Lock PIN
                PermissionCard(
                    title = "Vault App Lock & PIN",
                    description = "Protect your encrypted evidence and prevent unauthorized users from opening LockGuard to inspect photos.",
                    icon = Icons.Rounded.VpnKey,
                    isGranted = securityStatus.isAppLockConfigured,
                    onRequest = onNavigateToPinSetup
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Finish Setup Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            settingsRepository.setOnboardingCompleted(true)
                            onFinishSetup()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Enter LockGuard Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Navy800, Navy900)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (isGranted) StatusGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isGranted) StatusGreen.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Rounded.CheckCircle else icon,
                        contentDescription = null,
                        tint = if (isGranted) StatusGreen else ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGranted) "Active & Ready" else "Setup Recommended",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGranted) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Enable Permission", color = ElectricBlue)
                }
            }
        }
    }
}
