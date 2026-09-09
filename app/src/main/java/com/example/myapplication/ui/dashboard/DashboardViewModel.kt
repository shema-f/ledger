package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.intelligence.FinancialHealthCalculator
import com.example.myapplication.domain.intelligence.FinancialHealthReport
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.DashboardSummary
import com.example.myapplication.domain.model.LedgerRecord
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.LedgerRepository
import com.example.myapplication.domain.repository.LoanDebtRepository
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val ledgerRepository: LedgerRepository,
    private val transactionRepository: TransactionRepository? = null,
    private val loanDebtRepository: LoanDebtRepository? = null,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    private val calculator = FinancialHealthCalculator()

    val dashboardSummary: StateFlow<DashboardSummary> = ledgerRepository.getDashboardSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardSummary()
        )

    val recentLedgerRecords: StateFlow<List<LedgerRecord>> = ledgerRepository.getRecentRecords(20)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customers: StateFlow<List<Customer>> = ledgerRepository.getCustomersSortedByName()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val financialHealthReport: StateFlow<FinancialHealthReport> = combine(
        dashboardSummary,
        transactionRepository?.getAllTransactions() ?: flowOf(emptyList()),
        loanDebtRepository?.getAllLoansDebts() ?: flowOf(emptyList()),
        accountRepository?.getAllAccounts() ?: flowOf(emptyList())
    ) { summary, txs, loans, accs ->
        calculator.calculateHealth(summary, txs, loans, accs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = calculator.calculateHealth(DashboardSummary(), emptyList(), emptyList(), emptyList())
    )

    fun addDebt(customerId: Long, amount: Double, description: String, dueDate: Long? = null) {
        viewModelScope.launch {
            ledgerRepository.addDebt(
                customerId = customerId,
                amount = amount,
                description = description,
                dueDate = dueDate
            )
        }
    }

    fun recordPayment(customerId: Long, amount: Double, description: String) {
        viewModelScope.launch {
            ledgerRepository.recordPayment(
                customerId = customerId,
                amount = amount,
                description = description
            )
        }
    }

    fun recordCashSale(amount: Double, description: String) {
        viewModelScope.launch {
            ledgerRepository.recordCashSale(
                amount = amount,
                description = description
            )
        }
    }

    class Factory(
        private val ledgerRepository: LedgerRepository,
        private val transactionRepository: TransactionRepository? = null,
        private val loanDebtRepository: LoanDebtRepository? = null,
        private val accountRepository: AccountRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(
                    ledgerRepository = ledgerRepository,
                    transactionRepository = transactionRepository,
                    loanDebtRepository = loanDebtRepository,
                    accountRepository = accountRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
