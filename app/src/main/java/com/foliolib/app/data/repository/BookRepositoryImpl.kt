package com.foliolib.app.data.repository

import com.foliolib.app.core.di.IoDispatcher
import com.foliolib.app.data.local.dao.BookDao
import com.foliolib.app.data.mapper.BookMapper.toDomainModel
import com.foliolib.app.data.mapper.BookMapper.toEntity
import com.foliolib.app.data.remote.service.BookApiService
import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.repository.BookRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val bookApiService: BookApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BookRepository {

    // Remote operations
    override suspend fun searchBooks(query: String): Result<List<Book>> = withContext(ioDispatcher) {
        try {
            Timber.d("Searching books: $query")
            val result = bookApiService.searchBooks(query)

            result.map { items ->
                items.map { it.toDomainModel() }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching books")
            Result.failure(e)
        }
    }

    override suspend fun getBookByIsbn(isbn: String): Result<Book> = withContext(ioDispatcher) {
        try {
            Timber.d("Getting book by ISBN: $isbn")
            val result = bookApiService.getBookByIsbn(isbn)

            result.map { it.toDomainModel() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting book by ISBN")
            Result.failure(e)
        }
    }

    // Local operations
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks()
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(ioDispatcher)
    }

    override fun getBookById(bookId: String): Flow<Book?> {
        return bookDao.getBookById(bookId)
            .map { it?.toDomainModel() }
            .flowOn(ioDispatcher)
    }

    override fun getBooksByStatus(status: String): Flow<List<Book>> {
        return bookDao.getBooksByStatus(status)
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(ioDispatcher)
    }

    override fun getCurrentlyReadingBooks(): Flow<List<Book>> {
        return bookDao.getCurrentlyReadingBooks()
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(ioDispatcher)
    }

    override fun searchLocalBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query)
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun insertBook(book: Book) = withContext(ioDispatcher) {
        Timber.d("Inserting book: ${book.title}")
        bookDao.insertBook(book.toEntity())
        Unit
    }

    override suspend fun updateBook(book: Book) = withContext(ioDispatcher) {
        Timber.d("Updating book: ${book.title}")
        bookDao.updateBook(book.toEntity())
    }

    override suspend fun deleteBook(book: Book) = withContext(ioDispatcher) {
        Timber.d("Deleting book: ${book.title}")
        bookDao.deleteBook(book.toEntity())
    }

    override fun getBooksCount(): Flow<Int> {
        return bookDao.getBooksCount().flowOn(ioDispatcher)
    }

    override fun getFinishedBooksCount(): Flow<Int> {
        return bookDao.getFinishedBooksCount().flowOn(ioDispatcher)
    }
}
