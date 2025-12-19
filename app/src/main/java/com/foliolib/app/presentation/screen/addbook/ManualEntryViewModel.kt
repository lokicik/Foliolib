package com.foliolib.app.presentation.screen.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.R
import com.foliolib.app.data.mapper.BookMapper
import com.foliolib.app.domain.model.BookCondition
import com.foliolib.app.domain.usecase.book.AddBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ManualEntryUiState(
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
    val isAdding: Boolean = false,
    val error: Int? = null
)

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val addBookUseCase: AddBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

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

    fun addBook(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validate required fields
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = R.string.manual_entry_error_title) }
            return
        }

        if (state.author.isBlank()) {
            _uiState.update { it.copy(authorError = R.string.manual_entry_error_author) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdding = true, error = null) }

            val book = BookMapper.createManualBook(
                title = state.title,
                authors = listOf(state.author),
                isbn = state.isbn.takeIf { it.isNotBlank() },
                publisher = state.publisher.takeIf { it.isNotBlank() },
                publishedDate = state.publishedDate.takeIf { it.isNotBlank() },
                pageCount = state.pageCount.toIntOrNull(),
                description = state.description.takeIf { it.isNotBlank() },
                categories = state.categories.split(",").map { it.trim() }.filter { it.isNotBlank() },
                condition = state.condition
            )

            addBookUseCase(book)
                .onSuccess {
                    Timber.d("Manual book added successfully")
                    onSuccess()
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to add manual book")
                    _uiState.update {
                        it.copy(
                            isAdding = false,
                            error = R.string.manual_entry_error_add
                        )
                    }
                }
        }
    }
}
