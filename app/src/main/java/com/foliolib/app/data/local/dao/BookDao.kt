package com.foliolib.app.data.local.dao

import androidx.room.*
import com.foliolib.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY date_added DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookByIdOnce(bookId: String): BookEntity?

    @Query("SELECT * FROM books WHERE reading_status = :status ORDER BY date_added DESC")
    fun getBooksByStatus(status: String): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM books
        WHERE title LIKE '%' || :query || '%'
        OR authors LIKE '%' || :query || '%'
        OR isbn LIKE '%' || :query || '%'
        OR isbn13 LIKE '%' || :query || '%'
        ORDER BY date_added DESC
    """)
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isbn = :isbn OR isbn13 = :isbn LIMIT 1")
    suspend fun getBookByIsbn(isbn: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)

    @Query("SELECT COUNT(*) FROM books")
    fun getBooksCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM books WHERE reading_status = 'FINISHED'")
    fun getFinishedBooksCount(): Flow<Int>

    @Query("""
        SELECT * FROM books
        WHERE reading_status = 'READING'
        ORDER BY date_started DESC
    """)
    fun getCurrentlyReadingBooks(): Flow<List<BookEntity>>
}
