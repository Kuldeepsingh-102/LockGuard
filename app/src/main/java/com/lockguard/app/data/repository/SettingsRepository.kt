package com.lockguard.app.data.repository

import com.lockguard.app.domain.model.LockGuardSettings
import com.lockguard.app.domain.model.SecurityStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user preferences and device protection diagnostics.
 */
interface SettingsRepository {

    fun getSettings(): Flow<LockGuardSettings>

    fun getSecurityStatus(): Flow<SecurityStatus>

    fun isDeviceAdminActive(): Boolean

    suspend fun updateProtectionEnabled(enabled: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)

    suspend fun updateBiometricEnabled(enabled: Boolean)

    suspend fun updateAppLockEnabled(enabled: Boolean)

    suspend fun updateAutoLockTimeout(seconds: Int)

    suspend fun updatePhotoRetentionDays(days: Int)

    suspend fun updateMaxStoredPhotos(max: Int)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun resetAllSettings()
}
