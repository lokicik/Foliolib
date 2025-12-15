package com.foliolib.app.presentation.screen.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foliolib.app.presentation.components.book.BookCover
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Session") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showNoteDialog() }) {
                        Icon(Icons.Default.Note, contentDescription = "Add Note")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.endSession(onComplete = onNavigateBack)
                },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text("Finish Reading") }
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Book Info
                uiState.book?.let { book ->
                    BookCover(
                        imageUrl = book.largeImageUrl,
                        contentDescription = book.title,
                        modifier = Modifier.size(width = 120.dp, height = 180.dp)
                    )

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (book.authors.isNotEmpty()) {
                        Text(
                            text = book.authors.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reading Timer Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Reading Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = formatElapsedTime(uiState.elapsedTimeMillis),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Page Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Current Page",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Page ${uiState.currentPage}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            uiState.book?.pageCount?.let { totalPages ->
                                Text(
                                    text = "of $totalPages",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Page Slider
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Slider(
                                value = uiState.currentPage.toFloat(),
                                onValueChange = { viewModel.updateCurrentPage(it.toInt()) },
                                valueRange = 0f..(uiState.book?.pageCount?.toFloat() ?: 1000f),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateCurrentPage(
                                            (uiState.currentPage - 1).coerceAtLeast(0)
                                        )
                                    }
                                ) {
                                    Text("-1")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateCurrentPage(
                                            (uiState.currentPage + 1).coerceAtMost(
                                                uiState.book?.pageCount ?: 1000
                                            )
                                        )
                                    }
                                ) {
                                    Text("+1")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateCurrentPage(
                                            (uiState.currentPage + 10).coerceAtMost(
                                                uiState.book?.pageCount ?: 1000
                                            )
                                        )
                                    }
                                ) {
                                    Text("+10")
                                }
                            }
                        }

                        // Progress Indicator
                        uiState.book?.let { book ->
                            if (book.pageCount != null && book.pageCount > 0) {
                                val progress = uiState.currentPage.toFloat() / book.pageCount
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}% complete",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }

        // Note Dialog
        if (uiState.showNoteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideNoteDialog() },
                title = { Text("Add Note") },
                text = {
                    OutlinedTextField(
                        value = uiState.noteContent,
                        onValueChange = { viewModel.updateNoteContent(it) },
                        label = { Text("Your note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.saveNote() },
                        enabled = uiState.noteContent.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideNoteDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun formatElapsedTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
