package com.example.myapplication.ui.home

import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import com.example.myapplication.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class VaultHomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDocumentRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDocumentRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchQuery filters documents by title`() = runTest {
        fakeRepository.insertDocument(Document(title = "State Driver License", documentNumber = "DL1", category = DocumentCategory.DRIVER_LICENSE))
        fakeRepository.insertDocument(Document(title = "Passport Card", documentNumber = "P1", category = DocumentCategory.PASSPORT))

        val viewModel = VaultHomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.documents.size)

        viewModel.onSearchQueryChanged("Passport")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.uiState.value.documents
        assertEquals(1, filtered.size)
        assertEquals("Passport Card", filtered.first().title)
    }

    @Test
    fun `category selection filters documents by category`() = runTest {
        fakeRepository.insertDocument(Document(title = "State DL", documentNumber = "DL1", category = DocumentCategory.DRIVER_LICENSE))
        fakeRepository.insertDocument(Document(title = "Passport", documentNumber = "P1", category = DocumentCategory.PASSPORT))

        val viewModel = VaultHomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected(DocumentCategory.PASSPORT)
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.uiState.value.documents
        assertEquals(1, filtered.size)
        assertEquals(DocumentCategory.PASSPORT, filtered.first().category)
    }

    @Test
    fun `favorites filter shows only favorite documents`() = runTest {
        fakeRepository.insertDocument(Document(title = "Doc 1", documentNumber = "1", category = DocumentCategory.ID_CARD, isFavorite = true))
        fakeRepository.insertDocument(Document(title = "Doc 2", documentNumber = "2", category = DocumentCategory.ID_CARD, isFavorite = false))

        val viewModel = VaultHomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onShowFavoritesOnlyToggled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.uiState.value.documents
        assertEquals(1, filtered.size)
        assertTrue(filtered.first().isFavorite)
    }

    @Test
    fun `expiring and expired counts are calculated correctly`() = runTest {
        val now = System.currentTimeMillis()
        val expired = now - TimeUnit.DAYS.toMillis(5)
        val expiringSoon = now + TimeUnit.DAYS.toMillis(10)
        val active = now + TimeUnit.DAYS.toMillis(100)

        fakeRepository.insertDocument(Document(title = "Expired Doc", documentNumber = "1", category = DocumentCategory.ID_CARD, expiryDate = expired))
        fakeRepository.insertDocument(Document(title = "Expiring Doc", documentNumber = "2", category = DocumentCategory.ID_CARD, expiryDate = expiringSoon))
        fakeRepository.insertDocument(Document(title = "Active Doc", documentNumber = "3", category = DocumentCategory.ID_CARD, expiryDate = active))

        val viewModel = VaultHomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.expiredCount)
        assertEquals(1, state.expiringCount)
    }

    private class FakeDocumentRepository : DocumentRepository {
        private val documents = mutableListOf<Document>()
        private var nextId = 1L

        override fun getAllDocuments(): Flow<List<Document>> = flowOf(documents)
        override fun getDocumentById(id: Long): Flow<Document?> = flowOf(documents.find { it.id == id })
        override suspend fun getDocumentByIdDirect(id: Long): Document? = documents.find { it.id == id }
        override fun searchDocuments(query: String): Flow<List<Document>> = flowOf(documents.filter { it.title.contains(query, ignoreCase = true) })
        override fun getDocumentsByCategory(category: DocumentCategory): Flow<List<Document>> = flowOf(documents.filter { it.category == category })
        override fun getFavoriteDocuments(): Flow<List<Document>> = flowOf(documents.filter { it.isFavorite })

        override suspend fun insertDocument(document: Document): Long {
            val id = if (document.id == 0L) nextId++ else document.id
            val newDoc = document.copy(id = id)
            documents.add(newDoc)
            return id
        }

        override suspend fun updateDocument(document: Document) {
            val index = documents.indexOfFirst { it.id == document.id }
            if (index >= 0) {
                documents[index] = document
            }
        }

        override suspend fun deleteDocument(document: Document) {
            documents.removeAll { it.id == document.id }
        }

        override suspend fun deleteDocumentById(id: Long) {
            documents.removeAll { it.id == id }
        }

        override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
            val index = documents.indexOfFirst { it.id == id }
            if (index >= 0) {
                documents[index] = documents[index].copy(isFavorite = isFavorite)
            }
        }
    }
}
