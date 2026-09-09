package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.DashboardSummary
import com.example.myapplication.domain.model.LedgerRecord
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun getCustomersSortedByDebt(): Flow<List<Customer>>
    fun getCustomersSortedByName(): Flow<List<Customer>>
    fun searchCustomers(query: String): Flow<List<Customer>>
    suspend fun getCustomerById(id: Long): Customer?
    suspend fun addCustomer(customer: Customer): Long
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)

    fun getRecordsForCustomer(customerId: Long): Flow<List<LedgerRecord>>
    fun getRecentRecords(limit: Int = 20): Flow<List<LedgerRecord>>
    
    suspend fun addDebt(
        customerId: Long,
        amount: Double,
        description: String,
        dueDate: Long? = null,
        momoTxId: String? = null
    ): Long

    suspend fun recordPayment(
        customerId: Long,
        amount: Double,
        description: String,
        momoTxId: String? = null
    ): Long

    suspend fun recordCashSale(
        amount: Double,
        description: String
    ): Long

    suspend fun addLedgerRecord(record: LedgerRecord): Long
    suspend fun deleteLedgerRecord(record: LedgerRecord)

    fun getTotalOutstandingDebt(): Flow<Double>
    fun getTodaySales(): Flow<Double>
    fun getActiveDebtorsCount(): Flow<Int>
    fun getDashboardSummary(): Flow<DashboardSummary>
}
