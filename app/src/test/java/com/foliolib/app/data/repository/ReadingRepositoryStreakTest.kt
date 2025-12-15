package com.foliolib.app.data.repository

import com.foliolib.app.data.local.dao.NoteDao
import com.foliolib.app.data.local.dao.ReadingSessionDao
import com.foliolib.app.data.local.entity.ReadingSessionEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingRepositoryStreakTest {

    private lateinit var readingSessionDao: ReadingSessionDao
    private lateinit var noteDao: NoteDao
    private lateinit var repository: ReadingRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Before
    fun setup() {
        readingSessionDao = mockk(relaxed = true)
        noteDao = mockk(relaxed = true)
        repository = ReadingRepositoryImpl(
            readingSessionDao,
            noteDao,
            testDispatcher
        )
    }

    @Test
    fun `getReadingStreak returns 0 when no sessions`() = runTest(testDispatcher) {
        // Given
        coEvery { readingSessionDao.getAllSessionsSync() } returns emptyList()

        // When
        val result = repository.getReadingStreak()

        // Then
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `getReadingStreak calculates streak for consecutive days`() = runTest(testDispatcher) {
        // Given
        val today = Calendar.getInstance()
        val sessions = listOf(
            createSession(today, 0), // Today
            createSession(today, -1), // Yesterday
            createSession(today, -2), // 2 days ago
            createSession(today, -3) // 3 days ago
        )
        coEvery { readingSessionDao.getAllSessionsSync() } returns sessions

        // When
        val result = repository.getReadingStreak()

        // Then
        assertEquals(4, result.getOrNull())
    }

    @Test
    fun `getReadingStreak breaks on non-consecutive days`() = runTest(testDispatcher) {
        // Given
        val today = Calendar.getInstance()
        val sessions = listOf(
            createSession(today, 0), // Today
            createSession(today, -1), // Yesterday
            createSession(today, -3) // Gap! (skipped day -2)
        )
        coEvery { readingSessionDao.getAllSessionsSync() } returns sessions

        // When
        val result = repository.getReadingStreak()

        // Then
        assertEquals(2, result.getOrNull()) // Only today and yesterday count
    }

    @Test
    fun `getReadingStreak handles single day streak`() = runTest(testDispatcher) {
        // Given
        val today = Calendar.getInstance()
        val sessions = listOf(createSession(today, 0)) // Only today
        coEvery { readingSessionDao.getAllSessionsSync() } returns sessions

        // When
        val result = repository.getReadingStreak()

        // Then
        assertEquals(1, result.getOrNull())
    }

    @Test
    fun `getReadingStreak handles multiple sessions same day`() = runTest(testDispatcher) {
        // Given
        val today = Calendar.getInstance()
        val sessions = listOf(
            createSession(today, 0), // Today session 1
            createSession(today, 0), // Today session 2
            createSession(today, -1) // Yesterday
        )
        coEvery { readingSessionDao.getAllSessionsSync() } returns sessions

        // When
        val result = repository.getReadingStreak()

        // Then
        assertEquals(2, result.getOrNull()) // 2 unique days
    }

    @Test
    fun `getLongestStreak finds maximum consecutive days`() = runTest(testDispatcher) {
        // Given
        val today = Calendar.getInstance()
        val sessions = listOf(
            // First streak: 3 days
            createSession(today, -10),
            createSession(today, -11),
            createSession(today, -12),
            // Gap
            // Second streak: 5 days (longest)
            createSession(today, -20),
            createSession(today, -21),
            createSession(today, -22),
            createSession(today, -23),
            createSession(today, -24),
            // Gap
            // Third streak: 2 days
            createSession(today, -30),
            createSession(today, -31)
        )
        coEvery { readingSessionDao.getAllSessionsSync() } returns sessions

        // When
        val result = repository.getLongestStreak()

        // Then
        assertEquals(5, result.getOrNull())
    }

    private fun createSession(baseCalendar: Calendar, daysOffset: Int): ReadingSessionEntity {
        val calendar = baseCalendar.clone() as Calendar
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return ReadingSessionEntity(
            id = UUID.randomUUID().toString(),
            bookId = "test-book",
            startPage = 0,
            endPage = 10,
            startTime = calendar.timeInMillis,
            endTime = calendar.timeInMillis + 3600000,
            duration = 3600000,
            pagesRead = 10,
            date = dateFormat.format(calendar.time)
        )
    }
}
