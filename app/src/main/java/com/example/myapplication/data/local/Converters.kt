package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.domain.model.DocumentCategory
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDocumentCategory(category: DocumentCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toDocumentCategory(value: String?): DocumentCategory? {
        return value?.let { DocumentCategory.fromString(it) }
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
