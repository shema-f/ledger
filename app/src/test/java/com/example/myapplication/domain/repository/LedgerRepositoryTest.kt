package com.example.myapplication.domain.repository

import com.example.myapplication.data.local.dao.CustomerDao
import com.example.myapplication.data.local.dao.LedgerRecordDao
import com.example.myapplication.data.local.entity.CustomerEntity
import com.example.myapplication.data.local.entity.LedgerRecordEntity
import com.example.myapplication.data.repository.LedgerRepositoryImpl
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class LedgerRepositoryTest {

    private lateinit var fakeCustomerDao: FakeCustomerDao
    private lateinit var fakeLedgerRecordDao: FakeLedgerRecordDao
    private lateinit var repository: LedgerRepositoryImpl

    @Before
    fun setUp() {
        fakeLedgerRecordDao = FakeLedgerRecordDao()
        fakeCustomerDao = FakeCustomerDao(fakeLedgerRecordDao)
        repository = LedgerRepositoryImpl(fakeCustomerDao, fakeLedgerRecordDao)
    }

    @Test
    fun addCustomer_initialTotalDebtIsZero() = runTest {
        val customer = Customer(
            fullName = "Kamanzi Jean",
            phoneNumber = "+250788123456",
            nickname = "Jeannot"
        )

        val id = repository.addCustomer(customer)
        val fetched = repository.getCustomerById(id)

        assertNotNull(fetched)
        assertEquals("Kamanzi Jean", fetched?.fullName)
        assertEquals(0.0, fetched?.totalDebt ?: -1.0, 0.001)
    }

    @Test
    fun addDebt_accumulatesDebtCorrectly() = runTest {
        val customerId = repository.addCustomer(
            Customer(fullName = "Mugisha Eric", phoneNumber = "+250788654321")
        )

        repository.addDebt(
            customerId = customerId,
            amount = 5000.0,
            description = "Rice and Sugar"
        )

        val updatedCustomer = repository.getCustomerById(customerId)
        assertNotNull(updatedCustomer)
        assertEquals(5000.0, updatedCustomer?.totalDebt ?: 0.0, 0.001)
    }

    @Test
    fun recordPayment_deductsDebtCorrectly() = runTest {
        val customerId = repository.addCustomer(
            Customer(fullName = "Uwase Marie", phoneNumber = "+250788999888")
        )

        repository.addDebt(
            customerId = customerId,
            amount = 10000.0,
            description = "Groceries credit"
        )

        repository.recordPayment(
            customerId = customerId,
            amount = 4000.0,
            description = "Partial payment via MoMo",
            momoTxId = "TX123456"
        )

        val updatedCustomer = repository.getCustomerById(customerId)
        assertNotNull(updatedCustomer)
        assertEquals(6000.0, updatedCustomer?.totalDebt ?: 0.0, 0.001)
    }

    @Test
    fun multipleDebtsAndPayments_calculatesExactBalance() = runTest {
        val customerId = repository.addCustomer(
            Customer(fullName = "Nshimiyimana Paul", phoneNumber = "+250788111222")
        )

        // +5000 debt
        repository.addDebt(customerId, 5000.0, "Items 1")
        // +3000 debt
        repository.addDebt(customerId, 3000.0, "Items 2")
        // -2000 payment
        repository.recordPayment(customerId, 2000.0, "Payment 1")
        // -1500 payment
        repository.recordPayment(customerId, 1500.0, "Payment 2")

        val customer = repository.getCustomerById(customerId)
        assertNotNull(customer)
        assertEquals(4500.0, customer?.totalDebt ?: 0.0, 0.001)
    }

    @Test
    fun recordCashSale_doesNotAffectCustomerDebt() = runTest {
        val customerId = repository.addCustomer(
            Customer(fullName = "Bizimana John", phoneNumber = "+250788000111")
        )

        repository.addDebt(customerId, 2000.0, "Credit items")
        repository.recordCashSale(15000.0, "Instant cash purchase")

        val customer = repository.getCustomerById(customerId)
        assertNotNull(customer)
        assertEquals(2000.0, customer?.totalDebt ?: 0.0, 0.001)
    }

    @Test
    fun deleteLedgerRecord_adjustsDebtAccordingly() = runTest {
        val customerId = repository.addCustomer(
            Customer(fullName = "Keza Alice", phoneNumber = "+250788333444")
        )

        val debtRecordId = repository.addDebt(customerId, 8000.0, "Flour")
        val records = repository.getRecordsForCustomer(customerId).first()
        val debtRecord = records.find { it.id == debtRecordId }

        assertNotNull(debtRecord)
        if (debtRecord != null) {
            repository.deleteLedgerRecord(debtRecord)
        }

        val customer = repository.getCustomerById(customerId)
        assertNotNull(customer)
        assertEquals(0.0, customer?.totalDebt ?: -1.0, 0.001)
    }

    // --- Fake DAOs for Unit Testing ---

    private class FakeCustomerDao(
        private val fakeLedgerRecordDao: FakeLedgerRecordDao
    ) : CustomerDao {
        val customers = mutableMapOf<Long, CustomerEntity>()
        private var nextId = 1L

        val customersFlow = MutableStateFlow<List<CustomerEntity>>(emptyList())

        private fun updateFlow() {
            customersFlow.value = customers.values.toList()
        }

        override fun getCustomersSortedByDebt(): Flow<List<CustomerEntity>> {
            return customersFlow.map { list -> list.sortedByDescending { it.totalDebt } }
        }

        override fun getCustomersSortedByName(): Flow<List<CustomerEntity>> {
            return customersFlow.map { list -> list.sortedBy { it.fullName } }
        }

        override fun searchCustomers(query: String): Flow<List<CustomerEntity>> {
            return customersFlow.map { list ->
                list.filter {
                    it.fullName.contains(query, ignoreCase = true) ||
                            (it.nickname?.contains(query, ignoreCase = true) == true) ||
                            it.phoneNumber.contains(query, ignoreCase = true)
                }
            }
        }

        override suspend fun getCustomerById(id: Long): CustomerEntity? = customers[id]

        override fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?> {
            return customersFlow.map { list -> list.find { it.id == id } }
        }

        override suspend fun insertCustomer(customer: CustomerEntity): Long {
            val id = if (customer.id == 0L) nextId++ else customer.id
            val entity = customer.copy(id = id)
            customers[id] = entity
            updateFlow()
            return id
        }

        override suspend fun updateCustomer(customer: CustomerEntity) {
            customers[customer.id] = customer
            updateFlow()
        }

        override suspend fun deleteCustomer(customer: CustomerEntity) {
            customers.remove(customer.id)
            updateFlow()
        }

        override suspend fun updateTotalDebt(customerId: Long, totalDebt: Double) {
            customers[customerId]?.let {
                customers[customerId] = it.copy(totalDebt = totalDebt)
                updateFlow()
            }
        }

        override suspend fun recalculateCustomerTotalDebt(customerId: Long) {
            val custRecords = fakeLedgerRecordDao.records.values.filter { it.customerId == customerId }
            val calculatedDebt = custRecords.sumOf {
                when (it.type) {
                    TransactionType.CREDIT -> it.amount
                    TransactionType.PAYMENT -> -it.amount
                    TransactionType.CASH_SALE -> 0.0
                }
            }
            updateTotalDebt(customerId, calculatedDebt)
        }

        override fun getActiveDebtorsCount(): Flow<Int> {
            return customersFlow.map { list -> list.count { it.totalDebt > 0 } }
        }
    }

    private class FakeLedgerRecordDao : LedgerRecordDao {
        val records = mutableMapOf<Long, LedgerRecordEntity>()
        private var nextId = 1L

        val recordsFlow = MutableStateFlow<List<LedgerRecordEntity>>(emptyList())

        private fun updateFlow() {
            recordsFlow.value = records.values.toList()
        }

        override fun getRecordsForCustomer(customerId: Long): Flow<List<LedgerRecordEntity>> {
            return recordsFlow.map { list ->
                list.filter { it.customerId == customerId }.sortedByDescending { it.timestamp }
            }
        }

        override fun getRecentRecords(limit: Int): Flow<List<LedgerRecordEntity>> {
            return recordsFlow.map { list ->
                list.sortedByDescending { it.timestamp }.take(limit)
            }
        }

        override fun getAllRecords(): Flow<List<LedgerRecordEntity>> {
            return recordsFlow.map { list -> list.sortedByDescending { it.timestamp } }
        }

        override suspend fun getRecordById(id: Long): LedgerRecordEntity? = records[id]

        override fun getTodaySalesSum(startOfDayTimestamp: Long): Flow<Double> {
            return recordsFlow.map { list ->
                list.filter {
                    it.timestamp >= startOfDayTimestamp &&
                            (it.type == TransactionType.CASH_SALE || it.type == TransactionType.CREDIT)
                }.sumOf { it.amount }
            }
        }

        override fun getTotalOutstandingDebt(): Flow<Double> {
            return recordsFlow.map { 0.0 }
        }

        override suspend fun insertRecord(record: LedgerRecordEntity): Long {
            val id = if (record.id == 0L) nextId++ else record.id
            val entity = record.copy(id = id)
            records[id] = entity
            updateFlow()
            return id
        }

        override suspend fun deleteRecord(record: LedgerRecordEntity) {
            records.remove(record.id)
            updateFlow()
        }
    }
}
