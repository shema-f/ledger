package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.MoMoLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoMoLogDao {

    @Query("SELECT * FROM momo_logs ORDER BY timestamp DESC")
    fun getAllMoMoLogs(): Flow<List<MoMoLogEntity>>

    @Query("SELECT * FROM momo_logs WHERE isReconciled = 0 ORDER BY timestamp DESC")
    fun getUnreconciledMoMoLogs(): Flow<List<MoMoLogEntity>>

    @Query("SELECT * FROM momo_logs WHERE txId = :txId LIMIT 1")
    suspend fun getMoMoLogByTxId(txId: String): MoMoLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoMoLog(moMoLog: MoMoLogEntity): Long

    @Query("UPDATE momo_logs SET isReconciled = :isReconciled WHERE id = :id")
    suspend fun markReconciled(id: Long, isReconciled: Boolean = true)

    @Query("UPDATE momo_logs SET isReconciled = :isReconciled WHERE txId = :txId")
    suspend fun markReconciledByTxId(txId: String, isReconciled: Boolean = true)
}
