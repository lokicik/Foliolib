package com.foliolib.app.data.local.dao

import androidx.room.*
import com.foliolib.app.data.local.entity.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Query("SELECT * FROM reading_sessions WHERE book_id = :bookId ORDER BY start_time DESC")
    fun getSessionsForBook(bookId: String): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions WHERE date = :date ORDER BY start_time DESC")
    fun getSessionsForDate(date: String): Flow<List<ReadingSessionEntity>>

    @Query("""
        SELECT * FROM reading_sessions
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC, start_time DESC
    """)
    fun getSessionsInDateRange(startDate: String, endDate: String): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions ORDER BY start_time DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<ReadingSessionEntity>>

    @Query("SELECT SUM(pages_read) FROM reading_sessions WHERE book_id = :bookId")
    fun getTotalPagesReadForBook(bookId: String): Flow<Int?>

    @Query("SELECT SUM(pages_read) FROM reading_sessions WHERE date = :date")
    fun getTotalPagesReadForDate(date: String): Flow<Int?>

    @Query("SELECT SUM(duration) FROM reading_sessions WHERE date = :date")
    fun getTotalReadingTimeForDate(date: String): Flow<Long?>

    @Query("SELECT COUNT(DISTINCT date) FROM reading_sessions")
    fun getTotalDaysRead(): Flow<Int>

    @Query("SELECT SUM(pages_read) FROM reading_sessions")
    fun getTotalPagesRead(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSessionEntity): Long

    @Update
    suspend fun updateSession(session: ReadingSessionEntity)

    @Delete
    suspend fun deleteSession(session: ReadingSessionEntity)

    @Query("DELETE FROM reading_sessions WHERE book_id = :bookId")
    suspend fun deleteSessionsForBook(bookId: String)

    @Query("SELECT * FROM reading_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ReadingSessionEntity?

    @Query("SELECT * FROM reading_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions ORDER BY start_time DESC")
    suspend fun getAllSessionsSync(): List<ReadingSessionEntity>
}
