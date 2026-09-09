package com.example.myapplication.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.LoanDebtRepository
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoanDebtViewModel(
    private val loanDebtRepository: LoanDebtRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val receivables: StateFlow<List<LoanDebt>> = loanDebtRepository
        .getLoansDebtsByDirection(DebtDirection.RECEIVABLE)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val payables: StateFlow<List<LoanDebt>> = loanDebtRepository
        .getLoansDebtsByDirection(DebtDirection.PAYABLE)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalReceivables: StateFlow<Double> = loanDebtRepository.getTotalReceivables()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalPayables: StateFlow<Double> = loanDebtRepository.getTotalPayables()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val accounts: StateFlow<List<Account>> = accountRepository.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addLoanDebt(loanDebt: LoanDebt) {
        viewModelScope.launch {
            loanDebtRepository.insertLoanDebt(loanDebt)
        }
    }

    fun addLoanDebt(
        personOrInstitution: String,
        phoneNumber: String?,
        direction: DebtDirection,
        principalAmount: Double,
        dueDate: Long? = null
    ) {
        val loanDebt = LoanDebt(
            personOrInstitution = personOrInstitution,
            phoneNumber = phoneNumber,
            direction = direction,
            principalAmount = principalAmount,
            remainingAmount = principalAmount,
            dueDate = dueDate,
            status = DebtStatus.ACTIVE,
            createdAt = System.currentTimeMillis()
        )
        addLoanDebt(loanDebt)
    }

    fun recordRepayment(
        loanDebtId: Long,
        repaymentAmount: Double,
        accountId: Long? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (repaymentAmount <= 0) {
            onError("Repayment amount must be greater than zero")
            return
        }

        viewModelScope.launch {
            val loanDebt = loanDebtRepository.getLoanDebtById(loanDebtId)
            if (loanDebt == null) {
                onError("Record not found")
                return@launch
            }

            val updatedLoanDebt = loanDebtRepository.recordRepayment(loanDebtId, repaymentAmount)

            if (accountId != null && updatedLoanDebt != null) {
                val account = accountRepository.getAccountById(accountId)
                if (account != null) {
                    val newBalance = if (loanDebt.direction == DebtDirection.RECEIVABLE) {
                        account.currentBalance + repaymentAmount
                    } else {
                        account.currentBalance - repaymentAmount
                    }
                    accountRepository.updateBalance(accountId, newBalance)

                    val transaction = Transaction(
                        accountId = accountId,
                        type = TransactionType.DEBT_REPAY,
                        amount = repaymentAmount,
                        description = "Repayment (${loanDebt.direction}): ${loanDebt.personOrInstitution}",
                        timestamp = System.currentTimeMillis()
                    )
                    transactionRepository.insertTransaction(transaction)
                }
            }

            onSuccess()
        }
    }

    fun updateDebtStatus(id: Long, status: DebtStatus) {
        viewModelScope.launch {
            loanDebtRepository.updateStatus(id, status)
        }
    }

    class Factory(
        private val loanDebtRepository: LoanDebtRepository,
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoanDebtViewModel::class.java)) {
                return LoanDebtViewModel(
                    loanDebtRepository,
                    accountRepository,
                    transactionRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
