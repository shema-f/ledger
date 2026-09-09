package com.example.myapplication.domain.reconciliation

import android.content.Context
import android.content.ContextWrapper
import com.example.myapplication.data.local.dao.CustomerDao
import com.example.myapplication.data.local.dao.LedgerRecordDao
import com.example.myapplication.data.local.entity.CustomerEntity
import com.example.myapplication.data.local.entity.LedgerRecordEntity
import com.example.myapplication.data.repository.LedgerRepositoryImpl
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.MoMoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AutoReconciliationEngineTest {

    private lateinit var fakeCustomerDao: FakeCustomerDao
    private lateinit var fakeLedgerRecordDao: FakeLedgerRecordDao
    private lateinit var ledgerRepository: LedgerRepositoryImpl
    private lateinit var fakeMoMoRepository: FakeMoMoRepository
    private lateinit var engine: AutoReconciliationEngine

    @Before
    fun setUp() {
        val context = FakeContext()
        fakeLedgerRecordDao = FakeLedgerRecordDao()
        fakeCustomerDao = FakeCustomerDao(fakeLedgerRecordDao)
        ledgerRepository = LedgerRepositoryImpl(fakeCustomerDao, fakeLedgerRecordDao)
        fakeMoMoRepository = FakeMoMoRepository()

        engine = AutoReconciliationEngine(
            context = context,
            moMoRepository = fakeMoMoRepository,
            ledgerRepository = ledgerRepository
        )
    }

    @Test
    fun testPhoneMatching_reconcilesPaymentCorrectly() = runTest {
        val customerId = ledgerRepository.addCustomer(
            Customer(fullName = "Gakwaya Jean", phoneNumber = "0788123456")
        )
        ledgerRepository.addDebt(customerId, 10000.0, "Initial debt")

        val momoLog = MoMoLog(
            senderName = "GAKWAYA JEAN",
            senderPhone = "250788123456",
            amount = 5000.0,
            txId = "TX1001",
            rawText = "Yahawe 5,000 RWF..."
        )
        fakeMoMoRepository.addMoMoLog(momoLog)

        val result = engine.reconcile(momoLog)

        assertTrue(result)
        val updatedCustomer = ledgerRepository.getCustomerById(customerId)
        assertNotNull(updatedCustomer)
        assertEquals(5000.0, updatedCustomer?.totalDebt ?: 0.0, 0.001)

        val records = ledgerRepository.getRecordsForCustomer(customerId).first()
        val paymentRecord = records.find { it.momoTxId == "TX1001" }
        assertNotNull(paymentRecord)
        assertEquals(TransactionType.PAYMENT, paymentRecord?.type)
        assertEquals(5000.0, paymentRecord?.amount ?: 0.0, 0.001)

        val log = fakeMoMoRepository.getMoMoLogByTxId("TX1001")
        assertTrue(log?.isReconciled == true)
    }

    @Test
    fun testNameMatching_reconcilesWhenPhoneNotMatching() = runTest {
        val customerId = ledgerRepository.addCustomer(
            Customer(fullName = "Jean Baptiste Rukamba", phoneNumber = "0788999999", nickname = "J B")
        )
        ledgerRepository.addDebt(customerId, 15000.0, "Credit purchase")

        val momoLog = MoMoLog(
            senderName = "Jean Baptiste",
            senderPhone = null,
            amount = 15000.0,
            txId = "TX1002",
            rawText = "Received 15000 RWF..."
        )
        fakeMoMoRepository.addMoMoLog(momoLog)

        val result = engine.reconcile(momoLog)

        assertTrue(result)
        val updatedCustomer = ledgerRepository.getCustomerById(customerId)
        assertEquals(0.0, updatedCustomer?.totalDebt ?: -1.0, 0.001)

        val log = fakeMoMoRepository.getMoMoLogByTxId("TX1002")
        assertTrue(log?.isReconciled == true)
    }

    @Test
    fun testExactSettlement() = runTest {
        val customerId = ledgerRepository.addCustomer(
            Customer(fullName = "Mukamanzi Marie", phoneNumber = "0788777666")
        )
        ledgerRepository.addDebt(customerId, 8000.0, "Goods credit")

        val momoLog = MoMoLog(
            senderName = "MUKAMANZI MARIE",
            senderPhone = "250788777666",
            amount = 8000.0,
            txId = "TX1003",
            rawText = "Received 8000 RWF..."
        )
        fakeMoMoRepository.addMoMoLog(momoLog)

        val result = engine.reconcile(momoLog)

        assertTrue(result)
        val updatedCustomer = ledgerRepository.getCustomerById(customerId)
        assertEquals(0.0, updatedCustomer?.totalDebt ?: -1.0, 0.001)
    }

    @Test
    fun testOverpayment_capsPaymentAtCustomerDebt() = runTest {
        val customerId = ledgerRepository.addCustomer(
            Customer(fullName = "Bizimana Eric", phoneNumber = "0788333222")
        )
        ledgerRepository.addDebt(customerId, 6000.0, "Small debt")

        val momoLog = MoMoLog(
            senderName = "Bizimana Eric",
            senderPhone = "250788333222",
            amount = 10000.0,
            txId = "TX1004",
            rawText = "Received 10000 RWF..."
        )
        fakeMoMoRepository.addMoMoLog(momoLog)

        val result = engine.reconcile(momoLog)

        assertTrue(result)
        val updatedCustomer = ledgerRepository.getCustomerById(customerId)
        assertEquals(0.0, updatedCustomer?.totalDebt ?: -1.0, 0.001)

        val records = ledgerRepository.getRecordsForCustomer(customerId).first()
        val payment = records.find { it.momoTxId == "TX1004" }
        assertEquals(6000.0, payment?.amount ?: 0.0, 0.001)
    }

    @Test
    fun testUnmatchedLog_returnsFalseAndDoesNotCreatePayment() = runTest {
        val customerId = ledgerRepository.addCustomer(
            Customer(fullName = "Marie Uwase", phoneNumber = "0788111222")
        )
        ledgerRepository.addDebt(customerId, 5000.0, "Existing debt")

        val momoLog = MoMoLog(
            senderName = "Unknown Person",
            senderPhone = "0789000000",
            amount = 5000.0,
            txId = "TX1005",
            rawText = "Received 5000 RWF..."
        )
        fakeMoMoRepository.addMoMoLog(momoLog)

        val result = engine.reconcile(momoLog)

        assertFalse(result)
        val updatedCustomer = ledgerRepository.getCustomerById(customerId)
        assertEquals(5000.0, updatedCustomer?.totalDebt ?: 0.0, 0.001)

        val log = fakeMoMoRepository.getMoMoLogByTxId("TX1005")
        assertFalse(log?.isReconciled == true)
    }

    @Test
    fun testZeroDebtCustomer_doesNotReconcile() = runTest {
        val customerId = ledgerRepository.addCustomer(
            Customer(fullName = "Zero Debt Customer", phoneNumber = "0788555444")
        )

        val momoLog = MoMoLog(
            senderName = "Zero Debt Customer",
            senderPhone = "250788555444",
            amount = 5000.0,
            txId = "TX1006",
            rawText = "Received 5000 RWF..."
        )
        fakeMoMoRepository.addMoMoLog(momoLog)

        val result = engine.reconcile(momoLog)

        assertFalse(result)
        val updatedCustomer = ledgerRepository.getCustomerById(customerId)
        assertEquals(0.0, updatedCustomer?.totalDebt ?: -1.0, 0.001)

        val log = fakeMoMoRepository.getMoMoLogByTxId("TX1006")
        assertFalse(log?.isReconciled == true)
    }

    // --- Fake Repository and DAOs ---

    private class FakeContext : ContextWrapper(null) {
        override fun getSystemService(name: String): Any? = null
        override fun getApplicationContext(): Context = this
    }

    private class FakeMoMoRepository : MoMoRepository {
        val logs = mutableListOf<MoMoLog>()

        override fun getAllMoMoLogs(): Flow<List<MoMoLog>> = flowOf(logs)
        override fun getUnreconciledMoMoLogs(): Flow<List<MoMoLog>> = flowOf(logs.filter { !it.isReconciled })
        override suspend fun getMoMoLogByTxId(txId: String): MoMoLog? = logs.find { it.txId == txId }

        override suspend fun addMoMoLog(moMoLog: MoMoLog): Long {
            val id = if (moMoLog.id == 0L) (logs.size + 1).toLong() else moMoLog.id
            val entry = moMoLog.copy(id = id)
            logs.add(entry)
            return id
        }

        override suspend fun markReconciled(id: Long, isReconciled: Boolean) {
            val index = logs.indexOfFirst { it.id == id }
            if (index != -1) {
                logs[index] = logs[index].copy(isReconciled = isReconciled)
            }
        }

        override suspend fun markReconciledByTxId(txId: String, isReconciled: Boolean) {
            val index = logs.indexOfFirst { it.txId == txId }
            if (index != -1) {
                logs[index] = logs[index].copy(isReconciled = isReconciled)
            }
        }
    }

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
            val calculatedDebt = custRecords.sumOf { record ->
                when (record.type) {
                    TransactionType.CREDIT -> record.amount
                    TransactionType.PAYMENT -> -record.amount
                    TransactionType.CASH_SALE -> 0.0
                    else -> 0.0
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
