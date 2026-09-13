package com.lockguard.app.data.repository

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.lockguard.app.data.camera.CameraCaptureManager
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.security.BiometricHelper
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.domain.model.LockGuardSettings
import com.lockguard.app.domain.model.SecurityStatus
import com.lockguard.app.receiver.LockGuardDeviceAdminReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [SettingsRepository].
 * Tracks device health, permissions, admin state, and user preferences.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val context: Context,
    private val pinManager: PinManager,
    private val biometricHelper: BiometricHelper,
    private val cameraCaptureManager: CameraCaptureManager,
    private val intruderDao: IntruderDao
) : SettingsRepository {

    companion object {
        private const val PREFS_SETTINGS = "lockguard_settings"
        private const val KEY_PROTECTION_ENABLED = "protection_enabled"
        private const val KEY_NOTIF_ENABLED = "notif_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_APPLOCK_ENABLED = "applock_enabled"
        private const val KEY_AUTOLOCK_TIMEOUT = "autolock_timeout"
        private const val KEY_RETENTION_DAYS = "retention_days"
        private const val KEY_MAX_PHOTOS = "max_photos"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)

    private val settingsFlow = MutableStateFlow(loadSettings())

    private fun loadSettings(): LockGuardSettings {
        return LockGuardSettings(
            isProtectionEnabled = prefs.getBoolean(KEY_PROTECTION_ENABLED, true),
            isNotificationsEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            isBiometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true),
            isAppLockEnabled = prefs.getBoolean(KEY_APPLOCK_ENABLED, pinManager.isPinSet()),
            autoLockTimeoutSeconds = prefs.getInt(KEY_AUTOLOCK_TIMEOUT, 30),
            photoRetentionDays = prefs.getInt(KEY_RETENTION_DAYS, 30),
            maxStoredPhotos = prefs.getInt(KEY_MAX_PHOTOS, 50),
            isOnboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        )
    }

    override fun getSettings(): Flow<LockGuardSettings> = settingsFlow

    override fun isDeviceAdminActive(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, LockGuardDeviceAdminReceiver::class.java)
        return dpm?.isAdminActive(adminComponent) == true
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun getSecurityStatus(): Flow<SecurityStatus> {
        return combine(
            settingsFlow,
            intruderDao.getTotalAttemptsCount(),
            intruderDao.getTotalPhotosCount(),
            intruderDao.getLastEvent()
        ) { settings, attempts, photos, lastEvent ->
            SecurityStatus(
                isProtectionEnabled = settings.isProtectionEnabled,
                isCameraPermissionGranted = cameraCaptureManager.hasCameraPermission(),
                isDeviceAdminActive = isDeviceAdminActive(),
                isNotificationPermissionGranted = isNotificationPermissionGranted(),
                isAppLockConfigured = pinManager.isPinSet(),
                isBiometricAvailable = biometricHelper.isBiometricAvailable(),
                totalAttempts = attempts,
                photosCount = photos,
                lastAttemptTimestamp = lastEvent?.timestamp
            )
        }
    }

    override suspend fun updateProtectionEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun updateBiometricEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun updateAppLockEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_APPLOCK_ENABLED, enabled).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun updateAutoLockTimeout(seconds: Int) = withContext(Dispatchers.IO) {
        prefs.edit().putInt(KEY_AUTOLOCK_TIMEOUT, seconds).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun updatePhotoRetentionDays(days: Int) = withContext(Dispatchers.IO) {
        prefs.edit().putInt(KEY_RETENTION_DAYS, days).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun updateMaxStoredPhotos(max: Int) = withContext(Dispatchers.IO) {
        prefs.edit().putInt(KEY_MAX_PHOTOS, max).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
        settingsFlow.value = loadSettings()
    }

    override suspend fun resetAllSettings() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
        pinManager.clearPin()
        settingsFlow.value = loadSettings()
    }
}
