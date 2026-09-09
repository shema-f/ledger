package com.example.myapplication.ui.momo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.repository.LedgerRepository
import com.example.myapplication.domain.repository.MoMoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MoMoFeedViewModel(
    private val moMoRepository: MoMoRepository,
    private val ledgerRepository: LedgerRepository
) : ViewModel() {

    private val _filterUnreconciledOnly = MutableStateFlow(false)
    val filterUnreconciledOnly: StateFlow<Boolean> = _filterUnreconciledOnly.asStateFlow()

    val momoLogs: StateFlow<List<MoMoLog>> = _filterUnreconciledOnly
        .flatMapLatest { unreconciledOnly ->
            if (unreconciledOnly) {
                moMoRepository.getUnreconciledMoMoLogs()
            } else {
                moMoRepository.getAllMoMoLogs()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customers: StateFlow<List<Customer>> = ledgerRepository.getCustomersSortedByName()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setFilterUnreconciled(unreconciledOnly: Boolean) {
        _filterUnreconciledOnly.value = unreconciledOnly
    }

    fun reconcilePayment(momoLog: MoMoLog, customerId: Long) {
        viewModelScope.launch {
            // Record payment in ledger
            ledgerRepository.recordPayment(
                customerId = customerId,
                amount = momoLog.amount,
                description = "MoMo Payment (${momoLog.txId})",
                momoTxId = momoLog.txId
            )
            // Mark MoMo Log as reconciled
            if (momoLog.id != 0L) {
                moMoRepository.markReconciled(momoLog.id, true)
            } else {
                moMoRepository.markReconciledByTxId(momoLog.txId, true)
            }
        }
    }

    class Factory(
        private val moMoRepository: MoMoRepository,
        private val ledgerRepository: LedgerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoMoFeedViewModel::class.java)) {
                return MoMoFeedViewModel(moMoRepository, ledgerRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
