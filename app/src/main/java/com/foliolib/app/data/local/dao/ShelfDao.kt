package com.foliolib.app.data.local.dao

import androidx.room.*
import com.foliolib.app.data.local.entity.BookShelfCrossRef
import com.foliolib.app.data.local.entity.ShelfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfDao {

    @Query("SELECT * FROM shelves ORDER BY sort_order ASC, name ASC")
    fun getAllShelves(): Flow<List<ShelfEntity>>

    @Query("SELECT * FROM shelves WHERE id = :shelfId")
    fun getShelfById(shelfId: String): Flow<ShelfEntity?>

    @Query("SELECT * FROM shelves WHERE is_default = 1 ORDER BY sort_order ASC")
    fun getDefaultShelves(): Flow<List<ShelfEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelf(shelf: ShelfEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelves(shelves: List<ShelfEntity>)

    @Update
    suspend fun updateShelf(shelf: ShelfEntity)

    @Delete
    suspend fun deleteShelf(shelf: ShelfEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookToShelf(crossRef: BookShelfCrossRef)

    @Delete
    suspend fun removeBookFromShelf(crossRef: BookShelfCrossRef)

    @Query("DELETE FROM book_shelf_cross_ref WHERE book_id = :bookId AND shelf_id = :shelfId")
    suspend fun removeBookFromShelfById(bookId: String, shelfId: String)

    @Query("DELETE FROM book_shelf_cross_ref WHERE book_id = :bookId")
    suspend fun removeBookFromAllShelves(bookId: String)

    @Query("""
        SELECT b.* FROM books b
        INNER JOIN book_shelf_cross_ref ref ON b.id = ref.book_id
        WHERE ref.shelf_id = :shelfId
        ORDER BY ref.added_at DESC
    """)
    fun getBooksInShelf(shelfId: String): Flow<List<com.foliolib.app.data.local.entity.BookEntity>>

    @Query("""
        SELECT COUNT(*) FROM book_shelf_cross_ref
        WHERE shelf_id = :shelfId
    """)
    fun getBookCountInShelf(shelfId: String): Flow<Int>
}
