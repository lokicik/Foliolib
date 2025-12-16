package com.foliolib.app.presentation.screen.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToManualEntry: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            scope.launch {
                when (event) {
                    is SearchEvent.BookAdded -> {
                        Toast.makeText(context, "${event.bookTitle} added to library", Toast.LENGTH_SHORT).show()
                        snackbarHostState.showSnackbar(
                            message = "${event.bookTitle} added to library",
                            duration = SnackbarDuration.Short
                        )
                    }
                    is SearchEvent.BookAddError -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        // Search bar
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search books...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Manual entry button
        OutlinedButton(
            onClick = onNavigateToManualEntry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Can't find your book? Add it manually")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                ErrorMessage(error = uiState.error!!)
            }

            uiState.searchResults.isEmpty() && uiState.hasSearched -> {
                EmptySearchResults()
            }

            uiState.searchResults.isNotEmpty() -> {
                SearchResults(
                    books = uiState.searchResults,
                    addedBookIds = uiState.addedBookIds,
                    onAddBook = { viewModel.addBookToLibrary(it) }
                )
            }

            else -> {
                SearchPrompt()
            }
        }
        }
    }
}

@Composable
private fun SearchResults(
    books: List<com.foliolib.app.domain.model.Book>,
    addedBookIds: Set<String>,
    onAddBook: (com.foliolib.app.domain.model.Book) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books) { book ->
            BookSearchItem(
                book = book,
                isAdded = addedBookIds.contains(book.id),
                onAddClick = { onAddBook(book) }
            )
        }
    }
}

@Composable
private fun BookSearchItem(
    book: com.foliolib.app.domain.model.Book,
    isAdded: Boolean = false,
    onAddClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessHigh
        ),
        finishedListener = { isAnimating = false }
    )

    val buttonContainerColor by animateColorAsState(
        targetValue = if (isAdded) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Book cover
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = book.title,
                modifier = Modifier.size(width = 60.dp, height = 90.dp),
                contentScale = ContentScale.Crop
            )

            // Book info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (book.authors.isNotEmpty()) {
                    Text(
                        text = book.authors.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    book.publishedDate?.let {
                        Text(
                            text = it.take(4), // Show just the year
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    book.pageCount?.let {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$it pages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Add button
            FilledIconButton(
                onClick = {
                    if (!isAdded) {
                        isAnimating = true
                        onAddClick()
                    }
                },
                enabled = !isAdded,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = buttonContainerColor)
            ) {
                AnimatedContent(targetState = isAdded, label = "AddedIcon") { added ->
                    Icon(
                        imageVector = if (added) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (added) "Added" else "Add to library"
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "😕",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Search failed",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySearchResults() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "No books found",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchPrompt() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Search for books",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Enter a title, author, or ISBN",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
