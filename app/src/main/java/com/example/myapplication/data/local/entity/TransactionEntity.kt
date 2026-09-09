package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val productId: Long? = null,
    val type: TransactionType,
    val amount: Double,
    val profit: Double = 0.0,
    val description: String,
    val referenceCode: String? = null,
    val timestamp: Long
)
