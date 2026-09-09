package com.example.myapplication.ui.loans

import com.example.myapplication.data.repository.AccountRepositoryImpl
import com.example.myapplication.data.repository.LoanDebtRepositoryImpl
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.FakeAccountDao
import com.example.myapplication.domain.repository.FakeLoanDebtDao
import com.example.myapplication.domain.repository.FakeTransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class LoanDebtViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loanDebtDao: FakeLoanDebtDao
    private lateinit var accountDao: FakeAccountDao
    private lateinit var transactionDao: FakeTransactionDao

    private lateinit var loanDebtRepository: LoanDebtRepositoryImpl
    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var transactionRepository: TransactionRepositoryImpl

    private lateinit var viewModel: LoanDebtViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        loanDebtDao = FakeLoanDebtDao()
        accountDao = FakeAccountDao()
        transactionDao = FakeTransactionDao()

        loanDebtRepository = LoanDebtRepositoryImpl(loanDebtDao)
        accountRepository = AccountRepositoryImpl(accountDao)
        transactionRepository = TransactionRepositoryImpl(transactionDao)

        viewModel = LoanDebtViewModel(
            loanDebtRepository = loanDebtRepository,
            accountRepository = accountRepository,
            transactionRepository = transactionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddReceivableAndPayable() = runTest {
        backgroundScope.launch { viewModel.receivables.collect {} }
        backgroundScope.launch { viewModel.payables.collect {} }
        backgroundScope.launch { viewModel.totalReceivables.collect {} }
        backgroundScope.launch { viewModel.totalPayables.collect {} }

        // Add customer debt (Receivable)
        viewModel.addLoanDebt(
            personOrInstitution = "Mugisha Eric",
            phoneNumber = "0788111222",
            direction = DebtDirection.RECEIVABLE,
            principalAmount = 50000.0
        )

        // Add business loan (Payable)
        viewModel.addLoanDebt(
            personOrInstitution = "Bank of Kigali",
            phoneNumber = null,
            direction = DebtDirection.PAYABLE,
            principalAmount = 200000.0
        )
        advanceUntilIdle()

        val receivables = viewModel.receivables.value
        assertEquals(1, receivables.size)
        assertEquals("Mugisha Eric", receivables[0].personOrInstitution)

        val payables = viewModel.payables.value
        assertEquals(1, payables.size)
        assertEquals("Bank of Kigali", payables[0].personOrInstitution)

        assertEquals(50000.0, viewModel.totalReceivables.value, 0.001)
        assertEquals(200000.0, viewModel.totalPayables.value, 0.001)
    }

    @Test
    fun testRecordPartialRepaymentCustomerDebt() = runTest {
        backgroundScope.launch { viewModel.receivables.collect {} }
        backgroundScope.launch { viewModel.totalReceivables.collect {} }

        val accountId = accountRepository.insertAccount(
            Account(name = "MTN MoMo", accountType = AccountType.MOBILE_MONEY, currentBalance = 10000.0)
        )

        val debtId = loanDebtRepository.insertLoanDebt(
            LoanDebt(
                personOrInstitution = "Keza Alice",
                phoneNumber = "0788333444",
                direction = DebtDirection.RECEIVABLE,
                principalAmount = 50000.0,
                remainingAmount = 50000.0,
                status = DebtStatus.ACTIVE,
                createdAt = System.currentTimeMillis()
            )
        )
        advanceUntilIdle()

        var successCalled = false
        viewModel.recordRepayment(
            loanDebtId = debtId,
            repaymentAmount = 20000.0,
            accountId = accountId,
            onSuccess = { successCalled = true }
        )
        advanceUntilIdle()

        assertTrue(successCalled)

        // Check debt remaining amount and status
        val updatedDebt = loanDebtRepository.getLoanDebtById(debtId)
        assertNotNull(updatedDebt)
        assertEquals(30000.0, updatedDebt!!.remainingAmount, 0.001)
        assertEquals(DebtStatus.ACTIVE, updatedDebt.status)

        // Check account balance increased (10,000 + 20,000 = 30,000)
        val account = accountRepository.getAccountById(accountId)
        assertEquals(30000.0, account?.currentBalance ?: 0.0, 0.001)

        // Check transaction recorded
        val transactions = transactionRepository.getAllTransactions().first()
        assertEquals(1, transactions.size)
        assertEquals(TransactionType.DEBT_REPAY, transactions[0].type)
        assertEquals(20000.0, transactions[0].amount, 0.001)
    }

    @Test
    fun testRecordFullRepaymentBusinessLoan() = runTest {
        backgroundScope.launch { viewModel.payables.collect {} }
        backgroundScope.launch { viewModel.totalPayables.collect {} }

        val accountId = accountRepository.insertAccount(
            Account(name = "BK", accountType = AccountType.BANK, currentBalance = 150000.0)
        )

        val loanId = loanDebtRepository.insertLoanDebt(
            LoanDebt(
                personOrInstitution = "Urwego SACCO",
                direction = DebtDirection.PAYABLE,
                principalAmount = 100000.0,
                remainingAmount = 100000.0,
                status = DebtStatus.ACTIVE,
                createdAt = System.currentTimeMillis()
            )
        )
        advanceUntilIdle()

        var successCalled = false
        viewModel.recordRepayment(
            loanDebtId = loanId,
            repaymentAmount = 100000.0,
            accountId = accountId,
            onSuccess = { successCalled = true }
        )
        advanceUntilIdle()

        assertTrue(successCalled)

        // Check loan settled
        val updatedLoan = loanDebtRepository.getLoanDebtById(loanId)
        assertNotNull(updatedLoan)
        assertEquals(0.0, updatedLoan!!.remainingAmount, 0.001)
        assertEquals(DebtStatus.SETTLED, updatedLoan.status)

        // Check total payables is now 0.0
        assertEquals(0.0, viewModel.totalPayables.value, 0.001)

        // Check account balance decreased (150,000 - 100,000 = 50,000)
        val account = accountRepository.getAccountById(accountId)
        assertEquals(50000.0, account?.currentBalance ?: 0.0, 0.001)
    }

    @Test
    fun testUpdateDebtStatus() = runTest {
        backgroundScope.launch { viewModel.receivables.collect {} }

        val debtId = loanDebtRepository.insertLoanDebt(
            LoanDebt(
                personOrInstitution = "Defaulting Customer",
                direction = DebtDirection.RECEIVABLE,
                principalAmount = 10000.0,
                remainingAmount = 10000.0,
                status = DebtStatus.ACTIVE,
                createdAt = System.currentTimeMillis()
            )
        )
        advanceUntilIdle()

        viewModel.updateDebtStatus(debtId, DebtStatus.DEFAULTED)
        advanceUntilIdle()

        val updated = loanDebtRepository.getLoanDebtById(debtId)
        assertEquals(DebtStatus.DEFAULTED, updated?.status)
    }
}
