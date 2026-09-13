package com.lockguard.app.domain.model

/**
 * Identifies the origin and context of a detected unauthorized authentication event.
 */
enum class DetectionSource(val displayName: String, val description: String) {
    /**
     * Triggered through Android's DeviceAdminReceiver (watch-login policy)
     * when an incorrect PIN/Pattern/Password was entered on the OS lock screen.
     */
    SYSTEM_LOCK_SCREEN(
        displayName = "System Lock Screen",
        description = "Detected via Android DeviceAdminReceiver policy"
    ),

    /**
     * Triggered when an unauthorized person attempts to access LockGuard
     * with an incorrect in-app vault PIN.
     */
    IN_APP_PIN_FAILURE(
        displayName = "In-App Vault Lock",
        description = "Failed PIN entry attempt inside LockGuard"
    ),

    /**
     * Safe user-initiated security test to verify camera capture and encryption.
     */
    TEST_SIMULATION(
        displayName = "Diagnostic Test",
        description = "Manual verification of intruder capture system"
    )
}
