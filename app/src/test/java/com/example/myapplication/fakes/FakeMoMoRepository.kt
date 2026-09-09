package com.example.myapplication.fakes

import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.repository.MoMoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMoMoRepository : MoMoRepository {

    private val logsFlow = MutableStateFlow<List<MoMoLog>>(emptyList())
    private var nextId = 1L

    override fun getAllMoMoLogs(): Flow<List<MoMoLog>> {
        return logsFlow.map { list -> list.sortedByDescending { it.timestamp } }
    }

    override fun getUnreconciledMoMoLogs(): Flow<List<MoMoLog>> {
        return logsFlow.map { list ->
            list.filter { !it.isReconciled }.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun getMoMoLogByTxId(txId: String): MoMoLog? {
        return logsFlow.value.find { it.txId == txId }
    }

    override suspend fun addMoMoLog(moMoLog: MoMoLog): Long {
        val id = if (moMoLog.id == 0L) nextId++ else moMoLog.id
        val newLog = moMoLog.copy(id = id)
        logsFlow.value = logsFlow.value + newLog
        return id
    }

    override suspend fun markReconciled(id: Long, isReconciled: Boolean) {
        logsFlow.value = logsFlow.value.map {
            if (it.id == id) it.copy(isReconciled = isReconciled) else it
        }
    }

    override suspend fun markReconciledByTxId(txId: String, isReconciled: Boolean) {
        logsFlow.value = logsFlow.value.map {
            if (it.txId == txId) it.copy(isReconciled = isReconciled) else it
        }
    }
}
