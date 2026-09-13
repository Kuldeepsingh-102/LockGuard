package com.lockguard.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for intruder event forensic records.
 */
@Dao
interface IntruderDao {

    @Query("SELECT * FROM intruder_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<IntruderEventEntity>>

    @Query("SELECT * FROM intruder_events WHERE photoPath IS NOT NULL ORDER BY timestamp DESC")
    fun getEventsWithPhotos(): Flow<List<IntruderEventEntity>>

    @Query("SELECT * FROM intruder_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<IntruderEventEntity>>

    @Query("SELECT * FROM intruder_events WHERE id = :id")
    suspend fun getEventById(id: Long): IntruderEventEntity?

    @Query("SELECT * FROM intruder_events WHERE timestamp < :cutoffTimestamp")
    suspend fun getEventsOlderThan(cutoffTimestamp: Long): List<IntruderEventEntity>

    @Query("SELECT COUNT(*) FROM intruder_events")
    fun getTotalAttemptsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM intruder_events WHERE photoPath IS NOT NULL")
    fun getTotalPhotosCount(): Flow<Int>

    @Query("SELECT * FROM intruder_events ORDER BY timestamp DESC LIMIT 1")
    fun getLastEvent(): Flow<IntruderEventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: IntruderEventEntity): Long

    @Query("DELETE FROM intruder_events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM intruder_events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM intruder_events")
    suspend fun deleteAll()

    @Query("SELECT photoPath FROM intruder_events WHERE photoPath IS NOT NULL")
    suspend fun getAllPhotoPaths(): List<String>
}
