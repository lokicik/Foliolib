package com.foliolib.app.domain.model

data class Statistics(
    val totalBooksRead: Int,
    val totalPagesRead: Int,
    val totalReadingTime: Long, // minutes
    val averagePagesPerDay: Float,
    val booksReadThisYear: Int,
    val booksReadThisMonth: Int,
    val favoriteGenres: List<GenreStats> = emptyList(),
    val favoriteAuthors: List<AuthorStats> = emptyList(),
    val readingTrend: List<TrendData> = emptyList()
)

data class GenreStats(
    val genre: String,
    val count: Int,
    val percentage: Float
)

data class AuthorStats(
    val author: String,
    val count: Int
)

data class TrendData(
    val date: String,
    val booksRead: Int,
    val pagesRead: Int
)
