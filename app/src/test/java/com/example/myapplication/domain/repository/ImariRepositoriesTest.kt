package com.example.myapplication.domain.repository

import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.LoanDebtDao
import com.example.myapplication.data.local.dao.ProductDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.LoanDebtEntity
import com.example.myapplication.data.local.entity.ProductEntity
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.data.repository.AccountRepositoryImpl
import com.example.myapplication.data.repository.LoanDebtRepositoryImpl
import com.example.myapplication.data.repository.ProductRepositoryImpl
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAccountDao : AccountDao {
    private val accounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    private var idCounter = 1L

    override fun getAllAccounts(): Flow<List<AccountEntity>> = accounts

    override fun getTotalBalance(): Flow<Double> {
        return accounts.map { list -> list.sumOf { it.currentBalance } }
    }

    override suspend fun getAccountById(id: Long): AccountEntity? {
        return accounts.value.find { it.id == id }
    }

    override suspend fun insertAccount(account: AccountEntity): Long {
        val newId = if (account.id == 0L) idCounter++ else account.id
        val entity = account.copy(id = newId)
        accounts.value = accounts.value.filterNot { it.id == newId } + entity
        return newId
    }

    override suspend fun insertAccounts(accounts: List<AccountEntity>) {
        accounts.forEach { insertAccount(it) }
    }

    override suspend fun updateBalance(accountId: Long, newBalance: Double): Int {
        if (accounts.value.none { it.id == accountId }) return 0
        accounts.value = accounts.value.map {
            if (it.id == accountId) it.copy(currentBalance = newBalance) else it
        }
        return 1
    }

    override suspend fun getAccountCount(): Int = accounts.value.size
}

class FakeProductDao : ProductDao {
    private val products = MutableStateFlow<List<ProductEntity>>(emptyList())
    private var idCounter = 1L

    override fun getAllProducts(): Flow<List<ProductEntity>> = products

    override fun getLowStockProducts(): Flow<List<ProductEntity>> {
        return products.map { list -> list.filter { it.currentStock <= it.minAlertStock } }
    }

    override fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return products.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) || (it.barcode?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    override suspend fun getProductById(id: Long): ProductEntity? {
        return products.value.find { it.id == id }
    }

    override suspend fun getProductByBarcode(barcode: String): ProductEntity? {
        return products.value.find { it.barcode == barcode }
    }

    override suspend fun insertProduct(product: ProductEntity): Long {
        val newId = if (product.id == 0L) idCounter++ else product.id
        val entity = product.copy(id = newId)
        products.value = products.value.filterNot { it.id == newId } + entity
        return newId
    }

    override suspend fun updateProduct(product: ProductEntity) {
        products.value = products.value.map { if (it.id == product.id) product else it }
    }

    override suspend fun updateStock(id: Long, newStock: Int): Int {
        if (products.value.none { it.id == id }) return 0
        products.value = products.value.map {
            if (it.id == id) it.copy(currentStock = newStock) else it
        }
        return 1
    }

    override suspend fun deleteProduct(id: Long): Int {
        val initialSize = products.value.size
        products.value = products.value.filterNot { it.id == id }
        return if (products.value.size < initialSize) 1 else 0
    }
}

class FakeTransactionDao : TransactionDao {
    private val transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private var idCounter = 1L

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = transactions

    override fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> {
        return transactions.map { list -> list.filter { it.type == type } }
    }

    override fun getTotalIncome(): Flow<Double> {
        return transactions.map { list ->
            list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        }
    }

    override fun getTotalExpense(): Flow<Double> {
        return transactions.map { list ->
            list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        }
    }

    override fun getTotalProfit(): Flow<Double> {
        return transactions.map { list -> list.sumOf { it.profit } }
    }

    override suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactions.value.find { it.id == id }
    }

    override suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val newId = if (transaction.id == 0L) idCounter++ else transaction.id
        val entity = transaction.copy(id = newId)
        transactions.value = transactions.value + entity
        return newId
    }

    override suspend fun deleteTransaction(id: Long): Int {
        val initialSize = transactions.value.size
        transactions.value = transactions.value.filterNot { it.id == id }
        return if (transactions.value.size < initialSize) 1 else 0
    }
}

class FakeLoanDebtDao : LoanDebtDao {
    private val loansDebts = MutableStateFlow<List<LoanDebtEntity>>(emptyList())
    private var idCounter = 1L

    override fun getLoansDebtsByDirection(direction: DebtDirection): Flow<List<LoanDebtEntity>> {
        return loansDebts.map { list -> list.filter { it.direction == direction } }
    }

    override fun getAllLoansDebts(): Flow<List<LoanDebtEntity>> = loansDebts

    override suspend fun getLoanDebtById(id: Long): LoanDebtEntity? {
        return loansDebts.value.find { it.id == id }
    }

    override fun getTotalReceivables(): Flow<Double> {
        return loansDebts.map { list ->
            list.filter { it.direction == DebtDirection.RECEIVABLE && it.status != DebtStatus.SETTLED }
                .sumOf { it.remainingAmount }
        }
    }

    override fun getTotalPayables(): Flow<Double> {
        return loansDebts.map { list ->
            list.filter { it.direction == DebtDirection.PAYABLE && it.status != DebtStatus.SETTLED }
                .sumOf { it.remainingAmount }
        }
    }

    override suspend fun insertLoanDebt(loanDebt: LoanDebtEntity): Long {
        val newId = if (loanDebt.id == 0L) idCounter++ else loanDebt.id
        val entity = loanDebt.copy(id = newId)
        loansDebts.value = loansDebts.value.filterNot { it.id == newId } + entity
        return newId
    }

