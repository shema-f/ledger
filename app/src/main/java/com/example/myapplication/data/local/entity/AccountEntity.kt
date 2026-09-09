package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val accountType: AccountType,
    val accountNumberOrPhone: String? = null,
    val currentBalance: Double,
    val currency: String = "RWF",
    val isDefault: Boolean = false
)
