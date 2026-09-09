package com.example.myapplication.ui.momo

import com.example.myapplication.ui.theme.*

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.ui.dashboard.formatCurrency
import com.example.myapplication.util.LocalStrings
import com.example.myapplication.util.localizedString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoMoLiveFeedScreen(
    viewModel: MoMoFeedViewModel,
    modifier: Modifier = Modifier
) {
    val filterUnreconciled by viewModel.filterUnreconciledOnly.collectAsState()
    val logs by viewModel.momoLogs.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var selectedLogForReconciliation by remember { mutableStateOf<MoMoLog?>(null) }

    MoMoLiveFeedContent(
        filterUnreconciled = filterUnreconciled,
        onFilterUnreconciledChange = { viewModel.setFilterUnreconciled(it) },
        logs = logs,
        onReconcileClick = { log -> selectedLogForReconciliation = log },
        modifier = modifier
    )

    selectedLogForReconciliation?.let { log ->
        ManualReconciliationDialog(
            moMoLog = log,
            customers = customers,
            onDismiss = { selectedLogForReconciliation = null },
            onConfirm = { customerId ->
                viewModel.reconcilePayment(log, customerId)
                selectedLogForReconciliation = null
            }
        )
    }
}

@Composable
fun MoMoLiveFeedContent(
    filterUnreconciled: Boolean,
    onFilterUnreconciledChange: (Boolean) -> Unit,
    logs: List<MoMoLog>,
    onReconcileClick: (MoMoLog) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Filter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = localizedString("Incoming Payment Alerts"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Intercepted Mobile Money Transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilterChip(
                selected = filterUnreconciled,
                onClick = { onFilterUnreconciledChange(!filterUnreconciled) },
                label = { Text("Unreconciled Only") },
                leadingIcon = {
                    if (filterUnreconciled) {
                        Icon(
                            imageVector = Icons.Rounded.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (filterUnreconciled) "No unreconciled MoMo receipts found." else "No MoMo transaction logs captured yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs, key = { it.id.toString() + "_" + it.txId }) { log ->
                    MoMoLogCard(
                        log = log,
                        onReconcileClick = { onReconcileClick(log) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MoMoLogCard(
    log: MoMoLog,
    onReconcileClick: () -> Unit
) {
    val formattedDate = remember(log.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (log.isReconciled) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (log.isReconciled) SuccessEmeraldContainer else WarningAmberContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (log.isReconciled) Icons.Rounded.CheckCircle else Icons.Rounded.HourglassEmpty,
                                contentDescription = null,
                                tint = if (log.isReconciled) OnSuccessEmeraldContainer else OnWarningAmberContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = log.senderName ?: log.senderPhone ?: "MoMo Sender",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        log.senderPhone?.let { phone ->
                            if (log.senderName != null) {
                                Text(
                                    text = phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatCurrency(log.amount)} RWF",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = SuccessEmerald
                    )

                    // Status Badge Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (log.isReconciled) SuccessEmeraldContainer else WarningAmberContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (log.isReconciled) localizedString("Auto-Reconciled") else localizedString("Pending Match"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (log.isReconciled) OnSuccessEmeraldContainer else OnWarningAmberContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "TxID: ${log.txId} • $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.rawText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!log.isReconciled) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onReconcileClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(localizedString("Assign Transaction"))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualReconciliationDialog(
    moMoLog: MoMoLog,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (customerId: Long) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Reconciliation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Payment: ${formatCurrency(moMoLog.amount)} RWF",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "From: ${moMoLog.senderName ?: moMoLog.senderPhone ?: "Unknown"} (Tx: ${moMoLog.txId})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Select debtor customer to apply this payment to:",
                    style = MaterialTheme.typography.bodySmall
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.let { "${it.fullName} (Debt: ${formatCurrency(it.totalDebt)} RWF)" } ?: "Select Customer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer") },
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
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = customer.fullName)
                                        Text(
                                            text = "${formatCurrency(customer.totalDebt)} RWF",
                                            fontWeight = FontWeight.Bold,
                                            color = if (customer.totalDebt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCustomer = customer
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                errorText?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cust = selectedCustomer
                    if (cust == null) {
                        errorText = "Please select a customer"
                    } else {
                        onConfirm(cust.id)
                    }
                }
            ) {
                Text("Confirm Reconciliation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MoMoLiveFeedContentPreview() {
    MaterialTheme {
        MoMoLiveFeedContent(
            filterUnreconciled = false,
            onFilterUnreconciledChange = {},
            logs = listOf(
                MoMoLog(
                    id = 1,
                    senderName = "Jean Paul",
                    senderPhone = "0788123456",
                    amount = 5000.0,
                    txId = "209384759",
                    rawText = "You have received 5,000 RWF from Jean Paul (250788123456). Ref: 209384759.",
                    timestamp = System.currentTimeMillis(),
                    isReconciled = false
                ),
                MoMoLog(
                    id = 2,
                    senderName = "Aline Uwase",
                    senderPhone = "0789654321",
                    amount = 10000.0,
                    txId = "209384760",
                    rawText = "You have received 10,000 RWF from Aline Uwase.",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isReconciled = true
                )
            ),
            onReconcileClick = {}
        )
    }
}
