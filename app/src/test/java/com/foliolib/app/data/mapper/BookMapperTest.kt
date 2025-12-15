package com.foliolib.app.data.mapper

import com.foliolib.app.data.remote.dto.GoogleBookItem
import com.foliolib.app.data.remote.dto.VolumeInfo
import com.foliolib.app.domain.model.BookCondition
import com.foliolib.app.domain.model.ReadingStatus
import org.junit.Assert.*
import org.junit.Test

class BookMapperTest {

    @Test
    fun `toDomainModel converts GoogleBookItem correctly`() {
        // Given
        val googleBook = GoogleBookItem(
            id = "test-id-123",
            volumeInfo = VolumeInfo(
                title = "Test Book",
                authors = listOf("Author One", "Author Two"),
                publisher = "Test Publisher",
                publishedDate = "2024-01-01",
                description = "A test book description",
                pageCount = 300,
                categories = listOf("Fiction", "Adventure"),
                imageLinks = null,
                language = "en",
                isbn10 = "1234567890",
                isbn13 = "1234567890123"
            )
        )

        // When
        val domainBook = BookMapper.toDomainModel(googleBook)

        // Then
        assertEquals("test-id-123", domainBook.id)
        assertEquals("Test Book", domainBook.title)
        assertEquals(2, domainBook.authors.size)
        assertEquals("Author One", domainBook.authors[0])
        assertEquals("Author Two", domainBook.authors[1])
        assertEquals("Test Publisher", domainBook.publisher)
        assertEquals("2024-01-01", domainBook.publishedDate)
        assertEquals("A test book description", domainBook.description)
        assertEquals(300, domainBook.pageCount)
        assertEquals(2, domainBook.categories.size)
        assertEquals("Fiction", domainBook.categories[0])
        assertEquals(ReadingStatus.NONE, domainBook.readingStatus)
        assertFalse(domainBook.isManualEntry)
    }

    @Test
    fun `toDomainModel handles null values correctly`() {
        // Given
        val googleBook = GoogleBookItem(
            id = "test-id",
            volumeInfo = VolumeInfo(
                title = "Minimal Book",
                authors = null,
                publisher = null,
                publishedDate = null,
                description = null,
                pageCount = null,
                categories = null,
                imageLinks = null,
                language = null,
                isbn10 = null,
                isbn13 = null
            )
        )

        // When
        val domainBook = BookMapper.toDomainModel(googleBook)

        // Then
        assertEquals("test-id", domainBook.id)
        assertEquals("Minimal Book", domainBook.title)
        assertTrue(domainBook.authors.isEmpty())
        assertNull(domainBook.publisher)
        assertNull(domainBook.description)
        assertNull(domainBook.pageCount)
        assertTrue(domainBook.categories.isEmpty())
        assertNull(domainBook.thumbnailUrl)
    }

    @Test
    fun `toDomainModel upgrades http to https for thumbnails`() {
        // Given
        val googleBook = GoogleBookItem(
            id = "test-id",
            volumeInfo = VolumeInfo(
                title = "Test Book",
                authors = null,
                publisher = null,
                publishedDate = null,
                description = null,
                pageCount = null,
                categories = null,
                imageLinks = VolumeInfo.ImageLinks(
                    thumbnail = "http://example.com/image.jpg",
                    smallThumbnail = null
                ),
                language = null,
                isbn10 = null,
                isbn13 = null
            )
        )

        // When
        val domainBook = BookMapper.toDomainModel(googleBook)

        // Then
        assertEquals("https://example.com/image.jpg", domainBook.thumbnailUrl)
    }

    @Test
    fun `createManualBook creates book with correct properties`() {
        // When
        val book = BookMapper.createManualBook(
            title = "Manual Book",
            authors = listOf("Manual Author"),
            isbn = "1234567890",
            publisher = "Manual Publisher",
            publishedDate = "2024",
            pageCount = 250,
            description = "Manual description",
            condition = BookCondition.GOOD
        )

        // Then
        assertEquals("Manual Book", book.title)
        assertEquals(1, book.authors.size)
        assertEquals("Manual Author", book.authors[0])
        assertEquals("1234567890", book.isbn)
        assertEquals("Manual Publisher", book.publisher)
        assertEquals("2024", book.publishedDate)
        assertEquals(250, book.pageCount)
        assertEquals("Manual description", book.description)
        assertEquals(BookCondition.GOOD, book.condition)
        assertTrue(book.isManualEntry)
        assertEquals(ReadingStatus.NONE, book.readingStatus)
        assertNotNull(book.id)
    }

    @Test
    fun `createManualBook handles empty authors list`() {
        // When
        val book = BookMapper.createManualBook(
            title = "Book Without Author",
            authors = emptyList()
        )

        // Then
        assertTrue(book.authors.isEmpty())
    }

    @Test
    fun `createManualBook sets dateAdded to current time`() {
        // Given
        val beforeTime = System.currentTimeMillis()

        // When
        val book = BookMapper.createManualBook(
            title = "Test",
            authors = emptyList()
        )

        // Then
        val afterTime = System.currentTimeMillis()
        assertTrue(book.dateAdded >= beforeTime)
        assertTrue(book.dateAdded <= afterTime)
    }
}
