package com.example.myapplication.ui.reports

import com.example.myapplication.data.repository.AccountRepositoryImpl
import com.example.myapplication.data.repository.LoanDebtRepositoryImpl
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.FakeAccountDao
import com.example.myapplication.domain.repository.FakeLoanDebtDao
import com.example.myapplication.domain.repository.FakeTransactionDao
import com.example.myapplication.fakes.FakeLedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var accountDao: FakeAccountDao
    private lateinit var loanDebtDao: FakeLoanDebtDao

    private lateinit var transactionRepository: TransactionRepositoryImpl
    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var ledgerRepository: FakeLedgerRepository
    private lateinit var loanDebtRepository: LoanDebtRepositoryImpl

    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        transactionDao = FakeTransactionDao()
        accountDao = FakeAccountDao()
        loanDebtDao = FakeLoanDebtDao()

        transactionRepository = TransactionRepositoryImpl(transactionDao)
        accountRepository = AccountRepositoryImpl(accountDao)
        ledgerRepository = FakeLedgerRepository()
        loanDebtRepository = LoanDebtRepositoryImpl(loanDebtDao)

        viewModel = ReportsViewModel(
            transactionRepository = transactionRepository,
            accountRepository = accountRepository,
            ledgerRepository = ledgerRepository,
            loanDebtRepository = loanDebtRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiState() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DateRangeFilter.ALL_TIME, state.selectedDateRange)
        assertEquals(0.0, state.grossRevenue, 0.001)
        assertEquals(0.0, state.cogs, 0.001)
        assertEquals(0.0, state.grossProfit, 0.001)
        assertEquals(0.0, state.uncollectedDebts, 0.001)
        assertEquals(0.0, state.outstandingLoans, 0.001)
    }

    @Test
    fun testFinancialSummaryCalculations() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val accId = accountRepository.insertAccount(
            Account(name = "MTN MoMo", accountType = AccountType.MOBILE_MONEY, currentBalance = 50000.0)
        )

        val now = System.currentTimeMillis()

        // Income sale
        transactionRepository.insertTransaction(
            Transaction(
                accountId = accId,
                type = TransactionType.INCOME,
                amount = 100000.0,
                description = "POS Sale",
                timestamp = now
            )
        )

        // Expense (COGS)
        transactionRepository.insertTransaction(
            Transaction(
                accountId = accId,
                type = TransactionType.EXPENSE,
                amount = 40000.0,
                description = "Stock purchase",
                timestamp = now
            )
        )

        // Customer Debt in Ledger
        ledgerRepository.addCustomer(Customer(fullName = "John Doe", phoneNumber = "0788111111", totalDebt = 25000.0))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(100000.0, state.grossRevenue, 0.001)
        assertEquals(40000.0, state.cogs, 0.001)
        assertEquals(60000.0, state.grossProfit, 0.001)
        assertEquals(25000.0, state.uncollectedDebts, 0.001)
        assertEquals(1, state.accountBalances.size)
        assertEquals("MTN MoMo", state.accountBalances[0].name)
    }

    @Test
    fun testDateRangeFilterChange() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.setDateRangeFilter(DateRangeFilter.TODAY)
        advanceUntilIdle()

        assertEquals(DateRangeFilter.TODAY, viewModel.uiState.value.selectedDateRange)
    }
}
