package com.foliolib.app.presentation.screen.search

import com.foliolib.app.domain.model.Book

data class SearchUiState(
    val query: String = "",
    val searchResults: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val addedBookIds: Set<String> = emptySet()
)

sealed class SearchEvent {
    data class BookAdded(val bookTitle: String) : SearchEvent()
    data class BookAddError(val message: String) : SearchEvent()
}
