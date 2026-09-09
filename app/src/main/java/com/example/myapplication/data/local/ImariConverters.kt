package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.model.DebtDirection
import com.example.myapplication.domain.model.DebtStatus
import com.example.myapplication.domain.model.TransactionType

class ImariConverters {
    @TypeConverter
    fun fromAccountType(type: AccountType?): String? = type?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? = value?.let {
        runCatching { enumValueOf<AccountType>(it) }.getOrNull()
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? = type?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let {
        runCatching { enumValueOf<TransactionType>(it) }.getOrNull()
    }

    @TypeConverter
    fun fromDebtDirection(direction: DebtDirection?): String? = direction?.name

    @TypeConverter
    fun toDebtDirection(value: String?): DebtDirection? = value?.let {
        runCatching { enumValueOf<DebtDirection>(it) }.getOrNull()
    }

    @TypeConverter
    fun fromDebtStatus(status: DebtStatus?): String? = status?.name

    @TypeConverter
    fun toDebtStatus(value: String?): DebtStatus? = value?.let {
        runCatching { enumValueOf<DebtStatus>(it) }.getOrNull()
    }
}
