package com.foliolib.app.presentation.screen.library

import com.foliolib.app.domain.model.Book

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val isGridView: Boolean = true,
    val sortBy: SortOption = SortOption.DATE_ADDED,
    val filterStatus: String? = null
)

enum class SortOption {
    TITLE,
    AUTHOR,
    DATE_ADDED,
    RATING
}
