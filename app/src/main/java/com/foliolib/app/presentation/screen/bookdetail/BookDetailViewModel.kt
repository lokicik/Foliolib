package com.foliolib.app.presentation.screen.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.model.ReadingStatus
import com.foliolib.app.domain.usecase.book.DeleteBookUseCase
import com.foliolib.app.domain.usecase.book.GetBookDetailsUseCase
import com.foliolib.app.domain.usecase.book.UpdateBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBookDetailsUseCase: GetBookDetailsUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        loadBookDetails()
    }

    private fun loadBookDetails() {
        viewModelScope.launch {
            getBookDetailsUseCase(bookId)
                .catch { e ->
                    Timber.e(e, "Error loading book details")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load book"
                        )
                    }
                }
                .collect { book ->
                    _uiState.update {
                        it.copy(
                            book = book,
                            isLoading = false,
                            error = if (book == null) "Book not found" else null
                        )
                    }
                }
        }
    }

    fun updateReadingStatus(status: ReadingStatus) {
        val currentBook = _uiState.value.book ?: return

        viewModelScope.launch {
            val updatedBook = currentBook.copy(
                readingStatus = status,
                dateStarted = if (status == ReadingStatus.READING && currentBook.dateStarted == null) {
                    System.currentTimeMillis()
                } else {
                    currentBook.dateStarted
                },
                dateFinished = if (status == ReadingStatus.FINISHED) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            )

            updateBookUseCase(updatedBook)
                .onSuccess {
                    Timber.d("Reading status updated to $status")
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to update reading status")
                }
        }
    }

    fun updateCurrentPage(page: Int) {
        val currentBook = _uiState.value.book ?: return

        viewModelScope.launch {
            val updatedBook = currentBook.copy(currentPage = page)
            updateBookUseCase(updatedBook)
        }
    }

    fun updateRating(rating: Float) {
        val currentBook = _uiState.value.book ?: return

        viewModelScope.launch {
            val updatedBook = currentBook.copy(rating = rating)
            updateBookUseCase(updatedBook)
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteBook(onDeleted: () -> Unit) {
        val currentBook = _uiState.value.book ?: return

        viewModelScope.launch {
            deleteBookUseCase(currentBook)
                .onSuccess {
                    Timber.d("Book deleted successfully")
                    onDeleted()
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to delete book")
                }
        }
    }
}
