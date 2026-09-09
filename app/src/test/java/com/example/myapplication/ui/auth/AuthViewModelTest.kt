package com.example.myapplication.ui.auth

import com.example.myapplication.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

    private lateinit var fakeSecurityManager: FakeSecurityManager

    @Before
    fun setUp() {
        fakeSecurityManager = FakeSecurityManager()
    }

    @Test
    fun `initial state when PIN not set asks to create PIN`() {
        val viewModel = AuthViewModel(fakeSecurityManager)
        val state = viewModel.uiState.value

        assertFalse(state.isPinSet)
        assertTrue(state.isCreatingPin)
        assertEquals("Create Security PIN", state.authStatusMessage)
    }

    @Test
    fun `creating PIN requires confirmation and sets PIN`() {
        val viewModel = AuthViewModel(fakeSecurityManager)

        // First PIN entry: 1234
        "1234".forEach { viewModel.onPinDigitEntered(it.toString()) }

        var state = viewModel.uiState.value
        assertEquals("Re-enter PIN to confirm", state.authStatusMessage)
        assertEquals("1234", state.confirmPinInput)
        assertEquals("", state.pinInput)

        // Confirm PIN entry: 1234
        "1234".forEach { viewModel.onPinDigitEntered(it.toString()) }

        state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertTrue(state.isPinSet)
        assertTrue(fakeSecurityManager.isPinSet)
        assertTrue(fakeSecurityManager.verifyPin("1234"))
    }

    @Test
    fun `creating PIN with mismatching confirmation fails and resets`() {
        val viewModel = AuthViewModel(fakeSecurityManager)

        // First PIN entry: 1234
        "1234".forEach { viewModel.onPinDigitEntered(it.toString()) }

        // Confirm PIN entry: 9999 (mismatch)
        "9999".forEach { viewModel.onPinDigitEntered(it.toString()) }

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("PINs do not match. Try again.", state.pinError)
    }

    @Test
    fun `verifying existing PIN authenticates on correct PIN`() {
        fakeSecurityManager.setPin("5555")
        val viewModel = AuthViewModel(fakeSecurityManager)

        assertTrue(viewModel.uiState.value.isPinSet)

        "5555".forEach { viewModel.onPinDigitEntered(it.toString()) }

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
    }

    @Test
    fun `verifying existing PIN sets error on incorrect PIN`() {
        fakeSecurityManager.setPin("5555")
        val viewModel = AuthViewModel(fakeSecurityManager)

        "1111".forEach { viewModel.onPinDigitEntered(it.toString()) }

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("Incorrect PIN. Please try again.", state.pinError)
    }

    @Test
    fun `onBiometricSuccess authenticates state`() {
        val viewModel = AuthViewModel(fakeSecurityManager)
        viewModel.onBiometricSuccess()

        assertTrue(viewModel.uiState.value.isAuthenticated)
    }

    private class FakeSecurityManager : SecurityManager() {
        private var pinHash: String? = null
        override var isBiometricEnabled: Boolean = false
        override val isPinSet: Boolean
            get() = pinHash != null

        override fun setPin(pin: String) {
            pinHash = pin
        }

        override fun verifyPin(pin: String): Boolean {
            return pinHash == pin
        }

        override fun clearPin() {
            pinHash = null
        }

        override var lastUnlockedTime: Long = 0L
    }
}
