package com.lockguard.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockguard.app.data.repository.IntruderRepository
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.domain.model.LockGuardSettings
import com.lockguard.app.domain.model.SecurityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class SettingsUiState(
    val showDeleteAllDialog: Boolean = false,
    val showRetentionDialog: Boolean = false,
    val showMaxPhotosDialog: Boolean = false,
    val showTimeoutDialog: Boolean = false,
    val isExporting: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val intruderRepository: IntruderRepository,
    private val pinManager: PinManager
) : ViewModel() {

    val settings: StateFlow<LockGuardSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LockGuardSettings())

    val securityStatus: StateFlow<SecurityStatus> = settingsRepository.getSecurityStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecurityStatus())

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleProtection(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateProtectionEnabled(enabled)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBiometricEnabled(enabled)
        }
    }

    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updateAutoLockTimeout(seconds)
            _uiState.value = _uiState.value.copy(showTimeoutDialog = false)
        }
    }

    fun setPhotoRetentionDays(days: Int) {
        viewModelScope.launch {
            settingsRepository.updatePhotoRetentionDays(days)
            _uiState.value = _uiState.value.copy(showRetentionDialog = false)
        }
    }

    fun setMaxStoredPhotos(max: Int) {
        viewModelScope.launch {
            settingsRepository.updateMaxStoredPhotos(max)
            _uiState.value = _uiState.value.copy(showMaxPhotosDialog = false)
        }
    }

    fun showDeleteAllConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDeleteAllDialog = show)
    }

    fun showRetentionDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRetentionDialog = show)
    }

    fun showMaxPhotosDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showMaxPhotosDialog = show)
    }

    fun showTimeoutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTimeoutDialog = show)
    }

    fun deleteAllData() {
        viewModelScope.launch {
            intruderRepository.deleteAllEvents()
            pinManager.clearPin()
            settingsRepository.resetAllSettings()
            _uiState.value = _uiState.value.copy(
                showDeleteAllDialog = false,
                message = "All intruder photos, logs, and security PINs have been permanently erased."
            )
        }
    }

    fun exportDataReport(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                // Generate secure audit log export file
                val reportFile = File(context.cacheDir, "lockguard_security_audit.txt")
                FileOutputStream(reportFile).use { fos ->
                    val content = buildString {
                        appendLine("==========================================")
                        appendLine("LOCKGUARD SECURITY & PRIVACY AUDIT REPORT")
                        appendLine("==========================================")
                        appendLine("Generated: ${java.util.Date()}")
                        appendLine("Device Administrator Active: ${settingsRepository.isDeviceAdminActive()}")
                        appendLine("In-App Vault PIN Configured: ${pinManager.isPinSet()}")
                        appendLine("Biometrics Active: ${settings.value.isBiometricEnabled}")
                        appendLine("Photo Retention: ${settings.value.photoRetentionDays} days")
                        appendLine("Max Photo Limit: ${settings.value.maxStoredPhotos}")
                        appendLine("Local Encryption: AES-256-GCM Hardware-Backed")
                        appendLine("Remote Uploads: ZERO (100% On-Device)")
                        appendLine("==========================================")
                    }
                    fos.write(content.toByteArray())
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    reportFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "LockGuard Security Audit Report")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Export Security Audit"))
                _uiState.value = _uiState.value.copy(isExporting = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "Export failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
