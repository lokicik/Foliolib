package com.foliolib.app.presentation.screen.shelves

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.foliolib.app.R
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.foliolib.app.domain.model.Shelf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelvesScreen(
    onShelfClick: (String) -> Unit = {},
    viewModel: ShelvesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shelves_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.shelves_create))
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Default shelves section
                item {
                    Text(
                        text = stringResource(R.string.shelves_default),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(
                    uiState.shelves.filter { it.isDefault },
                    key = { it.id }
                ) { shelf ->
                    ShelfCard(
                        shelf = shelf,
                        onClick = { onShelfClick(shelf.id) },
                        onDelete = null // Can't delete default shelves
                    )
                }

                // Custom shelves section
                val customShelves = uiState.shelves.filter { !it.isDefault }
                if (customShelves.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.shelves_custom),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(
                        customShelves,
                        key = { it.id }
                    ) { shelf ->
                        ShelfCard(
                            shelf = shelf,
                            onClick = { onShelfClick(shelf.id) },
                            onDelete = { viewModel.deleteShelf(shelf.id) }
                        )
                    }
                }
            }
        }

        // Create Shelf Dialog
        if (uiState.showCreateDialog) {
            CreateShelfDialog(
                shelfName = uiState.newShelfName,
                shelfDescription = uiState.newShelfDescription,
                shelfColor = uiState.newShelfColor,
                onNameChange = { viewModel.updateNewShelfName(it) },
                onDescriptionChange = { viewModel.updateNewShelfDescription(it) },
                onColorChange = { viewModel.updateNewShelfColor(it) },
                onConfirm = { viewModel.createShelf() },
                onDismiss = { viewModel.hideCreateDialog() }
            )
        }
    }
}

@Composable
private fun ShelfCard(
    shelf: Shelf,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            try {
                                Color(shelf.color.toColorInt())
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = shelf.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    shelf.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = if (shelf.bookCount == 1) stringResource(R.string.common_books_count_singular) else stringResource(R.string.common_books_count, shelf.bookCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delete button for custom shelves
            if (onDelete != null) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.shelves_delete_shelf),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.shelves_delete)) },
            text = { Text(stringResource(R.string.shelves_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun CreateShelfDialog(
    shelfName: String,
    shelfDescription: String,
    shelfColor: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorOptions = listOf(
        "#8B5CF6" to "Violet",
        "#6366F1" to "Indigo",
        "#EC4899" to "Pink",
        "#10B981" to "Green",
        "#F59E0B" to "Amber",
        "#EF4444" to "Red",
        "#3B82F6" to "Blue",
        "#8B4513" to "Brown"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shelves_create)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = shelfName,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.shelves_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = shelfDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.shelves_description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Text(
                    text = stringResource(R.string.shelves_color_label),
                    style = MaterialTheme.typography.labelMedium
                )

                // Color picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    try {
                                        Color(color.toColorInt())
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                .clickable { onColorChange(color) }
                                .then(
                                    if (color == shelfColor) {
                                        Modifier.padding(4.dp)
                                    } else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = shelfName.isNotBlank()
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
