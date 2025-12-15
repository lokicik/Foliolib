package com.foliolib.app.domain.repository

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.model.Shelf
import kotlinx.coroutines.flow.Flow

interface ShelfRepository {
    // Shelf CRUD
    suspend fun createShelf(shelf: Shelf): Result<Unit>
    suspend fun updateShelf(shelf: Shelf): Result<Unit>
    suspend fun deleteShelf(shelfId: String): Result<Unit>
    fun getAllShelves(): Flow<List<Shelf>>
    fun getShelfById(shelfId: String): Flow<Shelf?>

    // Book-Shelf relationships
    suspend fun addBookToShelf(bookId: String, shelfId: String): Result<Unit>
    suspend fun removeBookFromShelf(bookId: String, shelfId: String): Result<Unit>
    fun getBooksInShelf(shelfId: String): Flow<List<Book>>
    fun getShelvesForBook(bookId: String): Flow<List<Shelf>>

    // Default shelves
    suspend fun ensureDefaultShelves(): Result<Unit>
}
