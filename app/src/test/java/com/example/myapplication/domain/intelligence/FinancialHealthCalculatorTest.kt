package com.example.myapplication.domain.intelligence

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.DashboardSummary
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FinancialHealthCalculatorTest {

    private lateinit var calculator: FinancialHealthCalculator

    @Before
    fun setUp() {
        calculator = FinancialHealthCalculator()
    }

    @Test
    fun testCalculateHealth_DefaultEmptyInputs() {
        val report = calculator.calculateHealth(
            dashboardSummary = DashboardSummary(),
            transactions = emptyList(),
            loanDebts = emptyList(),
            accounts = emptyList()
        )

        assertNotNull(report)
        assertTrue(report.overallScore in 0..100)
        assertNotNull(report.statusTitle)
        assertNotNull(report.keyInsight)
        assertEquals(0.0, report.forecastExpectedIncome, 0.01)
        assertEquals(0.0, report.forecastExpectedExpenses, 0.01)
        assertEquals(0.0, report.forecastNetRemaining, 0.01)
        assertNull(report.forecastAlert)
    }

    @Test
    fun testCalculateHealth_HighIncomeLowExpense_HighHealthScore() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            Transaction(
                id = 1,
                accountId = 1,
                type = TransactionType.INCOME,
                amount = 1_000_000.0,
                description = "Bulk Wholesale Sale",
                timestamp = now - 86400000 * 5
            ),
            Transaction(
                id = 2,
                accountId = 1,
                type = TransactionType.EXPENSE,
                amount = 100_000.0,
                description = "Utilities & Rent",
                timestamp = now
            )
        )

        val accounts = listOf(
            Account(
                id = 1,
                name = "MoMo Merchant",
                accountType = AccountType.MOBILE_MONEY,
                currentBalance = 500_000.0
            )
        )

        val report = calculator.calculateHealth(
            dashboardSummary = DashboardSummary(todaySales = 200_000.0),
            transactions = transactions,
            loanDebts = emptyList(),
            accounts = accounts
        )

        assertTrue("Overall score should be high ($report)", report.overallScore >= 80)
        assertEquals("Strong", report.statusTitle)
        assertTrue(report.cashFlowScore >= 80)
        assertTrue(report.debtScore >= 90)
        assertTrue(report.savingsScore >= 80)
        assertTrue(report.expensesScore >= 80)
        assertTrue(report.profitabilityScore >= 80)

        assertTrue(report.forecastExpectedIncome > report.forecastExpectedExpenses)
        assertTrue(report.forecastNetRemaining > 0)
        assertNull(report.forecastAlert)
    }

    @Test
    fun testCalculateHealth_HighDebtAndExpenses_TriggersAlert() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            Transaction(
                id = 1,
                accountId = 1,
                type = TransactionType.EXPENSE,
                amount = 800_000.0,
                description = "Stock purchase",
                timestamp = now
            )
        )

        val loanDebts = listOf(
            LoanDebt(
                id = 1,
                personOrInstitution = "BK Bank Loan",
                direction = DebtDirection.PAYABLE,
                principalAmount = 2_000_000.0,
                remainingAmount = 1_500_000.0,
                status = DebtStatus.ACTIVE,
                createdAt = now
            )
        )

        val accounts = listOf(
            Account(
                id = 1,
                name = "Cash Box",
                accountType = AccountType.CASH,
                currentBalance = 100_000.0
            )
        )

        val report = calculator.calculateHealth(
            dashboardSummary = DashboardSummary(totalOutstandingDebt = 500_000.0),
            transactions = transactions,
            loanDebts = loanDebts,
            accounts = accounts
        )

        assertTrue(report.overallScore < 70)
        assertNotNull(report.forecastAlert)
        assertTrue(report.forecastAlert!!.contains("Cash shortage warning"))
    }

    @Test
    fun testCalculateHealth_ForecastProjectionsMath() {
        val now = System.currentTimeMillis()
        val daysAgo5 = now - (86400000L * 5)
        val transactions = listOf(
            Transaction(
                id = 1,
                accountId = 1,
                type = TransactionType.INCOME,
                amount = 500_000.0,
                description = "Sale A",
                timestamp = daysAgo5
            ),
            Transaction(
                id = 2,
                accountId = 1,
                type = TransactionType.EXPENSE,
                amount = 100_000.0,
                description = "Expense A",
                timestamp = now
            )
        )

        val accounts = listOf(
            Account(
                id = 1,
                name = "Bank Account",
                accountType = AccountType.BANK,
                currentBalance = 200_000.0
            )
        )

        val report = calculator.calculateHealth(
            dashboardSummary = DashboardSummary(),
            transactions = transactions,
            loanDebts = emptyList(),
            accounts = accounts
        )

        val expectedDailyIncome = 500_000.0 / 5.0
        val expectedDailyExpense = 100_000.0 / 5.0

        assertEquals(expectedDailyIncome * 30, report.forecastExpectedIncome, 0.01)
        assertEquals(expectedDailyExpense * 30, report.forecastExpectedExpenses, 0.01)
        val expectedNet = 200_000.0 + (expectedDailyIncome * 30) - (expectedDailyExpense * 30)
        assertEquals(expectedNet, report.forecastNetRemaining, 0.01)
    }
}
