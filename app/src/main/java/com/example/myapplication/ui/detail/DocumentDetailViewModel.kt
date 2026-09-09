package com.example.myapplication.ui.detail

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.repository.DocumentRepository
import com.example.myapplication.util.QRCodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentDetailUiState(
    val isLoading: Boolean = true,
    val document: Document? = null,
    val qrCodeBitmap: Bitmap? = null,
    val qrCodePayload: String? = null,
    val isDeleted: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showFullImageDialog: Boolean = false,
    val showQrCodeDialog: Boolean = false,
    val errorMessage: String? = null
)

class DocumentDetailViewModel(
    private val repository: DocumentRepository,
    private val documentId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentDetailUiState())
    val uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    init {
        loadDocument()
    }

    fun loadDocument() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getDocumentById(documentId).collect { doc ->
                if (doc != null) {
                    val payload = QRCodeGenerator.generateVerificationPayload(doc)
                    val bitmap = try {
                        QRCodeGenerator.generateQrCode(payload, 512)
                    } catch (t: Throwable) {
                        null
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            document = doc,
                            qrCodePayload = payload,
                            qrCodeBitmap = bitmap
                        )
                    }
                } else if (!_uiState.value.isDeleted) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Document not found")
                    }
                }
            }
        }
    }

    fun toggleFavorite() {
        val currentDoc = _uiState.value.document ?: return
        viewModelScope.launch {
            repository.toggleFavorite(currentDoc.id, !currentDoc.isFavorite)
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmation = show) }
    }

    fun deleteDocument(onDeleted: () -> Unit = {}) {
        val currentDoc = _uiState.value.document ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteConfirmation = false, isDeleted = true) }
            repository.deleteDocumentById(currentDoc.id)
            onDeleted()
        }
    }

    fun setShowFullImageDialog(show: Boolean) {
        _uiState.update { it.copy(showFullImageDialog = show) }
    }

    fun setShowQrCodeDialog(show: Boolean) {
        _uiState.update { it.copy(showQrCodeDialog = show) }
    }

    class Factory(
        private val repository: DocumentRepository,
        private val documentId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DocumentDetailViewModel(repository, documentId) as T
        }
    }
}
