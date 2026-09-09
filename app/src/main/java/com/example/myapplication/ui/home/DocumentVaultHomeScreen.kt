package com.example.myapplication.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import com.example.myapplication.domain.model.ExpirationStatus
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentVaultHomeScreen(
    viewModel: VaultHomeViewModel,
    onScanClick: () -> Unit,
    onAddClick: () -> Unit,
    onDocumentClick: (Long) -> Unit,
    onLockClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ideni Vault",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onLockClick) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Lock Vault"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            SpeedDialFab(
                isExpanded = uiState.isSpeedDialExpanded,
                onToggle = { viewModel.toggleSpeedDial() },
                onScanClick = {
                    viewModel.setSpeedDialExpanded(false)
                    onScanClick()
                },
                onAddClick = {
                    viewModel.setSpeedDialExpanded(false)
                    onAddClick()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            SearchBarSection(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) }
            )

            // Category Filter Chips & Favorites Toggle
            FilterChipsSection(
                selectedCategory = uiState.selectedCategory,
                showFavoritesOnly = uiState.showFavoritesOnly,
                onCategorySelected = { viewModel.onCategorySelected(it) },
                onFavoritesToggled = { viewModel.onShowFavoritesOnlyToggled(it) }
            )

            // Expiration Alert Banner
            if (uiState.expiringCount > 0 || uiState.expiredCount > 0) {
                ExpirationAlertBanner(
                    expiringCount = uiState.expiringCount,
                    expiredCount = uiState.expiredCount
                )
            }

            // Document List or Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.documents.isEmpty()) {
                    EmptyVaultView(
                        searchQuery = uiState.searchQuery,
                        selectedCategory = uiState.selectedCategory,
                        showFavoritesOnly = uiState.showFavoritesOnly,
                        onScanClick = onScanClick,
                        onAddClick = onAddClick
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.documents, key = { it.id }) { doc ->
                            DocumentListItemCard(
                                document = doc,
                                onClick = { onDocumentClick(doc.id) },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(doc.id, doc.isFavorite)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search by title, ID, issuer, notes...") },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear Search")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FilterChipsSection(
    selectedCategory: DocumentCategory?,
    showFavoritesOnly: Boolean,
    onCategorySelected: (DocumentCategory?) -> Unit,
    onFavoritesToggled: (Boolean) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorites Chip
        item {
            FilterChip(
                selected = showFavoritesOnly,
                onClick = { onFavoritesToggled(!showFavoritesOnly) },
                label = { Text("Favorites") },
                leadingIcon = {
                    Icon(
                        imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorites",
                        modifier = Modifier.size(18.dp),
                        tint = if (showFavoritesOnly) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        // "All" Category Chip
        item {
            FilterChip(
                selected = selectedCategory == null && !showFavoritesOnly,
                onClick = {
                    onCategorySelected(null)
                    if (showFavoritesOnly) onFavoritesToggled(false)
                },
                label = { Text("All Docs") }
            )
        }

        // Category Chips
        items(DocumentCategory.entries) { cat ->
            FilterChip(
                selected = selectedCategory == cat,
                onClick = {
                    if (selectedCategory == cat) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(cat)
                    }
                },
                label = { Text(cat.displayName) }
            )
        }
    }
}

@Composable
private fun ExpirationAlertBanner(
    expiringCount: Int,
    expiredCount: Int
) {
    val message = buildString {
        if (expiredCount > 0) {
            append("$expiredCount EXPIRED document${if (expiredCount > 1) "s" else ""}")
        }
        if (expiringCount > 0) {
            if (isNotEmpty()) append(" & ")
            append("$expiringCount expiring soon")
        }
    }

    val isError = expiredCount > 0
    val bgColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
    val textColor = if (isError) Color(0xFFC62828) else Color(0xFFE65100)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Expiration Alert",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Attention required: $message",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SpeedDialFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onScanClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Manually Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onAddClick)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Add Manually",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Add Manually")
                    }
                }

                // Scan Document Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onScanClick)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Scan Document",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onScanClick,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Scan Document")
                    }
                }
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.Clear else Icons.Filled.Add,
                contentDescription = if (isExpanded) "Close actions" else "Add or scan document"
            )
        }
    }
}

@Composable
private fun DocumentListItemCard(
    document: Document,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon
            val imageFile = document.imagePath?.let { File(it) }
            if (imageFile != null && imageFile.exists() && imageFile.length() > 0) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = document.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                if (document.documentNumber.isNotBlank()) {
                    Text(
                        text = "ID: ${document.documentNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expiry status badge
                document.expiryDate?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    ExpirationBadge(status = document.expirationStatus)
                }
            }

            // Favorite Icon Button
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (document.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (document.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExpirationBadge(status: ExpirationStatus) {
    val (bgColor, textColor, labelText) = when (status) {
        ExpirationStatus.ACTIVE -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "Valid"
        )
        ExpirationStatus.EXPIRING_SOON -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Expiring Soon"
        )
        ExpirationStatus.EXPIRED -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            "Expired"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        contentColor = textColor
    ) {
        Text(
            text = labelText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun EmptyVaultView(
    searchQuery: String,
    selectedCategory: DocumentCategory?,
    showFavoritesOnly: Boolean,
    onScanClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val isFiltered = searchQuery.isNotEmpty() || selectedCategory != null || showFavoritesOnly

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.FolderSpecial,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isFiltered) "No Matching Documents" else "Your Vault is Empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFiltered) "Try adjusting your search query or filters to find your documents."
            else "Scan driver's licenses, passports, or ID cards to store them safely on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (!isFiltered) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onScanClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan First Document", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Enter Details Manually", fontSize = 16.sp)
            }
        }
    }
}
