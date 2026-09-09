package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import com.example.myapplication.domain.model.LoanDebt
import kotlinx.coroutines.flow.Flow

interface LoanDebtRepository {
    fun getLoansDebtsByDirection(direction: DebtDirection): Flow<List<LoanDebt>>
    fun getAllLoansDebts(): Flow<List<LoanDebt>>
    fun getTotalReceivables(): Flow<Double>
    fun getTotalPayables(): Flow<Double>
    suspend fun getLoanDebtById(id: Long): LoanDebt?
    suspend fun insertLoanDebt(loanDebt: LoanDebt): Long
    suspend fun recordRepayment(id: Long, repaymentAmount: Double): LoanDebt?
    suspend fun updateStatus(id: Long, status: DebtStatus)
}
