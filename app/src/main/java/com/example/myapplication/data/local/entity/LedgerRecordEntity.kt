package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.TransactionType

@Entity(tableName = "ledger_records")
data class LedgerRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long? = null,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val momoTxId: String? = null,
    val dueDate: Long? = null,
    val timestamp: Long
)
