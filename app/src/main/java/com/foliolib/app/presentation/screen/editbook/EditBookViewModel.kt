package com.foliolib.app.presentation.screen.editbook

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.R
import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.BookCondition
import com.foliolib.app.domain.usecase.book.GetBookDetailsUseCase
import com.foliolib.app.domain.usecase.book.UpdateBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditBookUiState(
    val isLoading: Boolean = true,
    val bookId: String = "",
    val title: String = "",
    val titleError: Int? = null,
    val author: String = "",
    val authorError: Int? = null,
    val isbn: String = "",
    val publisher: String = "",
    val publishedDate: String = "",
    val pageCount: String = "",
    val description: String = "",
    val categories: String = "",
    val condition: BookCondition? = null,
    val isSaving: Boolean = false,
    val error: Int? = null,
    val originalBook: Book? = null
)

@HiltViewModel
class EditBookViewModel @Inject constructor(
    private val getBookDetailsUseCase: GetBookDetailsUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])
    private val _uiState = MutableStateFlow(EditBookUiState())
    val uiState: StateFlow<EditBookUiState> = _uiState.asStateFlow()

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            getBookDetailsUseCase(bookId).collect { book ->
                if (book != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bookId = book.id,
                            title = book.title,
                            author = book.authors.joinToString(", "),
                            isbn = book.isbn ?: book.isbn13 ?: "",
                            publisher = book.publisher ?: "",
                            publishedDate = book.publishedDate ?: "",
                            pageCount = book.pageCount.toString(),
                            description = book.description ?: "",
                            categories = book.categories.joinToString(", "),
                            condition = book.condition,
                            originalBook = book
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = R.string.error_book_not_found) }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = if (title.isBlank()) R.string.manual_entry_error_title else null
            )
        }
    }

    fun updateAuthor(author: String) {
        _uiState.update {
            it.copy(
                author = author,
                authorError = if (author.isBlank()) R.string.manual_entry_error_author else null
            )
        }
    }

    fun updateIsbn(isbn: String) {
        _uiState.update { it.copy(isbn = isbn) }
    }

    fun updatePublisher(publisher: String) {
        _uiState.update { it.copy(publisher = publisher) }
    }

    fun updatePublishedDate(date: String) {
        _uiState.update { it.copy(publishedDate = date) }
    }

    fun updatePageCount(pageCount: String) {
        _uiState.update { it.copy(pageCount = pageCount) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCategories(categories: String) {
        _uiState.update { it.copy(categories = categories) }
    }

    fun updateCondition(condition: BookCondition) {
        _uiState.update { it.copy(condition = condition) }
    }

    fun saveBook(onSuccess: () -> Unit) {
        val state = _uiState.value
        val originalBook = state.originalBook ?: return

        // Validate required fields
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = R.string.manual_entry_error_title) }
            return
        }

        if (state.author.isBlank()) {
            _uiState.update { it.copy(authorError = R.string.manual_entry_error_author) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val updatedBook = originalBook.copy(
                title = state.title.trim(),
                authors = state.author.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                isbn = if (state.isbn.length == 10) state.isbn else null,
                isbn13 = if (state.isbn.length == 13) state.isbn else null,
                publisher = state.publisher.trim().ifBlank { null },
                publishedDate = state.publishedDate.trim().ifBlank { null },
                pageCount = state.pageCount.toIntOrNull() ?: 0,
                description = state.description.trim().ifBlank { null },
                categories = state.categories.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                condition = state.condition
            )

            updateBookUseCase(updatedBook)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false, error = R.string.search_error) }
                }
        }
    }
}
