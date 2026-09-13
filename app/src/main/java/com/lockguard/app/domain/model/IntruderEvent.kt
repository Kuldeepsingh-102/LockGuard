package com.lockguard.app.domain.model

/**
 * Domain representation of an unauthorized authentication event.
 * Contains forensic metadata and path to the AES-256 encrypted photo.
 */
data class IntruderEvent(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val attemptNumber: Int = 1,
    val photoPath: String? = null,
    val source: DetectionSource = DetectionSource.IN_APP_PIN_FAILURE,
    val notes: String? = null,
    val isEncrypted: Boolean = true
)
