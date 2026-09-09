package com.example.myapplication.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.ui.customer.CustomerDetailScreen
import com.example.myapplication.ui.customer.CustomerListScreen
import com.example.myapplication.ui.customer.CustomerViewModel
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.momo.MoMoFeedViewModel
import com.example.myapplication.ui.momo.MoMoLiveFeedScreen
import com.example.myapplication.ui.onboarding.NotificationOnboardingScreen

enum class BottomNavTab(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    CUSTOMERS("Customers", Icons.Rounded.Group),
    MOMO_FEED("MoMo Feed", Icons.AutoMirrored.Rounded.ReceiptLong),
    PERMISSIONS("Permissions", Icons.Rounded.Security)
}

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    customerViewModel: CustomerViewModel,
    momoFeedViewModel: MoMoFeedViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(BottomNavTab.DASHBOARD) }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }

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
                    BottomNavTab.CUSTOMERS -> {
                        CustomerListScreen(
                            viewModel = customerViewModel,
                            onCustomerClick = { customerId ->
                                selectedCustomerId = customerId
                            }
                        )
                    }
                    BottomNavTab.MOMO_FEED -> {
                        MoMoLiveFeedScreen(
                            viewModel = momoFeedViewModel
                        )
                    }
                    BottomNavTab.PERMISSIONS -> {
                        NotificationOnboardingScreen()
                    }
                }
            }
        }
    }
}
