package com.foliolib.app.domain.repository

import com.foliolib.app.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    // Remote operations
    suspend fun searchBooks(query: String): Result<List<Book>>
    suspend fun getBookByIsbn(isbn: String): Result<Book>

    // Local operations
    fun getAllBooks(): Flow<List<Book>>
    fun getBookById(bookId: String): Flow<Book?>
    fun getBooksByStatus(status: String): Flow<List<Book>>
    fun getCurrentlyReadingBooks(): Flow<List<Book>>
    fun searchLocalBooks(query: String): Flow<List<Book>>

    suspend fun insertBook(book: Book)
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(book: Book)

    fun getBooksCount(): Flow<Int>
    fun getFinishedBooksCount(): Flow<Int>
}
