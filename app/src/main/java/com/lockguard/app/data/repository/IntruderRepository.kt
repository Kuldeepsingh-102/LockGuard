package com.lockguard.app.data.repository

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.domain.model.IntruderEvent
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Single source of truth for managing intruder forensic events, encrypted photos, and security alerts.
 */
interface IntruderRepository {

    fun getAllEvents(): Flow<List<IntruderEvent>>

    fun getEventsWithPhotos(): Flow<List<IntruderEvent>>

    fun getRecentEvents(limit: Int = 5): Flow<List<IntruderEvent>>

    fun getTotalAttemptsCount(): Flow<Int>

    fun getTotalPhotosCount(): Flow<Int>

    fun getLastEvent(): Flow<IntruderEvent?>

    suspend fun getEventById(id: Long): IntruderEvent?

    /**
     * Records a failed authentication attempt with optional CameraX capture.
     */
    suspend fun recordAttempt(
        source: DetectionSource,
        lifecycleOwner: LifecycleOwner? = null,
        notes: String? = null
    ): IntruderEvent

    /**
     * Decrypts an encrypted intruder photo into memory as a Bitmap.
     */
    suspend fun loadDecryptedPhoto(filePath: String): Bitmap?

    /**
     * Generates a temporary decrypted cache file for user-initiated sharing.
     */
    suspend fun preparePhotoForExport(filePath: String): File?

    suspend fun deleteEvent(event: IntruderEvent): Boolean

    suspend fun deleteEvents(ids: List<Long>): Boolean

    suspend fun deleteAllEvents(): Boolean

    suspend fun purgeOldEvents(cutoffTimestamp: Long): Int
}
