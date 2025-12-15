package com.foliolib.app.presentation.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.usecase.book.GetAllBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllBooksUseCase: GetAllBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            getAllBooksUseCase()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { books ->
                    _uiState.update { state ->
                        state.copy(
                            books = sortBooks(books, state.sortBy),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.update { state ->
            state.copy(
                sortBy = sortOption,
                books = sortBooks(state.books, sortOption)
            )
        }
    }

    private fun sortBooks(books: List<com.foliolib.app.domain.model.Book>, sortBy: SortOption): List<com.foliolib.app.domain.model.Book> {
        return when (sortBy) {
            SortOption.TITLE -> books.sortedBy { it.title }
            SortOption.AUTHOR -> books.sortedBy { it.authors.firstOrNull() }
            SortOption.DATE_ADDED -> books.sortedByDescending { it.dateAdded }
            SortOption.RATING -> books.sortedByDescending { it.rating ?: 0f }
        }
    }
}
