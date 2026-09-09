package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.local.KayiDatabase
import com.example.myapplication.data.repository.LedgerRepositoryImpl
import com.example.myapplication.data.repository.MoMoRepositoryImpl
import com.example.myapplication.domain.repository.LedgerRepository
import com.example.myapplication.domain.repository.MoMoRepository
import com.example.myapplication.security.SecurityManager
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.BiometricAuthScreen
import com.example.myapplication.ui.customer.CustomerViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.momo.MoMoFeedViewModel
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    private lateinit var ledgerRepository: LedgerRepository
    private lateinit var moMoRepository: MoMoRepository
    private lateinit var securityManager: SecurityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KayiDatabase.getInstance(applicationContext)
        ledgerRepository = LedgerRepositoryImpl(database.customerDao(), database.ledgerRecordDao())
        moMoRepository = MoMoRepositoryImpl(database.moMoLogDao())
        securityManager = SecurityManager(applicationContext)

        val isLockedInitial = securityManager.shouldLockApp()

        setContent {
            MyApplicationTheme {
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
                        val momoFeedViewModel: MoMoFeedViewModel = viewModel(
                            factory = MoMoFeedViewModel.Factory(moMoRepository, ledgerRepository)
                        )

                        AppNavigation(
                            dashboardViewModel = dashboardViewModel,
                            customerViewModel = customerViewModel,
                            momoFeedViewModel = momoFeedViewModel
                        )
                    }
                }
            }
        }
    }
}
