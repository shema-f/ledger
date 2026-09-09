package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.LoanDebtEntity
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDebtDao {
    @Query("SELECT * FROM loans_debts WHERE direction = :direction ORDER BY createdAt DESC")
    fun getLoansDebtsByDirection(direction: DebtDirection): Flow<List<LoanDebtEntity>>

    @Query("SELECT * FROM loans_debts ORDER BY createdAt DESC")
    fun getAllLoansDebts(): Flow<List<LoanDebtEntity>>

    @Query("SELECT * FROM loans_debts WHERE id = :id")
    suspend fun getLoanDebtById(id: Long): LoanDebtEntity?

    @Query("SELECT COALESCE(SUM(remainingAmount), 0.0) FROM loans_debts WHERE direction = 'RECEIVABLE' AND status != 'SETTLED'")
    fun getTotalReceivables(): Flow<Double>

    @Query("SELECT COALESCE(SUM(remainingAmount), 0.0) FROM loans_debts WHERE direction = 'PAYABLE' AND status != 'SETTLED'")
    fun getTotalPayables(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanDebt(loanDebt: LoanDebtEntity): Long

    @Query("UPDATE loans_debts SET remainingAmount = :remainingAmount, status = :status WHERE id = :id")
    suspend fun updateRepayment(id: Long, remainingAmount: Double, status: DebtStatus): Int

    @Query("UPDATE loans_debts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DebtStatus): Int
}
