package com.lockguard.app.domain.model

/**
 * User-configurable security and privacy preferences.
 */
data class LockGuardSettings(
    val isProtectionEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isBiometricEnabled: Boolean = true,
    val isAppLockEnabled: Boolean = false,
    val autoLockTimeoutSeconds: Int = 30, // 0 = immediately, 30, 60, 300
    val photoRetentionDays: Int = 30,      // 0 = keep forever, 7, 14, 30
    val maxStoredPhotos: Int = 50,         // 0 = unlimited, 25, 50, 100
    val isOnboardingCompleted: Boolean = false
)
