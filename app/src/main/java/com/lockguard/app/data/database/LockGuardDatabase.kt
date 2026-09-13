package com.lockguard.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Main Room Database for LockGuard.
 * Keeps persistent history of failed authentication events and encrypted photo links.
 */
@Database(
    entities = [IntruderEventEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LockGuardDatabase : RoomDatabase() {
    abstract fun intruderDao(): IntruderDao
}
