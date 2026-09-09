package com.example.myapplication.ui.business

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.customer.CustomerListScreen
import com.example.myapplication.ui.customer.CustomerViewModel
import com.example.myapplication.ui.inventory.InventoryScreen
import com.example.myapplication.ui.inventory.InventoryViewModel
import com.example.myapplication.ui.loans.LoanDebtScreen
import com.example.myapplication.ui.loans.LoanDebtViewModel

enum class BusinessSubTab(
    val title: String,
    val icon: ImageVector
) {
    INVENTORY_POS("Inventory & POS", Icons.Rounded.Inventory),
    LOANS_DEBTS("Debt & Loans", Icons.Rounded.AccountBalance),
    CUSTOMERS("Customer Ledger", Icons.Rounded.People)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHubScreen(
    inventoryViewModel: InventoryViewModel,
    loanDebtViewModel: LoanDebtViewModel,
    customerViewModel: CustomerViewModel,
    modifier: Modifier = Modifier,
    onCustomerClick: ((Long) -> Unit)? = null,
    initialTabOrdinal: Int = 0
) {
    var selectedTabOrdinal by remember { mutableIntStateOf(initialTabOrdinal) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "IFARANGA Business Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Stoke, Amadeni, n'Abakiriya",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedTabOrdinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BusinessSubTab.entries.forEachIndexed { index, subTab ->
                        Tab(
                            selected = selectedTabOrdinal == index,
                            onClick = { selectedTabOrdinal = index },
                            text = {
                                Text(
                                    text = subTab.title,
                                    fontWeight = if (selectedTabOrdinal == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = subTab.icon,
                                    contentDescription = subTab.title
                                )
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTabOrdinal,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "BusinessTabTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { targetIndex ->
            when (targetIndex) {
                0 -> {
                    InventoryScreen(
                        viewModel = inventoryViewModel
                    )
                }
                1 -> {
                    LoanDebtScreen(
                        viewModel = loanDebtViewModel
                    )
                }
                2 -> {
                    CustomerListScreen(
                        viewModel = customerViewModel,
                        onCustomerClick = { customerId ->
                            onCustomerClick?.invoke(customerId)
                        }
                    )
                }
            }
        }
    }
}
