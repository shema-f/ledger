package com.example.myapplication.ui.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExpenseCategory(
    val displayName: String,
    val kinyarwandaName: String
) {
    FOOD_AND_LIVING("Food & Living", "Ibyo kurya na mbere"),
    TRANSPORT_AND_FUEL("Transport & Fuel", "Ingendo na Esanse"),
    INVENTORY_STOCK("Inventory Stock", "Marandise/Stoke"),
    RENT_AND_UTILITIES("Rent & Utilities", "Igihe na Mazi/Inzu"),
    AIRTIME_AND_DATA("Airtime & Data", "Minite na Interineti"),
    OTHER("Other", "Ibindi")
}

data class ExpenseCategoryItem(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Float
)

data class AccountWalletItem(
    val id: Long,
    val name: String,
    val accountType: AccountType,
    val balance: Double,
    val currency: String = "RWF"
)

data class MoneyFlowUiState(
    val moneyIn: Double = 2450000.0,
    val saved: Double = 520000.0,
    val spent: Double = 1720000.0,
    val categoryExpenses: List<ExpenseCategoryItem> = emptyList(),
    val accountWallets: List<AccountWalletItem> = emptyList(),
    val isLoading: Boolean = false
) {
    val retained: Double
        get() = (moneyIn - spent).coerceAtLeast(0.0)
}

