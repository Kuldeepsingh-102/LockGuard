package com.lockguard.app.ui.screens.settings

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockguard.app.R
import com.lockguard.app.receiver.LockGuardDeviceAdminReceiver
import com.lockguard.app.ui.components.LockGuardTopBar
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy900
import com.lockguard.app.ui.theme.Navy950
import com.lockguard.app.ui.theme.StatusGreen
import com.lockguard.app.ui.theme.StatusRed

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onChangePin: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val securityStatus by viewModel.securityStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LockGuardTopBar(
                title = "Settings",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Section 1: Protection & Detection
                SettingsSectionHeader(title = "Intruder Protection & Detection")

                SettingsCard {
                    SettingsToggleRow(
                        title = "Intruder Detection Guard",
                        subtitle = "Enable or pause unauthorized attempt detection",
                        icon = Icons.Rounded.Security,
                        isChecked = settings.isProtectionEnabled,
                        onCheckedChange = { viewModel.toggleProtection(it) }
                    )

                    SettingsDivider()

                    SettingsClickRow(
                        title = "Screen Unlock Monitor",
                        subtitle = if (securityStatus.isDeviceAdminActive) "Active (Device Administrator)" else "Tap to enable Device Admin policy",
                        icon = Icons.Rounded.Security,
                        valueColor = if (securityStatus.isDeviceAdminActive) StatusGreen else ElectricBlue,
                        onClick = {
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

                    SettingsDivider()

                    SettingsClickRow(
                        title = "Camera Permission",
                        subtitle = if (securityStatus.isCameraPermissionGranted) "Granted (Front Camera Ready)" else "Required for intruder photo capture",
                        icon = Icons.Rounded.CameraAlt,
                        valueColor = if (securityStatus.isCameraPermissionGranted) StatusGreen else ElectricBlue,
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )

                    SettingsDivider()

                    SettingsToggleRow(
                        title = "Discreet Security Alerts",
                        subtitle = "Notify on unauthorized attempts without exposing photos",
                        icon = Icons.Rounded.Notifications,
                        isChecked = settings.isNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Vault & App Lock
                SettingsSectionHeader(title = "App Vault & Authentication")

                SettingsCard {
                    SettingsToggleRow(
                        title = "Biometric Unlock",
                        subtitle = "Use fingerprint or face unlock to access evidence",
                        icon = Icons.Rounded.Fingerprint,
                        isChecked = settings.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) }
                    )

                    SettingsDivider()

                    SettingsClickRow(
                        title = "Change App Lock PIN",
                        subtitle = "Set or update your 4-digit in-app vault PIN",
                        icon = Icons.Rounded.Pin,
                        onClick = onChangePin
                    )

                    SettingsDivider()

                    SettingsClickRow(
                        title = "Auto-Lock Inactivity Timeout",
                        subtitle = when (settings.autoLockTimeoutSeconds) {
                            0 -> "Immediately on backgrounding"
                            30 -> "After 30 seconds"
                            60 -> "After 1 minute"
                            else -> "After ${settings.autoLockTimeoutSeconds / 60} minutes"
                        },
                        icon = Icons.Rounded.Timer,
                        onClick = { viewModel.showTimeoutDialog(true) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Storage & Retention Policy
                SettingsSectionHeader(title = "Evidence Storage & Privacy")

                SettingsCard {
                    SettingsClickRow(
                        title = "Auto-Delete Retention Window",
                        subtitle = if (settings.photoRetentionDays == 0) "Keep forever" else "Auto-purge after ${settings.photoRetentionDays} days",
                        icon = Icons.Rounded.Timer,
                        onClick = { viewModel.showRetentionDialog(true) }
                    )

                    SettingsDivider()

                    SettingsClickRow(
                        title = "Maximum Stored Photos",
                        subtitle = if (settings.maxStoredPhotos == 0) "Unlimited" else "Limit to ${settings.maxStoredPhotos} photos",
                        icon = Icons.Rounded.CameraAlt,
                        onClick = { viewModel.showMaxPhotosDialog(true) }
                    )

                    SettingsDivider()

                    SettingsClickRow(
                        title = "Export Security Audit Report",
                        subtitle = "Generate an encrypted summary of detected events",
                        icon = Icons.Rounded.FileDownload,
                        onClick = { viewModel.exportDataReport(context) }
                    )

                    SettingsDivider()

                    SettingsClickRow(
                        title = "Delete All Evidence & Data",
                        subtitle = "Permanently wipe all photos, logs, and reset PIN",
                        icon = Icons.Rounded.DeleteForever,
                        valueColor = StatusRed,
                        onClick = { viewModel.showDeleteAllConfirmation(true) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 4: Privacy & Information
                SettingsSectionHeader(title = "Transparency & Policy")

                SettingsCard {
                    SettingsClickRow(
                        title = "Privacy & Security Policy",
                        subtitle = "Zero server uploads, local Keystore encryption guarantee",
                        icon = Icons.Rounded.Policy,
                        onClick = onNavigateToPrivacy
                    )

                    SettingsDivider()

                    SettingsClickRow(
                        title = "About LockGuard",
                        subtitle = "Version 1.0.0 • Architecture & Android Security",
                        icon = Icons.Rounded.Info,
                        onClick = onNavigateToAbout
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        // Delete All Confirmation Dialog
        if (uiState.showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteAllConfirmation(false) },
                title = { Text(text = "Erase All Data & Reset?") },
                text = {
                    Text(
                        text = "This will permanently delete all encrypted intruder photographs, reset the event database, and erase your in-app PIN. This action cannot be reversed."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteAllData() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                    ) {
                        Text(text = "Erase Everything", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteAllConfirmation(false) }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }

        // Auto-Lock Timeout Dialog
        if (uiState.showTimeoutDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showTimeoutDialog(false) },
                title = { Text(text = "Inactivity Auto-Lock") },
                text = {
                    Column {
                        listOf(0 to "Immediately", 30 to "30 seconds", 60 to "1 minute", 300 to "5 minutes").forEach { (sec, label) ->
                            TextButton(
                                onClick = { viewModel.setAutoLockTimeout(sec) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = label,
                                    color = if (settings.autoLockTimeoutSeconds == sec) ElectricBlue else Color.White
                                )
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Photo Retention Dialog
        if (uiState.showRetentionDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showRetentionDialog(false) },
                title = { Text(text = "Photo Retention Policy") },
                text = {
                    Column {
                        listOf(7 to "7 days", 14 to "14 days", 30 to "30 days", 0 to "Keep Forever").forEach { (days, label) ->
                            TextButton(
                                onClick = { viewModel.setPhotoRetentionDays(days) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = label,
                                    color = if (settings.photoRetentionDays == days) ElectricBlue else Color.White
                                )
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Max Photos Dialog
        if (uiState.showMaxPhotosDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showMaxPhotosDialog(false) },
                title = { Text(text = "Maximum Stored Photos") },
                text = {
                    Column {
                        listOf(25 to "25 photos", 50 to "50 photos", 100 to "100 photos", 0 to "Unlimited").forEach { (max, label) ->
                            TextButton(
                                onClick = { viewModel.setMaxStoredPhotos(max) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = label,
                                    color = if (settings.maxStoredPhotos == max) ElectricBlue else Color.White
                                )
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = ElectricBlue,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy900, RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Navy800, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricBlue,
                uncheckedTrackColor = Navy800
            )
        )
    }
}

@Composable
private fun SettingsClickRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    valueColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Navy800, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = valueColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
