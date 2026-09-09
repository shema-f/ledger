package com.example.myapplication.ui.money

import com.example.myapplication.data.repository.AccountRepositoryImpl
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.FakeAccountDao
import com.example.myapplication.domain.repository.FakeTransactionDao
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoneyFlowViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var accountDao: FakeAccountDao

    private lateinit var transactionRepository: TransactionRepositoryImpl
    private lateinit var accountRepository: AccountRepositoryImpl

    private lateinit var viewModel: MoneyFlowViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        transactionDao = FakeTransactionDao()
        accountDao = FakeAccountDao()

        transactionRepository = TransactionRepositoryImpl(transactionDao)
        accountRepository = AccountRepositoryImpl(accountDao)

        viewModel = MoneyFlowViewModel(
            transactionRepository = transactionRepository,
            accountRepository = accountRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDefaultMoneyFlowMath() = runTest {
        val defaultViewModel = MoneyFlowViewModel()
        val state = defaultViewModel.uiState.value

        assertEquals(2450000.0, state.moneyIn, 0.01)
        assertEquals(520000.0, state.saved, 0.01)
        assertEquals(1720000.0, state.spent, 0.01)

        // Retained = Money In - Spent (2,450,000 - 1,720,000 = 730,000)
        assertEquals(730000.0, state.retained, 0.01)

        // Category expenses sum check
        val categoriesSum = state.categoryExpenses.sumOf { it.amount }
        assertEquals(1720000.0, categoriesSum, 1.0)

        // Category percentages sum check (~100%)
        val percentagesSum = state.categoryExpenses.sumOf { it.percentage.toDouble() }
        assertEquals(100.0, percentagesSum, 1.0)

        // Wallets check
        val wallets = state.accountWallets
        assertEquals(5, wallets.size)
        assertTrue(wallets.any { it.name.contains("MTN") })
        assertTrue(wallets.any { it.name.contains("Airtel") })
        assertTrue(wallets.any { it.name.contains("Bank") || it.name.contains("BK") })
        assertTrue(wallets.any { it.name.contains("Cash") })
        assertTrue(wallets.any { it.name.contains("SACCO") })
    }

    @Test
    fun testMoneyFlowWithTransactionsFromRepository() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        // Seed transactions
        transactionRepository.insertTransaction(
            Transaction(
                accountId = 1L,
                type = TransactionType.INCOME,
                amount = 2500000.0,
                description = "Store sales revenue",
                timestamp = System.currentTimeMillis()
            )
        )
        transactionRepository.insertTransaction(
            Transaction(
                accountId = 1L,
                type = TransactionType.EXPENSE,
                amount = 1000000.0,
                description = "Inventory stock purchase",
                timestamp = System.currentTimeMillis()
            )
        )
        transactionRepository.insertTransaction(
            Transaction(
                accountId = 1L,
                type = TransactionType.EXPENSE,
                amount = 400000.0,
                description = "Shop rent payment",
                timestamp = System.currentTimeMillis()
            )
        )
        transactionRepository.insertTransaction(
            Transaction(
                accountId = 1L,
                type = TransactionType.EXPENSE,
                amount = 100000.0,
                description = "Staff food & lunch",
                timestamp = System.currentTimeMillis()
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2500000.0, state.moneyIn, 0.01)
        assertEquals(1500000.0, state.spent, 0.01)

        // Retained = Money In - Spent (2,500,000 - 1,500,000 = 1,000,000)
        assertEquals(1000000.0, state.retained, 0.01)

        val inventoryItem = state.categoryExpenses.find { it.category == ExpenseCategory.INVENTORY_STOCK }
        assertNotNull(inventoryItem)
        assertEquals(1000000.0, inventoryItem!!.amount, 0.01)

        val rentItem = state.categoryExpenses.find { it.category == ExpenseCategory.RENT_AND_UTILITIES }
        assertNotNull(rentItem)
        assertEquals(400000.0, rentItem!!.amount, 0.01)

        val foodItem = state.categoryExpenses.find { it.category == ExpenseCategory.FOOD_AND_LIVING }
        assertNotNull(foodItem)
        assertEquals(100000.0, foodItem!!.amount, 0.01)
    }

    @Test
    fun testUpdateFlowValuesAndSetSavings() = runTest {
        viewModel.updateFlowValues(moneyIn = 3000000.0, spent = 2000000.0, saved = 600000.0)
        var state = viewModel.uiState.value

        assertEquals(3000000.0, state.moneyIn, 0.01)
        assertEquals(2000000.0, state.spent, 0.01)
        assertEquals(600000.0, state.saved, 0.01)
        assertEquals(1000000.0, state.retained, 0.01)

        viewModel.setSavings(800000.0)
        state = viewModel.uiState.value
        assertEquals(800000.0, state.saved, 0.01)
    }

    @Test
    fun testCategoryMappingFromDescription() {
        assertEquals(ExpenseCategory.FOOD_AND_LIVING, viewModel.mapCategoryFromDescription("Staff lunch food"))
        assertEquals(ExpenseCategory.TRANSPORT_AND_FUEL, viewModel.mapCategoryFromDescription("Fuel for delivery bike"))
        assertEquals(ExpenseCategory.INVENTORY_STOCK, viewModel.mapCategoryFromDescription("New stock inventory"))
        assertEquals(ExpenseCategory.RENT_AND_UTILITIES, viewModel.mapCategoryFromDescription("Monthly shop rent and electricity"))
        assertEquals(ExpenseCategory.AIRTIME_AND_DATA, viewModel.mapCategoryFromDescription("MTN Airtime minite and data"))
        assertEquals(ExpenseCategory.OTHER, viewModel.mapCategoryFromDescription("General miscellaneous"))
    }

    @Test
    fun testAccountWalletsBreakdown() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        accountRepository.insertAccount(
            Account(id = 1, name = "MTN MoMo Main", accountType = AccountType.MOBILE_MONEY, currentBalance = 900000.0)
        )
        accountRepository.insertAccount(
            Account(id = 2, name = "Airtel Money Agent", accountType = AccountType.MOBILE_MONEY, currentBalance = 250000.0)
        )
        accountRepository.insertAccount(
            Account(id = 3, name = "Bank of Kigali (BK)", accountType = AccountType.BANK, currentBalance = 1200000.0)
        )
        accountRepository.insertAccount(
            Account(id = 4, name = "Cash Register 1", accountType = AccountType.CASH, currentBalance = 150000.0)
        )
        accountRepository.insertAccount(
            Account(id = 5, name = "Urwego SACCO", accountType = AccountType.SACCO, currentBalance = 90000.0)
        )

        advanceUntilIdle()

        val wallets = viewModel.uiState.value.accountWallets
        assertEquals(5, wallets.size)

        val mtnWallet = wallets.find { it.name.contains("MTN") }
        assertNotNull(mtnWallet)
        assertEquals(900000.0, mtnWallet!!.balance, 0.01)

        val bkWallet = wallets.find { it.name.contains("BK") || it.name.contains("Bank") }
        assertNotNull(bkWallet)
        assertEquals(1200000.0, bkWallet!!.balance, 0.01)
    }
}
