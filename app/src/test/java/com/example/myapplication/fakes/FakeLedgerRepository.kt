package com.example.myapplication.fakes

import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.DashboardSummary
import com.example.myapplication.domain.model.LedgerRecord
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeLedgerRepository : LedgerRepository {

    private val customersFlow = MutableStateFlow<List<Customer>>(emptyList())
    private val recordsFlow = MutableStateFlow<List<LedgerRecord>>(emptyList())
    private var nextCustomerId = 1L
    private var nextRecordId = 1L

    override fun getCustomersSortedByDebt(): Flow<List<Customer>> {
        return customersFlow.map { list -> list.sortedByDescending { it.totalDebt } }
    }

    override fun getCustomersSortedByName(): Flow<List<Customer>> {
        return customersFlow.map { list -> list.sortedBy { it.fullName } }
    }

    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customersFlow.map { list ->
            list.filter {
                it.fullName.contains(query, ignoreCase = true) ||
                        (it.nickname?.contains(query, ignoreCase = true) == true) ||
                        it.phoneNumber.contains(query)
            }
        }
    }

    override suspend fun getCustomerById(id: Long): Customer? {
        return customersFlow.value.find { it.id == id }
    }

    override suspend fun addCustomer(customer: Customer): Long {
        val id = if (customer.id == 0L) nextCustomerId++ else customer.id
        val newCustomer = customer.copy(id = id)
        customersFlow.value = customersFlow.value + newCustomer
        return id
    }

    override suspend fun updateCustomer(customer: Customer) {
        customersFlow.value = customersFlow.value.map {
            if (it.id == customer.id) customer else it
        }
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customersFlow.value = customersFlow.value.filter { it.id != customer.id }
        recordsFlow.value = recordsFlow.value.filter { it.customerId != customer.id }
    }

    override fun getRecordsForCustomer(customerId: Long): Flow<List<LedgerRecord>> {
        return recordsFlow.map { list ->
            list.filter { it.customerId == customerId }.sortedByDescending { it.timestamp }
        }
    }

    override fun getRecentRecords(limit: Int): Flow<List<LedgerRecord>> {
        return recordsFlow.map { list ->
            list.sortedByDescending { it.timestamp }.take(limit)
        }
    }

    override suspend fun addDebt(
        customerId: Long,
        amount: Double,
        description: String,
        dueDate: Long?,
        momoTxId: String?
    ): Long {
        val record = LedgerRecord(
            id = nextRecordId++,
            customerId = customerId,
            type = TransactionType.CREDIT,
            amount = amount,
            description = description,
            dueDate = dueDate,
            momoTxId = momoTxId,
            timestamp = System.currentTimeMillis()
        )
        recordsFlow.value = recordsFlow.value + record

        // Update customer total debt
        val cust = getCustomerById(customerId)
        if (cust != null) {
            updateCustomer(cust.copy(totalDebt = cust.totalDebt + amount))
        }

        return record.id
    }

    override suspend fun recordPayment(
        customerId: Long,
        amount: Double,
        description: String,
        momoTxId: String?
    ): Long {
        val record = LedgerRecord(
            id = nextRecordId++,
            customerId = customerId,
            type = TransactionType.PAYMENT,
            amount = amount,
            description = description,
            momoTxId = momoTxId,
            timestamp = System.currentTimeMillis()
        )
        recordsFlow.value = recordsFlow.value + record

        // Update customer total debt
        val cust = getCustomerById(customerId)
        if (cust != null) {
            updateCustomer(cust.copy(totalDebt = (cust.totalDebt - amount).coerceAtLeast(0.0)))
        }

        return record.id
    }

    override suspend fun recordCashSale(
        amount: Double,
        description: String
    ): Long {
        val record = LedgerRecord(
            id = nextRecordId++,
            customerId = null,
            type = TransactionType.CASH_SALE,
            amount = amount,
            description = description,
            timestamp = System.currentTimeMillis()
        )
        recordsFlow.value = recordsFlow.value + record
        return record.id
    }

    override suspend fun addLedgerRecord(record: LedgerRecord): Long {
        val id = if (record.id == 0L) nextRecordId++ else record.id
        val newRecord = record.copy(id = id)
        recordsFlow.value = recordsFlow.value + newRecord
        return id
    }

    override suspend fun deleteLedgerRecord(record: LedgerRecord) {
        recordsFlow.value = recordsFlow.value.filter { it.id != record.id }
    }

    override fun getTotalOutstandingDebt(): Flow<Double> {
        return customersFlow.map { list ->
            list.filter { it.totalDebt > 0 }.sumOf { it.totalDebt }
        }
    }

    override fun getTodaySales(): Flow<Double> {
        return recordsFlow.map { list ->
            list.filter { it.type == TransactionType.CASH_SALE || it.type == TransactionType.CREDIT }
                .sumOf { it.amount }
        }
    }

    override fun getActiveDebtorsCount(): Flow<Int> {
        return customersFlow.map { list ->
            list.count { it.totalDebt > 0 }
        }
    }

    override fun getDashboardSummary(): Flow<DashboardSummary> {
        return combine(
            getTotalOutstandingDebt(),
            getTodaySales(),
            getActiveDebtorsCount()
        ) { totalDebt, todaySales, debtorsCount ->
            DashboardSummary(
                totalOutstandingDebt = totalDebt,
                todaySales = todaySales,
                activeDebtorsCount = debtorsCount
            )
        }
    }
}
