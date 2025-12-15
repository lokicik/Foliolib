package com.foliolib.app.domain.model

data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val isbn: String? = null,
    val isbn13: String? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val categories: List<String> = emptyList(),
    val thumbnailUrl: String? = null,
    val largeImageUrl: String? = null,
    val language: String? = null,
    val condition: BookCondition? = null,
    val currentPage: Int = 0,
    val readingStatus: ReadingStatus = ReadingStatus.NONE,
    val rating: Float? = null,
    val notes: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateStarted: Long? = null,
    val dateFinished: Long? = null,
    val isManualEntry: Boolean = false
)
