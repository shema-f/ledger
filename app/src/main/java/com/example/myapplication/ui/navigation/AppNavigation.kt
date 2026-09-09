package com.example.myapplication.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.service.FinancialSms
import com.example.myapplication.service.PaymentAlertManager
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.ImariAuthScreen
import com.example.myapplication.ui.components.ImariLogo
import com.example.myapplication.ui.customer.CustomerDetailScreen
import com.example.myapplication.ui.customer.CustomerListScreen
import com.example.myapplication.ui.customer.CustomerViewModel
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.inventory.InventoryScreen
import com.example.myapplication.ui.inventory.InventoryViewModel
import com.example.myapplication.ui.loans.LoanDebtScreen
import com.example.myapplication.ui.loans.LoanDebtViewModel
import com.example.myapplication.ui.momo.MoMoFeedViewModel
import com.example.myapplication.ui.momo.MoMoLiveFeedScreen
import com.example.myapplication.ui.onboarding.NotificationOnboardingScreen
import com.example.myapplication.ui.reconciliation.SmsReconciliationBottomSheet
import com.example.myapplication.ui.reconciliation.SmsReconciliationViewModel
import com.example.myapplication.ui.reports.ReportsScreen
import com.example.myapplication.ui.reports.ReportsViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PrimaryEmeraldTealLight
import kotlinx.coroutines.launch

enum class AppDrawerItem(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    INVENTORY("Inventory & POS Lite", Icons.Rounded.Inventory),
    CUSTOMERS("Customer Ledger & Debts", Icons.Rounded.People),
    LOANS("Loans & Business Liabilities", Icons.Rounded.AccountBalance),
    MOMO_FEED("MoMo & Bank Live Feed", Icons.Rounded.QrCode),
    REPORTS("Financial Reports & PDF", Icons.Rounded.BarChart),
    SECURITY("Security & Permissions", Icons.Rounded.Lock)
}

@ExperimentalMaterial3Api
@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    customerViewModel: CustomerViewModel,
    inventoryViewModel: InventoryViewModel,
    loanDebtViewModel: LoanDebtViewModel,
    momoFeedViewModel: MoMoFeedViewModel,
    reconciliationViewModel: SmsReconciliationViewModel? = null,
    reportsViewModel: ReportsViewModel? = null,
    authViewModel: AuthViewModel? = null,
    onLockApp: (() -> Unit)? = null,
    isDarkMode: Boolean = false,
    onToggleDarkMode: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppDrawerItem.DASHBOARD) }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var liveAlertSms by remember { mutableStateOf<FinancialSms?>(null) }
    var isDarkTheme by remember { mutableStateOf(isDarkMode) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
                    // Drawer Header
                    DrawerHeader(
                        shopName = "Imari Merchant",
                        currency = "RWF"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Drawer Items
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
                                        text = item.title,
                                        fontWeight = if (currentTab == item) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                selected = currentTab == item && selectedCustomerId == null,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                    selectedCustomerId = null
                                    currentTab = item
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    // Drawer Footer
                    DrawerFooter(
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
                            text = if (selectedCustomerId != null) "Customer Detail" else currentTab.title,
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
                                    contentDescription = "Open Navigation Menu"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
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
            } else {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) { targetTab ->
                    when (targetTab) {
                        AppDrawerItem.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigateToMoMoFeed = { currentTab = AppDrawerItem.MOMO_FEED },
                                onNavigateToCustomers = { currentTab = AppDrawerItem.CUSTOMERS },
                                onCustomerClick = { customerId ->
                                    selectedCustomerId = customerId
                                }
                            )
                        }
                        AppDrawerItem.INVENTORY -> {
                            InventoryScreen(
                                viewModel = inventoryViewModel
                            )
                        }
                        AppDrawerItem.CUSTOMERS -> {
                            CustomerListScreen(
                                viewModel = customerViewModel,
                                onCustomerClick = { customerId ->
                                    selectedCustomerId = customerId
                                }
                            )
                        }
                        AppDrawerItem.LOANS -> {
                            LoanDebtScreen(
                                viewModel = loanDebtViewModel
                            )
                        }
                        AppDrawerItem.MOMO_FEED -> {
                            MoMoLiveFeedScreen(
                                viewModel = momoFeedViewModel
                            )
                        }
                        AppDrawerItem.REPORTS -> {
                            if (reportsViewModel != null) {
                                ReportsScreen(
                                    viewModel = reportsViewModel,
                                    authViewModel = authViewModel
                                )
                            }
                        }
                        AppDrawerItem.SECURITY -> {
                            if (authViewModel != null) {
                                var selectedSubTab by remember { mutableIntStateOf(0) }
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SecondaryTabRow(selectedTabIndex = selectedSubTab) {
                                        Tab(
                                            selected = selectedSubTab == 0,
                                            onClick = { selectedSubTab = 0 },
                                            text = { Text("Security & PIN") }
                                        )
                                        Tab(
                                            selected = selectedSubTab == 1,
                                            onClick = { selectedSubTab = 1 },
                                            text = { Text("App Permissions") }
                                        )
                                    }
                                    if (selectedSubTab == 0) {
                                        ImariAuthScreen(viewModel = authViewModel)
                                    } else {
                                        NotificationOnboardingScreen()
                                    }
                                }
                            } else {
                                NotificationOnboardingScreen()
                            }
                        }
                    }
                }
            }

            // Display interactive reconciliation bottom sheet on live financial alert
            liveAlertSms?.let { sms ->
                if (reconciliationViewModel != null) {
                    SmsReconciliationBottomSheet(
                        financialSms = sms,
                        viewModel = reconciliationViewModel,
                        onDismiss = { liveAlertSms = null }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerHeader(
    shopName: String = "Imari Merchant",
    currency: String = "RWF"
) {
    Surface(
        color = PrimaryEmeraldTealLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            ImariLogo(
                isHorizontal = true,
                iconSize = 48.dp,
                showTagline = true,
                textColor = Color.White,
                taglineColor = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = shopName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Smart Financial Ledger",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🇷🇼",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerFooter(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onLockApp: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleDarkMode(!isDarkMode) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (isDarkMode) "Dark" else "Light",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    modifier = Modifier.scale(0.85f)
                )
            }

            if (onLockApp != null) {
                OutlinedButton(
                    onClick = onLockApp,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LockReset,
                        contentDescription = "Lock App",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lock App",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawerHeaderPreview() {
    MyApplicationTheme {
        DrawerHeader(
            shopName = "Imari Merchant",
            currency = "RWF"
        )
    }
}
