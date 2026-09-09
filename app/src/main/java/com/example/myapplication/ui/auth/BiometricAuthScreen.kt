package com.example.myapplication.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.security.BiometricPromptManager

@Composable
fun BiometricAuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val biometricPromptManager = remember { BiometricPromptManager(context.applicationContext) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    // Collect Biometric Results
    LaunchedEffect(Unit) {
        biometricPromptManager.promptResults.collect { result ->
            when (result) {
                is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                    viewModel.onBiometricSuccess()
                }
                is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                    viewModel.onBiometricError(result.error)
                }
                is BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                    viewModel.onBiometricError("Authentication failed")
                }
                else -> {}
            }
        }
    }

    // Trigger biometric prompt automatically on start if biometric is supported
    LaunchedEffect(Unit) {
        if (biometricPromptManager.canAuthenticate() && (context as? FragmentActivity) != null) {
            biometricPromptManager.showBiometricPrompt(
                activity = context as FragmentActivity,
                title = "Unlock Ideni Vault",
                subtitle = "Authenticate to access your secure documents"
            )
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ideni Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.authStatusMessage ?: "Authenticate to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (uiState.pinError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.pinError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // PIN Dots Indicator Section
            PinDotsIndicator(pinLength = uiState.pinInput.length)

            // Keypad Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                val canUseBiometric = biometricPromptManager.canAuthenticate() && (context as? FragmentActivity) != null

                KeypadGrid(
                    onDigitClick = { viewModel.onPinDigitEntered(it) },
                    onDeleteClick = { viewModel.onPinDelete() },
                    onBiometricClick = {
                        if (canUseBiometric) {
                            biometricPromptManager.showBiometricPrompt(
                                activity = context as FragmentActivity,
                                title = "Unlock Ideni Vault"
                            )
                        }
                    },
                    canUseBiometric = canUseBiometric
                )

                if (uiState.isCreatingPin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.skipPinSetup() }) {
                        Text("Skip PIN Setup for Now")
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDotsIndicator(pinLength: Int, maxPinLength: Int = 4) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxPinLength) { index ->
            val isFilled = index < pinLength
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun KeypadGrid(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
    canUseBiometric: Boolean
) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("BIO", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in buttons) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (btn in row) {
                    when (btn) {
                        "BIO" -> {
                            if (canUseBiometric) {
                                KeypadIconButton(
                                    icon = Icons.Filled.Fingerprint,
                                    onClick = onBiometricClick,
                                    contentDescription = "Biometric Unlock"
                                )
                            } else {
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }
                        "DEL" -> {
                            KeypadIconButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                onClick = onDeleteClick,
                                contentDescription = "Delete Digit"
                            )
                        }
                        else -> {
                            KeypadNumberButton(
                                number = btn,
                                onClick = { onDigitClick(btn) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadNumberButton(
    number: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun KeypadIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String
) {
    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier.size(72.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
