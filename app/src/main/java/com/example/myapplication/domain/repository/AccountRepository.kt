package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getTotalBalance(): Flow<Double>
    suspend fun getAccountById(id: Long): Account?
    suspend fun insertAccount(account: Account): Long
    suspend fun updateBalance(accountId: Long, newBalance: Double)
    suspend fun seedDefaultAccounts()
}
