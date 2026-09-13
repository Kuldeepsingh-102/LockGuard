package com.lockguard.app

import com.lockguard.app.domain.model.SecurityLevel
import com.lockguard.app.domain.model.SecurityStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying security status and health scoring.
 */
class SecurityStatusTest {

    @Test
    fun `maximum protection when all permissions and policies are active`() {
        val status = SecurityStatus(
            isProtectionEnabled = true,
            isCameraPermissionGranted = true,
            isDeviceAdminActive = true,
            isNotificationPermissionGranted = true,
            isAppLockConfigured = true,
            isBiometricAvailable = true
        )

        assertEquals(SecurityLevel.MAXIMUM, status.securityLevel)
    }

    @Test
    fun `disabled level when master protection is switched off`() {
        val status = SecurityStatus(
            isProtectionEnabled = false,
            isCameraPermissionGranted = true,
            isDeviceAdminActive = true,
            isNotificationPermissionGranted = true,
            isAppLockConfigured = true
        )

        assertEquals(SecurityLevel.DISABLED, status.securityLevel)
    }

    @Test
    fun `action required level when setup has not been performed`() {
        val status = SecurityStatus(
            isProtectionEnabled = true,
            isCameraPermissionGranted = false,
            isDeviceAdminActive = false,
            isAppLockConfigured = false
        )

        assertEquals(SecurityLevel.ACTION_REQUIRED, status.securityLevel)
    }
}
