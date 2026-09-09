package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.dao.CustomerDao
import com.example.myapplication.data.local.dao.LedgerRecordDao
import com.example.myapplication.data.local.dao.MoMoLogDao
import com.example.myapplication.data.local.entity.CustomerEntity
import com.example.myapplication.data.local.entity.LedgerRecordEntity
import com.example.myapplication.data.local.entity.MoMoLogEntity

@Database(
    entities = [
        CustomerEntity::class,
        LedgerRecordEntity::class,
        MoMoLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(KayiConverters::class)
abstract class KayiDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun ledgerRecordDao(): LedgerRecordDao
    abstract fun moMoLogDao(): MoMoLogDao

    companion object {
        @Volatile
        private var INSTANCE: KayiDatabase? = null

        fun getInstance(context: Context): KayiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KayiDatabase::class.java,
                    "kayi_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
