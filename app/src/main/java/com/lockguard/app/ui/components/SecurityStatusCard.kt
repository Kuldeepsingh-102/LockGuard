package com.lockguard.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lockguard.app.domain.model.SecurityLevel
import com.lockguard.app.domain.model.SecurityStatus
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.ElectricCyan
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy900
import com.lockguard.app.ui.theme.StatusAmber
import com.lockguard.app.ui.theme.StatusGreen
import com.lockguard.app.ui.theme.StatusRed

@Composable
fun SecurityStatusCard(
    status: SecurityStatus,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = status.securityLevel

    val statusColor = when (level) {
        SecurityLevel.MAXIMUM, SecurityLevel.PROTECTED -> StatusGreen
        SecurityLevel.PARTIAL -> StatusAmber
        SecurityLevel.ACTION_REQUIRED, SecurityLevel.DISABLED -> StatusRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Navy800, Navy900)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(statusColor.copy(alpha = 0.8f), ElectricBlue.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShieldLogo(size = 46.dp, isAnimated = true)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "LockGuard Shield",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = level.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor
                        )
                    }
                }
                StatusBadge(level = level)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Diagnostic checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DiagnosticItem(label = "Camera", isReady = status.isCameraPermissionGranted)
                DiagnosticItem(label = "Screen Monitor", isReady = status.isDeviceAdminActive)
                DiagnosticItem(label = "Vault PIN", isReady = status.isAppLockConfigured)
                DiagnosticItem(label = "Alerts", isReady = status.isNotificationPermissionGranted)
            }

            if (level != SecurityLevel.MAXIMUM && level != SecurityLevel.PROTECTED) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSetupClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Complete Security Setup")
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(
    label: String,
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isReady) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
            contentDescription = null,
            tint = if (isReady) StatusGreen else StatusAmber,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isReady) Color.White.copy(alpha = 0.85f) else StatusAmber
        )
    }
}
