package com.foliolib.app.data.repository

import com.foliolib.app.core.di.IoDispatcher
import com.foliolib.app.data.local.dao.BookDao
import com.foliolib.app.data.local.dao.ReadingSessionDao
import com.foliolib.app.domain.model.Statistics
import com.foliolib.app.domain.repository.AuthorStats
import com.foliolib.app.domain.repository.GenreStats
import com.foliolib.app.domain.repository.ReadingTrend
import com.foliolib.app.domain.repository.StatisticsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val readingSessionDao: ReadingSessionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StatisticsRepository {

    override fun getOverallStatistics(): Flow<Statistics> {
        return combine(
            bookDao.getFinishedBooksCount(),
            readingSessionDao.getTotalPagesRead(),
            readingSessionDao.getTotalDaysRead(),
            bookDao.getBooksCount()
        ) { finishedBooks, totalPages, daysRead, totalBooks ->
            val averagePagesPerDay = if (daysRead > 0) {
                (totalPages ?: 0).toFloat() / daysRead
            } else 0f

            Statistics(
                totalBooksRead = finishedBooks,
                totalPagesRead = totalPages ?: 0,
                totalReadingTime = 0L, // Calculate from sessions
                averagePagesPerDay = averagePagesPerDay,
                booksReadThisYear = 0, // TODO: Calculate
                booksReadThisMonth = 0, // TODO: Calculate
                favoriteGenres = emptyList(),
                favoriteAuthors = emptyList(),
                readingTrend = emptyList()
            )
        }
    }

    override suspend fun getTotalBooksRead(): Result<Int> = withContext(ioDispatcher) {
        try {
            // For now, return finished books count
            Result.success(0) // Will be populated from Flow
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalPagesRead(): Result<Int> = withContext(ioDispatcher) {
        try {
            Result.success(0) // Will be populated from Flow
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalReadingTimeMinutes(): Result<Long> = withContext(ioDispatcher) {
        try {
            // Sum all reading session durations
            Result.success(0L)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAveragePagesPerDay(): Result<Int> = withContext(ioDispatcher) {
        try {
            Result.success(0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGenreDistribution(): Flow<List<GenreStats>> {
        return bookDao.getAllBooks().map { books ->
            // Extract genres from all books
            val genreMap = mutableMapOf<String, Int>()

            books.forEach { book ->
                val categories = try {
                    book.categoriesJson?.removeSurrounding("[", "]")
                        ?.split(",")
                        ?.map { it.trim().removeSurrounding("\"") }
                        ?.filter { it.isNotBlank() } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                categories.forEach { genre ->
                    genreMap[genre] = (genreMap[genre] ?: 0) + 1
                }
            }

            val total = genreMap.values.sum().toFloat()

            genreMap.map { (genre, count) ->
                GenreStats(
                    genre = genre,
                    count = count,
                    percentage = if (total > 0) (count / total) * 100 else 0f
                )
            }.sortedByDescending { it.count }
        }
    }

    override fun getReadingTrendsByMonth(months: Int): Flow<List<ReadingTrend>> {
        return readingSessionDao.getAllSessions().map { sessions ->
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

            // Create map of month -> stats
            val trendMap = mutableMapOf<String, Pair<Int, Int>>() // month -> (bookCount, pagesRead)

            sessions.forEach { session ->
                calendar.timeInMillis = session.startTime
                val monthKey = dateFormat.format(calendar.time)

                val current = trendMap[monthKey] ?: (0 to 0)
                trendMap[monthKey] = (current.first + 1) to (current.second + session.pagesRead)
            }

            trendMap.map { (month, stats) ->
                ReadingTrend(
                    month = month,
                    booksRead = stats.first,
                    pagesRead = stats.second
                )
            }.sortedBy { it.month }
        }
    }

    override fun getBooksReadPerMonth(): Flow<Int> {
        return flowOf(0) // TODO: Calculate current month
    }

    override fun getBooksReadThisYear(): Flow<Int> {
        return bookDao.getAllBooks().map { books ->
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            books.count { book ->
                book.dateFinished?.let { finishedDate ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = finishedDate
                    calendar.get(Calendar.YEAR) == currentYear
                } ?: false
            }
        }
    }

    override fun getFavoriteAuthors(limit: Int): Flow<List<AuthorStats>> {
        return bookDao.getAllBooks().map { books ->
            val authorMap = mutableMapOf<String, Int>()

            books.forEach { book ->
                val authors = try {
                    book.authorsJson.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotBlank() }
                } catch (e: Exception) {
                    emptyList()
                }

                authors.forEach { author ->
                    authorMap[author] = (authorMap[author] ?: 0) + 1
                }
            }

            authorMap.map { (author, count) ->
                AuthorStats(author = author, bookCount = count)
            }.sortedByDescending { it.bookCount }
                .take(limit)
        }
    }

    override suspend fun getAverageReadingSpeed(): Result<Double> = withContext(ioDispatcher) {
        try {
            // Calculate pages per hour from reading sessions
            val sessions = readingSessionDao.getAllSessionsSync()

            if (sessions.isEmpty()) {
                return@withContext Result.success(0.0)
            }

            val totalPages = sessions.sumOf { it.pagesRead }
            val totalHours = sessions.sumOf { it.duration } / (1000.0 * 60.0 * 60.0)

            val speed = if (totalHours > 0) totalPages / totalHours else 0.0

            Result.success(speed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
