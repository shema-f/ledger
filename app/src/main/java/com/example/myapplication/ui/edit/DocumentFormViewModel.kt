package com.example.myapplication.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import com.example.myapplication.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentFormUiState(
    val id: Long = 0L,
    val title: String = "",
    val titleError: String? = null,
    val documentNumber: String = "",
    val category: DocumentCategory = DocumentCategory.DRIVER_LICENSE,
    val issueDate: Long? = null,
    val expiryDate: Long? = null,
    val issuer: String = "",
    val notes: String = "",
    val imagePath: String? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedDocumentId: Long? = null,
    val errorMessage: String? = null
)

class DocumentFormViewModel(
    private val repository: DocumentRepository,
    initialImagePath: String? = null,
    documentId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentFormUiState(imagePath = initialImagePath))
    val uiState: StateFlow<DocumentFormUiState> = _uiState.asStateFlow()

    init {
        if (documentId != null && documentId > 0L) {
            loadDocument(documentId)
        }
    }

    fun loadDocument(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val doc = repository.getDocumentByIdDirect(id)
            if (doc != null) {
                _uiState.update {
                    it.copy(
                        id = doc.id,
                        title = doc.title,
                        documentNumber = doc.documentNumber,
                        category = doc.category,
                        issueDate = doc.issueDate,
                        expiryDate = doc.expiryDate,
                        issuer = doc.issuer,
                        notes = doc.notes,
                        imagePath = doc.imagePath ?: it.imagePath,
                        isFavorite = doc.isFavorite,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Document not found") }
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.update {
            it.copy(
                title = newTitle,
                titleError = if (newTitle.isNotBlank()) null else it.titleError
            )
        }
    }

    fun onDocumentNumberChanged(newNumber: String) {
        _uiState.update { it.copy(documentNumber = newNumber) }
    }

    fun onCategoryChanged(newCategory: DocumentCategory) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun onIssueDateChanged(newDate: Long?) {
        _uiState.update { it.copy(issueDate = newDate) }
    }

    fun onExpiryDateChanged(newDate: Long?) {
        _uiState.update { it.copy(expiryDate = newDate) }
    }

    fun onIssuerChanged(newIssuer: String) {
        _uiState.update { it.copy(issuer = newIssuer) }
    }

    fun onNotesChanged(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
    }

    fun onImagePathChanged(newPath: String?) {
        _uiState.update { it.copy(imagePath = newPath) }
    }

    fun onFavoriteToggled(isFav: Boolean) {
        _uiState.update { it.copy(isFavorite = isFav) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveDocument(onSuccess: (Long) -> Unit = {}) {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val document = Document(
                    id = currentState.id,
                    title = currentState.title.trim(),
                    documentNumber = currentState.documentNumber.trim(),
                    category = currentState.category,
                    issueDate = currentState.issueDate,
                    expiryDate = currentState.expiryDate,
                    issuer = currentState.issuer.trim(),
                    notes = currentState.notes.trim(),
                    imagePath = currentState.imagePath,
                    isFavorite = currentState.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )

                val id = if (document.id == 0L) {
                    repository.insertDocument(document)
                } else {
                    repository.updateDocument(document)
                    document.id
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        savedDocumentId = id
                    )
                }
                onSuccess(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.localizedMessage ?: "Failed to save document"
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: DocumentRepository,
        private val initialImagePath: String? = null,
        private val documentId: Long? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DocumentFormViewModel(repository, initialImagePath, documentId) as T
        }
    }
}
