package com.lockguard.app.ui.screens.about

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lockguard.app.ui.components.LockGuardTopBar
import com.lockguard.app.ui.components.ShieldLogo
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.ElectricCyan
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy900
import com.lockguard.app.ui.theme.Navy950

@Composable
fun AboutScreen(
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
                title = "About LockGuard",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                ShieldLogo(size = 90.dp, isAnimated = true)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "LockGuard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Version 1.0.0 (Production Build)",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "LockGuard is a modern privacy-first Android security application engineered to identify unauthorized device access attempts while rigorously upholding user privacy and Android security rules.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Tech Stack & Architecture Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy900, RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Engineering Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TechItem(title = "Kotlin & Jetpack Compose", desc = "Declarative Material 3 UI with Edge-to-Edge")
                    TechItem(title = "CameraX Subsystem", desc = "Low-latency front camera capture & orientation handling")
                    TechItem(title = "Android Keystore", desc = "Hardware-backed AES-256-GCM symmetric encryption")
                    TechItem(title = "Room Database", desc = "Local reactive SQLite persistence with Flow")
                    TechItem(title = "Hilt Dependency Injection", desc = "Loose coupling & clean architectural layers")
                    TechItem(title = "Device Administration API", desc = "Official OS lock-screen watch-login policy")
                    TechItem(title = "WorkManager", desc = "Automated background retention & quota enforcement")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TechItem(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = ElectricBlue,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
