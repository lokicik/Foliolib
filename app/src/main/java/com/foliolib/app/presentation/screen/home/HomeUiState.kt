package com.foliolib.app.presentation.screen.home

import com.foliolib.app.domain.model.Book

data class HomeUiState(
    val currentlyReading: List<Book> = emptyList(),
    val totalBooks: Int = 0,
    val finishedBooks: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true
)
