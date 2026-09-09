package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.CustomerDao
import com.example.myapplication.data.local.dao.LedgerRecordDao
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.DashboardSummary
import com.example.myapplication.domain.model.LedgerRecord
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class LedgerRepositoryImpl(
    private val customerDao: CustomerDao,
    private val ledgerRecordDao: LedgerRecordDao
) : LedgerRepository {

    override fun getCustomersSortedByDebt(): Flow<List<Customer>> {
        return customerDao.getCustomersSortedByDebt().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getCustomersSortedByName(): Flow<List<Customer>> {
        return customerDao.getCustomersSortedByName().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.searchCustomers(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getCustomerById(id: Long): Customer? {
        return customerDao.getCustomerById(id)?.toDomain()
    }

    override suspend fun addCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer.toEntity())
    }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer.toEntity())
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer.toEntity())
    }

    override fun getRecordsForCustomer(customerId: Long): Flow<List<LedgerRecord>> {
        return ledgerRecordDao.getRecordsForCustomer(customerId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getRecentRecords(limit: Int): Flow<List<LedgerRecord>> {
        return ledgerRecordDao.getRecentRecords(limit).map { list ->
            list.map { it.toDomain() }
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
            customerId = customerId,
            type = TransactionType.CREDIT,
            amount = amount,
            description = description,
            dueDate = dueDate,
            momoTxId = momoTxId,
            timestamp = System.currentTimeMillis()
        )
        return addLedgerRecord(record)
    }

    override suspend fun recordPayment(
        customerId: Long,
        amount: Double,
        description: String,
        momoTxId: String?
    ): Long {
        val record = LedgerRecord(
            customerId = customerId,
            type = TransactionType.PAYMENT,
            amount = amount,
            description = description,
            momoTxId = momoTxId,
            timestamp = System.currentTimeMillis()
        )
        return addLedgerRecord(record)
    }

    override suspend fun recordCashSale(
        amount: Double,
        description: String
    ): Long {
        val record = LedgerRecord(
            customerId = null,
            type = TransactionType.CASH_SALE,
            amount = amount,
            description = description,
            timestamp = System.currentTimeMillis()
        )
        return addLedgerRecord(record)
    }

    override suspend fun addLedgerRecord(record: LedgerRecord): Long {
        val insertedId = ledgerRecordDao.insertRecord(record.toEntity())
        if (record.customerId != null) {
            val customer = customerDao.getCustomerById(record.customerId)
            if (customer != null) {
                val updatedDebt = when (record.type) {
                    TransactionType.CREDIT -> customer.totalDebt + record.amount
                    TransactionType.PAYMENT -> customer.totalDebt - record.amount
                    TransactionType.CASH_SALE -> customer.totalDebt
                }
                customerDao.updateTotalDebt(record.customerId, updatedDebt)
            }
            customerDao.recalculateCustomerTotalDebt(record.customerId)
        }
        return insertedId
    }

    override suspend fun deleteLedgerRecord(record: LedgerRecord) {
        ledgerRecordDao.deleteRecord(record.toEntity())
        if (record.customerId != null) {
            val customer = customerDao.getCustomerById(record.customerId)
            if (customer != null) {
                val updatedDebt = when (record.type) {
                    TransactionType.CREDIT -> customer.totalDebt - record.amount
                    TransactionType.PAYMENT -> customer.totalDebt + record.amount
                    TransactionType.CASH_SALE -> customer.totalDebt
                }
                customerDao.updateTotalDebt(record.customerId, updatedDebt)
            }
            customerDao.recalculateCustomerTotalDebt(record.customerId)
        }
    }

    override fun getTotalOutstandingDebt(): Flow<Double> {
        return ledgerRecordDao.getTotalOutstandingDebt()
    }

    override fun getTodaySales(): Flow<Double> {
        return ledgerRecordDao.getTodaySalesSum(getStartOfToday())
    }

    override fun getActiveDebtorsCount(): Flow<Int> {
        return customerDao.getActiveDebtorsCount()
    }

    override fun getDashboardSummary(): Flow<DashboardSummary> {
        return combine(
            getTotalOutstandingDebt(),
            getTodaySales(),
            getActiveDebtorsCount()
        ) { totalDebt, todaySales, activeDebtors ->
            DashboardSummary(
                totalOutstandingDebt = totalDebt,
                todaySales = todaySales,
                activeDebtorsCount = activeDebtors
            )
        }
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
