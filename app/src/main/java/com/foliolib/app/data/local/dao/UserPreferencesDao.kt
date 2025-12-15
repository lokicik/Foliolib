package com.foliolib.app.data.local.dao

import androidx.room.*
import com.foliolib.app.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getUserPreferencesOnce(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferencesEntity)

    @Update
    suspend fun updateUserPreferences(preferences: UserPreferencesEntity)

    @Query("UPDATE user_preferences SET theme_mode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String)

    @Query("UPDATE user_preferences SET reading_reminder_enabled = :enabled WHERE id = 1")
    suspend fun updateReadingReminderEnabled(enabled: Boolean)

    @Query("UPDATE user_preferences SET current_streak = :streak WHERE id = 1")
    suspend fun updateCurrentStreak(streak: Int)

    @Query("UPDATE user_preferences SET longest_streak = :streak WHERE id = 1")
    suspend fun updateLongestStreak(streak: Int)

    @Query("UPDATE user_preferences SET last_read_date = :date WHERE id = 1")
    suspend fun updateLastReadDate(date: String)

    @Query("UPDATE user_preferences SET onboarding_completed = :completed WHERE id = 1")
    suspend fun updateOnboardingCompleted(completed: Boolean)
}
