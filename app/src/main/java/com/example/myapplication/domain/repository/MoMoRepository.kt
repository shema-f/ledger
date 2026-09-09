package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.MoMoLog
import kotlinx.coroutines.flow.Flow

interface MoMoRepository {
    fun getAllMoMoLogs(): Flow<List<MoMoLog>>
    fun getUnreconciledMoMoLogs(): Flow<List<MoMoLog>>
    suspend fun getMoMoLogByTxId(txId: String): MoMoLog?
    suspend fun addMoMoLog(moMoLog: MoMoLog): Long
    suspend fun markReconciled(id: Long, isReconciled: Boolean = true)
    suspend fun markReconciledByTxId(txId: String, isReconciled: Boolean = true)
}
