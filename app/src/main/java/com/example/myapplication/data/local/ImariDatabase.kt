package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.CustomerDao
import com.example.myapplication.data.local.dao.LedgerRecordDao
import com.example.myapplication.data.local.dao.LoanDebtDao
import com.example.myapplication.data.local.dao.MoMoLogDao
import com.example.myapplication.data.local.dao.ProductDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.CustomerEntity
import com.example.myapplication.data.local.entity.LedgerRecordEntity
import com.example.myapplication.data.local.entity.LoanDebtEntity
import com.example.myapplication.data.local.entity.MoMoLogEntity
import com.example.myapplication.data.local.entity.ProductEntity
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.domain.model.AccountType
import java.util.concurrent.Executors

@Database(
    entities = [
        AccountEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        LoanDebtEntity::class,
        CustomerEntity::class,
        LedgerRecordEntity::class,
        MoMoLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ImariConverters::class)
abstract class ImariDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun loanDebtDao(): LoanDebtDao
    abstract fun customerDao(): CustomerDao
    abstract fun ledgerRecordDao(): LedgerRecordDao
    abstract fun moMoLogDao(): MoMoLogDao

    companion object {
        @Volatile
        private var INSTANCE: ImariDatabase? = null

        val DEFAULT_ACCOUNTS = listOf(
            AccountEntity(name = "MTN MoMo", accountType = AccountType.MOBILE_MONEY, currentBalance = 0.0, isDefault = true),
            AccountEntity(name = "Bank of Kigali (BK)", accountType = AccountType.BANK, currentBalance = 0.0, isDefault = false),
            AccountEntity(name = "Airtel Money", accountType = AccountType.MOBILE_MONEY, currentBalance = 0.0, isDefault = false),
            AccountEntity(name = "I&M Bank", accountType = AccountType.BANK, currentBalance = 0.0, isDefault = false),
            AccountEntity(name = "Equity Bank", accountType = AccountType.BANK, currentBalance = 0.0, isDefault = false),
            AccountEntity(name = "BPR", accountType = AccountType.BANK, currentBalance = 0.0, isDefault = false),
            AccountEntity(name = "Urwego SACCO", accountType = AccountType.SACCO, currentBalance = 0.0, isDefault = false),
            AccountEntity(name = "Main Cash Register", accountType = AccountType.CASH, currentBalance = 0.0, isDefault = false)
        )

        fun getInstance(context: Context): ImariDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImariDatabase::class.java,
                    "imari_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Executors.newSingleThreadExecutor().execute {
                            DEFAULT_ACCOUNTS.forEach { account ->
                                db.execSQL(
                                    "INSERT INTO accounts (name, accountType, accountNumberOrPhone, currentBalance, currency, isDefault) VALUES (?, ?, ?, ?, ?, ?)",
                                    arrayOf(
                                        account.name,
                                        account.accountType.name,
                                        account.accountNumberOrPhone,
                                        account.currentBalance,
                                        account.currency,
                                        if (account.isDefault) 1 else 0
                                    )
                                )
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
