package com.example.myapplication.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.ProductRepository
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showLowStockOnly = MutableStateFlow(false)
    val showLowStockOnly: StateFlow<Boolean> = _showLowStockOnly.asStateFlow()

    val products: StateFlow<List<Product>> = combine(
        productRepository.getAllProducts(),
        _searchQuery,
        _showLowStockOnly
    ) { productList, query, lowStockOnly ->
        productList.filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    (product.barcode?.contains(query, ignoreCase = true) == true)
            val matchesLowStock = !lowStockOnly || (product.currentStock <= product.minAlertStock)
            matchesQuery && matchesLowStock
        }
    }.stateIn(
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleLowStockFilter(enabled: Boolean) {
        _showLowStockOnly.value = enabled
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.insertProduct(product)
        }
    }

    fun addProduct(
        name: String,
        barcode: String?,
        buyingPrice: Double,
        sellingPrice: Double,
        currentStock: Int,
        minAlertStock: Int = 5
    ) {
        val product = Product(
            name = name,
            barcode = barcode,
            buyingPrice = buyingPrice,
            sellingPrice = sellingPrice,
            currentStock = currentStock,
            minAlertStock = minAlertStock
        )
        addProduct(product)
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productRepository.insertProduct(product)
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
        }
    }

    fun conductPosSale(
        product: Product,
        quantity: Int,
        accountId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (quantity <= 0) {
            onError("Quantity must be greater than zero")
            return
        }
        if (product.currentStock < quantity) {
            onError("Insufficient stock available (${product.currentStock} in stock)")
            return
        }

        viewModelScope.launch {
            val totalSaleAmount = product.sellingPrice * quantity
            val profit = productRepository.deductStockAndCalculateProfit(product.id, quantity)

            val account = accountRepository.getAccountById(accountId)
            if (account != null) {
                accountRepository.updateBalance(account.id, account.currentBalance + totalSaleAmount)
            }

            val transaction = Transaction(
                accountId = accountId,
                productId = product.id,
                type = TransactionType.INCOME,
                amount = totalSaleAmount,
                profit = profit,
                description = "POS Sale: ${product.name} (x$quantity)",
                timestamp = System.currentTimeMillis()
            )
            transactionRepository.insertTransaction(transaction)

            onSuccess()
        }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
                return InventoryViewModel(
                    productRepository,
                    accountRepository,
                    transactionRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
