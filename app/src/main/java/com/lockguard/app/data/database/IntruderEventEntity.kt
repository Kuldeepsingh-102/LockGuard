package com.lockguard.app.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.domain.model.IntruderEvent

/**
 * Room persistence entity for recording unauthorized authentication attempts.
 * Stores metadata and encrypted photo reference locally on the device.
 */
@Entity(
    tableName = "intruder_events",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["source"])
    ]
)
data class IntruderEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val attemptNumber: Int = 1,
    val photoPath: String? = null,
    val source: DetectionSource = DetectionSource.IN_APP_PIN_FAILURE,
    val notes: String? = null,
    val isEncrypted: Boolean = true
) {
    fun toDomain(): IntruderEvent {
        return IntruderEvent(
            id = id,
            timestamp = timestamp,
            attemptNumber = attemptNumber,
            photoPath = photoPath,
            source = source,
            notes = notes,
            isEncrypted = isEncrypted
        )
    }

    companion object {
        fun fromDomain(domain: IntruderEvent): IntruderEventEntity {
            return IntruderEventEntity(
                id = domain.id,
                timestamp = domain.timestamp,
                attemptNumber = domain.attemptNumber,
                photoPath = domain.photoPath,
                source = domain.source,
                notes = domain.notes,
                isEncrypted = domain.isEncrypted
            )
        }
    }
}
