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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.util.LocalStrings
import com.example.myapplication.util.localizedString
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.myapplication.ui.components.ImariLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImariAuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onAuthSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section: Imari Logo Component
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                ImariLogo(
                    iconSize = 80.dp,
                    showTagline = true,
                    isHorizontal = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Merchant profile summary chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { viewModel.openOnboardingDialog() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.shopName} (${uiState.businessSector})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Merchant Profile",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (uiState.pinError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.pinError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Google One-Tap Sign-In Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign in to sync your ledger safely",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.triggerGoogleOneTapSignIn() },
                        enabled = !uiState.isGoogleOneTapLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        if (uiState.isGoogleOneTapLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (uiState.googleUserEmail != null)
                                    "Signed in as ${uiState.googleUserEmail}"
                                else
                                    "Google One-Tap Sign-In",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Offline PIN Code Entry Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = localizedString("Enter PIN"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // PIN Dots Indicator
                PinDotsIndicator(pinLength = uiState.pinInput.length)

                Spacer(modifier = Modifier.height(16.dp))

                // Keypad
                KeypadGrid(
                    onDigitClick = { viewModel.onPinDigitEntered(it) },
                    onDeleteClick = { viewModel.onPinDelete() }
                )
            }
        }
    }

    // Merchant Onboarding Dialog
    if (uiState.showOnboardingDialog) {
        MerchantOnboardingDialog(
            currentShopName = uiState.shopName,
            currentSector = uiState.businessSector,
            currentCurrency = uiState.baseCurrency,
            onDismiss = { viewModel.closeOnboardingDialog() },
            onSave = { name, sector, currency ->
                viewModel.updateMerchantProfile(name, sector, currency)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MerchantOnboardingDialog(
    currentShopName: String,
    currentSector: String,
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var shopName by remember { mutableStateOf(currentShopName) }
    var selectedSector by remember { mutableStateOf(currentSector) }
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }

    val sectors = listOf("Retail", "Mini-Market", "Salon", "Hardware", "Other")
    var sectorExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Merchant Profile Onboarding", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = sectorExpanded,
                    onExpandedChange = { sectorExpanded = !sectorExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSector,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Business Sector") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = sectorExpanded,
                        onDismissRequest = { sectorExpanded = false }
                    ) {
                        sectors.forEach { sector ->
                            DropdownMenuItem(
                                text = { Text(sector) },
                                onClick = {
                                    selectedSector = sector
                                    sectorExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedCurrency,
                    onValueChange = { selectedCurrency = it },
                    label = { Text("Currency") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shopName.isNotBlank()) {
                        onSave(shopName.trim(), selectedSector, selectedCurrency)
                    }
                }
            ) {
                Text("Save Merchant Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
                    .size(16.dp)
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
    onDeleteClick: () -> Unit
) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in buttons) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (btn in row) {
                    when (btn) {
                        "" -> Spacer(modifier = Modifier.size(64.dp))
                        "DEL" -> KeypadIconButton(
                            icon = Icons.AutoMirrored.Filled.Backspace,
                            onClick = onDeleteClick,
                            contentDescription = "Delete"
                        )
                        else -> KeypadNumberButton(
                            number = btn,
                            onClick = { onDigitClick(btn) }
                        )
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
            .size(64.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number,
                fontSize = 22.sp,
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
        modifier = Modifier.size(64.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
