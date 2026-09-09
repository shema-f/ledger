package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.local.ImariDatabase
import com.example.myapplication.data.repository.AccountRepositoryImpl
import com.example.myapplication.data.repository.LedgerRepositoryImpl
import com.example.myapplication.data.repository.LoanDebtRepositoryImpl
import com.example.myapplication.data.repository.MoMoRepositoryImpl
import com.example.myapplication.data.repository.ProductRepositoryImpl
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.LedgerRepository
import com.example.myapplication.domain.repository.LoanDebtRepository
import com.example.myapplication.domain.repository.MoMoRepository
import com.example.myapplication.domain.repository.ProductRepository
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.security.SecurityManager
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.BiometricAuthScreen
import com.example.myapplication.ui.customer.CustomerViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.inventory.InventoryViewModel
import com.example.myapplication.ui.loans.LoanDebtViewModel
import com.example.myapplication.ui.momo.MoMoFeedViewModel
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.reconciliation.SmsReconciliationViewModel
import com.example.myapplication.ui.reports.ReportsViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    private lateinit var ledgerRepository: LedgerRepository
    private lateinit var moMoRepository: MoMoRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var loanDebtRepository: LoanDebtRepository
    private lateinit var securityManager: SecurityManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ImariDatabase.getInstance(applicationContext)
        ledgerRepository = LedgerRepositoryImpl(database.customerDao(), database.ledgerRecordDao())
        moMoRepository = MoMoRepositoryImpl(database.moMoLogDao())
        productRepository = ProductRepositoryImpl(database.productDao())
        accountRepository = AccountRepositoryImpl(database.accountDao())
        transactionRepository = TransactionRepositoryImpl(database.transactionDao())
        loanDebtRepository = LoanDebtRepositoryImpl(database.loanDebtDao())

        securityManager = SecurityManager(applicationContext)

        val isLockedInitial = securityManager.shouldLockApp()

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                var isLocked by remember { mutableStateOf(isLockedInitial) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLocked) {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(securityManager)
                        )

                        BiometricAuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = {
                                isLocked = false
                            }
                        )
                    } else {
                        val dashboardViewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModel.Factory(ledgerRepository)
                        )
                        val customerViewModel: CustomerViewModel = viewModel(
                            factory = CustomerViewModel.Factory(ledgerRepository)
                        )
                        val inventoryViewModel: InventoryViewModel = viewModel(
                            factory = InventoryViewModel.Factory(
                                productRepository,
                                accountRepository,
                                transactionRepository
                            )
                        )
                        val loanDebtViewModel: LoanDebtViewModel = viewModel(
                            factory = LoanDebtViewModel.Factory(
                                loanDebtRepository,
                                accountRepository,
                                transactionRepository
                            )
                        )
                        val momoFeedViewModel: MoMoFeedViewModel = viewModel(
                            factory = MoMoFeedViewModel.Factory(moMoRepository, ledgerRepository)
                        )
                        val smsReconciliationViewModel: SmsReconciliationViewModel = viewModel(
                            factory = SmsReconciliationViewModel.Factory(
                                productRepository,
                                accountRepository,
                                transactionRepository,
                                loanDebtRepository,
                                ledgerRepository,
                                moMoRepository
                            )
                        )
                        val reportsViewModel: ReportsViewModel = viewModel(
                            factory = ReportsViewModel.Factory(
                                transactionRepository,
                                accountRepository,
                                ledgerRepository,
                                loanDebtRepository
                            )
                        )
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(securityManager)
                        )

                        AppNavigation(
                            dashboardViewModel = dashboardViewModel,
                            customerViewModel = customerViewModel,
                            inventoryViewModel = inventoryViewModel,
                            loanDebtViewModel = loanDebtViewModel,
                            momoFeedViewModel = momoFeedViewModel,
                            reconciliationViewModel = smsReconciliationViewModel,
                            reportsViewModel = reportsViewModel,
                            authViewModel = authViewModel,
                            onLockApp = { isLocked = true },
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = it }
                        )
                    }
                }
            }
        }
    }
}
