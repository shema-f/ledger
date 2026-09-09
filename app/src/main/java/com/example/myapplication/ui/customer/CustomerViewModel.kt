package com.example.myapplication.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.LedgerRecord
import com.example.myapplication.domain.repository.LedgerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class CustomerSortOrder {
    DEBT_DESC,
    ALPHABETICAL,
    RECENT
}

enum class ReminderLanguage {
    KINYARWANDA,
    ENGLISH,
    FRENCH
}

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModel(
    private val ledgerRepository: LedgerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(CustomerSortOrder.DEBT_DESC)
    val sortOrder: StateFlow<CustomerSortOrder> = _sortOrder.asStateFlow()

    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    val customers: StateFlow<List<Customer>> = combine(
        _searchQuery,
        _sortOrder
    ) { query, sort ->
        query to sort
    }.flatMapLatest { (query, sort) ->
        val flow = if (query.isBlank()) {
            when (sort) {
                CustomerSortOrder.DEBT_DESC -> ledgerRepository.getCustomersSortedByDebt()
                CustomerSortOrder.ALPHABETICAL -> ledgerRepository.getCustomersSortedByName()
                CustomerSortOrder.RECENT -> ledgerRepository.getCustomersSortedByDebt()
            }
        } else {
            ledgerRepository.searchCustomers(query)
        }
        flow
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedCustomer: StateFlow<Customer?> = combine(
        _selectedCustomerId,
        customers
    ) { id, list ->
        if (id == null) null else list.find { it.id == id }
    }.flatMapLatest { customer ->
        if (customer != null) {
            flowOf(customer)
        } else if (_selectedCustomerId.value != null) {
            val id = _selectedCustomerId.value!!
            flowOf(ledgerRepository.getCustomerById(id))
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val selectedCustomerRecords: StateFlow<List<LedgerRecord>> = _selectedCustomerId
        .flatMapLatest { id ->
            if (id != null) {
                ledgerRepository.getRecordsForCustomer(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: CustomerSortOrder) {
        _sortOrder.value = order
    }

    fun selectCustomer(customerId: Long?) {
        _selectedCustomerId.value = customerId
    }

    fun addCustomer(fullName: String, phoneNumber: String, nickname: String? = null) {
        viewModelScope.launch {
            val customer = Customer(
                fullName = fullName,
                phoneNumber = phoneNumber,
                nickname = nickname
            )
            ledgerRepository.addCustomer(customer)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            ledgerRepository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            ledgerRepository.deleteCustomer(customer)
            if (_selectedCustomerId.value == customer.id) {
                _selectedCustomerId.value = null
            }
        }
    }

    fun addDebt(customerId: Long, amount: Double, description: String, dueDate: Long? = null) {
        viewModelScope.launch {
            ledgerRepository.addDebt(customerId, amount, description, dueDate)
        }
    }

    fun recordPayment(customerId: Long, amount: Double, description: String) {
        viewModelScope.launch {
            ledgerRepository.recordPayment(customerId, amount, description)
        }
    }

    fun generateDebtReminder(customer: Customer, language: ReminderLanguage): String {
        val amountFormatted = String.format(Locale.US, "%,.0f", customer.totalDebt)
        return when (language) {
            ReminderLanguage.KINYARWANDA ->
                "Mwaramutse ${customer.fullName}, mufe $amountFormatted RWF kuri Kayi y'Ideni. Murakoze!"
            ReminderLanguage.ENGLISH ->
                "Hello ${customer.fullName}, this is a reminder regarding your outstanding debt of $amountFormatted RWF on Kayi y'Ideni. Thank you!"
            ReminderLanguage.FRENCH ->
                "Bonjour ${customer.fullName}, ceci est un rappel concernant votre dette de $amountFormatted RWF sur Kayi y'Ideni. Merci!"
        }
    }

    class Factory(private val ledgerRepository: LedgerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
                return CustomerViewModel(ledgerRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
