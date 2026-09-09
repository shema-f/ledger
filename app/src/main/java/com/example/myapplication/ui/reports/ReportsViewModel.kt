package com.example.myapplication.ui.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.LedgerRepository
import com.example.myapplication.domain.repository.LoanDebtRepository
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.util.PdfReportData
import com.example.myapplication.util.PdfReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class DateRangeFilter(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class ReportsUiState(
    val selectedDateRange: DateRangeFilter = DateRangeFilter.ALL_TIME,
    val grossRevenue: Double = 0.0,
    val cogs: Double = 0.0,
    val grossProfit: Double = 0.0,
    val uncollectedDebts: Double = 0.0,
    val outstandingLoans: Double = 0.0,
    val accountBalances: List<Account> = emptyList(),
    val isGeneratingPdf: Boolean = false,
    val pdfReportFile: File? = null
)

class ReportsViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val ledgerRepository: LedgerRepository,
    private val loanDebtRepository: LoanDebtRepository
) : ViewModel() {

    private val _selectedDateRange = MutableStateFlow(DateRangeFilter.ALL_TIME)
    val selectedDateRange: StateFlow<DateRangeFilter> = _selectedDateRange.asStateFlow()

    private val _pdfReportFile = MutableStateFlow<File?>(null)
    val pdfReportFile: StateFlow<File?> = _pdfReportFile.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    val uiState: StateFlow<ReportsUiState> = combine(
        _selectedDateRange,
        transactionRepository.getAllTransactions(),
        accountRepository.getAllAccounts(),
        ledgerRepository.getTotalOutstandingDebt(),
        loanDebtRepository.getTotalPayables(),
        _isGeneratingPdf,
        _pdfReportFile
    ) { flows ->
        val dateRange = flows[0] as DateRangeFilter
        @Suppress("UNCHECKED_CAST")
        val transactions = flows[1] as List<Transaction>
        @Suppress("UNCHECKED_CAST")
        val accounts = flows[2] as List<Account>
        val uncollectedDebts = flows[3] as Double
        val outstandingLoans = flows[4] as Double
        val isGenerating = flows[5] as Boolean
        val pdfFile = flows[6] as File?

        val now = System.currentTimeMillis()
        val minTimestamp = when (dateRange) {
            DateRangeFilter.TODAY -> getStartOfDayTimestamp(now)
            DateRangeFilter.THIS_WEEK -> getStartOfWeekTimestamp(now)
            DateRangeFilter.THIS_MONTH -> getStartOfMonthTimestamp(now)
            DateRangeFilter.ALL_TIME -> 0L
        }

        val filteredTx = transactions.filter { it.timestamp >= minTimestamp }

        val grossRevenue = filteredTx
            .filter { it.type == TransactionType.INCOME || it.type == TransactionType.CASH_SALE }
            .sumOf { it.amount }

        val cogs = filteredTx
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val grossProfit = grossRevenue - cogs

        ReportsUiState(
            selectedDateRange = dateRange,
            grossRevenue = grossRevenue,
            cogs = cogs,
            grossProfit = grossProfit,
            uncollectedDebts = uncollectedDebts,
            outstandingLoans = outstandingLoans,
            accountBalances = accounts,
            isGeneratingPdf = isGenerating,
            pdfReportFile = pdfFile
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )

    fun setDateRangeFilter(filter: DateRangeFilter) {
        _selectedDateRange.value = filter
    }

    fun generatePdfReport(context: Context, shopName: String) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val state = uiState.value
                val wallets = state.accountBalances.map { it.name to it.currentBalance }
                val reportData = PdfReportData(
                    shopName = shopName,
                    dateRangeText = state.selectedDateRange.displayName,
                    grossRevenue = state.grossRevenue,
                    cogs = state.cogs,
                    grossProfit = state.grossProfit,
                    walletBalances = wallets,
                    uncollectedDebts = state.uncollectedDebts,
                    outstandingLoans = state.outstandingLoans
                )
                val file = PdfReportGenerator.generatePdfReport(context, reportData)
                _pdfReportFile.value = file
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun clearPdfReportEvent() {
        _pdfReportFile.value = null
    }

    private fun getStartOfDayTimestamp(now: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeekTimestamp(now: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonthTimestamp(now: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val accountRepository: AccountRepository,
        private val ledgerRepository: LedgerRepository,
        private val loanDebtRepository: LoanDebtRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
                return ReportsViewModel(
                    transactionRepository,
                    accountRepository,
                    ledgerRepository,
                    loanDebtRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
