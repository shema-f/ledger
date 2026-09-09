package com.example.myapplication.domain.model

data class LedgerRecord(
    val id: Long = 0,
    val customerId: Long? = null,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val momoTxId: String? = null,
    val dueDate: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
