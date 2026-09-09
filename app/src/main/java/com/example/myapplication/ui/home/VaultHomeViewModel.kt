package com.example.myapplication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import com.example.myapplication.domain.model.ExpirationStatus
import com.example.myapplication.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaultHomeUiState(
    val searchQuery: String = "",
    val selectedCategory: DocumentCategory? = null, // null = All
    val showFavoritesOnly: Boolean = false,
    val documents: List<Document> = emptyList(),
    val expiringCount: Int = 0,
    val expiredCount: Int = 0,
    val isSpeedDialExpanded: Boolean = false,
    val isLoading: Boolean = false
)

class VaultHomeViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<DocumentCategory?>(null)
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _isSpeedDialExpanded = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(VaultHomeUiState())
    val uiState: StateFlow<VaultHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllDocuments(),
                _searchQuery,
                _selectedCategory,
                _showFavoritesOnly,
                _isSpeedDialExpanded
            ) { allDocs, query, category, favoritesOnly, speedDial ->
                val expiringCount = allDocs.count { it.expirationStatus == ExpirationStatus.EXPIRING_SOON }
                val expiredCount = allDocs.count { it.expirationStatus == ExpirationStatus.EXPIRED }

                val filtered = allDocs.filter { doc ->
                    val matchesQuery = if (query.isBlank()) true else {
                        doc.title.contains(query, ignoreCase = true) ||
                                doc.documentNumber.contains(query, ignoreCase = true) ||
                                doc.issuer.contains(query, ignoreCase = true) ||
                                doc.notes.contains(query, ignoreCase = true)
                    }
                    val matchesCategory = category == null || doc.category == category
                    val matchesFavorite = !favoritesOnly || doc.isFavorite

                    matchesQuery && matchesCategory && matchesFavorite
                }

                VaultHomeUiState(
                    searchQuery = query,
                    selectedCategory = category,
                    showFavoritesOnly = favoritesOnly,
                    documents = filtered,
                    expiringCount = expiringCount,
                    expiredCount = expiredCount,
                    isSpeedDialExpanded = speedDial,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: DocumentCategory?) {
        _selectedCategory.value = category
    }

    fun onShowFavoritesOnlyToggled(showOnly: Boolean) {
        _showFavoritesOnly.value = showOnly
    }

    fun toggleSpeedDial() {
        _isSpeedDialExpanded.update { !it }
    }

    fun setSpeedDialExpanded(expanded: Boolean) {
        _isSpeedDialExpanded.value = expanded
    }

    fun toggleFavorite(documentId: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(documentId, !currentFavorite)
        }
    }

    class Factory(
        private val repository: DocumentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VaultHomeViewModel(repository) as T
        }
    }
}
