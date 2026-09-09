package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.ProfitAndLossSummary
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    fun getTotalIncome(): Flow<Double>
    fun getTotalExpense(): Flow<Double>
    fun getTotalProfit(): Flow<Double>
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(id: Long)
    fun calculateProfitAndLoss(): Flow<ProfitAndLossSummary>
}
