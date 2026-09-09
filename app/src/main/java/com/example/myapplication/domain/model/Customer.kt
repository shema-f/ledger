package com.example.myapplication.domain.model

data class Customer(
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val nickname: String? = null,
    val totalDebt: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
