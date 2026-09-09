package com.example.myapplication.domain.model

data class LoanDebt(
    val id: Long = 0,
    val personOrInstitution: String,
    val phoneNumber: String? = null,
    val direction: DebtDirection,
    val principalAmount: Double,
    val remainingAmount: Double,
    val dueDate: Long? = null,
    val status: DebtStatus,
    val createdAt: Long
)
