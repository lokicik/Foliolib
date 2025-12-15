package com.foliolib.app.domain.model

data class ReadingSession(
    val id: String,
    val bookId: String,
    val startPage: Int,
    val endPage: Int,
    val startTime: Long,
    val endTime: Long,
    val duration: Long, // milliseconds
    val pagesRead: Int,
    val date: String // YYYY-MM-DD format
)
