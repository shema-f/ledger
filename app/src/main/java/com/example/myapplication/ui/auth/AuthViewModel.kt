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
    val authStatusMessage: String? = null,

    // Merchant Profile
    val shopName: String = "Smart Merchant",
    val businessSector: String = "Retail",
    val baseCurrency: String = "RWF",
    val showOnboardingDialog: Boolean = false,

    // Google One-Tap & Offline Mode
    val isGoogleOneTapLoading: Boolean = false,
    val googleUserEmail: String? = null,
    val googleDisplayName: String? = null,
    val isOfflineMode: Boolean = false
)

class AuthViewModel(
    private val securityManager: SecurityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isPinSet = securityManager.isPinSet,
            isBiometricEnabled = securityManager.isBiometricEnabled,
            isCreatingPin = !securityManager.isPinSet,
            authStatusMessage = if (securityManager.isPinSet) "Enter Security PIN" else "Create Security PIN",
            shopName = securityManager.shopName,
            businessSector = securityManager.businessSector,
            baseCurrency = securityManager.baseCurrency,
            googleUserEmail = securityManager.googleUserEmail,
            googleDisplayName = securityManager.googleDisplayName,
            isOfflineMode = securityManager.isOfflineMode
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateMerchantProfile(
        shopName: String,
        businessSector: String,
        baseCurrency: String = "RWF"
    ) {
        securityManager.shopName = shopName
        securityManager.businessSector = businessSector
        securityManager.baseCurrency = baseCurrency
        _uiState.update {
            it.copy(
                shopName = shopName,
                businessSector = businessSector,
                baseCurrency = baseCurrency,
                showOnboardingDialog = false
            )
        }
    }

    fun openOnboardingDialog() {
        _uiState.update { it.copy(showOnboardingDialog = true) }
    }

    fun closeOnboardingDialog() {
        _uiState.update { it.copy(showOnboardingDialog = false) }
    }

    fun triggerGoogleOneTapSignIn() {
        _uiState.update { it.copy(isGoogleOneTapLoading = true) }
        val email = "merchant@gmail.com"
        val name = "Keza Smart Merchant"
        onGoogleSignInSuccess(email, name)
    }

    fun onGoogleSignInSuccess(email: String, displayName: String) {
        securityManager.googleUserEmail = email
        securityManager.googleDisplayName = displayName
        securityManager.lastUnlockedTime = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                isGoogleOneTapLoading = false,
                googleUserEmail = email,
                googleDisplayName = displayName,
                isAuthenticated = true,
                authStatusMessage = "Signed in as $displayName"
            )
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        securityManager.isOfflineMode = enabled
        _uiState.update { it.copy(isOfflineMode = enabled) }
    }

    fun unlockWithOfflinePin(pin: String): Boolean {
        if (securityManager.verifyPin(pin)) {
            securityManager.lastUnlockedTime = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    pinError = null,
                    isOfflineMode = true
                )
            }
            return true
        } else {
            _uiState.update {
                it.copy(pinError = "Incorrect PIN")
            }
            return false
        }
    }

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
