package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus

@Entity(tableName = "loans_debts")
data class LoanDebtEntity(
    @PrimaryKey(autoGenerate = true)
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
