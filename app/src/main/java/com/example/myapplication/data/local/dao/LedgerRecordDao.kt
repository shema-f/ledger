package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.LedgerRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerRecordDao {

    @Query("SELECT * FROM ledger_records WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getRecordsForCustomer(customerId: Long): Flow<List<LedgerRecordEntity>>

    @Query("SELECT * FROM ledger_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 20): Flow<List<LedgerRecordEntity>>

    @Query("SELECT * FROM ledger_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<LedgerRecordEntity>>

    @Query("SELECT * FROM ledger_records WHERE id = :id")
    suspend fun getRecordById(id: Long): LedgerRecordEntity?

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM ledger_records 
        WHERE timestamp >= :startOfDayTimestamp 
        AND (type = 'CASH_SALE' OR type = 'CREDIT')
    """)
    fun getTodaySalesSum(startOfDayTimestamp: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalDebt), 0.0) FROM customers WHERE totalDebt > 0")
    fun getTotalOutstandingDebt(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LedgerRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: LedgerRecordEntity)
}
