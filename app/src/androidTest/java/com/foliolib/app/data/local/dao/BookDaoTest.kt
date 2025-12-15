package com.foliolib.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.foliolib.app.data.local.database.FolioDatabase
import com.foliolib.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var database: FolioDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FolioDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetBook() = runTest {
        // Given
        val book = createTestBook("1", "Test Book")

        // When
        bookDao.insertBook(book)
        val books = bookDao.getAllBooks().first()

        // Then
        assertEquals(1, books.size)
        assertEquals("Test Book", books[0].title)
    }

    @Test
    fun updateBook() = runTest {
        // Given
        val book = createTestBook("1", "Original Title")
        bookDao.insertBook(book)

        // When
        val updatedBook = book.copy(title = "Updated Title")
        bookDao.updateBook(updatedBook)
        val books = bookDao.getAllBooks().first()

        // Then
        assertEquals(1, books.size)
        assertEquals("Updated Title", books[0].title)
    }

    @Test
    fun deleteBook() = runTest {
        // Given
        val book = createTestBook("1", "Test Book")
        bookDao.insertBook(book)

        // When
        bookDao.deleteBook(book)
        val books = bookDao.getAllBooks().first()

        // Then
        assertEquals(0, books.size)
    }

    @Test
    fun getBookById() = runTest {
        // Given
        val book1 = createTestBook("1", "Book 1")
        val book2 = createTestBook("2", "Book 2")
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        // When
        val retrievedBook = bookDao.getBookById("2").first()

        // Then
        assertNotNull(retrievedBook)
        assertEquals("Book 2", retrievedBook?.title)
    }

    @Test
    fun searchBooksByTitle() = runTest {
        // Given
        bookDao.insertBook(createTestBook("1", "The Great Gatsby"))
        bookDao.insertBook(createTestBook("2", "Great Expectations"))
        bookDao.insertBook(createTestBook("3", "To Kill a Mockingbird"))

        // When
        val results = bookDao.searchBooks("Great").first()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.title.contains("Great") })
    }

    @Test
    fun getCurrentlyReadingBooks() = runTest {
        // Given
        bookDao.insertBook(createTestBook("1", "Reading Book", "READING"))
        bookDao.insertBook(createTestBook("2", "Finished Book", "FINISHED"))
        bookDao.insertBook(createTestBook("3", "Another Reading", "READING"))

        // When
        val readingBooks = bookDao.getCurrentlyReadingBooks().first()

        // Then
        assertEquals(2, readingBooks.size)
        assertTrue(readingBooks.all { it.readingStatus == "READING" })
    }

    @Test
    fun getFinishedBooksCount() = runTest {
        // Given
        bookDao.insertBook(createTestBook("1", "Book 1", "FINISHED"))
        bookDao.insertBook(createTestBook("2", "Book 2", "READING"))
        bookDao.insertBook(createTestBook("3", "Book 3", "FINISHED"))
        bookDao.insertBook(createTestBook("4", "Book 4", "FINISHED"))

        // When
        val count = bookDao.getFinishedBooksCount().first()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun getBooksCount() = runTest {
        // Given
        bookDao.insertBook(createTestBook("1", "Book 1"))
        bookDao.insertBook(createTestBook("2", "Book 2"))
        bookDao.insertBook(createTestBook("3", "Book 3"))

        // When
        val count = bookDao.getBooksCount().first()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun replaceBookOnConflict() = runTest {
        // Given
        val book = createTestBook("1", "Original")
        bookDao.insertBook(book)

        // When - insert same ID with different data
        val duplicateBook = createTestBook("1", "Replaced")
        bookDao.insertBook(duplicateBook)
        val books = bookDao.getAllBooks().first()

        // Then - should replace, not duplicate
        assertEquals(1, books.size)
        assertEquals("Replaced", books[0].title)
    }

    private fun createTestBook(
        id: String,
        title: String,
        readingStatus: String = "NONE"
    ) = BookEntity(
        id = id,
        title = title,
        authorsJson = "[\"Test Author\"]",
        isbn = null,
        isbn13 = null,
        publisher = null,
        publishedDate = null,
        pageCount = 300,
        description = "Test description",
        thumbnailUrl = null,
        largeImageUrl = null,
        language = "en",
        categoriesJson = "[\"Fiction\"]",
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
