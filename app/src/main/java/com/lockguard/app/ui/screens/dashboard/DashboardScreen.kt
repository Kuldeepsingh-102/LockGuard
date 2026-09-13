package com.lockguard.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockguard.app.domain.model.SecurityLevel
import com.lockguard.app.ui.components.EmptyStateView
import com.lockguard.app.ui.components.IntruderPhotoCard
import com.lockguard.app.ui.components.SecurityStatusCard
import com.lockguard.app.ui.components.ShieldLogo
import com.lockguard.app.ui.components.StatCard
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.ElectricCyan
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy950
import com.lockguard.app.ui.theme.StatusGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val securityStatus by viewModel.securityStatus.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.testFeedbackMessage) {
        uiState.testFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedbackMessage()
        }
    }

    val lastAttemptFormatted = remember(securityStatus.lastAttemptTimestamp) {
        val ts = securityStatus.lastAttemptTimestamp
        if (ts != null) {
            val date = Date(ts)
            val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            formatter.format(date)
        } else {
            "None recorded"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShieldLogo(size = 36.dp, isAnimated = false)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LockGuard",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .size(42.dp)
                            .background(Navy800, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = "Intruder Gallery",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .background(Navy800, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Security Status Banner
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                SecurityStatusCard(
                    status = securityStatus,
                    onSetupClick = onNavigateToSetup
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Diagnostic Simulation Button
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = { viewModel.performTestCapture(lifecycleOwner) },
                    enabled = !uiState.isCapturingTest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy800,
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isCapturingTest) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ElectricCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Capturing Front Camera...")
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Intruder Capture & Encryption",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2x2 Grid of Key Stats
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Failed Attempts",
                        value = "${securityStatus.totalAttempts}",
                        icon = Icons.Rounded.Warning,
                        modifier = Modifier.weight(1f),
                        accentColor = if (securityStatus.totalAttempts > 0) MaterialTheme.colorScheme.error else StatusGreen
                    )
                    StatCard(
                        title = "Captured Photos",
                        value = "${securityStatus.photosCount}",
                        icon = Icons.Rounded.CameraAlt,
                        modifier = Modifier.weight(1f),
                        accentColor = ElectricBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Last Attempt",
                        value = if (securityStatus.lastAttemptTimestamp != null) "Detected" else "None",
                        subtitle = lastAttemptFormatted,
                        icon = Icons.Rounded.History,
                        modifier = Modifier.weight(1f),
                        accentColor = ElectricCyan
                    )
                    StatCard(
                        title = "Guard Shield",
                        value = if (securityStatus.isProtectionEnabled) "Active" else "Disabled",
                        subtitle = if (securityStatus.isDeviceAdminActive) "Screen & App Lock" else "App Lock Only",
                        icon = Icons.Rounded.Security,
                        modifier = Modifier.weight(1f),
                        accentColor = if (securityStatus.isProtectionEnabled) StatusGreen else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recent Intruder Photos Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Intruder Events",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                if (recentEvents.isNotEmpty()) {
                    TextButton(onClick = onNavigateToGallery) {
                        Text(
                            text = "View All (${recentEvents.size})",
                            color = ElectricCyan,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Intruder Photos Strip
            if (recentEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    EmptyStateView(
                        title = "No Unauthorized Attempts",
                        subtitle = "Your device has no failed access records. Use the Test Capture button above to simulate an event."
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentEvents, key = { it.id }) { event ->
                        IntruderPhotoCard(
                            event = event,
                            onLoadBitmap = { path -> viewModel.loadPhotoBitmap(path) },
                            onClick = { onNavigateToDetail(event.id) },
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
