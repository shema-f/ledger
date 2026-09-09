package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.MoMoLogDao
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.repository.MoMoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoMoRepositoryImpl(
    private val moMoLogDao: MoMoLogDao
) : MoMoRepository {

    override fun getAllMoMoLogs(): Flow<List<MoMoLog>> {
        return moMoLogDao.getAllMoMoLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getUnreconciledMoMoLogs(): Flow<List<MoMoLog>> {
        return moMoLogDao.getUnreconciledMoMoLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getMoMoLogByTxId(txId: String): MoMoLog? {
        return moMoLogDao.getMoMoLogByTxId(txId)?.toDomain()
    }

    override suspend fun addMoMoLog(moMoLog: MoMoLog): Long {
        return moMoLogDao.insertMoMoLog(moMoLog.toEntity())
    }

    override suspend fun markReconciled(id: Long, isReconciled: Boolean) {
        moMoLogDao.markReconciled(id, isReconciled)
    }

    override suspend fun markReconciledByTxId(txId: String, isReconciled: Boolean) {
        moMoLogDao.markReconciledByTxId(txId, isReconciled)
    }
}
