package com.example.myapplication.ui.customer

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeLedgerRepository
    private lateinit var viewModel: CustomerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLedgerRepository()
        viewModel = CustomerViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddAndUpdateCustomer() = runTest {
        viewModel.addCustomer(fullName = "Eric Kalisa", phoneNumber = "0788001122", nickname = "Eri")
        advanceUntilIdle()

        val customers = repository.getCustomersSortedByName().first()
        assertEquals(1, customers.size)
        val customer = customers[0]
        assertEquals("Eric Kalisa", customer.fullName)
        assertEquals("0788001122", customer.phoneNumber)
        assertEquals("Eri", customer.nickname)

        // Update
        viewModel.updateCustomer(customer.copy(nickname = "Kalin"))
        advanceUntilIdle()

        val updatedCust = repository.getCustomerById(customer.id)
        assertNotNull(updatedCust)
        assertEquals("Kalin", updatedCust!!.nickname)
    }

    @Test
    fun testSearchQueryAndSorting() = runTest {
        repository.addCustomer(Customer(id = 1, fullName = "Zebra Corp", phoneNumber = "0788111111", totalDebt = 100.0))
        repository.addCustomer(Customer(id = 2, fullName = "Alpha Trader", phoneNumber = "0788222222", totalDebt = 500.0))
        advanceUntilIdle()

        // Test DEBT_DESC default sort
        val sortedByDebt = repository.getCustomersSortedByDebt().first()
        assertEquals("Alpha Trader", sortedByDebt[0].fullName)
        assertEquals("Zebra Corp", sortedByDebt[1].fullName)

        // Test search
        viewModel.setSearchQuery("Alpha")
        advanceUntilIdle()
        val searchResult = repository.searchCustomers("Alpha").first()
        assertEquals(1, searchResult.size)
        assertEquals("Alpha Trader", searchResult[0].fullName)
    }

    @Test
    fun testSelectCustomerAndRecords() = runTest {
        val id = repository.addCustomer(Customer(fullName = "Divine Mukasa", phoneNumber = "0788333333"))
        repository.addDebt(customerId = id, amount = 2000.0, description = "Milk")
        advanceUntilIdle()

        viewModel.selectCustomer(id)
        advanceUntilIdle()

        assertEquals(id, viewModel.selectedCustomerId.value)

        val records = repository.getRecordsForCustomer(id).first()
        assertEquals(1, records.size)
        assertEquals(TransactionType.CREDIT, records[0].type)
        assertEquals(2000.0, records[0].amount, 0.01)
    }

    @Test
    fun testGenerateDebtReminderTemplates() {
        val customer = Customer(
            id = 10,
            fullName = "Claude Mugisha",
            phoneNumber = "0788999888",
            totalDebt = 12500.0
        )

        val kiny = viewModel.generateDebtReminder(customer, ReminderLanguage.KINYARWANDA)
        assertTrue(kiny.contains("Claude Mugisha"))
        assertTrue(kiny.contains("12,500"))
        assertTrue(kiny.contains("kuri Imari"))

        val eng = viewModel.generateDebtReminder(customer, ReminderLanguage.ENGLISH)
        assertTrue(eng.contains("Claude Mugisha"))
        assertTrue(eng.contains("12,500"))
        assertTrue(eng.contains("outstanding debt"))

        val fr = viewModel.generateDebtReminder(customer, ReminderLanguage.FRENCH)
        assertTrue(fr.contains("Claude Mugisha"))
        assertTrue(fr.contains("12,500"))
        assertTrue(fr.contains("votre dette"))
    }

    @Test
    fun testDeleteCustomer() = runTest {
        val id = repository.addCustomer(Customer(fullName = "To Delete", phoneNumber = "0788000000"))
        advanceUntilIdle()

        viewModel.selectCustomer(id)
        advanceUntilIdle()

        val cust = repository.getCustomerById(id)
        assertNotNull(cust)

        viewModel.deleteCustomer(cust!!)
        advanceUntilIdle()

        assertNull(repository.getCustomerById(id))
        assertNull(viewModel.selectedCustomerId.value)
    }
}
