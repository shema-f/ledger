package com.example.myapplication.ui.inventory

import com.example.myapplication.data.repository.AccountRepositoryImpl
import com.example.myapplication.data.repository.ProductRepositoryImpl
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.FakeAccountDao
import com.example.myapplication.domain.repository.FakeProductDao
import com.example.myapplication.domain.repository.FakeTransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var productDao: FakeProductDao
    private lateinit var accountDao: FakeAccountDao
    private lateinit var transactionDao: FakeTransactionDao

    private lateinit var productRepository: ProductRepositoryImpl
    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var transactionRepository: TransactionRepositoryImpl

    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        productDao = FakeProductDao()
        accountDao = FakeAccountDao()
        transactionDao = FakeTransactionDao()

        productRepository = ProductRepositoryImpl(productDao)
        accountRepository = AccountRepositoryImpl(accountDao)
        transactionRepository = TransactionRepositoryImpl(transactionDao)

        viewModel = InventoryViewModel(
            productRepository = productRepository,
            accountRepository = accountRepository,
            transactionRepository = transactionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddProductAndProductList() = runTest {
        backgroundScope.launch { viewModel.products.collect {} }

        viewModel.addProduct(
            name = "Inyange Milk 500ml",
            barcode = "123456",
            buyingPrice = 400.0,
            sellingPrice = 600.0,
            currentStock = 20,
            minAlertStock = 5
        )
        advanceUntilIdle()

        val products = viewModel.products.value
        assertEquals(1, products.size)
        val product = products[0]
        assertEquals("Inyange Milk 500ml", product.name)
        assertEquals("123456", product.barcode)
        assertEquals(400.0, product.buyingPrice, 0.001)
        assertEquals(600.0, product.sellingPrice, 0.001)
        assertEquals(20, product.currentStock)
    }

    @Test
    fun testSearchQueryFilter() = runTest {
        backgroundScope.launch { viewModel.products.collect {} }

        viewModel.addProduct("Inyange Milk", "111", 400.0, 600.0, 20)
        viewModel.addProduct("Bwiza Bread", "222", 800.0, 1000.0, 15)
        advanceUntilIdle()

        // Before filter
        assertEquals(2, viewModel.products.value.size)

        // Apply search query
        viewModel.setSearchQuery("Bread")
        advanceUntilIdle()

        val filtered = viewModel.products.value
        assertEquals(1, filtered.size)
        assertEquals("Bwiza Bread", filtered[0].name)
    }

    @Test
    fun testLowStockAlertFilter() = runTest {
        backgroundScope.launch { viewModel.products.collect {} }

        viewModel.addProduct("High Stock Item", "111", 100.0, 150.0, 20, 5)
        viewModel.addProduct("Low Stock Item", "222", 100.0, 150.0, 2, 5)
        advanceUntilIdle()

        assertEquals(2, viewModel.products.value.size)

        // Toggle Low Stock filter
        viewModel.toggleLowStockFilter(true)
        advanceUntilIdle()

        val lowStockOnly = viewModel.products.value
        assertEquals(1, lowStockOnly.size)
        assertEquals("Low Stock Item", lowStockOnly[0].name)
    }

    @Test
    fun testConductPosSaleSuccess() = runTest {
        backgroundScope.launch { viewModel.products.collect {} }
        backgroundScope.launch { viewModel.accounts.collect {} }

        // Seed account
        val accountId = accountRepository.insertAccount(
            Account(
                name = "MTN MoMo",
                accountType = AccountType.MOBILE_MONEY,
                currentBalance = 10000.0
            )
        )

        // Add product
        val productId = productRepository.insertProduct(
            Product(
                name = "Bottled Water 1L",
                buyingPrice = 200.0,
                sellingPrice = 300.0,
                currentStock = 10,
                minAlertStock = 3
            )
        )
        advanceUntilIdle()

        val product = productRepository.getProductById(productId)
        assertNotNull(product)

        var successCalled = false
        viewModel.conductPosSale(
            product = product!!,
            quantity = 3,
            accountId = accountId,
            onSuccess = { successCalled = true }
        )
        advanceUntilIdle()

        assertTrue(successCalled)

        // Check stock updated
        val updatedProduct = productRepository.getProductById(productId)
        assertEquals(7, updatedProduct?.currentStock)

        // Check target account balance updated (10,000 + 3 * 300 = 10,900)
        val updatedAccount = accountRepository.getAccountById(accountId)
        assertEquals(10900.0, updatedAccount?.currentBalance ?: 0.0, 0.001)

        // Check transaction inserted
        val transactions = transactionRepository.getAllTransactions().first()
        assertEquals(1, transactions.size)
        val tx = transactions[0]
        assertEquals(TransactionType.INCOME, tx.type)
        assertEquals(900.0, tx.amount, 0.001) // 3 * 300
        assertEquals(300.0, tx.profit, 0.001) // 3 * (300 - 200)
    }

    @Test
    fun testUpdateAndDeleteProduct() = runTest {
        backgroundScope.launch { viewModel.products.collect {} }

        val id = productRepository.insertProduct(
            Product(
                name = "Test Item",
                buyingPrice = 100.0,
                sellingPrice = 150.0,
                currentStock = 5
            )
        )
        advanceUntilIdle()

        val item = productRepository.getProductById(id)
        assertNotNull(item)

        viewModel.updateProduct(item!!.copy(sellingPrice = 180.0))
        advanceUntilIdle()

        val updated = productRepository.getProductById(id)
        assertEquals(180.0, updated?.sellingPrice ?: 0.0, 0.001)

        viewModel.deleteProduct(id)
        advanceUntilIdle()

        val deleted = productRepository.getProductById(id)
        assertTrue(deleted == null)
    }

    @Test
    fun testFindProductByBarcode() = runTest {
        backgroundScope.launch { viewModel.products.collect {} }

        viewModel.addProduct(
            name = "Inyange Mango Juice",
            barcode = "888999",
            buyingPrice = 300.0,
            sellingPrice = 500.0,
            currentStock = 12
        )
        advanceUntilIdle()

        val found = viewModel.findProductByBarcode("888999")
        assertNotNull(found)
        assertEquals("Inyange Mango Juice", found?.name)

        val notFound = viewModel.findProductByBarcode("000000")
        assertTrue(notFound == null)
    }
}
