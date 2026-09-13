package com.lockguard.app.ui.screens.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockguard.app.data.repository.IntruderRepository
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.domain.model.DetectionSource
import com.lockguard.app.domain.model.IntruderEvent
import com.lockguard.app.domain.model.SecurityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isCapturingTest: Boolean = false,
    val testFeedbackMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val intruderRepository: IntruderRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val securityStatus: StateFlow<SecurityStatus> = settingsRepository.getSecurityStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecurityStatus())

    val recentEvents: StateFlow<List<IntruderEvent>> = intruderRepository.getRecentEvents(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    suspend fun loadPhotoBitmap(filePath: String): Bitmap? {
        return intruderRepository.loadDecryptedPhoto(filePath)
    }

    /**
     * Executes a safe diagnostic test capture to verify CameraX front-camera capture
     * and hardware-backed AES-256-GCM encryption without locking the phone.
     */
    fun performTestCapture(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCapturingTest = true, testFeedbackMessage = null)
            try {
                val event = intruderRepository.recordAttempt(
                    source = DetectionSource.TEST_SIMULATION,
                    lifecycleOwner = lifecycleOwner,
                    notes = "Manual test verification performed from LockGuard Dashboard."
                )

                _uiState.value = _uiState.value.copy(
                    isCapturingTest = false,
                    testFeedbackMessage = if (event.photoPath != null) {
                        "Test capture successful! Encrypted photo saved to secure vault."
                    } else {
                        "Test logged! Camera permission needed to capture photos."
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCapturingTest = false,
                    testFeedbackMessage = "Test capture error: ${e.localizedMessage ?: "Unknown"}"
                )
            }
        }
    }

    fun dismissFeedbackMessage() {
        _uiState.value = _uiState.value.copy(testFeedbackMessage = null)
    }
}
