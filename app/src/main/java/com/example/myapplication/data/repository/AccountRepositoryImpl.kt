package com.example.myapplication.data.repository

import com.example.myapplication.data.local.ImariDatabase
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalBalance(): Flow<Double> {
        return accountDao.getTotalBalance()
    }

    override suspend fun getAccountById(id: Long): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateBalance(accountId: Long, newBalance: Double) {
        accountDao.updateBalance(accountId, newBalance)
    }

    override suspend fun seedDefaultAccounts() {
        if (accountDao.getAccountCount() == 0) {
            accountDao.insertAccounts(ImariDatabase.DEFAULT_ACCOUNTS)
        }
    }
}
