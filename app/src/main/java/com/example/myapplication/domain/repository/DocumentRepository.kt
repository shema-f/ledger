package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<Document>>
    fun getDocumentById(id: Long): Flow<Document?>
    suspend fun getDocumentByIdDirect(id: Long): Document?
    fun searchDocuments(query: String): Flow<List<Document>>
    fun getDocumentsByCategory(category: DocumentCategory): Flow<List<Document>>
    fun getFavoriteDocuments(): Flow<List<Document>>
    suspend fun insertDocument(document: Document): Long
    suspend fun updateDocument(document: Document)
    suspend fun deleteDocument(document: Document)
    suspend fun deleteDocumentById(id: Long)
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
}
