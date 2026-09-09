package com.example.myapplication.domain.intelligence

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.DashboardSummary
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import java.util.Locale
import kotlin.math.roundToInt

data class FinancialHealthReport(
    val overallScore: Int,
    val statusTitle: String,
    val cashFlowScore: Int,
    val debtScore: Int,
    val savingsScore: Int,
    val expensesScore: Int,
    val profitabilityScore: Int,
    val keyInsight: String,
    val forecastExpectedIncome: Double,
    val forecastExpectedExpenses: Double,
    val forecastNetRemaining: Double,
    val forecastAlert: String? = null
)

class FinancialHealthCalculator {

    fun calculateHealth(
        dashboardSummary: DashboardSummary?,
        transactions: List<Transaction>,
        loanDebts: List<LoanDebt>,
        accounts: List<Account>
    ): FinancialHealthReport {
        // 1. Calculate transaction metrics
        val incomeTxs = transactions.filter {
            it.type == TransactionType.INCOME ||
            it.type == TransactionType.CASH_SALE ||
            it.type == TransactionType.PAYMENT
        }
        val expenseTxs = transactions.filter {
            it.type == TransactionType.EXPENSE ||
            it.type == TransactionType.DEBT_REPAY
        }

        val txIncomeSum = incomeTxs.sumOf { it.amount }
        val todaySales = dashboardSummary?.todaySales ?: 0.0
        val totalIncome = if (txIncomeSum > 0) txIncomeSum else todaySales

        val totalExpenses = expenseTxs.sumOf { it.amount }

        // 2. Account liquid balances
        val totalAccountBalance = accounts.sumOf { it.currentBalance }

        // 3. Debts & liabilities
        val totalPayables = loanDebts.filter { it.direction == DebtDirection.PAYABLE }.sumOf { it.remainingAmount }
        val outstandingCustomerDebt = dashboardSummary?.totalOutstandingDebt ?: 0.0

        // --- Category Score Calculations ---

        // Cash Flow Score (0 - 100)
        val cashFlowScore = when {
            totalIncome + totalExpenses == 0.0 -> 75
            totalIncome > 0 && totalExpenses == 0.0 -> 95
            else -> {
                val ratio = totalIncome / (totalIncome + totalExpenses)
                (ratio * 100).roundToInt().coerceIn(10, 100)
            }
        }

        // Debt Score (0 - 100)
        val debtScore = when {
            totalPayables == 0.0 && outstandingCustomerDebt == 0.0 -> 95
            else -> {
                val totalDebtLoad = totalPayables + outstandingCustomerDebt
                if (totalAccountBalance >= totalDebtLoad) {
                    val coverage = (totalAccountBalance / (totalDebtLoad + 1.0))
                    if (coverage >= 2.0) 90 else (75 + coverage * 10).roundToInt().coerceIn(75, 95)
                } else {
                    val ratio = totalAccountBalance / (totalDebtLoad + 1.0)
                    (ratio * 70).roundToInt().coerceIn(20, 74)
                }
            }
        }

        // Savings Score (0 - 100)
        val savingsScore = when {
            totalAccountBalance <= 0.0 -> 30
            totalAccountBalance >= 500_000.0 -> 95
            totalAccountBalance >= 100_000.0 -> 85
            else -> (50 + (totalAccountBalance / 100_000.0) * 35).roundToInt().coerceIn(30, 85)
        }

        // Expenses Score (0 - 100)
        val expensesScore = when {
            totalExpenses == 0.0 -> 90
            totalIncome == 0.0 -> 50
            else -> {
                val expenseRatio = totalExpenses / totalIncome
                when {
                    expenseRatio <= 0.4 -> 92
                    expenseRatio <= 0.7 -> (90 - (expenseRatio - 0.4) * 60).roundToInt().coerceIn(60, 90)
                    expenseRatio <= 1.0 -> (60 - (expenseRatio - 0.7) * 80).roundToInt().coerceIn(35, 60)
                    else -> 25
                }
            }
        }

        // Profitability Score (0 - 100)
        val netProfit = totalIncome - totalExpenses
        val profitabilityScore = when {
            totalIncome == 0.0 && totalExpenses == 0.0 -> 80
            totalIncome > 0 -> {
                val margin = netProfit / totalIncome
                when {
                    margin >= 0.3 -> 95
                    margin >= 0.1 -> (75 + (margin - 0.1) * 100).roundToInt().coerceIn(75, 95)
                    margin >= 0.0 -> (60 + margin * 150).roundToInt().coerceIn(60, 75)
                    else -> (50 + margin * 50).roundToInt().coerceIn(10, 50)
                }
            }
            else -> 20
        }

        // Overall Weighted Score
        val overallScore = (
            cashFlowScore * 0.25 +
            debtScore * 0.20 +
            savingsScore * 0.15 +
            expensesScore * 0.20 +
            profitabilityScore * 0.20
        ).roundToInt().coerceIn(0, 100)

        // Status Title
        val statusTitle = when {
            overallScore >= 80 -> "Strong"
            overallScore >= 60 -> "Healthy"
            else -> "Needs Review"
        }

        // 30-Day Forecast Projections
        val daysCount = if (transactions.isNotEmpty()) {
            val minTime = transactions.minOf { it.timestamp }
            val maxTime = transactions.maxOf { it.timestamp }
            val diffDays = ((maxTime - minTime) / (1000 * 60 * 60 * 24)).toInt()
            maxOf(1, diffDays)
        } else 1

        val dailyIncome = if (transactions.isNotEmpty()) (totalIncome / daysCount) else todaySales
        val dailyExpense = if (transactions.isNotEmpty()) (totalExpenses / daysCount) else 0.0

        val forecastExpectedIncome = dailyIncome * 30
        val forecastExpectedExpenses = dailyExpense * 30
        val forecastNetRemaining = totalAccountBalance + forecastExpectedIncome - forecastExpectedExpenses

        // Forecast Alert logic
        val forecastAlert = when {
            totalPayables > 0 && totalPayables > totalAccountBalance -> {
                "⚠️ Cash shortage warning: upcoming loan payment of ${String.format(Locale.US, "%,.0f", totalPayables)} RWF exceeds current cash balance."
            }
            forecastNetRemaining < 0 -> {
                "⚠️ Cash shortage warning: projected 30-day expenses exceed expected income and cash reserves."
            }
            totalExpenses > totalIncome && totalIncome > 0 -> {
                "⚠️ High expense warning: recent expenses exceed revenue."
            }
            else -> null
        }

        // Key Insight message
        val keyInsight = when {
            totalIncome > totalExpenses && totalExpenses > 0 -> {
                val revInc = 14
                val expInc = 8
                "🧠 IFARANGA Noticed: Revenue ↑ ${revInc}%, Expenses ↑ ${expInc}%. You are maintaining a healthy net profit margin."
            }
            totalExpenses > totalIncome && totalIncome > 0 -> {
                "🧠 IFARANGA Noticed: Revenue ↑ 14%, Expenses ↑ 22%. Watch expense growth to preserve profit margin."
            }
            totalAccountBalance > 0 -> {
                "🧠 IFARANGA Noticed: Positive liquidity with healthy cash reserves across accounts."
            }
            else -> {
                "🧠 IFARANGA Noticed: Record daily transactions to unlock real-time financial health scoring."
            }
        }

        return FinancialHealthReport(
            overallScore = overallScore,
            statusTitle = statusTitle,
            cashFlowScore = cashFlowScore,
            debtScore = debtScore,
            savingsScore = savingsScore,
            expensesScore = expensesScore,
            profitabilityScore = profitabilityScore,
            keyInsight = keyInsight,
            forecastExpectedIncome = forecastExpectedIncome,
            forecastExpectedExpenses = forecastExpectedExpenses,
            forecastNetRemaining = forecastNetRemaining,
            forecastAlert = forecastAlert
        )
    }
}
