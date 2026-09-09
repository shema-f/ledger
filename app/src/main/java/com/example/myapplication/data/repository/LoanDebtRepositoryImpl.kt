package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.LoanDebtDao
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.repository.LoanDebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoanDebtRepositoryImpl(
    private val loanDebtDao: LoanDebtDao
) : LoanDebtRepository {

    override fun getLoansDebtsByDirection(direction: DebtDirection): Flow<List<LoanDebt>> {
        return loanDebtDao.getLoansDebtsByDirection(direction).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllLoansDebts(): Flow<List<LoanDebt>> {
        return loanDebtDao.getAllLoansDebts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalReceivables(): Flow<Double> {
        return loanDebtDao.getTotalReceivables()
    }

    override fun getTotalPayables(): Flow<Double> {
        return loanDebtDao.getTotalPayables()
    }

    override suspend fun getLoanDebtById(id: Long): LoanDebt? {
        return loanDebtDao.getLoanDebtById(id)?.toDomain()
    }

    override suspend fun insertLoanDebt(loanDebt: LoanDebt): Long {
        return loanDebtDao.insertLoanDebt(loanDebt.toEntity())
    }

    override suspend fun recordRepayment(id: Long, repaymentAmount: Double): LoanDebt? {
        val loanDebtEntity = loanDebtDao.getLoanDebtById(id) ?: return null
        val newRemaining = (loanDebtEntity.remainingAmount - repaymentAmount).coerceAtLeast(0.0)
        val newStatus = if (newRemaining == 0.0) DebtStatus.SETTLED else loanDebtEntity.status
        loanDebtDao.updateRepayment(id, newRemaining, newStatus)
        return loanDebtDao.getLoanDebtById(id)?.toDomain()
    }

    override suspend fun updateStatus(id: Long, status: DebtStatus) {
        loanDebtDao.updateStatus(id, status)
    }
}
