package com.example.myapplication.ui.edit

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentFormViewModelTest {

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
    fun `saveDocument with blank title fails validation`() = runTest {
        val viewModel = DocumentFormViewModel(fakeRepository)

        viewModel.saveDocument()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Title is required", state.titleError)
        assertFalse(state.isSaved)
    }

    @Test
    fun `saveDocument with valid title succeeds and inserts document`() = runTest {
        val viewModel = DocumentFormViewModel(fakeRepository, initialImagePath = "/path/to/scan.jpg")

        viewModel.onTitleChanged("Driver's License")
        viewModel.onCategoryChanged(DocumentCategory.DRIVER_LICENSE)
        viewModel.onDocumentNumberChanged("DL987654321")
        viewModel.onIssuerChanged("DMV NY")
        viewModel.onNotesChanged("Valid for 5 years")

        var savedId: Long? = null
        viewModel.saveDocument { id -> savedId = id }

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertNotNull(savedId)

        val insertedDoc = fakeRepository.getDocumentByIdDirect(savedId!!)
        assertNotNull(insertedDoc)
        assertEquals("Driver's License", insertedDoc?.title)
        assertEquals(DocumentCategory.DRIVER_LICENSE, insertedDoc?.category)
        assertEquals("DL987654321", insertedDoc?.documentNumber)
        assertEquals("/path/to/scan.jpg", insertedDoc?.imagePath)
    }

    @Test
    fun `loadDocument loads existing document into form`() = runTest {
        val docId = fakeRepository.insertDocument(
            Document(
                title = "Passport",
                documentNumber = "P123456",
                category = DocumentCategory.PASSPORT,
                issuer = "US State Dept",
                notes = "Personal passport"
            )
        )

        val viewModel = DocumentFormViewModel(fakeRepository, documentId = docId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Passport", state.title)
        assertEquals(DocumentCategory.PASSPORT, state.category)
        assertEquals("P123456", state.documentNumber)
        assertEquals("US State Dept", state.issuer)
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
