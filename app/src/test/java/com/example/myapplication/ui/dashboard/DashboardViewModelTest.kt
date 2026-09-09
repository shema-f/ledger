package com.example.myapplication.ui.dashboard

import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.fakes.FakeLedgerRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeLedgerRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLedgerRepository()
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialDashboardSummary() = runTest {
        advanceUntilIdle()
        val summary = viewModel.dashboardSummary.value
        assertEquals(0.0, summary.totalOutstandingDebt, 0.01)
        assertEquals(0.0, summary.todaySales, 0.01)
        assertEquals(0, summary.activeDebtorsCount)
    }

    @Test
    fun testAddDebtUpdatesSummaryAndRecentRecords() = runTest {
        val custId = repository.addCustomer(
            Customer(fullName = "Jean Paul", phoneNumber = "0788123456")
        )
        advanceUntilIdle()

        viewModel.addDebt(customerId = custId, amount = 5000.0, description = "Sugar")
        advanceUntilIdle()

        val summary = repository.getDashboardSummary().first()
        assertEquals(5000.0, summary.totalOutstandingDebt, 0.01)
        assertEquals(1, summary.activeDebtorsCount)

        val recent = repository.getRecentRecords(10).first()
        assertEquals(1, recent.size)
        assertEquals(TransactionType.CREDIT, recent[0].type)
        assertEquals(5000.0, recent[0].amount, 0.01)
        assertEquals("Sugar", recent[0].description)
    }

    @Test
    fun testRecordPaymentReducesDebt() = runTest {
        val custId = repository.addCustomer(
            Customer(fullName = "Aline Uwase", phoneNumber = "0789654321")
        )
        repository.addDebt(customerId = custId, amount = 10000.0, description = "Rice")
        advanceUntilIdle()

        viewModel.recordPayment(customerId = custId, amount = 4000.0, description = "MoMo Payment")
        advanceUntilIdle()

        val customer = repository.getCustomerById(custId)
        assertNotNull(customer)
        assertEquals(6000.0, customer!!.totalDebt, 0.01)

        val recent = repository.getRecentRecords(10).first()
        assertEquals(2, recent.size)
        assertEquals(TransactionType.PAYMENT, recent[0].type)
        assertEquals(4000.0, recent[0].amount, 0.01)
    }

    @Test
    fun testRecordCashSaleUpdatesTodaySales() = runTest {
        viewModel.recordCashSale(amount = 15000.0, description = "Oil bottle")
        advanceUntilIdle()

        val summary = repository.getDashboardSummary().first()
        assertEquals(15000.0, summary.todaySales, 0.01)

        val recent = repository.getRecentRecords(10).first()
        assertEquals(1, recent.size)
        assertEquals(TransactionType.CASH_SALE, recent[0].type)
        assertEquals(15000.0, recent[0].amount, 0.01)
    }
}
