package com.foliolib.app.domain.repository

import com.foliolib.app.domain.model.Statistics
import kotlinx.coroutines.flow.Flow

data class GenreStats(
    val genre: String,
    val count: Int,
    val percentage: Float
)

data class ReadingTrend(
    val month: String,
    val booksRead: Int,
    val pagesRead: Int
)

data class AuthorStats(
    val author: String,
    val bookCount: Int
)

interface StatisticsRepository {
    // Overall statistics
    fun getOverallStatistics(): Flow<Statistics>
    suspend fun getTotalBooksRead(): Result<Int>
    suspend fun getTotalPagesRead(): Result<Int>
    suspend fun getTotalReadingTimeMinutes(): Result<Long>
    suspend fun getAveragePagesPerDay(): Result<Int>

    // Genre statistics
    fun getGenreDistribution(): Flow<List<GenreStats>>

    // Reading trends
    fun getReadingTrendsByMonth(months: Int = 12): Flow<List<ReadingTrend>>
    fun getBooksReadPerMonth(): Flow<Int>
    fun getBooksReadThisYear(): Flow<Int>

    // Author statistics
    fun getFavoriteAuthors(limit: Int = 10): Flow<List<AuthorStats>>

    // Reading speed
    suspend fun getAverageReadingSpeed(): Result<Double> // pages per hour
}
