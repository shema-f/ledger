package com.example.myapplication.ui.money

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyFlowScreen(
    viewModel: MoneyFlowViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    MoneyFlowContent(
        uiState = uiState,
        modifier = modifier,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyFlowContent(
    uiState: MoneyFlowUiState,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "IFARANGA Money Flow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Amakuru y'Injiye, Isaho n'Izigama",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Visual Money Flow Diagram
            item {
                Text(
                    text = "Money Flow Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                VisualMoneyFlowDiagram(
                    moneyIn = uiState.moneyIn,
                    saved = uiState.saved,
                    spent = uiState.spent,
                    retained = uiState.retained
                )
            }

            // Section 2: Expenses by Category Breakdown Cards
            item {
                Text(
                    text = "Expenses by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ibyasohotse kuri buri gipimo cya serivisi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(uiState.categoryExpenses) { categoryItem ->
                ExpenseCategoryCard(item = categoryItem)
            }

            // Section 3: Account Wallets Breakdown
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Account Wallets Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Amasanduku na Konti z'ubwishingizi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(uiState.accountWallets) { wallet ->
                AccountWalletCard(wallet = wallet)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun VisualMoneyFlowDiagram(
    moneyIn: Double,
    saved: Double,
    spent: Double,
    retained: Double,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. MONEY IN
            MoneyFlowCard(
                title = "MONEY IN (+ Injiye)",
                amount = moneyIn,
                containerColor = Color(0xFF059669), // Emerald Green
                contentColor = Color.White,
                icon = Icons.Rounded.ArrowDownward
            )

            // Flow Arrow Down
            FlowArrow()

            // 2. SAVED & SPENT (Split Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SAVED
                Box(modifier = Modifier.weight(1f)) {
                    MoneyFlowCard(
                        title = "SAVED (Bikijwe)",
                        amount = saved,
                        containerColor = Color(0xFF0284C7), // Sky Blue
                        contentColor = Color.White,
                        icon = Icons.Rounded.Savings,
                        compact = true
                    )
                }

                // SPENT
                Box(modifier = Modifier.weight(1f)) {
                    MoneyFlowCard(
                        title = "SPENT (Byasohotse)",
                        amount = spent,
                        containerColor = Color(0xFFDC2626), // Crimson Red
                        contentColor = Color.White,
                        icon = Icons.Rounded.AccountBalanceWallet,
                        compact = true
                    )
                }
            }

            // Flow Arrow Down
            FlowArrow()

            // 3. RETAINED
            MoneyFlowCard(
                title = "RETAINED (Inzira / Ayagumyeho)",
                amount = retained,
                containerColor = Color(0xFF7C3AED), // Deep Purple
                contentColor = Color.White,
                icon = Icons.Rounded.AccountBalance
            )
        }
    }
}

@Composable
private fun FlowArrow() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowDownward,
                contentDescription = "Flow Down",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MoneyFlowCard(
    title: String,
    amount: Double,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 12.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(if (compact) 18.dp else 22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatRwandaCurrency(amount),
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ExpenseCategoryCard(
    item: ExpenseCategoryItem,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getCategoryIconAndColor(item.category)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.category.displayName,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = item.category.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.category.kinyarwandaName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatRwandaCurrency(item.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f%%", item.percentage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun AccountWalletCard(
    wallet: AccountWalletItem,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getWalletIconAndColor(wallet.name)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = wallet.name,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = wallet.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = wallet.accountType.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = formatRwandaCurrency(wallet.balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (wallet.balance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        }
    }
}

fun getCategoryIconAndColor(category: ExpenseCategory): Pair<ImageVector, Color> {
    return when (category) {
        ExpenseCategory.FOOD_AND_LIVING -> Icons.Rounded.Restaurant to Color(0xFFF59E0B)
        ExpenseCategory.TRANSPORT_AND_FUEL -> Icons.Rounded.DirectionsCar to Color(0xFF3B82F6)
        ExpenseCategory.INVENTORY_STOCK -> Icons.Rounded.Inventory2 to Color(0xFF10B981)
        ExpenseCategory.RENT_AND_UTILITIES -> Icons.Rounded.HomeWork to Color(0xFF8B5CF6)
        ExpenseCategory.AIRTIME_AND_DATA -> Icons.Rounded.PhoneAndroid to Color(0xFFEC4899)
        ExpenseCategory.OTHER -> Icons.Rounded.MoreHoriz to Color(0xFF6B7280)
    }
}

fun getWalletIconAndColor(name: String): Pair<ImageVector, Color> {
    val nameLower = name.lowercase()
    return when {
        nameLower.contains("mtn") -> Icons.Rounded.Smartphone to Color(0xFFEAB308) // Yellow
        nameLower.contains("airtel") -> Icons.Rounded.Smartphone to Color(0xFFEF4444) // Red
        nameLower.contains("bank") || nameLower.contains("bk") -> Icons.Rounded.AccountBalance to Color(0xFF2563EB) // Blue
        nameLower.contains("cash") -> Icons.Rounded.PointOfSale to Color(0xFF059669) // Emerald
        nameLower.contains("sacco") -> Icons.Rounded.Storefront to Color(0xFF0D9488) // Teal
        else -> Icons.Rounded.AccountBalanceWallet to Color(0xFF6366F1)
    }
}

fun formatRwandaCurrency(amount: Double): String {
    return String.format(Locale.US, "%,.0f RWF", amount)
}
