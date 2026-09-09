package com.example.myapplication.ui.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.LedgerRepository
import com.example.myapplication.domain.repository.LoanDebtRepository
import com.example.myapplication.domain.repository.MoMoRepository
import com.example.myapplication.domain.repository.ProductRepository
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.service.FinancialSms
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReconciliationOption {
    POS_SALE,
    SETTLE_DEBT,
    GENERAL_REVENUE
}

enum class DebtSelectionType {
    CUSTOMER_LEDGER,
    LOAN_RECEIVABLE
}

class SmsReconciliationViewModel(
    private val productRepository: ProductRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val loanDebtRepository: LoanDebtRepository,
    private val ledgerRepository: LedgerRepository,
    private val moMoRepository: MoMoRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val accounts: StateFlow<List<Account>> = accountRepository.getAllAccounts()
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

    val loanDebts: StateFlow<List<LoanDebt>> = loanDebtRepository.getAllLoansDebts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun reconcilePosSale(
        sms: FinancialSms,
        product: Product,
        account: Account,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val profit = productRepository.deductStockAndCalculateProfit(product.id, 1)
            val transaction = Transaction(
                accountId = account.id,
                productId = product.id,
                type = TransactionType.INCOME,
                amount = sms.amount,
                profit = profit,
                description = "POS Sale (${product.name}) via ${sms.provider}",
                referenceCode = sms.txReference,
                timestamp = System.currentTimeMillis()
            )
            transactionRepository.insertTransaction(transaction)
            accountRepository.updateBalance(account.id, account.currentBalance + sms.amount)
            moMoRepository.markReconciledByTxId(sms.txReference, true)
            onSuccess()
        }
    }

    fun reconcileSettleCustomerDebt(
        sms: FinancialSms,
        customer: Customer,
        account: Account?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            ledgerRepository.recordPayment(
                customerId = customer.id,
                amount = sms.amount,
                description = "Customer payment settlement (${sms.txReference})",
                momoTxId = sms.txReference
            )
            if (account != null) {
                accountRepository.updateBalance(account.id, account.currentBalance + sms.amount)
                val transaction = Transaction(
                    accountId = account.id,
                    type = TransactionType.DEBT_REPAY,
                    amount = sms.amount,
                    description = "Customer debt settlement from ${customer.fullName}",
                    referenceCode = sms.txReference,
                    timestamp = System.currentTimeMillis()
                )
                transactionRepository.insertTransaction(transaction)
            }
            moMoRepository.markReconciledByTxId(sms.txReference, true)
            onSuccess()
        }
    }

    fun reconcileSettleLoan(
        sms: FinancialSms,
        loanDebt: LoanDebt,
        account: Account?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            loanDebtRepository.recordRepayment(loanDebt.id, sms.amount)
            if (account != null) {
                accountRepository.updateBalance(account.id, account.currentBalance + sms.amount)
                val transaction = Transaction(
                    accountId = account.id,
                    type = TransactionType.DEBT_REPAY,
                    amount = sms.amount,
                    description = "Loan debt settlement from ${loanDebt.personOrInstitution}",
                    referenceCode = sms.txReference,
                    timestamp = System.currentTimeMillis()
                )
                transactionRepository.insertTransaction(transaction)
            }
            moMoRepository.markReconciledByTxId(sms.txReference, true)
            onSuccess()
        }
    }

    fun reconcileGeneralRevenue(
        sms: FinancialSms,
        account: Account,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            accountRepository.updateBalance(account.id, account.currentBalance + sms.amount)
            val transaction = Transaction(
                accountId = account.id,
                type = TransactionType.INCOME,
                amount = sms.amount,
                description = "General revenue/deposit via ${sms.provider}",
                referenceCode = sms.txReference,
                timestamp = System.currentTimeMillis()
            )
            transactionRepository.insertTransaction(transaction)
            moMoRepository.markReconciledByTxId(sms.txReference, true)
            onSuccess()
        }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository,
        private val loanDebtRepository: LoanDebtRepository,
        private val ledgerRepository: LedgerRepository,
        private val moMoRepository: MoMoRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SmsReconciliationViewModel::class.java)) {
                return SmsReconciliationViewModel(
                    productRepository,
                    accountRepository,
                    transactionRepository,
                    loanDebtRepository,
                    ledgerRepository,
                    moMoRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
