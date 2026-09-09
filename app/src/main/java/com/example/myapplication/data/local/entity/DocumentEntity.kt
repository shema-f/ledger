package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.DocumentCategory

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val documentNumber: String,
    val category: DocumentCategory,
    val issueDate: Long? = null,
    val expiryDate: Long? = null,
    val issuer: String = "",
    val notes: String = "",
    val imagePath: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
