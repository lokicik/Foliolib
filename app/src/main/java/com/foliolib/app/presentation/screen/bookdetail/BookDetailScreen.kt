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
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { uiState.book?.let { onViewNotes(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "View notes"
                        )
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete book"
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
                title = { Text("Delete Book?") },
                text = { Text("This will permanently remove this book from your library.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteBook(onDeleted = onNavigateBack)
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                        Text("Cancel")
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
                        text = "$it pages",
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
                Text("Start Reading Session")
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
                        text = "Description",
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
                text = "Reading Status",
                style = MaterialTheme.typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = when (currentStatus) {
                        ReadingStatus.WANT_TO_READ -> "Want to Read"
                        ReadingStatus.READING -> "Currently Reading"
                        ReadingStatus.FINISHED -> "Finished"
                        ReadingStatus.DNF -> "Did Not Finish"
                        else -> "Not Started"
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
                        ReadingStatus.WANT_TO_READ to "Want to Read",
                        ReadingStatus.READING to "Currently Reading",
                        ReadingStatus.FINISHED to "Finished",
                        ReadingStatus.DNF to "Did Not Finish"
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
                text = "Reading Progress",
                style = MaterialTheme.typography.titleMedium
            )

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { currentPage.toFloat() / totalPages },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(currentPage.toFloat() / totalPages * 100).toInt()}% complete",
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
                    label = { Text("Current Page") },
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
                    Text("Update")
                }
            }

            Text(
                text = "of $totalPages pages",
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
                text = "Your Rating",
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
                            contentDescription = "${index + 1} stars",
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
                text = "Book Information",
                style = MaterialTheme.typography.titleMedium
            )

            book.publisher?.let {
                InfoRow(label = "Publisher", value = it)
            }

            book.isbn?.let {
                InfoRow(label = "ISBN", value = it)
            }

            book.isbn13?.let {
                InfoRow(label = "ISBN-13", value = it)
            }

            if (book.categories.isNotEmpty()) {
                InfoRow(
                    label = "Categories",
                    value = book.categories.joinToString(", ")
                )
            }

            book.language?.let {
                InfoRow(label = "Language", value = it)
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
            text = "Error",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
