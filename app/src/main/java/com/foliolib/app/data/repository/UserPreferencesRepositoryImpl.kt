package com.foliolib.app.data.repository

import com.foliolib.app.core.di.IoDispatcher
import com.foliolib.app.data.local.dao.UserPreferencesDao
import com.foliolib.app.data.local.entity.UserPreferencesEntity
import com.foliolib.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferencesEntity?> {
        return userPreferencesDao.getUserPreferences()
            .flowOn(ioDispatcher)
    }

    override suspend fun updateThemeMode(themeMode: String) = withContext(ioDispatcher) {
        Timber.d("Updating theme mode to: $themeMode")
        try {
            // Ensure preferences exist first
            ensureDefaultPreferences()
            userPreferencesDao.updateThemeMode(themeMode)
        } catch (e: Exception) {
            Timber.e(e, "Error updating theme mode")
            throw e
        }
    }

    override suspend fun updateReadingReminderEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        Timber.d("Updating reading reminder enabled to: $enabled")
        try {
            ensureDefaultPreferences()
            userPreferencesDao.updateReadingReminderEnabled(enabled)
        } catch (e: Exception) {
            Timber.e(e, "Error updating reading reminder")
            throw e
        }
    }

    override suspend fun updateReadingReminderTime(time: String) = withContext(ioDispatcher) {
        Timber.d("Updating reading reminder time to: $time")
        try {
            ensureDefaultPreferences()
            userPreferencesDao.updateReadingReminderTime(time)
        } catch (e: Exception) {
            Timber.e(e, "Error updating reading reminder time")
            throw e
        }
    }

    override suspend fun ensureDefaultPreferences() = withContext(ioDispatcher) {
        try {
            val existing = userPreferencesDao.getUserPreferencesOnce()
            if (existing == null) {
                Timber.d("Creating default user preferences")
                userPreferencesDao.insertUserPreferences(UserPreferencesEntity())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error ensuring default preferences")
            // Don't throw, let the caller handle it
        }
    }
}
