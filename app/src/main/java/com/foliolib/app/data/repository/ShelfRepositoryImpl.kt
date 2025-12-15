package com.foliolib.app.data.repository

import com.foliolib.app.core.di.IoDispatcher
import com.foliolib.app.data.local.dao.BookDao
import com.foliolib.app.data.local.dao.ShelfDao
import com.foliolib.app.data.local.entity.BookShelfCrossRef
import com.foliolib.app.data.local.entity.ShelfEntity
import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.Shelf
import com.foliolib.app.domain.repository.ShelfRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShelfRepositoryImpl @Inject constructor(
    private val shelfDao: ShelfDao,
    private val bookDao: BookDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ShelfRepository {

    override suspend fun createShelf(shelf: Shelf): Result<Unit> = withContext(ioDispatcher) {
        try {
            shelfDao.insertShelf(shelf.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateShelf(shelf: Shelf): Result<Unit> = withContext(ioDispatcher) {
        try {
            shelfDao.updateShelf(shelf.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteShelf(shelfId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val shelf = shelfDao.getShelfById(shelfId)
            // Don't collect flow, get the entity directly
            // Note: This is simplified - in production you'd want to handle this better
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllShelves(): Flow<List<Shelf>> =
        shelfDao.getAllShelves().map { shelves ->
            shelves.map { it.toDomainModel() }
        }

    override fun getShelfById(shelfId: String): Flow<Shelf?> =
        shelfDao.getShelfById(shelfId).map { it?.toDomainModel() }

    override suspend fun addBookToShelf(bookId: String, shelfId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val crossRef = BookShelfCrossRef(
                    bookId = bookId,
                    shelfId = shelfId,
                    addedAt = System.currentTimeMillis()
                )
                shelfDao.addBookToShelf(crossRef)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun removeBookFromShelf(bookId: String, shelfId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                shelfDao.removeBookFromShelfById(bookId, shelfId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun getBooksInShelf(shelfId: String): Flow<List<Book>> =
        shelfDao.getBooksInShelf(shelfId).map { bookEntities ->
            bookEntities.map { it.toDomainModel() }
        }

    override fun getShelvesForBook(bookId: String): Flow<List<Shelf>> {
        // This would require a new DAO query - for now return empty
        // TODO: Add getShelvesForBook query to ShelfDao
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override suspend fun ensureDefaultShelves(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val defaultShelves = listOf(
                ShelfEntity(
                    id = "shelf_reading",
                    name = "Reading",
                    description = "Books you're currently reading",
                    color = "#8B5CF6", // Violet
                    icon = "menu_book",
                    isDefault = true,
                    sortOrder = 1
                ),
                ShelfEntity(
                    id = "shelf_want_to_read",
                    name = "Want to Read",
                    description = "Books on your reading list",
                    color = "#6366F1", // Indigo
                    icon = "bookmark",
                    isDefault = true,
                    sortOrder = 2
                ),
                ShelfEntity(
                    id = "shelf_finished",
                    name = "Finished",
                    description = "Books you've completed",
                    color = "#10B981", // Green
                    icon = "check_circle",
                    isDefault = true,
                    sortOrder = 3
                ),
                ShelfEntity(
                    id = "shelf_dnf",
                    name = "Did Not Finish",
                    description = "Books you stopped reading",
                    color = "#F59E0B", // Amber
                    icon = "block",
                    isDefault = true,
                    sortOrder = 4
                ),
                ShelfEntity(
                    id = "shelf_wishlist",
                    name = "Wishlist",
                    description = "Books you want to own",
                    color = "#EC4899", // Pink
                    icon = "favorite",
                    isDefault = true,
                    sortOrder = 5
                )
            )
            shelfDao.insertShelves(defaultShelves)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mappers
    private fun ShelfEntity.toDomainModel() = Shelf(
        id = id,
        name = name,
        description = description,
        color = color,
        icon = icon,
        isDefault = isDefault,
        sortOrder = sortOrder,
        createdAt = createdAt,
        bookCount = 0 // This would need to be joined from the database
    )

    private fun Shelf.toEntity() = ShelfEntity(
        id = id,
        name = name,
        description = description,
        color = color,
        icon = icon,
        isDefault = isDefault,
        sortOrder = sortOrder,
        createdAt = createdAt
    )

    private fun com.foliolib.app.data.local.entity.BookEntity.toDomainModel() = Book(
        id = id,
        title = title,
        authors = parseAuthors(authorsJson),
        isbn = isbn,
        isbn13 = isbn13,
        publisher = publisher,
        publishedDate = publishedDate,
        pageCount = pageCount,
        description = description,
        thumbnailUrl = thumbnailUrl,
        largeImageUrl = largeImageUrl,
        language = language,
        categories = parseCategories(categoriesJson ?: "[]"),
        currentPage = currentPage,
        readingStatus = com.foliolib.app.domain.model.ReadingStatus.valueOf(readingStatus),
        rating = rating,
        dateAdded = dateAdded,
        dateStarted = dateStarted,
        dateFinished = dateFinished,
        isManualEntry = isManualEntry,
        condition = condition?.let { com.foliolib.app.domain.model.BookCondition.valueOf(it) }
    )

    private fun parseAuthors(json: String): List<String> {
        return try {
            json.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseCategories(json: String): List<String> {
        return try {
            json.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
