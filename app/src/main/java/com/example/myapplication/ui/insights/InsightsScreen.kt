package com.example.myapplication.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.intelligence.FinancialHealthReport
import com.example.myapplication.ui.components.FinancialHealthScoreCard
import com.example.myapplication.ui.components.FinancialPulseCard
import com.example.myapplication.ui.components.IfarangaIntelligenceCard
import com.example.myapplication.ui.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val healthReport by viewModel.financialHealthReport.collectAsState()

    InsightsContent(
        healthReport = healthReport,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsContent(
    healthReport: FinancialHealthReport,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "IFARANGA Insights",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Financial Health & Smart Forecasting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Financial Pulse Card
            item {
                FinancialPulseCard(
                    moneyIn = healthReport.forecastExpectedIncome / 30.0,
                    moneyOut = healthReport.forecastExpectedExpenses / 30.0,
                    netMovement = (healthReport.forecastExpectedIncome - healthReport.forecastExpectedExpenses) / 30.0,
                    statusTitle = healthReport.statusTitle,
                    isHealthy = healthReport.overallScore >= 60,
                    microInsight = "Daily average flow calculated across all accounts and ledger sales."
                )
            }

            // 2. Financial Health Score Card
            item {
                FinancialHealthScoreCard(
                    overallScore = healthReport.overallScore,
                    statusTitle = healthReport.statusTitle,
                    cashFlowScore = healthReport.cashFlowScore,
                    debtScore = healthReport.debtScore,
                    savingsScore = healthReport.savingsScore,
                    expensesScore = healthReport.expensesScore,
                    profitabilityScore = healthReport.profitabilityScore
                )
            }

            // 3. IFARANGA Intelligence Card
            item {
                IfarangaIntelligenceCard(
                    keyInsight = healthReport.keyInsight,
                    expectedInflow = healthReport.forecastExpectedIncome,
                    expectedExpenses = healthReport.forecastExpectedExpenses,
                    expectedRemaining = healthReport.forecastNetRemaining,
                    cashShortageAlert = healthReport.forecastAlert
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
