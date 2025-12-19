package com.foliolib.app.presentation.screen.editbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foliolib.app.R
import androidx.compose.ui.res.stringResource
import com.foliolib.app.domain.model.BookCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_book_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveBook(onSuccess = onNavigateBack) },
                        enabled = !uiState.isSaving && !uiState.isLoading
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.edit_book_save)
                            )
                        }
                    }
                }
            )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Required fields
                Text(
                    text = stringResource(R.string.manual_entry_required),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text(stringResource(R.string.manual_entry_book_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.titleError != null,
                    supportingText = {
                        uiState.titleError?.let { Text(stringResource(it)) }
                    }
                )

                OutlinedTextField(
                    value = uiState.author,
                    onValueChange = { viewModel.updateAuthor(it) },
                    label = { Text(stringResource(R.string.manual_entry_author)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.authorError != null,
                    supportingText = {
                        uiState.authorError?.let { Text(stringResource(it)) }
                    }
                )

                Divider()

                // Optional fields
                Text(
                    text = stringResource(R.string.manual_entry_optional),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = uiState.isbn,
                    onValueChange = { viewModel.updateIsbn(it) },
                    label = { Text(stringResource(R.string.manual_entry_isbn)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.publisher,
                    onValueChange = { viewModel.updatePublisher(it) },
                    label = { Text(stringResource(R.string.manual_entry_publisher)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.publishedDate,
                    onValueChange = { viewModel.updatePublishedDate(it) },
                    label = { Text(stringResource(R.string.manual_entry_published_date_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.pageCount,
                    onValueChange = { viewModel.updatePageCount(it) },
                    label = { Text(stringResource(R.string.manual_entry_page_count)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text(stringResource(R.string.manual_entry_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                OutlinedTextField(
                    value = uiState.categories,
                    onValueChange = { viewModel.updateCategories(it) },
                    label = { Text(stringResource(R.string.manual_entry_categories)) },
                    placeholder = { Text(stringResource(R.string.manual_entry_categories_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Book Condition Selector
                var conditionExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = conditionExpanded,
                    onExpandedChange = { conditionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.condition?.name?.replace("_", " ") ?: stringResource(R.string.manual_entry_select_condition),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.manual_entry_condition)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = conditionExpanded,
                        onDismissRequest = { conditionExpanded = false }
                    ) {
                        BookCondition.values().forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition.name.replace("_", " ")) },
                                onClick = {
                                    viewModel.updateCondition(condition)
                                    conditionExpanded = false
                                }
                            )
                        }
                    }
                }

                // Error message
                if (uiState.error != null) {
                    Text(
                        text = stringResource(uiState.error!!),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
