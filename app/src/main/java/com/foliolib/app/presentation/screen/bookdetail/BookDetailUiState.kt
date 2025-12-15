package com.foliolib.app.presentation.screen.bookdetail

import com.foliolib.app.domain.model.Book

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)