    override suspend fun updateRepayment(id: Long, remainingAmount: Double, status: DebtStatus): Int {
        if (loansDebts.value.none { it.id == id }) return 0
        loansDebts.value = loansDebts.value.map {
            if (it.id == id) it.copy(remainingAmount = remainingAmount, status = status) else it
        }
        return 1
    }

    override suspend fun updateStatus(id: Long, status: DebtStatus): Int {
        if (loansDebts.value.none { it.id == id }) return 0
        loansDebts.value = loansDebts.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
        return 1
    }
}

class ImariRepositoriesTest {

    private lateinit var accountDao: FakeAccountDao
    private lateinit var productDao: FakeProductDao
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var loanDebtDao: FakeLoanDebtDao

    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var productRepository: ProductRepositoryImpl
    private lateinit var transactionRepository: TransactionRepositoryImpl
    private lateinit var loanDebtRepository: LoanDebtRepositoryImpl

    @Before
    fun setUp() {
        accountDao = FakeAccountDao()
        productDao = FakeProductDao()
        transactionDao = FakeTransactionDao()
        loanDebtDao = FakeLoanDebtDao()

        accountRepository = AccountRepositoryImpl(accountDao)
        productRepository = ProductRepositoryImpl(productDao)
        transactionRepository = TransactionRepositoryImpl(transactionDao)
        loanDebtRepository = LoanDebtRepositoryImpl(loanDebtDao)
    }

    @Test
    fun testAccountBalanceTracking() = runTest {
        // Test seeding default financial accounts
        accountRepository.seedDefaultAccounts()
        val defaultAccounts = accountRepository.getAllAccounts().first()
        assertEquals(8, defaultAccounts.size)

        val mtnMomo = defaultAccounts.find { it.name == "MTN MoMo" }
        assertNotNull(mtnMomo)
        assertTrue(mtnMomo!!.isDefault)

        // Update balance
        accountRepository.updateBalance(mtnMomo.id, 150000.0)
        val updatedMomo = accountRepository.getAccountById(mtnMomo.id)
        assertEquals(150000.0, updatedMomo?.currentBalance ?: 0.0, 0.001)

        // Add custom account
        accountRepository.insertAccount(
            Account(
                name = "BK Business",
                accountType = AccountType.BANK,
                currentBalance = 500000.0
            )
        )

        val totalBalance = accountRepository.getTotalBalance().first()
        assertEquals(650000.0, totalBalance, 0.001)
    }

    @Test
    fun testProductStockDeductionAndProfitComputation() = runTest {
        val productId = productRepository.insertProduct(
            Product(
                name = "Inyange Milk 500ml",
                buyingPrice = 400.0,
                sellingPrice = 600.0,
                currentStock = 20,
                minAlertStock = 5
            )
        )

        // Deduct 5 units
        val profit = productRepository.deductStockAndCalculateProfit(productId, 5)
        // Expected profit: 5 * (600 - 400) = 1000.0
        assertEquals(1000.0, profit, 0.001)

        val updatedProduct = productRepository.getProductById(productId)
        assertNotNull(updatedProduct)
        assertEquals(15, updatedProduct!!.currentStock)

        // Deduct 12 units to trigger low stock alert
        productRepository.deductStockAndCalculateProfit(productId, 12)
        val lowStockProducts = productRepository.getLowStockProducts().first()
        assertEquals(1, lowStockProducts.size)
        assertEquals(productId, lowStockProducts[0].id)
    }

    @Test
    fun testLoanDebtRepaymentMath() = runTest {
        val loanDebtId = loanDebtRepository.insertLoanDebt(
            LoanDebt(
                personOrInstitution = "Mugisha Eric",
                phoneNumber = "+250788111222",
                direction = DebtDirection.RECEIVABLE,
                principalAmount = 50000.0,
                remainingAmount = 50000.0,
                status = DebtStatus.ACTIVE,
                createdAt = System.currentTimeMillis()
            )
        )

        // Partial repayment of 20,000
        val partialUpdated = loanDebtRepository.recordRepayment(loanDebtId, 20000.0)
        assertNotNull(partialUpdated)
        assertEquals(30000.0, partialUpdated!!.remainingAmount, 0.001)
        assertEquals(DebtStatus.ACTIVE, partialUpdated.status)

        val receivablesMid = loanDebtRepository.getTotalReceivables().first()
        assertEquals(30000.0, receivablesMid, 0.001)

        // Full remaining repayment of 30,000
        val finalUpdated = loanDebtRepository.recordRepayment(loanDebtId, 30000.0)
        assertNotNull(finalUpdated)
        assertEquals(0.0, finalUpdated!!.remainingAmount, 0.001)
        assertEquals(DebtStatus.SETTLED, finalUpdated.status)

        val receivablesEnd = loanDebtRepository.getTotalReceivables().first()
        assertEquals(0.0, receivablesEnd, 0.001)
    }

    @Test
    fun testProfitAndLossCalculation() = runTest {
        transactionRepository.insertTransaction(
            Transaction(
                accountId = 1,
                type = TransactionType.INCOME,
                amount = 200000.0,
                profit = 50000.0,
                description = "Daily Sales",
                timestamp = System.currentTimeMillis()
            )
        )

        transactionRepository.insertTransaction(
            Transaction(
                accountId = 1,
                type = TransactionType.EXPENSE,
                amount = 40000.0,
                profit = 0.0,
                description = "Rent Payment",
                timestamp = System.currentTimeMillis()
            )
        )

        val summary = transactionRepository.calculateProfitAndLoss().first()
        assertEquals(200000.0, summary.totalIncome, 0.001)
        assertEquals(40000.0, summary.totalExpense, 0.001)
        assertEquals(50000.0, summary.totalProfit, 0.001)
        assertEquals(160000.0, summary.netIncome, 0.001)
    }
}
