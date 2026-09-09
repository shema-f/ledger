package com.example.myapplication.ui.momo

import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.fakes.FakeLedgerRepository
import com.example.myapplication.fakes.FakeMoMoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoMoFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var moMoRepository: FakeMoMoRepository
    private lateinit var ledgerRepository: FakeLedgerRepository
    private lateinit var viewModel: MoMoFeedViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        moMoRepository = FakeMoMoRepository()
        ledgerRepository = FakeLedgerRepository()
        viewModel = MoMoFeedViewModel(moMoRepository, ledgerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadAndFilterMoMoLogs() = runTest {
        moMoRepository.addMoMoLog(
            MoMoLog(id = 1, senderName = "P1", amount = 1000.0, txId = "TX1", rawText = "text1", isReconciled = false)
        )
        moMoRepository.addMoMoLog(
            MoMoLog(id = 2, senderName = "P2", amount = 2000.0, txId = "TX2", rawText = "text2", isReconciled = true)
        )
        advanceUntilIdle()

        // Unfiltered should return all 2 logs
        val allLogs = moMoRepository.getAllMoMoLogs().first()
        assertEquals(2, allLogs.size)

        // Filter unreconciled
        viewModel.setFilterUnreconciled(true)
        advanceUntilIdle()
        assertTrue(viewModel.filterUnreconciledOnly.value)

        val unreconciledLogs = moMoRepository.getUnreconciledMoMoLogs().first()
        assertEquals(1, unreconciledLogs.size)
        assertEquals("TX1", unreconciledLogs[0].txId)
        assertFalse(unreconciledLogs[0].isReconciled)
    }

    @Test
    fun testReconcilePaymentUpdatesLedgerAndMoMoLog() = runTest {
        val custId = ledgerRepository.addCustomer(
            Customer(fullName = "Jean Paul", phoneNumber = "0788123456", totalDebt = 5000.0)
        )
        moMoRepository.addMoMoLog(
            MoMoLog(id = 10, senderName = "Jean Paul", amount = 3000.0, txId = "TX_MOMO_100", rawText = "Received 3000 RWF", isReconciled = false)
        )
        advanceUntilIdle()

        val logToReconcile = moMoRepository.getMoMoLogByTxId("TX_MOMO_100")!!

        viewModel.reconcilePayment(logToReconcile, custId)
        advanceUntilIdle()

        // Check MoMo Log is now reconciled
        val updatedLog = moMoRepository.getMoMoLogByTxId("TX_MOMO_100")
        assertTrue(updatedLog!!.isReconciled)

        // Check Customer debt is reduced by 3000 RWF (5000 - 3000 = 2000)
        val customer = ledgerRepository.getCustomerById(custId)
        assertEquals(2000.0, customer!!.totalDebt, 0.01)

        // Check Ledger payment record exists
        val records = ledgerRepository.getRecordsForCustomer(custId).first()
        assertEquals(1, records.size)
        assertEquals(TransactionType.PAYMENT, records[0].type)
        assertEquals(3000.0, records[0].amount, 0.01)
        assertEquals("TX_MOMO_100", records[0].momoTxId)
    }
}
