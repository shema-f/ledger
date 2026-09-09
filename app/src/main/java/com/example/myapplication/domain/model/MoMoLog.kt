package com.example.myapplication.domain.model

data class MoMoLog(
    val id: Long = 0,
    val senderName: String? = null,
    val senderPhone: String? = null,
    val amount: Double,
    val txId: String,
    val balanceAfter: Double? = null,
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isReconciled: Boolean = false
)
