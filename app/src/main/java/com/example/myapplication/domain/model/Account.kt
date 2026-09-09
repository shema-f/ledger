package com.example.myapplication.domain.model

data class Account(
    val id: Long = 0,
    val name: String,
    val accountType: AccountType,
    val accountNumberOrPhone: String? = null,
    val currentBalance: Double,
    val currency: String = "RWF",
    val isDefault: Boolean = false
)
