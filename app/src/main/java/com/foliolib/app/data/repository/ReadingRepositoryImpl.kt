package com.foliolib.app.data.repository

import com.foliolib.app.core.di.IoDispatcher
import com.foliolib.app.data.local.dao.NoteDao
import com.foliolib.app.data.local.dao.ReadingSessionDao
import com.foliolib.app.data.local.entity.HighlightEntity
import com.foliolib.app.data.local.entity.NoteEntity
import com.foliolib.app.data.local.entity.ReadingSessionEntity
import com.foliolib.app.domain.model.Note
import com.foliolib.app.domain.model.ReadingSession
import com.foliolib.app.domain.repository.ReadingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepositoryImpl @Inject constructor(
    private val readingSessionDao: ReadingSessionDao,
    private val noteDao: NoteDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReadingRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Reading Sessions
    override suspend fun startReadingSession(bookId: String): Result<ReadingSession> =
        withContext(ioDispatcher) {
            try {
                val currentTime = System.currentTimeMillis()
                val session = ReadingSessionEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    startPage = 0, // Will be updated when session ends
                    endPage = 0,
                    startTime = currentTime,
                    endTime = currentTime,
                    duration = 0,
                    pagesRead = 0,
                    date = dateFormat.format(Date(currentTime))
                )
                readingSessionDao.insertSession(session)
                Result.success(session.toDomainModel())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateReadingSession(session: ReadingSession): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                readingSessionDao.updateSession(session.toEntity())
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun endReadingSession(sessionId: String, pagesRead: Int): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val session = readingSessionDao.getSessionById(sessionId)
                if (session != null) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - session.startTime
                    val updatedSession = session.copy(
                        endTime = endTime,
                        duration = duration,
                        pagesRead = pagesRead,
                        endPage = session.startPage + pagesRead
                    )
                    readingSessionDao.updateSession(updatedSession)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Session not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun getActiveSession(bookId: String): Flow<ReadingSession?> =
        readingSessionDao.getSessionsForBook(bookId).map { sessions ->
            sessions.lastOrNull()?.toDomainModel()
        }

    override fun getAllSessionsForBook(bookId: String): Flow<List<ReadingSession>> =
        readingSessionDao.getSessionsForBook(bookId).map { sessions ->
            sessions.map { it.toDomainModel() }
        }

    override fun getAllSessions(): Flow<List<ReadingSession>> =
        readingSessionDao.getAllSessions().map { sessions ->
            sessions.map { it.toDomainModel() }
        }

    // Reading Streak
    override suspend fun getReadingStreak(): Result<Int> = withContext(ioDispatcher) {
        try {
            val allSessions = readingSessionDao.getAllSessionsSync()
            val streak = calculateStreak(allSessions)
            Result.success(streak)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLongestStreak(): Result<Int> = withContext(ioDispatcher) {
        try {
            val allSessions = readingSessionDao.getAllSessionsSync()
            val longestStreak = calculateLongestStreak(allSessions)
            Result.success(longestStreak)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastReadingDate(): Result<Long?> = withContext(ioDispatcher) {
        try {
            val lastSession = readingSessionDao.getAllSessionsSync().maxByOrNull { it.startTime }
            Result.success(lastSession?.startTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateStreak(sessions: List<ReadingSessionEntity>): Int {
        if (sessions.isEmpty()) return 0

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Group sessions by date
        val sessionsByDate = sessions.groupBy { session ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = session.startTime
            cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.keys.sorted().reversed()

        var streak = 0
        var checkDate = today.timeInMillis

        for (date in sessionsByDate) {
            if (date == checkDate || date == checkDate - 86400000) { // Today or yesterday
                streak++
                checkDate = date - 86400000 // Move back one day
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateLongestStreak(sessions: List<ReadingSessionEntity>): Int {
        if (sessions.isEmpty()) return 0

        val sessionsByDate = sessions.groupBy { session ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = session.startTime
            cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.keys.sorted()

        var maxStreak = 0
        var currentStreak = 1
        var previousDate = sessionsByDate.first()

        for (i in 1 until sessionsByDate.size) {
            val currentDate = sessionsByDate[i]
            if (currentDate == previousDate + 86400000) { // Consecutive day
                currentStreak++
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
            previousDate = currentDate
        }

        return maxOf(maxStreak, currentStreak)
    }

    // Notes
    override suspend fun addNote(note: Note): Result<Unit> = withContext(ioDispatcher) {
        try {
            noteDao.insertNote(note.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit> = withContext(ioDispatcher) {
        try {
            noteDao.updateNote(note.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            noteDao.deleteNoteById(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNotesForBook(bookId: String): Flow<List<Note>> =
        noteDao.getNotesForBook(bookId).map { notes ->
            notes.map { it.toDomainModel() }
        }

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { notes ->
            notes.map { it.toDomainModel() }
        }

    // Highlights
    override suspend fun addHighlight(bookId: String, text: String, pageNumber: Int?): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val highlight = HighlightEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    text = text,
                    page = pageNumber,
                    color = "#FFEB3B", // Default yellow
                    createdAt = System.currentTimeMillis()
                )
                noteDao.insertHighlight(highlight)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun getHighlightsForBook(bookId: String): Flow<List<String>> =
        noteDao.getHighlightsForBook(bookId).map { highlights ->
            highlights.map { it.text }
        }

    // Mappers
    private fun ReadingSessionEntity.toDomainModel() = ReadingSession(
        id = id,
        bookId = bookId,
        startPage = startPage,
        endPage = endPage,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        pagesRead = pagesRead,
        date = date
    )

    private fun ReadingSession.toEntity() = ReadingSessionEntity(
        id = id,
        bookId = bookId,
        startPage = startPage,
        endPage = endPage,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        pagesRead = pagesRead,
        date = date
    )

    private fun NoteEntity.toDomainModel() = Note(
        id = id,
        bookId = bookId,
        content = content,
        page = page,
        chapter = chapter,
        color = color,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Note.toEntity() = NoteEntity(
        id = id,
        bookId = bookId,
        content = content,
        page = page,
        chapter = chapter,
        color = color,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