class MoneyFlowViewModel(
    private val transactionRepository: TransactionRepository? = null,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MoneyFlowUiState(
            categoryExpenses = getDefaultCategories(1720000.0),
            accountWallets = getDefaultWallets()
        )
    )
    val uiState: StateFlow<MoneyFlowUiState> = _uiState.asStateFlow()

    init {
        loadMoneyFlowData()
    }

    private fun loadMoneyFlowData() {
        if (transactionRepository == null && accountRepository == null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val transactionsFlow = transactionRepository?.getAllTransactions()
            val accountsFlow = accountRepository?.getAllAccounts()

            if (transactionsFlow != null && accountsFlow != null) {
                combine(transactionsFlow, accountsFlow) { transactions, accounts ->
                    calculateFlowFromData(transactions, accounts)
                }.collect { updatedState ->
                    _uiState.value = updatedState
                }
            } else if (transactionsFlow != null) {
                transactionsFlow.collect { transactions ->
                    val updatedState = calculateFlowFromTransactions(transactions)
                    _uiState.value = updatedState
                }
            } else if (accountsFlow != null) {
                accountsFlow.collect { accounts ->
                    val updatedWallets = accountsToWallets(accounts)
                    _uiState.update { currentState ->
                        currentState.copy(
                            accountWallets = if (updatedWallets.isNotEmpty()) updatedWallets else getDefaultWallets(),
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateFlowFromData(
        transactions: List<Transaction>,
        accounts: List<Account>
    ): MoneyFlowUiState {
        val baseState = calculateFlowFromTransactions(transactions)
        val wallets = accountsToWallets(accounts)
        return baseState.copy(
            accountWallets = if (wallets.isNotEmpty()) wallets else getDefaultWallets(),
            isLoading = false
        )
    }

    private fun calculateFlowFromTransactions(transactions: List<Transaction>): MoneyFlowUiState {
        if (transactions.isEmpty()) {
            return MoneyFlowUiState(
                moneyIn = 2450000.0,
                saved = 520000.0,
                spent = 1720000.0,
                categoryExpenses = getDefaultCategories(1720000.0),
                accountWallets = _uiState.value.accountWallets,
                isLoading = false
            )
        }

        var moneyInSum = 0.0
        var spentSum = 0.0
        val categoryTotals = mutableMapOf<ExpenseCategory, Double>().apply {
            ExpenseCategory.entries.forEach { put(it, 0.0) }
        }

        transactions.forEach { tx ->
            when (tx.type) {
                TransactionType.INCOME, TransactionType.CASH_SALE, TransactionType.CREDIT -> {
                    moneyInSum += tx.amount
                }
                TransactionType.EXPENSE, TransactionType.PAYMENT, TransactionType.DEBT_REPAY -> {
                    spentSum += tx.amount
                    val category = mapCategoryFromDescription(tx.description)
                    categoryTotals[category] = (categoryTotals[category] ?: 0.0) + tx.amount
                }
                TransactionType.TRANSFER, TransactionType.DEBT_BORROW -> {
                    // Internal transfer / debt
                }
            }
        }

        val totalSpent = if (spentSum > 0) spentSum else 1.0
        val categoryItems = categoryTotals.map { (cat, amount) ->
            ExpenseCategoryItem(
                category = cat,
                amount = amount,
                percentage = ((amount / totalSpent) * 100f).toFloat()
            )
        }.sortedByDescending { it.amount }

        return MoneyFlowUiState(
            moneyIn = if (moneyInSum > 0) moneyInSum else 2450000.0,
            saved = _uiState.value.saved,
            spent = if (spentSum > 0) spentSum else 1720000.0,
            categoryExpenses = if (spentSum > 0) categoryItems else getDefaultCategories(1720000.0),
            accountWallets = _uiState.value.accountWallets,
            isLoading = false
        )
    }

    private fun accountsToWallets(accounts: List<Account>): List<AccountWalletItem> {
        val requiredNames = listOf(
            "MTN MoMo",
            "Airtel Money",
            "Bank of Kigali (BK)",
            "Main Cash Register",
            "Urwego SACCO"
        )
        val walletList = accounts.map { account ->
            AccountWalletItem(
                id = account.id,
                name = account.name,
                accountType = account.accountType,
                balance = account.currentBalance,
                currency = account.currency
            )
        }.toMutableList()

        if (walletList.none { it.name.contains("MTN", ignoreCase = true) }) {
            walletList.add(AccountWalletItem(101, "MTN MoMo", AccountType.MOBILE_MONEY, 890000.0))
        }
        if (walletList.none { it.name.contains("Airtel", ignoreCase = true) }) {
            walletList.add(AccountWalletItem(102, "Airtel Money", AccountType.MOBILE_MONEY, 210000.0))
        }
        if (walletList.none { it.name.contains("Bank", ignoreCase = true) || it.name.contains("BK", ignoreCase = true) }) {
            walletList.add(AccountWalletItem(103, "Bank of Kigali (BK)", AccountType.BANK, 1150000.0))
        }
        if (walletList.none { it.name.contains("Cash", ignoreCase = true) }) {
            walletList.add(AccountWalletItem(104, "Cash Register", AccountType.CASH, 120000.0))
        }
        if (walletList.none { it.name.contains("SACCO", ignoreCase = true) }) {
            walletList.add(AccountWalletItem(105, "SACCO", AccountType.SACCO, 80000.0))
        }

        return walletList
    }

    fun mapCategoryFromDescription(description: String): ExpenseCategory {
        val descLower = description.lowercase()
        return when {
            descLower.contains("food") || descLower.contains("eats") || descLower.contains("kurya") || descLower.contains("lunch") -> ExpenseCategory.FOOD_AND_LIVING
            descLower.contains("fuel") || descLower.contains("transport") || descLower.contains("taxi") || descLower.contains("moto") || descLower.contains("esanse") -> ExpenseCategory.TRANSPORT_AND_FUEL
            descLower.contains("stock") || descLower.contains("inventory") || descLower.contains("goods") || descLower.contains("marandise") -> ExpenseCategory.INVENTORY_STOCK
            descLower.contains("rent") || descLower.contains("water") || descLower.contains("electricity") || descLower.contains("inzu") || descLower.contains("amazi") || descLower.contains("umuriro") -> ExpenseCategory.RENT_AND_UTILITIES
            descLower.contains("airtime") || descLower.contains("data") || descLower.contains("internet") || descLower.contains("minite") -> ExpenseCategory.AIRTIME_AND_DATA
            else -> ExpenseCategory.OTHER
        }
    }

    fun updateFlowValues(moneyIn: Double, spent: Double, saved: Double) {
        val totalSpent = if (spent > 0) spent else 1.0
        val categories = getDefaultCategories(totalSpent)

        _uiState.update {
            it.copy(
                moneyIn = moneyIn,
                spent = spent,
                saved = saved,
                categoryExpenses = categories
            )
        }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        description: String,
        accountId: Long = 1L
    ) {
        viewModelScope.launch {
            if (transactionRepository != null) {
                val transaction = Transaction(
                    accountId = accountId,
                    type = type,
                    amount = amount,
                    description = description,
                    timestamp = System.currentTimeMillis()
                )
                transactionRepository.insertTransaction(transaction)
            } else {
                when (type) {
                    TransactionType.INCOME, TransactionType.CASH_SALE -> {
                        _uiState.update { it.copy(moneyIn = it.moneyIn + amount) }
                    }
                    TransactionType.EXPENSE -> {
                        _uiState.update { it.copy(spent = it.spent + amount) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun setSavings(amount: Double) {
        _uiState.update { it.copy(saved = amount) }
    }

    private fun getDefaultCategories(totalSpent: Double): List<ExpenseCategoryItem> {
        val inventoryAmount = totalSpent * 0.494
        val rentAmount = totalSpent * 0.203
        val foodAmount = totalSpent * 0.128
        val transportAmount = totalSpent * 0.087
        val airtimeAmount = totalSpent * 0.052
        val otherAmount = totalSpent * 0.036

        return listOf(
            ExpenseCategoryItem(ExpenseCategory.INVENTORY_STOCK, inventoryAmount, 49.4f),
            ExpenseCategoryItem(ExpenseCategory.RENT_AND_UTILITIES, rentAmount, 20.3f),
            ExpenseCategoryItem(ExpenseCategory.FOOD_AND_LIVING, foodAmount, 12.8f),
            ExpenseCategoryItem(ExpenseCategory.TRANSPORT_AND_FUEL, transportAmount, 8.7f),
            ExpenseCategoryItem(ExpenseCategory.AIRTIME_AND_DATA, airtimeAmount, 5.2f),
            ExpenseCategoryItem(ExpenseCategory.OTHER, otherAmount, 3.6f)
        )
    }

    private fun getDefaultWallets(): List<AccountWalletItem> {
        return listOf(
            AccountWalletItem(1, "MTN MoMo", AccountType.MOBILE_MONEY, 890000.0),
            AccountWalletItem(2, "Airtel Money", AccountType.MOBILE_MONEY, 210000.0),
            AccountWalletItem(3, "Bank of Kigali (BK)", AccountType.BANK, 1150000.0),
            AccountWalletItem(4, "Cash Register", AccountType.CASH, 120000.0),
            AccountWalletItem(5, "SACCO", AccountType.SACCO, 80000.0)
        )
    }

    class Factory(
        private val transactionRepository: TransactionRepository?,
        private val accountRepository: AccountRepository?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MoneyFlowViewModel(transactionRepository, accountRepository) as T
        }
    }
}
