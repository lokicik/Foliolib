package com.foliolib.app.presentation.screen.shelves

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.Shelf
import com.foliolib.app.domain.repository.ShelfRepository
import com.foliolib.app.domain.usecase.shelf.GetBooksInShelfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShelfDetailUiState(
    val shelf: Shelf? = null,
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShelfDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBooksInShelfUseCase: GetBooksInShelfUseCase,
    private val shelfRepository: ShelfRepository
) : ViewModel() {

    private val shelfId: String = checkNotNull(savedStateHandle["shelfId"])

    private val _uiState = MutableStateFlow(ShelfDetailUiState())
    val uiState: StateFlow<ShelfDetailUiState> = _uiState.asStateFlow()

    init {
        loadShelf()
        loadBooks()
    }

    private fun loadShelf() {
        viewModelScope.launch {
            shelfRepository.getShelfById(shelfId).collect { shelf ->
                _uiState.update { state ->
                    state.copy(
                        shelf = shelf,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadBooks() {
        viewModelScope.launch {
            getBooksInShelfUseCase(shelfId).collect { books ->
                _uiState.update { it.copy(books = books) }
            }
        }
    }
}
