package com.foliolib.app.presentation.screen.bookdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foliolib.app.R
import androidx.compose.ui.res.stringResource
import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.ReadingStatus
import com.foliolib.app.presentation.components.book.BookCover
import com.foliolib.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    onStartReading: (String) -> Unit = {},
    onViewNotes: (String) -> Unit = {},
    onEditBook: (String) -> Unit = {},
    onViewHistory: (String) -> Unit = {},
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.book_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { uiState.book?.let { onEditBook(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_book_title)
                        )
                    }
                    IconButton(onClick = { uiState.book?.let { onViewHistory(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.reading_history_title)
                        )
                    }
                    IconButton(onClick = { uiState.book?.let { onViewNotes(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = stringResource(R.string.book_detail_notes)
                        )
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.book_detail_delete)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    ErrorMessage(
                        error = uiState.error!!,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.book != null -> {
                    BookDetailContent(
                        book = uiState.book!!,
                        onUpdateStatus = { viewModel.updateReadingStatus(it) },
                        onUpdatePage = { viewModel.updateCurrentPage(it) },
                        onUpdateRating = { viewModel.updateRating(it) },
                        onStartReading = { onStartReading(uiState.book!!.id) }
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteDialog() },
                title = { Text(stringResource(R.string.book_detail_delete_title)) },
                text = { Text(stringResource(R.string.book_detail_delete_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteBook(onDeleted = onNavigateBack)
                        }
                    ) {
                        Text(stringResource(R.string.common_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun BookDetailContent(
    book: Book,
    onUpdateStatus: (ReadingStatus) -> Unit,
    onUpdatePage: (Int) -> Unit,
    onUpdateRating: (Float) -> Unit,
    onStartReading: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Book header with cover and title
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookCover(
                imageUrl = book.largeImageUrl ?: book.thumbnailUrl,
                contentDescription = book.title,
                modifier = Modifier.size(width = 120.dp, height = 180.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall
                )

                if (book.authors.isNotEmpty()) {
                    Text(
                        text = book.authors.joinToString(", "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                book.publishedDate?.let {
                    Text(
                        text = it.take(4),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                book.pageCount?.let {
                    Text(
                        text = stringResource(R.string.book_detail_page_count, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Reading Status Selector
        ReadingStatusSelector(
            currentStatus = book.readingStatus,
            onStatusChange = onUpdateStatus
        )

        // Start Reading Button
        if (book.readingStatus == ReadingStatus.READING) {
            Button(
                onClick = onStartReading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.book_detail_start_session))
            }
        }

        // Progress Section (if reading)
        if (book.readingStatus == ReadingStatus.READING && book.pageCount != null) {
            ProgressSection(
                currentPage = book.currentPage,
                totalPages = book.pageCount,
                onUpdatePage = onUpdatePage
            )
        }

        // Rating Section
        RatingSection(
            rating = book.rating,
            onRatingChange = onUpdateRating
        )

        // Description
        if (!book.description.isNullOrBlank()) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.manual_entry_description),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = book.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Book Details
        BookInfoCard(book = book)
    }
}

@Composable
private fun ReadingStatusSelector(
    currentStatus: ReadingStatus,
    onStatusChange: (ReadingStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.book_detail_reading_status),
                style = MaterialTheme.typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = when (currentStatus) {
                        ReadingStatus.WANT_TO_READ -> stringResource(R.string.status_want_to_read)
                        ReadingStatus.READING -> stringResource(R.string.status_reading)
                        ReadingStatus.FINISHED -> stringResource(R.string.status_finished)
                        ReadingStatus.DNF -> stringResource(R.string.status_dnf)
                        else -> stringResource(R.string.status_not_started)
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(
                        ReadingStatus.WANT_TO_READ to stringResource(R.string.status_want_to_read),
                        ReadingStatus.READING to stringResource(R.string.status_reading),
                        ReadingStatus.FINISHED to stringResource(R.string.status_finished),
                        ReadingStatus.DNF to stringResource(R.string.status_dnf)
                    ).forEach { (status, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onStatusChange(status)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(
    currentPage: Int,
    totalPages: Int,
    onUpdatePage: (Int) -> Unit
) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.book_detail_progress_title),
                style = MaterialTheme.typography.titleMedium
            )

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { currentPage.toFloat() / totalPages },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.book_detail_percent_complete, (currentPage.toFloat() / totalPages * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Page input
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { pageInput = it },
                    label = { Text(stringResource(R.string.book_detail_current_page)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                FilledTonalButton(
                    onClick = {
                        pageInput.toIntOrNull()?.let { page ->
                            if (page in 0..totalPages) {
                                onUpdatePage(page)
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.book_detail_update))
                }
            }

            Text(
                text = stringResource(R.string.book_detail_of_pages, totalPages),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RatingSection(
    rating: Float?,
    onRatingChange: (Float) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.book_detail_your_rating),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    IconButton(
                        onClick = { onRatingChange((index + 1).toFloat()) }
                    ) {
                        Icon(
                            imageVector = if (index < (rating?.toInt() ?: 0)) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = stringResource(R.string.book_detail_stars, index + 1),
                            tint = if (index < (rating?.toInt() ?: 0)) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookInfoCard(book: Book) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.book_detail_info_title),
                style = MaterialTheme.typography.titleMedium
            )

            book.publisher?.let {
                InfoRow(label = stringResource(R.string.book_detail_publisher), value = it)
            }

            book.isbn?.let {
                InfoRow(label = stringResource(R.string.book_detail_isbn), value = it)
            }

            book.isbn13?.let {
                InfoRow(label = stringResource(R.string.book_detail_isbn13), value = it)
            }

            if (book.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.book_detail_categories),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    book.categories.forEach { category ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(category) }
                        )
                    }
                }
            }

            book.language?.let {
                InfoRow(label = stringResource(R.string.book_detail_language), value = it)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun ErrorMessage(error: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "😕",
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = stringResource(R.string.common_error),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
