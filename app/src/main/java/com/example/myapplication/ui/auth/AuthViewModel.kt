package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val isPinSet: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val pinInput: String = "",
    val pinError: String? = null,
    val isCreatingPin: Boolean = false,
    val confirmPinInput: String = "",
    val isAuthenticated: Boolean = false,
    val authStatusMessage: String? = null
)

class AuthViewModel(
    private val securityManager: SecurityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isPinSet = securityManager.isPinSet,
            isBiometricEnabled = securityManager.isBiometricEnabled,
            isCreatingPin = !securityManager.isPinSet,
            authStatusMessage = if (securityManager.isPinSet) "Enter Security PIN" else "Create Security PIN"
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPinDigitEntered(digit: String) {
        val currentState = _uiState.value
        if (currentState.pinInput.length < 4) {
            val newPin = currentState.pinInput + digit
            _uiState.update { it.copy(pinInput = newPin, pinError = null) }

            if (newPin.length == 4) {
                if (!currentState.isPinSet) {
                    // Setting up new PIN
                    if (currentState.confirmPinInput.isEmpty()) {
                        // First entry complete, ask to confirm
                        _uiState.update {
                            it.copy(
                                confirmPinInput = newPin,
                                pinInput = "",
                                authStatusMessage = "Re-enter PIN to confirm"
                            )
                        }
                    } else if (currentState.confirmPinInput == newPin) {
                        // PIN matched and saved
                        securityManager.setPin(newPin)
                        securityManager.lastUnlockedTime = System.currentTimeMillis()
                        _uiState.update {
                            it.copy(
                                isPinSet = true,
                                isCreatingPin = false,
                                isAuthenticated = true
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                pinInput = "",
                                confirmPinInput = "",
                                pinError = "PINs do not match. Try again.",
                                authStatusMessage = "Create Security PIN"
                            )
                        }
                    }
                } else {
                    // Verifying existing PIN
                    if (securityManager.verifyPin(newPin)) {
                        securityManager.lastUnlockedTime = System.currentTimeMillis()
                        _uiState.update {
                            it.copy(
                                isAuthenticated = true,
                                pinError = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                pinInput = "",
                                pinError = "Incorrect PIN. Please try again."
                            )
                        }
                    }
                }
            }
        }
    }

    fun onPinDelete() {
        _uiState.update {
            if (it.pinInput.isNotEmpty()) {
                it.copy(pinInput = it.pinInput.dropLast(1), pinError = null)
            } else it
        }
    }

    fun onBiometricSuccess() {
        securityManager.lastUnlockedTime = System.currentTimeMillis()
        _uiState.update { it.copy(isAuthenticated = true) }
    }

    fun onBiometricError(error: String) {
        _uiState.update { it.copy(pinError = error) }
    }

    fun skipPinSetup() {
        securityManager.lastUnlockedTime = System.currentTimeMillis()
        _uiState.update { it.copy(isAuthenticated = true) }
    }

    class Factory(
        private val securityManager: SecurityManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(securityManager) as T
        }
    }
}
