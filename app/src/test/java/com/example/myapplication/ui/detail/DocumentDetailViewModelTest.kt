package com.example.myapplication.ui.detail

import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import com.example.myapplication.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentDetailViewModelTest {

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
    fun `loadDocument loads document and populates state`() = runTest {
        val docId = fakeRepository.insertDocument(
            Document(
                title = "Driver's License",
                documentNumber = "DL123456",
                category = DocumentCategory.DRIVER_LICENSE,
                issuer = "State DMV"
            )
        )

        val viewModel = DocumentDetailViewModel(fakeRepository, docId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.document)
        assertEquals("Driver's License", state.document?.title)
        assertNotNull(state.qrCodePayload)
    }

    @Test
    fun `toggleFavorite toggles favorite status in repository`() = runTest {
        val docId = fakeRepository.insertDocument(
            Document(
                title = "Passport",
                documentNumber = "P987654",
                category = DocumentCategory.PASSPORT,
                isFavorite = false
            )
        )

        val viewModel = DocumentDetailViewModel(fakeRepository, docId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedDoc = fakeRepository.getDocumentByIdDirect(docId)
        assertTrue(updatedDoc?.isFavorite == true)
    }

    @Test
    fun `deleteDocument removes document from repository`() = runTest {
        val docId = fakeRepository.insertDocument(
            Document(
                title = "Health Card",
                documentNumber = "HC111222",
                category = DocumentCategory.HEALTH_CARD
            )
        )

        val viewModel = DocumentDetailViewModel(fakeRepository, docId)
        testDispatcher.scheduler.advanceUntilIdle()

        var onDeletedCalled = false
        viewModel.deleteDocument(onDeleted = { onDeletedCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(onDeletedCalled)
        assertTrue(viewModel.uiState.value.isDeleted)
        assertNull(fakeRepository.getDocumentByIdDirect(docId))
    }

    private class FakeDocumentRepository : DocumentRepository {
        private val documentsFlow = MutableStateFlow<List<Document>>(emptyList())
        private var nextId = 1L

        override fun getAllDocuments(): Flow<List<Document>> = documentsFlow
        override fun getDocumentById(id: Long): Flow<Document?> = documentsFlow.map { list -> list.find { it.id == id } }
        override suspend fun getDocumentByIdDirect(id: Long): Document? = documentsFlow.value.find { it.id == id }
        override fun searchDocuments(query: String): Flow<List<Document>> = documentsFlow.map { list -> list.filter { it.title.contains(query, ignoreCase = true) } }
        override fun getDocumentsByCategory(category: DocumentCategory): Flow<List<Document>> = documentsFlow.map { list -> list.filter { it.category == category } }
        override fun getFavoriteDocuments(): Flow<List<Document>> = documentsFlow.map { list -> list.filter { it.isFavorite } }

        override suspend fun insertDocument(document: Document): Long {
            val id = if (document.id == 0L) nextId++ else document.id
            val newDoc = document.copy(id = id)
            documentsFlow.value = documentsFlow.value + newDoc
            return id
        }

        override suspend fun updateDocument(document: Document) {
            val list = documentsFlow.value.toMutableList()
            val index = list.indexOfFirst { it.id == document.id }
            if (index >= 0) {
                list[index] = document
                documentsFlow.value = list
            }
        }

        override suspend fun deleteDocument(document: Document) {
            documentsFlow.value = documentsFlow.value.filterNot { it.id == document.id }
        }

        override suspend fun deleteDocumentById(id: Long) {
            documentsFlow.value = documentsFlow.value.filterNot { it.id == id }
        }

        override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
            val list = documentsFlow.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index >= 0) {
                list[index] = list[index].copy(isFavorite = isFavorite)
                documentsFlow.value = list
            }
        }
    }
}
