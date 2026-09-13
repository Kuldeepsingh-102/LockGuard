package com.lockguard.app.data.database

import androidx.room.TypeConverter
import com.lockguard.app.domain.model.DetectionSource

/**
 * Type converters for storing custom enums in Room SQLite database.
 */
class Converters {
    @TypeConverter
    fun fromDetectionSource(source: DetectionSource): String {
        return source.name
    }

    @TypeConverter
    fun toDetectionSource(value: String): DetectionSource {
        return try {
            DetectionSource.valueOf(value)
        } catch (e: IllegalArgumentException) {
            DetectionSource.IN_APP_PIN_FAILURE
        }
    }
}
