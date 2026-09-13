package com.lockguard.app.data.repository

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.lockguard.app.data.camera.CameraCaptureManager
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.database.IntruderEventEntity
import com.lockguard.app.data.security.CryptoManager
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.domain.model.IntruderEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [IntruderRepository].
 * Coordinates Room DB, CameraX, and Hardware-backed AES-256-GCM encryption.
 */
@Singleton
class IntruderRepositoryImpl @Inject constructor(
    private val intruderDao: IntruderDao,
    private val cryptoManager: CryptoManager,
    private val cameraCaptureManager: CameraCaptureManager
) : IntruderRepository {

    override fun getAllEvents(): Flow<List<IntruderEvent>> {
        return intruderDao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEventsWithPhotos(): Flow<List<IntruderEvent>> {
        return intruderDao.getEventsWithPhotos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentEvents(limit: Int): Flow<List<IntruderEvent>> {
        return intruderDao.getRecentEvents(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalAttemptsCount(): Flow<Int> = intruderDao.getTotalAttemptsCount()

    override fun getTotalPhotosCount(): Flow<Int> = intruderDao.getTotalPhotosCount()

    override fun getLastEvent(): Flow<IntruderEvent?> {
        return intruderDao.getLastEvent().map { it?.toDomain() }
    }

    override suspend fun getEventById(id: Long): IntruderEvent? = withContext(Dispatchers.IO) {
        intruderDao.getEventById(id)?.toDomain()
    }

    override suspend fun recordAttempt(
        source: DetectionSource,
        lifecycleOwner: LifecycleOwner?,
        notes: String?
    ): IntruderEvent = withContext(Dispatchers.IO) {
        // Calculate consecutive attempt count
        val currentTotal = intruderDao.getTotalAttemptsCount().firstOrNull() ?: 0
        val attemptNumber = currentTotal + 1

        var photoPath: String? = null

        // If lifecycle owner is available and camera permission is granted, capture intruder photo
        if (lifecycleOwner != null && cameraCaptureManager.hasCameraPermission()) {
            val captureResult = cameraCaptureManager.captureIntruderPhoto(lifecycleOwner)
            if (captureResult.isSuccess) {
                photoPath = captureResult.getOrNull()
            }
        }

        val entity = IntruderEventEntity(
            timestamp = System.currentTimeMillis(),
            attemptNumber = attemptNumber,
            photoPath = photoPath,
            source = source,
            notes = notes,
            isEncrypted = photoPath != null
        )

        val insertedId = intruderDao.insert(entity)
        entity.copy(id = insertedId).toDomain()
    }

    override suspend fun loadDecryptedPhoto(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        cryptoManager.decryptPhotoToBitmap(filePath)
    }

    override suspend fun preparePhotoForExport(filePath: String): File? = withContext(Dispatchers.IO) {
        cryptoManager.createTemporaryShareFile(filePath)
    }

    override suspend fun deleteEvent(event: IntruderEvent): Boolean = withContext(Dispatchers.IO) {
        try {
            event.photoPath?.let { cryptoManager.deletePhotoFile(it) }
            intruderDao.deleteById(event.id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteEvents(ids: List<Long>): Boolean = withContext(Dispatchers.IO) {
        try {
            for (id in ids) {
                val entity = intruderDao.getEventById(id)
                entity?.photoPath?.let { cryptoManager.deletePhotoFile(it) }
            }
            intruderDao.deleteByIds(ids)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteAllEvents(): Boolean = withContext(Dispatchers.IO) {
        try {
            cryptoManager.clearAllPhotos()
            intruderDao.deleteAll()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun purgeOldEvents(cutoffTimestamp: Long): Int = withContext(Dispatchers.IO) {
        val oldEvents = intruderDao.getEventsOlderThan(cutoffTimestamp)
        for (event in oldEvents) {
            event.photoPath?.let { cryptoManager.deletePhotoFile(it) }
            intruderDao.deleteById(event.id)
        }
        oldEvents.size
    }
}
