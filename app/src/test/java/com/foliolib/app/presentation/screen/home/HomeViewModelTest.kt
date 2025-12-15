package com.foliolib.app.presentation.screen.home

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.ReadingStatus
import com.foliolib.app.domain.repository.BookRepository
import com.foliolib.app.domain.usecase.reading.GetReadingStreakUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var getReadingStreakUseCase: GetReadingStreakUseCase
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bookRepository = mockk(relaxed = true)
        getReadingStreakUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        // Given
        every { bookRepository.getCurrentlyReadingBooks() } returns flowOf(emptyList())
        every { bookRepository.getBooksCount() } returns flowOf(0)
        every { bookRepository.getFinishedBooksCount() } returns flowOf(0)
        every { getReadingStreakUseCase() } returns Result.success(0)

        // When
        viewModel = HomeViewModel(bookRepository, getReadingStreakUseCase)

        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads currently reading books correctly`() = runTest(testDispatcher) {
        // Given
        val testBooks = listOf(
            createTestBook("1", "Book 1", ReadingStatus.READING),
            createTestBook("2", "Book 2", ReadingStatus.READING)
        )
        every { bookRepository.getCurrentlyReadingBooks() } returns flowOf(testBooks)
        every { bookRepository.getBooksCount() } returns flowOf(10)
        every { bookRepository.getFinishedBooksCount() } returns flowOf(5)
        every { getReadingStreakUseCase() } returns Result.success(7)

        // When
        viewModel = HomeViewModel(bookRepository, getReadingStreakUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.currentlyReading.size)
        assertEquals("Book 1", state.currentlyReading[0].title)
        assertEquals("Book 2", state.currentlyReading[1].title)
    }

    @Test
    fun `loads book counts correctly`() = runTest(testDispatcher) {
        // Given
        every { bookRepository.getCurrentlyReadingBooks() } returns flowOf(emptyList())
        every { bookRepository.getBooksCount() } returns flowOf(25)
        every { bookRepository.getFinishedBooksCount() } returns flowOf(15)
        every { getReadingStreakUseCase() } returns Result.success(3)

        // When
        viewModel = HomeViewModel(bookRepository, getReadingStreakUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(25, state.totalBooks)
        assertEquals(15, state.finishedBooks)
    }

    @Test
    fun `loads reading streak correctly`() = runTest(testDispatcher) {
        // Given
        every { bookRepository.getCurrentlyReadingBooks() } returns flowOf(emptyList())
        every { bookRepository.getBooksCount() } returns flowOf(0)
        every { bookRepository.getFinishedBooksCount() } returns flowOf(0)
        every { getReadingStreakUseCase() } returns Result.success(14)

        // When
        viewModel = HomeViewModel(bookRepository, getReadingStreakUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(14, viewModel.uiState.value.currentStreak)
    }

    @Test
    fun `handles streak loading failure gracefully`() = runTest(testDispatcher) {
        // Given
        every { bookRepository.getCurrentlyReadingBooks() } returns flowOf(emptyList())
        every { bookRepository.getBooksCount() } returns flowOf(0)
        every { bookRepository.getFinishedBooksCount() } returns flowOf(0)
        every { getReadingStreakUseCase() } returns Result.failure(Exception("Test error"))

        // When
        viewModel = HomeViewModel(bookRepository, getReadingStreakUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(0, viewModel.uiState.value.currentStreak)
    }

    private fun createTestBook(
        id: String,
        title: String,
        readingStatus: ReadingStatus
    ) = Book(
        id = id,
        title = title,
        authors = listOf("Test Author"),
        isbn = null,
        isbn13 = null,
        publisher = null,
        publishedDate = null,
        pageCount = 300,
        description = null,
        thumbnailUrl = null,
        largeImageUrl = null,
        language = null,
        categories = emptyList(),
        averageRating = null,
        ratingsCount = null,
        currentPage = 0,
        readingStatus = readingStatus,
        rating = null,
        dateAdded = System.currentTimeMillis(),
        dateStarted = null,
        dateFinished = null,
        isManualEntry = false,
        condition = null
    )
}
