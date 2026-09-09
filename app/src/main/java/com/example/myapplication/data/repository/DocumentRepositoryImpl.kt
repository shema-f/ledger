package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.DocumentDao
import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import com.example.myapplication.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments().map { entities -> entities.toDomainList() }
    }

    override fun getDocumentById(id: Long): Flow<Document?> {
        return documentDao.getDocumentById(id).map { entity -> entity?.toDomain() }
    }

    override suspend fun getDocumentByIdDirect(id: Long): Document? {
        return documentDao.getDocumentByIdDirect(id)?.toDomain()
    }

    override fun searchDocuments(query: String): Flow<List<Document>> {
        return documentDao.searchDocuments(query).map { entities -> entities.toDomainList() }
    }

    override fun getDocumentsByCategory(category: DocumentCategory): Flow<List<Document>> {
        return documentDao.getDocumentsByCategory(category).map { entities -> entities.toDomainList() }
    }

    override fun getFavoriteDocuments(): Flow<List<Document>> {
        return documentDao.getFavoriteDocuments().map { entities -> entities.toDomainList() }
    }

    override suspend fun insertDocument(document: Document): Long {
        return documentDao.insertDocument(document.toEntity())
    }

    override suspend fun updateDocument(document: Document) {
        documentDao.updateDocument(document.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun deleteDocument(document: Document) {
        documentDao.deleteDocument(document.toEntity())
    }

    override suspend fun deleteDocumentById(id: Long) {
        documentDao.deleteDocumentById(id)
    }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        documentDao.toggleFavorite(id, isFavorite, System.currentTimeMillis())
    }
}
