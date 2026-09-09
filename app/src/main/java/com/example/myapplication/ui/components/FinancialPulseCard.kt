package com.example.myapplication.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Locale

@Composable
fun FinancialPulseCard(
    moneyIn: Double,
    moneyOut: Double,
    modifier: Modifier = Modifier,
    netMovement: Double = moneyIn - moneyOut,
    statusTitle: String = "Healthy",
    isHealthy: Boolean = netMovement >= 0,
    microInsight: String = "You are spending less than your daily average."
) {
    val statusColor = if (isHealthy) Color(0xFF059669) else Color(0xFFD97706)
    val statusContainer = if (isHealthy) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
    val statusOnContainer = if (isHealthy) Color(0xFF065F46) else Color(0xFF92400E)

    // Pulse animation for the dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

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
                .padding(18.dp)
        ) {
            // Header: Title with Pulse Dot + Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pulse Dot
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .scale(pulseScale)
                                .alpha(pulseAlpha)
                                .background(color = statusColor, shape = CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color = statusColor, shape = CircleShape)
                        )
                    }

                    Text(
                        text = "Financial Pulse",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status Tag Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isHealthy) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = statusOnContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusOnContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Metrics Row: Money In | Money Out | Net Movement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Money In
                MetricBox(
                    label = "Money In",
                    amount = "+${formatAmount(moneyIn)} RWF",
                    color = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Money Out
                MetricBox(
                    label = "Money Out",
                    amount = "-${formatAmount(moneyOut)} RWF",
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Net Movement
                MetricBox(
                    label = "Net Movement",
                    amount = "${if (netMovement >= 0) "+" else ""}${formatAmount(netMovement)} RWF",
                    color = if (netMovement >= 0) Color(0xFF0D9488) else Color(0xFFDC2626),
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Micro-insight statement
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = microInsight,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
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
fun FinancialPulseCardPreview() {
    MyApplicationTheme {
        FinancialPulseCard(
            moneyIn = 125000.0,
            moneyOut = 45000.0,
            netMovement = 80000.0,
            statusTitle = "Healthy",
            isHealthy = true,
            microInsight = "You are spending less than your daily average."
        )
    }
}
