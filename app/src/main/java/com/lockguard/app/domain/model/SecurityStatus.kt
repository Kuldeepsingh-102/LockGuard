package com.lockguard.app.domain.model

/**
 * Diagnostic health status representing the overall readiness and protection level of LockGuard.
 */
data class SecurityStatus(
    val isProtectionEnabled: Boolean = true,
    val isCameraPermissionGranted: Boolean = false,
    val isDeviceAdminActive: Boolean = false,
    val isNotificationPermissionGranted: Boolean = false,
    val isAppLockConfigured: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val totalAttempts: Int = 0,
    val photosCount: Int = 0,
    val lastAttemptTimestamp: Long? = null
) {
    /**
     * Protection score calculation:
     * - Fully protected: Protection enabled, Camera granted, App lock set, Device admin active
     * - Partially protected: Protection enabled and at least one detection vector ready
     * - Unprotected: Protection disabled or permissions completely lacking
     */
    val securityLevel: SecurityLevel
        get() {
            if (!isProtectionEnabled) return SecurityLevel.DISABLED
            return when {
                isCameraPermissionGranted && isDeviceAdminActive && isAppLockConfigured -> SecurityLevel.MAXIMUM
                isCameraPermissionGranted && (isDeviceAdminActive || isAppLockConfigured) -> SecurityLevel.PROTECTED
                isAppLockConfigured || isCameraPermissionGranted -> SecurityLevel.PARTIAL
                else -> SecurityLevel.ACTION_REQUIRED
            }
        }
}

enum class SecurityLevel(val label: String) {
    MAXIMUM("Maximum Protection"),
    PROTECTED("Actively Protected"),
    PARTIAL("Partially Protected"),
    ACTION_REQUIRED("Setup Required"),
    DISABLED("Protection Disabled")
}
