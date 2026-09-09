package com.example.myapplication.data.repository

import com.example.myapplication.data.local.entity.DocumentEntity
import com.example.myapplication.domain.model.Document

fun DocumentEntity.toDomain(): Document {
    return Document(
        id = id,
        title = title,
        documentNumber = documentNumber,
        category = category,
        issueDate = issueDate,
        expiryDate = expiryDate,
        issuer = issuer,
        notes = notes,
        imagePath = imagePath,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Document.toEntity(): DocumentEntity {
    return DocumentEntity(
        id = id,
        title = title,
        documentNumber = documentNumber,
        category = category,
        issueDate = issueDate,
        expiryDate = expiryDate,
        issuer = issuer,
        notes = notes,
        imagePath = imagePath,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<DocumentEntity>.toDomainList(): List<Document> {
    return map { it.toDomain() }
}
