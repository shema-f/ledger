package com.example.myapplication.ui.dashboard

import com.example.myapplication.ui.components.ImariLogo
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.LocalStrings
import com.example.myapplication.util.localizedString

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.DashboardSummary
import com.example.myapplication.domain.model.LedgerRecord
import com.example.myapplication.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ActiveQuickDialog {
    NONE,
    ADD_DEBT,
    RECORD_PAYMENT,
    CASH_SALE
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToMoMoFeed: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onCustomerClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val recentRecords by viewModel.recentLedgerRecords.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var activeDialog by remember { mutableStateOf(ActiveQuickDialog.NONE) }

    DashboardContent(
        summary = summary,
        recentRecords = recentRecords,
        customers = customers,
        onAddDebtClick = { activeDialog = ActiveQuickDialog.ADD_DEBT },
        onRecordPaymentClick = { activeDialog = ActiveQuickDialog.RECORD_PAYMENT },
        onCashSaleClick = { activeDialog = ActiveQuickDialog.CASH_SALE },
        onMoMoFeedClick = onNavigateToMoMoFeed,
        onCustomerClick = onCustomerClick,
        modifier = modifier
    )

    when (activeDialog) {
        ActiveQuickDialog.ADD_DEBT -> {
            QuickDebtDialog(
                customers = customers,
                onDismiss = { activeDialog = ActiveQuickDialog.NONE },
                onConfirm = { customerId, amount, description ->
                    viewModel.addDebt(customerId, amount, description)
                    activeDialog = ActiveQuickDialog.NONE
                }
            )
        }
        ActiveQuickDialog.RECORD_PAYMENT -> {
            QuickPaymentDialog(
                customers = customers,
                onDismiss = { activeDialog = ActiveQuickDialog.NONE },
                onConfirm = { customerId, amount, description ->
                    viewModel.recordPayment(customerId, amount, description)
                    activeDialog = ActiveQuickDialog.NONE
                }
            )
        }
        ActiveQuickDialog.CASH_SALE -> {
            QuickCashSaleDialog(
                onDismiss = { activeDialog = ActiveQuickDialog.NONE },
                onConfirm = { amount, description ->
                    viewModel.recordCashSale(amount, description)
                    activeDialog = ActiveQuickDialog.NONE
                }
            )
        }
        ActiveQuickDialog.NONE -> {}
    }
}

