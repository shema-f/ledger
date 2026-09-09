package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun FinancialHealthScoreCard(
    overallScore: Int,
    statusTitle: String,
    cashFlowScore: Int,
    debtScore: Int,
    savingsScore: Int,
    expensesScore: Int,
    profitabilityScore: Int,
    modifier: Modifier = Modifier,
    onTapExplanation: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val statusColor = when {
        overallScore >= 80 -> Color(0xFF059669) // Emerald
        overallScore >= 60 -> Color(0xFF0D9488) // Teal
        else -> Color(0xFFD97706) // Amber
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (onTapExplanation != null) {
                    onTapExplanation()
                } else {
                    isExpanded = !isExpanded
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Score Badge + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Score Badge Badge / Circular Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$overallScore",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                            Text(
                                text = "/100",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🟢 ",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$overallScore/100",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "Financial Health: $statusTitle",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showDialog = true }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Toggle Expand",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Breakdown Bars (Visible always or expand)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CategoryProgressBar(label = "Cash Flow", score = cashFlowScore, barColor = Color(0xFF059669))
                CategoryProgressBar(label = "Debt Management", score = debtScore, barColor = Color(0xFF2563EB))
                CategoryProgressBar(label = "Savings & Reserves", score = savingsScore, barColor = Color(0xFF7C3AED))
                CategoryProgressBar(label = "Expenses Ratio", score = expensesScore, barColor = Color(0xFFD97706))
                CategoryProgressBar(label = "Profitability", score = profitabilityScore, barColor = Color(0xFF0D9488))
            }

            // Expandable Detailed Tips & Explanation
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Score Breakdown & Optimization Tips",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Cash Flow: High cash influx improves liquidity.\n" +
                               "• Debt: Maintain debts lower than total account balances.\n" +
                               "• Savings: Keep cash reserves to buffer against slow periods.\n" +
                               "• Expenses: Keep operational expense ratio under 60%.\n" +
                               "• Profitability: High net profit margin raises your score.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Financial Health Score Explained",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your overall score is calculated out of 100 based on 5 weighted core pillars:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("1. Cash Flow (25%): Inflow vs Outflow ratio")
                    Text("2. Debt Load (20%): Payables relative to liquid balances")
                    Text("3. Savings & Reserves (15%): Cash buffer in MoMo & Banks")
                    Text("4. Expense Ratio (20%): Expense percentage of total revenue")
                    Text("5. Profitability (20%): Net profit margin percentage")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun CategoryProgressBar(
    label: String,
    score: Int,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$score/100",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { (score / 100f).coerceIn(0f, 1f) },
                color = barColor,
                trackColor = Color.Transparent,
                modifier = Modifier.clip(RoundedCornerShape(6.dp))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FinancialHealthScoreCardPreview() {
    MyApplicationTheme {
        FinancialHealthScoreCard(
            overallScore = 82,
            statusTitle = "Strong",
            cashFlowScore = 91,
            debtScore = 74,
            savingsScore = 80,
            expensesScore = 78,
            profitabilityScore = 86
        )
    }
}
