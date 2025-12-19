package com.foliolib.app.domain.repository

import com.foliolib.app.domain.model.Note
import com.foliolib.app.domain.model.ReadingSession
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {
    // Reading Sessions
    suspend fun startReadingSession(bookId: String): Result<ReadingSession>
    suspend fun updateReadingSession(session: ReadingSession): Result<Unit>
    suspend fun endReadingSession(sessionId: String, pagesRead: Int): Result<Unit>
    fun getActiveSession(bookId: String): Flow<ReadingSession?>
    fun getAllSessionsForBook(bookId: String): Flow<List<ReadingSession>>
    fun getAllSessions(): Flow<List<ReadingSession>>

    // Reading Streak
    suspend fun getReadingStreak(): Result<Int>
    suspend fun getLongestStreak(): Result<Int>
    suspend fun getLastReadingDate(): Result<Long?>
    suspend fun deleteEmptySessions()
    suspend fun deleteSessionsForBook(bookId: String): Result<Unit>

    // Notes
    suspend fun addNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(noteId: String): Result<Unit>
    fun getNotesForBook(bookId: String): Flow<List<Note>>
    fun getAllNotes(): Flow<List<Note>>

    // Highlights
    suspend fun addHighlight(bookId: String, text: String, pageNumber: Int?): Result<Unit>
    fun getHighlightsForBook(bookId: String): Flow<List<String>>
}
