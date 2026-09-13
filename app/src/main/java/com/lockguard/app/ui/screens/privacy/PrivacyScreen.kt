package com.lockguard.app.ui.screens.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lockguard.app.ui.components.LockGuardTopBar
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy900
import com.lockguard.app.ui.theme.Navy950
import com.lockguard.app.ui.theme.StatusGreen

@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LockGuardTopBar(
                title = "Privacy & Security Guarantee",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Our Architecture & Privacy Principles",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "LockGuard is architected strictly around zero-knowledge principles. Your security and biometric privacy are protected by hardware and mathematics, not marketing promises.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Pillar 1: 100% Local Storage & Zero Uploads
                PrivacyPillarCard(
                    title = "100% On-Device & Zero Cloud Telemetry",
                    description = "Captured photos and authentication logs are strictly saved in internal, sandboxed app-private storage. LockGuard contains zero tracking SDKs, zero background image uploads, and connects to no external photo servers.",
                    icon = Icons.Rounded.CloudOff,
                    accentColor = StatusGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pillar 2: Hardware-Backed Android Keystore AES-256
                PrivacyPillarCard(
                    title = "Hardware-Backed AES-256 Encryption",
                    description = "Intruder images are encrypted using AES/GCM/NoPadding before being written to storage. Encryption keys reside in the device's hardware-backed Secure Element / StrongBox Android KeyStore.",
                    icon = Icons.Rounded.Lock,
                    accentColor = ElectricBlue
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pillar 3: Zero Password Snooping Guarantee
                PrivacyPillarCard(
                    title = "Zero Password Access or Interception",
                    description = "LockGuard never attempts to read, store, intercept, or transmit your device lock-screen password, PIN, or pattern. Android's official Device Administrator API only transmits a signal that an incorrect attempt occurred.",
                    icon = Icons.Rounded.VpnKey,
                    accentColor = ElectricBlue
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pillar 4: Android OS Background Camera Rules
                PrivacyPillarCard(
                    title = "Android Background Camera Compliance",
                    description = "Modern Android blocks silent camera use from receivers while the phone is locked. LockGuard logs the failed unlock attempt immediately, then captures the photo only from a visible Capture screen launched via the security notification or right after unlock — never via hidden APIs.",
                    icon = Icons.Rounded.CameraAlt,
                    accentColor = StatusGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pillar 5: Complete User Sovereignty
                PrivacyPillarCard(
                    title = "Complete Data Sovereignty & Erase",
                    description = "You have full control. You can single-delete, multi-select delete, export evidence only on explicit action, or perform a total cryptographic wipe of all evidence directly from the settings.",
                    icon = Icons.Rounded.Shield,
                    accentColor = ElectricBlue
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PrivacyPillarCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy900, RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}