@Composable
fun DashboardContent(
    summary: DashboardSummary,
    recentRecords: List<LedgerRecord>,
    customers: List<Customer>,
    onAddDebtClick: () -> Unit,
    onRecordPaymentClick: () -> Unit,
    onCashSaleClick: () -> Unit,
    onMoMoFeedClick: () -> Unit,
    onCustomerClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            ImariLogo(
                iconSize = 40.dp,
                showTagline = true,
                isHorizontal = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            MerchantSummaryCard(summary = summary)
        }

        item {
            Text(
                text = localizedString("Quick Actions"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionGrid(
                onAddDebtClick = onAddDebtClick,
                onRecordPaymentClick = onRecordPaymentClick,
                onCashSaleClick = onCashSaleClick,
                onMoMoFeedClick = onMoMoFeedClick
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString("Recent Activity"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (recentRecords.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localizedString("No recent activity recorded"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(recentRecords) { record ->
                val customerName = record.customerId?.let { id ->
                    customers.find { it.id == id }?.fullName ?: "Customer #$id"
                } ?: "Cash Customer"

                RecentActivityCard(
                    record = record,
                    customerName = customerName,
                    onClick = {
                        record.customerId?.let { id -> onCustomerClick(id) }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MerchantSummaryCard(summary: DashboardSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = localizedString("Merchant Overview"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Imari Smart Ledger",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlight Total Debt
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = localizedString("Total Outstanding Debts"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatCurrency(summary.totalOutstandingDebt)} ${localizedString("RWF")}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.MoneyOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today's Sales
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizedString("Today's Sales"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatCurrency(summary.todaySales)} ${localizedString("RWF")}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Active Debtors
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizedString("Active Debtors"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${summary.activeDebtorsCount} Persons",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionGrid(
    onAddDebtClick: () -> Unit,
    onRecordPaymentClick: () -> Unit,
    onCashSaleClick: () -> Unit,
    onMoMoFeedClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = localizedString("+ Ideni Shya"),
                subtitle = localizedString("Add Debt"),
                icon = Icons.Rounded.Add,
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = onAddDebtClick,
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                title = localizedString("Kwishyura Ideni"),
                subtitle = localizedString("Record Payment"),
                icon = Icons.Rounded.Payments,
                backgroundColor = SuccessEmeraldContainer,
                contentColor = OnSuccessEmeraldContainer,
                onClick = onRecordPaymentClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = localizedString("Icuruzwa rya cash"),
                subtitle = localizedString("New Cash Sale"),
                icon = Icons.Rounded.PointOfSale,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onCashSaleClick,
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                title = localizedString("MoMo Live Feed"),
                subtitle = localizedString("Incoming Payment Alerts"),
                icon = Icons.Rounded.Receipt,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onMoMoFeedClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun RecentActivityCard(
    record: LedgerRecord,
    customerName: String,
    onClick: () -> Unit
) {
    val (badgeColor, textColor, label) = when (record.type) {
        TransactionType.CREDIT -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Ideni (+Debt)"
        )
        TransactionType.PAYMENT -> Triple(
            SuccessEmeraldContainer,
            OnSuccessEmeraldContainer,
            "Kwishyura (Payment)"
        )
        TransactionType.CASH_SALE -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Icuruzwa (Cash)"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            record.type.name
        )
    }

    val formattedDate = remember(record.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(record.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeColor,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when (record.type) {
                            TransactionType.CREDIT -> Icons.Rounded.ArrowUpward
                            TransactionType.PAYMENT -> Icons.Rounded.ArrowDownward
                            TransactionType.CASH_SALE -> Icons.Rounded.AttachMoney
                            else -> Icons.Rounded.Receipt
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${record.description} • $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatCurrency(record.amount)} RWF",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (record.type == TransactionType.CREDIT) MaterialTheme.colorScheme.error else textColor
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDebtDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (customerId: Long, amount: Double, description: String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizedString("Add Debt")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.fullName ?: localizedString("Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(localizedString("Customer Name")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text("${customer.fullName} (${customer.phoneNumber})") },
                                onClick = {
                                    selectedCustomer = customer
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("${localizedString("Amount")} (RWF)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                errorText?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val cust = selectedCustomer
                    if (cust == null) {
                        errorText = "Please select a customer"
                    } else if (amount == null || amount <= 0) {
                        errorText = "Enter a valid amount"
                    } else {
                        onConfirm(cust.id, amount, descriptionText.ifBlank { "Debt" })
                    }
                }
            ) {
                Text(localizedString("Add Debt"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString("Cancel"))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPaymentDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (customerId: Long, amount: Double, description: String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizedString("Record Payment")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.fullName ?: localizedString("Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(localizedString("Customer Name")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text("${customer.fullName} - Debt: ${formatCurrency(customer.totalDebt)} RWF") },
                                onClick = {
                                    selectedCustomer = customer
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Payment Amount (RWF)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Note / Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                errorText?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val cust = selectedCustomer
                    if (cust == null) {
                        errorText = "Please select a customer"
                    } else if (amount == null || amount <= 0) {
                        errorText = "Enter a valid amount"
                    } else {
                        onConfirm(cust.id, amount, descriptionText.ifBlank { "Payment" })
                    }
                }
            ) {
                Text(localizedString("Record Payment"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString("Cancel"))
            }
        }
    )
}

@Composable
fun QuickCashSaleDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, description: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizedString("New Cash Sale")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("${localizedString("Amount")} (RWF)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                errorText?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorText = "Enter a valid amount"
                    } else {
                        onConfirm(amount, descriptionText.ifBlank { "Cash Sale" })
                    }
                }
            ) {
                Text(localizedString("New Cash Sale"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString("Cancel"))
            }
        }
    )
}

fun formatCurrency(amount: Double): String {
    return String.format(Locale.US, "%,.0f", amount)
}

@Preview(showBackground = true)
@Composable
fun DashboardContentPreview() {
    MaterialTheme {
        DashboardContent(
            summary = DashboardSummary(
                totalOutstandingDebt = 45000.0,
                todaySales = 125000.0,
                activeDebtorsCount = 8
            ),
            recentRecords = listOf(
                LedgerRecord(
                    id = 1,
                    customerId = 1,
                    type = TransactionType.CREDIT,
                    amount = 5000.0,
                    description = "2x Rice bags",
                    timestamp = System.currentTimeMillis()
                ),
                LedgerRecord(
                    id = 2,
                    customerId = 1,
                    type = TransactionType.PAYMENT,
                    amount = 3000.0,
                    description = "MoMo Payment",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                LedgerRecord(
                    id = 3,
                    customerId = null,
                    type = TransactionType.CASH_SALE,
                    amount = 15000.0,
                    description = "Cooking Oil",
                    timestamp = System.currentTimeMillis() - 7200000
                )
            ),
            customers = listOf(
                Customer(id = 1, fullName = "Jean Paul", phoneNumber = "0788123456", totalDebt = 5000.0)
            ),
            onAddDebtClick = {},
            onRecordPaymentClick = {},
            onCashSaleClick = {},
            onMoMoFeedClick = {},
            onCustomerClick = {}
        )
    }
}
