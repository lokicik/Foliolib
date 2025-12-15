package com.foliolib.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1, // Single row table
    @ColumnInfo(name = "theme_mode") val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    @ColumnInfo(name = "reading_reminder_enabled") val readingReminderEnabled: Boolean = false,
    @ColumnInfo(name = "reading_reminder_time") val readingReminderTime: String? = null, // HH:MM format
    @ColumnInfo(name = "default_shelf_id") val defaultShelfId: String? = null,
    @ColumnInfo(name = "onboarding_completed") val onboardingCompleted: Boolean = false,
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "longest_streak") val longestStreak: Int = 0,
    @ColumnInfo(name = "last_read_date") val lastReadDate: String? = null
)
