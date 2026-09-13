package com.lockguard.app.ui.screens.applock

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockguard.app.data.repository.IntruderRepository
import com.lockguard.app.data.security.BiometricHelper
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.domain.model.DetectionSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PinScreenMode {
    AUTHENTICATE,
    CREATE_PIN,
    CONFIRM_PIN
}

data class AppLockUiState(
    val mode: PinScreenMode = PinScreenMode.AUTHENTICATE,
    val enteredPin: String = "",
    val firstEnteredPin: String = "",
    val errorMessage: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isUnlocked: Boolean = false,
    val isPinSetupComplete: Boolean = false,
    val returnRoute: String? = null
)

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val pinManager: PinManager,
    private val biometricHelper: BiometricHelper,
    private val intruderRepository: IntruderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val isChangingPin: Boolean = savedStateHandle.get<String>("isChangingPin")?.toBoolean() ?: false
    private val returnRouteParam: String? = savedStateHandle.get<String>("returnRoute")?.takeIf { it.isNotEmpty() }

    private val _uiState = MutableStateFlow(
        AppLockUiState(
            mode = if (!pinManager.isPinSet() || isChangingPin) PinScreenMode.CREATE_PIN else PinScreenMode.AUTHENTICATE,
            isBiometricAvailable = biometricHelper.isBiometricAvailable(),
            returnRoute = returnRouteParam
        )
    )
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

    fun onDigitClick(digit: String, lifecycleOwner: LifecycleOwner) {
        val current = _uiState.value.enteredPin
        if (current.length >= 4) return

        val next = current + digit
        _uiState.value = _uiState.value.copy(enteredPin = next, errorMessage = null)

        if (next.length == 4) {
            handleCompletePin(next, lifecycleOwner)
        }
    }

    fun onBackspaceClick() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                enteredPin = current.dropLast(1),
                errorMessage = null
            )
        }
    }

    private fun handleCompletePin(pin: String, lifecycleOwner: LifecycleOwner) {
        when (_uiState.value.mode) {
            PinScreenMode.AUTHENTICATE -> {
                val isValid = pinManager.verifyPin(pin)
                if (isValid) {
                    _uiState.value = _uiState.value.copy(isUnlocked = true)
                } else {
                    // Trigger intruder capture on failed PIN authentication!
                    viewModelScope.launch {
                        intruderRepository.recordAttempt(
                            source = DetectionSource.IN_APP_PIN_FAILURE,
                            lifecycleOwner = lifecycleOwner,
                            notes = "Failed PIN entry attempt inside LockGuard vault."
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        enteredPin = "",
                        errorMessage = "Incorrect PIN. Intruder capture triggered."
                    )
                }
            }

            PinScreenMode.CREATE_PIN -> {
                _uiState.value = _uiState.value.copy(
                    mode = PinScreenMode.CONFIRM_PIN,
                    firstEnteredPin = pin,
                    enteredPin = "",
                    errorMessage = null
                )
            }

            PinScreenMode.CONFIRM_PIN -> {
                if (pin == _uiState.value.firstEnteredPin) {
                    pinManager.setPin(pin)
                    _uiState.value = _uiState.value.copy(
                        isPinSetupComplete = true,
                        isUnlocked = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        mode = PinScreenMode.CREATE_PIN,
                        enteredPin = "",
                        firstEnteredPin = "",
                        errorMessage = "PINs do not match. Try again."
                    )
                }
            }
        }
    }

    fun triggerBiometricAuthentication(activity: FragmentActivity) {
        if (!biometricHelper.isBiometricAvailable() || _uiState.value.mode != PinScreenMode.AUTHENTICATE) return

        biometricHelper.authenticate(
            activity = activity,
            onSuccess = {
                pinManager.unlockApp()
                _uiState.value = _uiState.value.copy(isUnlocked = true)
            },
            onError = { _, errString ->
                _uiState.value = _uiState.value.copy(errorMessage = errString.toString())
            },
            onFailed = {
                _uiState.value = _uiState.value.copy(errorMessage = "Biometric not recognized.")
            }
        )
    }
}
