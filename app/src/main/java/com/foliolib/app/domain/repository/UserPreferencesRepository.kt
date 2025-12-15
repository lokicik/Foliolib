package com.foliolib.app.domain.repository

import com.foliolib.app.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferencesEntity?>
    suspend fun updateThemeMode(themeMode: String)
    suspend fun updateReadingReminderEnabled(enabled: Boolean)
    suspend fun ensureDefaultPreferences()
}
