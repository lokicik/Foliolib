package com.foliolib.app.data.local.dao

import androidx.room.*
import com.foliolib.app.data.local.entity.HighlightEntity
import com.foliolib.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // Notes
    @Query("SELECT * FROM notes WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getNotesForBook(bookId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY created_at DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("DELETE FROM notes WHERE book_id = :bookId")
    suspend fun deleteNotesForBook(bookId: String)

    // Highlights
    @Query("SELECT * FROM highlights WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getHighlightsForBook(bookId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE id = :highlightId")
    fun getHighlightById(highlightId: String): Flow<HighlightEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity): Long

    @Update
    suspend fun updateHighlight(highlight: HighlightEntity)

    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE book_id = :bookId")
    suspend fun deleteHighlightsForBook(bookId: String)

    // Combined queries
    @Query("SELECT COUNT(*) FROM notes WHERE book_id = :bookId")
    fun getNoteCountForBook(bookId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM highlights WHERE book_id = :bookId")
    fun getHighlightCountForBook(bookId: String): Flow<Int>
}
