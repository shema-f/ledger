package com.example.myapplication.ui.reconciliation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.Product
import com.example.myapplication.service.FinancialSms
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReconciliationBottomSheet(
    financialSms: FinancialSms,
    viewModel: SmsReconciliationViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current

    val products by viewModel.products.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val loanDebts by viewModel.loanDebts.collectAsState()

    var selectedOption by remember { mutableStateOf(ReconciliationOption.POS_SALE) }

    // Option 1: POS Sale selection
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedAccountForPos by remember { mutableStateOf<Account?>(null) }

    // Option 2: Debt Settlement selection
    var selectedDebtType by remember { mutableStateOf(DebtSelectionType.CUSTOMER_LEDGER) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedLoanDebt by remember { mutableStateOf<LoanDebt?>(null) }
    var selectedAccountForDebt by remember { mutableStateOf<Account?>(null) }

    // Option 3: General Revenue selection
    var selectedAccountForRevenue by remember { mutableStateOf<Account?>(null) }

    // Auto-select default account if available
    LaunchedEffect(accounts) {
        if (accounts.isNotEmpty()) {
            val defaultAcc = accounts.find { it.isDefault } ?: accounts.first()
            if (selectedAccountForPos == null) selectedAccountForPos = defaultAcc
            if (selectedAccountForDebt == null) selectedAccountForDebt = defaultAcc
            if (selectedAccountForRevenue == null) selectedAccountForRevenue = defaultAcc
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Title
            Text(
                text = "New Payment Alert Detected",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Provider Badge & Amount Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProviderBadge(provider = financialSms.provider)

                        val formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(financialSms.amount)
                        Text(
                            text = "RWF $formattedAmount",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Details: Sender, TxRef, Timestamp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Sender",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = financialSms.senderOrRecipient,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TxRef: ${financialSms.txReference}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(financialSms.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Merchant Assignment Options",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option 1 Card: Assign to Product POS Sale
            OptionSelectionCard(
                title = "Assign to Product POS Sale",
                description = "Select product, deduct 1 stock, record profit & revenue",
                icon = Icons.Rounded.PointOfSale,
                isSelected = selectedOption == ReconciliationOption.POS_SALE,
                onClick = { selectedOption = ReconciliationOption.POS_SALE }
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Product Selection Dropdown
                    ProductDropdown(
                        products = products,
                        selectedProduct = selectedProduct,
                        onProductSelected = { selectedProduct = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Account Selection Dropdown
                    AccountDropdown(
                        label = "Deposit to Account",
                        accounts = accounts,
                        selectedAccount = selectedAccountForPos,
                        onAccountSelected = { selectedAccountForPos = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2 Card: Settle Customer Debt / Loan
            OptionSelectionCard(
                title = "Settle Customer Debt / Loan",
                description = "Select customer or loan to reduce debt balance",
                icon = Icons.Rounded.AccountBalance,
                isSelected = selectedOption == ReconciliationOption.SETTLE_DEBT,
                onClick = { selectedOption = ReconciliationOption.SETTLE_DEBT }
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Toggle Customer vs Loan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedDebtType = DebtSelectionType.CUSTOMER_LEDGER }
                        ) {
                            RadioButton(
                                selected = selectedDebtType == DebtSelectionType.CUSTOMER_LEDGER,
                                onClick = { selectedDebtType = DebtSelectionType.CUSTOMER_LEDGER }
                            )
                            Text("Customer Debt", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedDebtType = DebtSelectionType.LOAN_RECEIVABLE }
                        ) {
                            RadioButton(
                                selected = selectedDebtType == DebtSelectionType.LOAN_RECEIVABLE,
                                onClick = { selectedDebtType = DebtSelectionType.LOAN_RECEIVABLE }
                            )
                            Text("Loan / Receivable", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedDebtType == DebtSelectionType.CUSTOMER_LEDGER) {
                        CustomerDropdown(
                            customers = customers,
                            selectedCustomer = selectedCustomer,
                            onCustomerSelected = { selectedCustomer = it }
                        )
                    } else {
                        LoanDebtDropdown(
                            loanDebts = loanDebts,
                            selectedLoanDebt = selectedLoanDebt,
                            onLoanDebtSelected = { selectedLoanDebt = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AccountDropdown(
                        label = "Target Account (Optional)",
                        accounts = accounts,
                        selectedAccount = selectedAccountForDebt,
                        onAccountSelected = { selectedAccountForDebt = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 3 Card: General Revenue / Deposit
            OptionSelectionCard(
                title = "General Revenue / Deposit",
                description = "Record directly to an Account balance as revenue",
                icon = Icons.Rounded.Money,
                isSelected = selectedOption == ReconciliationOption.GENERAL_REVENUE,
                onClick = { selectedOption = ReconciliationOption.GENERAL_REVENUE }
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    AccountDropdown(
                        label = "Select Target Account",
                        accounts = accounts,
                        selectedAccount = selectedAccountForRevenue,
                        onAccountSelected = { selectedAccountForRevenue = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button validation
            val isSubmitEnabled = when (selectedOption) {
                ReconciliationOption.POS_SALE -> selectedProduct != null && selectedAccountForPos != null
                ReconciliationOption.SETTLE_DEBT -> if (selectedDebtType == DebtSelectionType.CUSTOMER_LEDGER) selectedCustomer != null else selectedLoanDebt != null
                ReconciliationOption.GENERAL_REVENUE -> selectedAccountForRevenue != null
            }

            Button(
                onClick = {
                    when (selectedOption) {
                        ReconciliationOption.POS_SALE -> {
                            val prod = selectedProduct ?: return@Button
                            val acc = selectedAccountForPos ?: return@Button
                            viewModel.reconcilePosSale(financialSms, prod, acc) {
                                Toast.makeText(context, "POS Sale Reconciled Successfully!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        }
                        ReconciliationOption.SETTLE_DEBT -> {
                            val acc = selectedAccountForDebt
                            if (selectedDebtType == DebtSelectionType.CUSTOMER_LEDGER) {
                                val cust = selectedCustomer ?: return@Button
                                viewModel.reconcileSettleCustomerDebt(financialSms, cust, acc) {
                                    Toast.makeText(context, "Customer Debt Payment Reconciled!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            } else {
                                val loan = selectedLoanDebt ?: return@Button
                                viewModel.reconcileSettleLoan(financialSms, loan, acc) {
                                    Toast.makeText(context, "Loan Debt Repayment Reconciled!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            }
                        }
                        ReconciliationOption.GENERAL_REVENUE -> {
                            val acc = selectedAccountForRevenue ?: return@Button
                            viewModel.reconcileGeneralRevenue(financialSms, acc) {
                                Toast.makeText(context, "General Revenue Reconciled!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = isSubmitEnabled,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm Reconciliation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProviderBadge(provider: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when {
        provider.contains("MTN", ignoreCase = true) || provider.contains("MoMo", ignoreCase = true) ->
            Color(0xFFFFCC00) to Color.Black
        provider.contains("BK", ignoreCase = true) || provider.contains("Kigali", ignoreCase = true) ->
            Color(0xFF00529B) to Color.White
        provider.contains("Airtel", ignoreCase = true) ->
            Color(0xFFE60000) to Color.White
        provider.contains("I&M", ignoreCase = true) ->
            Color(0xFF008080) to Color.White
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = provider,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun OptionSelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val cardBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isSelected) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDropdown(
    products: List<Product>,
    selectedProduct: Product?,
    onProductSelected: (Product) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedProduct?.let { "${it.name} (Stock: ${it.currentStock}, Selling: RWF ${it.sellingPrice.toInt()})" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Product") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (products.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No products available") },
                    onClick = { expanded = false }
                )
            } else {
                products.forEach { prod ->
                    DropdownMenuItem(
                        text = { Text("${prod.name} (Stock: ${prod.currentStock}) - RWF ${prod.sellingPrice.toInt()}") },
                        onClick = {
                            onProductSelected(prod)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    label: String,
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedAccount?.let { "${it.name} (${it.accountType})" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { acc ->
                DropdownMenuItem(
                    text = { Text("${acc.name} (${acc.accountType}) - Bal: RWF ${acc.currentBalance.toInt()}") },
                    onClick = {
                        onAccountSelected(acc)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDropdown(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onCustomerSelected: (Customer) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCustomer?.let { "${it.fullName} (Debt: RWF ${it.totalDebt.toInt()})" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Customer") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (customers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No customers available") },
                    onClick = { expanded = false }
                )
            } else {
                customers.forEach { cust ->
                    DropdownMenuItem(
                        text = { Text("${cust.fullName} (Debt: RWF ${cust.totalDebt.toInt()})") },
                        onClick = {
                            onCustomerSelected(cust)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanDebtDropdown(
    loanDebts: List<LoanDebt>,
    selectedLoanDebt: LoanDebt?,
    onLoanDebtSelected: (LoanDebt) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLoanDebt?.let { "${it.personOrInstitution} (Rem: RWF ${it.remainingAmount.toInt()})" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Loan / Receivable") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (loanDebts.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No loan records available") },
                    onClick = { expanded = false }
                )
            } else {
                loanDebts.forEach { loan ->
                    DropdownMenuItem(
                        text = { Text("${loan.personOrInstitution} - Rem: RWF ${loan.remainingAmount.toInt()}") },
                        onClick = {
                            onLoanDebtSelected(loan)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
