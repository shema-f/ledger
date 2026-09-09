package com.example.myapplication.domain.model

data class Transaction(
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
