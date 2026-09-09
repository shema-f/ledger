package com.example.myapplication.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.service.FinancialSms
import com.example.myapplication.service.PaymentAlertManager
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.business.BusinessHubScreen
import com.example.myapplication.ui.components.IfarangaLogo
import com.example.myapplication.ui.customer.CustomerDetailScreen
import com.example.myapplication.ui.customer.CustomerViewModel
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.insights.InsightsScreen
import com.example.myapplication.ui.inventory.InventoryViewModel
import com.example.myapplication.ui.loans.LoanDebtViewModel
import com.example.myapplication.ui.momo.MoMoFeedViewModel
import com.example.myapplication.ui.momo.MoMoLiveFeedScreen
import com.example.myapplication.ui.money.MoneyFlowScreen
import com.example.myapplication.ui.money.MoneyFlowViewModel
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.reconciliation.SmsReconciliationViewModel
import com.example.myapplication.ui.reports.ReportsScreen
import com.example.myapplication.ui.reports.ReportsViewModel
import com.example.myapplication.util.AppLanguage
import com.example.myapplication.util.LanguageManager
import com.example.myapplication.util.LocalStrings
import kotlinx.coroutines.launch

enum class AppDrawerItem(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    INSIGHTS("IFARANGA Intelligence", Icons.Rounded.Psychology),
    INVENTORY("Inventory & POS Lite", Icons.Rounded.Inventory),
    CUSTOMERS("Customer Ledger & Debts", Icons.Rounded.People),
    LOANS("Loans & Business Liabilities", Icons.Rounded.AccountBalance),
    MOMO_FEED("MoMo & Bank Live Feed", Icons.Rounded.QrCode),
    REPORTS("Financial Reports & PDF", Icons.Rounded.BarChart),
    SECURITY("Security & Permissions", Icons.Rounded.Lock)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    customerViewModel: CustomerViewModel,
    inventoryViewModel: InventoryViewModel,
    loanDebtViewModel: LoanDebtViewModel,
    momoFeedViewModel: MoMoFeedViewModel,
    moneyFlowViewModel: MoneyFlowViewModel,
    reconciliationViewModel: SmsReconciliationViewModel? = null,
    reportsViewModel: ReportsViewModel? = null,
    authViewModel: AuthViewModel? = null,
    onLockApp: (() -> Unit)? = null,
    isDarkMode: Boolean = false,
    onToggleDarkMode: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(IfarangaTab.HOME) }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var drawerItemOverride by remember { mutableStateOf<AppDrawerItem?>(null) }
    var showQuickActionSheet by remember { mutableStateOf(false) }

    // Quick Action Dialog States
    var quickActionDialogType by remember { mutableStateOf<QuickActionType?>(null) }
    var quickActionAmountText by remember { mutableStateOf("") }
    var quickActionDescText by remember { mutableStateOf("") }

    var liveAlertSms by remember { mutableStateOf<FinancialSms?>(null) }
    var isDarkTheme by remember { mutableStateOf(isDarkMode) }

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        PaymentAlertManager.livePaymentAlerts.collect { sms ->
            liveAlertSms = sms
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DrawerHeader(
                        shopName = "IFARANGA Merchant",
                        currency = "RWF"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp)
                    ) {
                        AppDrawerItem.entries.forEach { item ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = LocalStrings.get(item.title, currentLanguage),
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                selected = drawerItemOverride == item,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                    selectedCustomerId = null
                                    drawerItemOverride = item
                                    when (item) {
                                        AppDrawerItem.DASHBOARD -> selectedTab = IfarangaTab.HOME
                                        AppDrawerItem.INSIGHTS -> selectedTab = IfarangaTab.INSIGHTS
                                        AppDrawerItem.INVENTORY, AppDrawerItem.LOANS, AppDrawerItem.CUSTOMERS -> selectedTab = IfarangaTab.BUSINESS
                                        AppDrawerItem.SECURITY -> selectedTab = IfarangaTab.PROFILE
                                        else -> {}
                                    }
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    DrawerFooter(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { newLanguage ->
                            LanguageManager.setLanguage(newLanguage)
                        },
                        isDarkMode = isDarkTheme,
                        onToggleDarkMode = { newDark ->
                            isDarkTheme = newDark
                            onToggleDarkMode?.invoke(newDark)
                        },
                        onLockApp = onLockApp
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedCustomerId != null) {
                                LocalStrings.get("Customer Detail", currentLanguage)
                            } else if (drawerItemOverride != null && drawerItemOverride != AppDrawerItem.DASHBOARD) {
                                LocalStrings.get(drawerItemOverride!!.title, currentLanguage)
                            } else {
                                selectedTab.label
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        if (selectedCustomerId != null) {
                            IconButton(onClick = { selectedCustomerId = null }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = "Open Menu"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                if (selectedCustomerId == null) {
                    IfarangaBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { newTab ->
                            drawerItemOverride = null
                            selectedTab = newTab
                        },
                        onQuickActionClick = {
                            showQuickActionSheet = true
                        }
                    )
                }
            }
        ) { paddingValues ->
            val currentCustId = selectedCustomerId
            if (currentCustId != null) {
                CustomerDetailScreen(
                    viewModel = customerViewModel,
                    customerId = currentCustId,
                    onNavigateBack = { selectedCustomerId = null },
                    modifier = Modifier.padding(paddingValues)
                )
            } else if (drawerItemOverride == AppDrawerItem.MOMO_FEED) {
                MoMoLiveFeedScreen(
                    viewModel = momoFeedViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            } else if (drawerItemOverride == AppDrawerItem.REPORTS) {
                if (reportsViewModel != null) {
                    ReportsScreen(
                        viewModel = reportsViewModel,
                        authViewModel = authViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            } else {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "5TabScreenTransition",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) { targetTab ->
                    when (targetTab) {
                        IfarangaTab.HOME -> {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigateToMoMoFeed = { drawerItemOverride = AppDrawerItem.MOMO_FEED },
                                onNavigateToCustomers = {
                                    drawerItemOverride = null
                                    selectedTab = IfarangaTab.BUSINESS
                                },
                                onCustomerClick = { customerId ->
                                    selectedCustomerId = customerId
                                }
                            )
                        }
                        IfarangaTab.MONEY -> {
                            MoneyFlowScreen(
                                viewModel = moneyFlowViewModel
                            )
                        }
                        IfarangaTab.BUSINESS -> {
                            val initialSubTab = when (drawerItemOverride) {
                                AppDrawerItem.LOANS -> 1
                                AppDrawerItem.CUSTOMERS -> 2
                                else -> 0
                            }
                            BusinessHubScreen(
                                inventoryViewModel = inventoryViewModel,
                                loanDebtViewModel = loanDebtViewModel,
                                customerViewModel = customerViewModel,
                                initialTabOrdinal = initialSubTab,
                                onCustomerClick = { customerId ->
                                    selectedCustomerId = customerId
                                }
                            )
                        }
                        IfarangaTab.INSIGHTS -> {
                            InsightsScreen(
                                viewModel = dashboardViewModel
                            )
                        }
                        IfarangaTab.PROFILE -> {
                            ProfileScreen(
                                authViewModel = authViewModel,
                                isDarkMode = isDarkTheme,
                                onToggleDarkMode = { newDark ->
                                    isDarkTheme = newDark
                                    onToggleDarkMode?.invoke(newDark)
                                },
                                onLockApp = onLockApp
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Action Bottom Sheet
    if (showQuickActionSheet) {
        QuickActionBottomSheet(
            onDismissRequest = { showQuickActionSheet = false },
            onActionSelected = { actionType ->
                showQuickActionSheet = false
                when (actionType) {
                    QuickActionType.INCOME, QuickActionType.EXPENSE, QuickActionType.SAVINGS_GOAL, QuickActionType.TRANSFER -> {
                        quickActionDialogType = actionType
                        quickActionAmountText = ""
                        quickActionDescText = ""
                    }
                    QuickActionType.DEBT -> {
                        drawerItemOverride = null
                        selectedTab = IfarangaTab.BUSINESS
                    }
                    QuickActionType.POS_SALE -> {
                        drawerItemOverride = null
                        selectedTab = IfarangaTab.BUSINESS
                    }
                }
            }
        )
    }

    // Quick Action Input Dialog
    val currentDialogAction = quickActionDialogType
    if (currentDialogAction != null) {
        AlertDialog(
            onDismissRequest = { quickActionDialogType = null },
            title = {
                Text(
                    text = "${currentDialogAction.title} (${currentDialogAction.labelKinyarwanda})",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quickActionAmountText,
                        onValueChange = { quickActionAmountText = it },
                        label = { Text("Amount (RWF)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quickActionDescText,
                        onValueChange = { quickActionDescText = it },
                        label = { Text("Description / Impamvu") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = quickActionAmountText.toDoubleOrNull() ?: 0.0
                        val desc = quickActionDescText.ifBlank { currentDialogAction.title }
                        if (amount > 0) {
                            when (currentDialogAction) {
                                QuickActionType.INCOME -> {
                                    moneyFlowViewModel.addTransaction(TransactionType.INCOME, amount, desc)
                                    Toast.makeText(context, "＋ Income Recorded: $amount RWF", Toast.LENGTH_SHORT).show()
                                }
                                QuickActionType.EXPENSE -> {
                                    moneyFlowViewModel.addTransaction(TransactionType.EXPENSE, amount, desc)
                                    Toast.makeText(context, "− Expense Recorded: $amount RWF", Toast.LENGTH_SHORT).show()
                                }
                                QuickActionType.SAVINGS_GOAL -> {
                                    moneyFlowViewModel.setSavings(amount)
                                    Toast.makeText(context, "🎯 Savings Goal Updated: $amount RWF", Toast.LENGTH_SHORT).show()
                                }
                                QuickActionType.TRANSFER -> {
                                    moneyFlowViewModel.addTransaction(TransactionType.TRANSFER, amount, desc)
                                    Toast.makeText(context, "↔ Transfer Recorded: $amount RWF", Toast.LENGTH_SHORT).show()
                                }
                                else -> {}
                            }
                        }
                        quickActionDialogType = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Emeza (Save)")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { quickActionDialogType = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Biba (Cancel)")
                }
            }
        )
    }
}

@Composable
fun DrawerHeader(
    shopName: String,
    currency: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IfarangaLogo(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = shopName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Currency: $currency (Rwanda)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun DrawerFooter(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onLockApp: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggleDarkMode
            )
        }

        if (onLockApp != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onLockApp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Rounded.Lock, contentDescription = "Lock App")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock App")
            }
        }
    }
}
