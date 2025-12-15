package com.foliolib.app.data.mapper

import com.foliolib.app.data.local.entity.BookEntity
import com.foliolib.app.data.remote.dto.GoogleBookItem
import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.BookCondition
import com.foliolib.app.domain.model.ReadingStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.UUID

object BookMapper {

    private val moshi = Moshi.Builder().build()
    private val stringListAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    // Google Books DTO → Domain Model
    fun GoogleBookItem.toDomainModel(): Book {
        val isbn10 = volumeInfo.industryIdentifiers
            ?.firstOrNull { it.type == "ISBN_10" }?.identifier
        val isbn13 = volumeInfo.industryIdentifiers
            ?.firstOrNull { it.type == "ISBN_13" }?.identifier

        // Upgrade http to https for images
        val thumbnailUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        val largeImageUrl = (volumeInfo.imageLinks?.large
            ?: volumeInfo.imageLinks?.medium
            ?: volumeInfo.imageLinks?.small
            ?: thumbnailUrl)?.replace("http://", "https://")

        return Book(
            id = id,
            title = volumeInfo.title,
            authors = volumeInfo.authors ?: emptyList(),
            isbn = isbn10,
            isbn13 = isbn13,
            publisher = volumeInfo.publisher,
            publishedDate = volumeInfo.publishedDate,
            description = volumeInfo.description,
            pageCount = volumeInfo.pageCount,
            categories = volumeInfo.categories ?: emptyList(),
            thumbnailUrl = thumbnailUrl,
            largeImageUrl = largeImageUrl,
            language = volumeInfo.language,
            condition = null,
            currentPage = 0,
            readingStatus = ReadingStatus.NONE,
            rating = volumeInfo.averageRating,
            notes = null,
            dateAdded = System.currentTimeMillis(),
            dateStarted = null,
            dateFinished = null,
            isManualEntry = false
        )
    }

    // Domain Model → Entity
    fun Book.toEntity(): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            authorsJson = stringListAdapter.toJson(authors),
            isbn = isbn,
            isbn13 = isbn13,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            pageCount = pageCount,
            categoriesJson = stringListAdapter.toJson(categories),
            thumbnailUrl = thumbnailUrl,
            largeImageUrl = largeImageUrl,
            language = language,
            condition = condition?.name,
            currentPage = currentPage,
            readingStatus = readingStatus.name,
            rating = rating,
            notes = notes,
            dateAdded = dateAdded,
            dateStarted = dateStarted,
            dateFinished = dateFinished,
            isManualEntry = isManualEntry
        )
    }

    // Entity → Domain Model
    fun BookEntity.toDomainModel(): Book {
        return Book(
            id = id,
            title = title,
            authors = stringListAdapter.fromJson(authorsJson) ?: emptyList(),
            isbn = isbn,
            isbn13 = isbn13,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            pageCount = pageCount,
            categories = categoriesJson?.let { stringListAdapter.fromJson(it) } ?: emptyList(),
            thumbnailUrl = thumbnailUrl,
            largeImageUrl = largeImageUrl,
            language = language,
            condition = condition?.let { BookCondition.valueOf(it) },
            currentPage = currentPage,
            readingStatus = ReadingStatus.valueOf(readingStatus),
            rating = rating,
            notes = notes,
            dateAdded = dateAdded,
            dateStarted = dateStarted,
            dateFinished = dateFinished,
            isManualEntry = isManualEntry
        )
    }

    // Create manual entry book
    fun createManualBook(
        title: String,
        authors: List<String>,
        isbn: String? = null,
        publisher: String? = null,
        publishedDate: String? = null,
        pageCount: Int? = null,
        description: String? = null,
        categories: List<String> = emptyList(),
        condition: BookCondition? = null
    ): Book {
        return Book(
            id = UUID.randomUUID().toString(),
            title = title,
            authors = authors,
            isbn = isbn,
            isbn13 = null,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            pageCount = pageCount,
            categories = categories,
            thumbnailUrl = null,
            largeImageUrl = null,
            language = null,
            condition = condition,
            currentPage = 0,
            readingStatus = ReadingStatus.NONE,
            rating = null,
            notes = null,
            dateAdded = System.currentTimeMillis(),
            dateStarted = null,
            dateFinished = null,
            isManualEntry = true
        )
    }
}
