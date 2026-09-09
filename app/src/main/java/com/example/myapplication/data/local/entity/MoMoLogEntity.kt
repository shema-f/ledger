package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "momo_logs",
    indices = [Index(value = ["txId"], unique = true)]
)
data class MoMoLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String? = null,
    val senderPhone: String? = null,
    val amount: Double,
    val txId: String,
    val balanceAfter: Double? = null,
    val rawText: String,
    val timestamp: Long,
    val isReconciled: Boolean = false
)
