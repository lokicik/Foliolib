package com.foliolib.app.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.usecase.book.AddBookUseCase
import com.foliolib.app.domain.usecase.book.SearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val addBookUseCase: AddBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _events = Channel<SearchEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _query = MutableStateFlow("")

    init {
        // Debounced search
        viewModelScope.launch {
            _query
                .debounce(500) // Wait 500ms after user stops typing
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { query ->
                    searchBooks(query)
                }
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
        _uiState.update { it.copy(query = query, error = null) }
    }

    private fun searchBooks(query: String) {
        viewModelScope.launch {
            Timber.d("Searching for: $query")
            _uiState.update { it.copy(isLoading = true, error = null) }

            searchBooksUseCase(query)
                .onSuccess { books ->
                    Timber.d("Found ${books.size} books")
                    _uiState.update {
                        it.copy(
                            searchResults = books,
                            isLoading = false,
                            hasSearched = true,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "Search failed")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasSearched = true,
                            error = error.message ?: "Search failed"
                        )
                    }
                }
        }
    }

    fun addBookToLibrary(book: com.foliolib.app.domain.model.Book) {
        viewModelScope.launch {
            Timber.d("Adding book to library: ${book.title}")
            addBookUseCase(book)
                .onSuccess {
                    Timber.d("Book added successfully: ${book.title}")
                    _uiState.update { it.copy(addedBookIds = it.addedBookIds + book.id) }
                    _events.send(SearchEvent.BookAdded(book.title))
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to add book")
                    _events.send(SearchEvent.BookAddError(error.message ?: "Failed to add book"))
                }
        }
    }

    fun clearSearch() {
        _query.value = ""
        _uiState.update {
            SearchUiState()
        }
    }
}
