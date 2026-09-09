package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Locale

@Composable
fun IfarangaIntelligenceCard(
    keyInsight: String,
    expectedInflow: Double,
    expectedExpenses: Double,
    expectedRemaining: Double,
    modifier: Modifier = Modifier,
    cashShortageAlert: String? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with Intelligence Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = "IFARANGA Intelligence",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "IFARANGA Intelligence",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time AI Financial Insights",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Smart Insight Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = keyInsight,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }

            // Cash Shortage Warning Banner if active
            cashShortageAlert?.let { alert ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2), // Light Red/Amber
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Cash Shortage Alert",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = alert,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // 30-Day Forecast Summary
            Text(
                text = "30-Day Forecast Projection",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expected Inflow
                ForecastMetricTile(
                    title = "Expected Inflow",
                    amount = "+${formatAmount(expectedInflow)} RWF",
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    color = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )

                // Expected Outflow/Expenses
                ForecastMetricTile(
                    title = "Expected Expenses",
                    amount = "-${formatAmount(expectedExpenses)} RWF",
                    icon = Icons.AutoMirrored.Rounded.TrendingDown,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )

                // Expected Remaining
                ForecastMetricTile(
                    title = "Net Remaining",
                    amount = "${if (expectedRemaining >= 0) "+" else ""}${formatAmount(expectedRemaining)} RWF",
                    icon = if (expectedRemaining >= 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                    color = if (expectedRemaining >= 0) Color(0xFF0D9488) else Color(0xFFDC2626),
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun ForecastMetricTile(
    title: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%,.0f", amount)
}

@Preview(showBackground = true)
@Composable
fun IfarangaIntelligenceCardPreview() {
    MyApplicationTheme {
        IfarangaIntelligenceCard(
            keyInsight = "🧠 IFARANGA Noticed: Revenue ↑ 14%, Expenses ↑ 22%. Watch expense growth to preserve profit margin.",
            expectedInflow = 3500000.0,
            expectedExpenses = 1800000.0,
            expectedRemaining = 1700000.0,
            cashShortageAlert = "⚠️ Cash shortage warning: upcoming loan payment of 250,000 RWF exceeds current cash balance."
        )
    }
}
