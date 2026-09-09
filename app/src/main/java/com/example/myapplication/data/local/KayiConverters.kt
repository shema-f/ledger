package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.domain.model.TransactionType

class KayiConverters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let {
            try {
                enumValueOf<TransactionType>(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
