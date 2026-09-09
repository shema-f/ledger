package com.example.myapplication.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.service.FinancialSms
import com.example.myapplication.service.PaymentAlertManager
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.ImariAuthScreen
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

enum class BottomNavTab(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    REPORTS("Reports", Icons.Rounded.BarChart),
    CUSTOMERS("Customers", Icons.Rounded.Group),
    INVENTORY("Inventory & POS", Icons.Rounded.Inventory),
    LOANS("Loans & Debts", Icons.Rounded.AccountBalance),
    MOMO_FEED("MoMo Feed", Icons.AutoMirrored.Rounded.ReceiptLong),
    AUTH("Security", Icons.Rounded.Lock),
    PERMISSIONS("Permissions", Icons.Rounded.Security)
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
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(BottomNavTab.DASHBOARD) }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var liveAlertSms by remember { mutableStateOf<FinancialSms?>(null) }

    LaunchedEffect(Unit) {
        PaymentAlertManager.livePaymentAlerts.collect { sms ->
            liveAlertSms = sms
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedCustomerId == null) {
                NavigationBar {
                    BottomNavTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = {
                                currentTab = tab
                            },
                            icon = {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            },
                            label = {
                                Text(tab.title)
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
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
                    BottomNavTab.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateToMoMoFeed = { currentTab = BottomNavTab.MOMO_FEED },
                            onNavigateToCustomers = { currentTab = BottomNavTab.CUSTOMERS },
                            onCustomerClick = { customerId ->
                                selectedCustomerId = customerId
                            }
                        )
                    }
                    BottomNavTab.REPORTS -> {
                        if (reportsViewModel != null) {
                            ReportsScreen(
                                viewModel = reportsViewModel,
                                authViewModel = authViewModel
                            )
                        }
                    }
                    BottomNavTab.CUSTOMERS -> {
                        CustomerListScreen(
                            viewModel = customerViewModel,
                            onCustomerClick = { customerId ->
                                selectedCustomerId = customerId
                            }
                        )
                    }
                    BottomNavTab.INVENTORY -> {
                        InventoryScreen(
                            viewModel = inventoryViewModel
                        )
                    }
                    BottomNavTab.LOANS -> {
                        LoanDebtScreen(
                            viewModel = loanDebtViewModel
                        )
                    }
                    BottomNavTab.MOMO_FEED -> {
                        MoMoLiveFeedScreen(
                            viewModel = momoFeedViewModel
                        )
                    }
                    BottomNavTab.AUTH -> {
                        if (authViewModel != null) {
                            ImariAuthScreen(
                                viewModel = authViewModel
                            )
                        }
                    }
                    BottomNavTab.PERMISSIONS -> {
                        NotificationOnboardingScreen()
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
