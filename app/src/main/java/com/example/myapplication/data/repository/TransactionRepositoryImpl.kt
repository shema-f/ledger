package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.domain.model.ProfitAndLossSummary
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalIncome(): Flow<Double> {
        return transactionDao.getTotalIncome()
    }

    override fun getTotalExpense(): Flow<Double> {
        return transactionDao.getTotalExpense()
    }

    override fun getTotalProfit(): Flow<Double> {
        return transactionDao.getTotalProfit()
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransaction(id)
    }

    override fun calculateProfitAndLoss(): Flow<ProfitAndLossSummary> {
        return combine(
            getTotalIncome(),
            getTotalExpense(),
            getTotalProfit()
        ) { income, expense, profit ->
            ProfitAndLossSummary(
                totalIncome = income,
                totalExpense = expense,
                totalProfit = profit,
                netIncome = income - expense
            )
        }
    }
}
