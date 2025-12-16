package com.foliolib.app.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.foliolib.app.domain.repository.ShelfRepository
import com.foliolib.app.domain.repository.UserPreferencesRepository
import com.foliolib.app.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val appVersion: String = "1.0.0",
    val isDarkTheme: Boolean = false,
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val showTimePicker: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showDataExportDialog: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences().collect { prefs ->
                if (prefs != null) {
                    val (hour, minute) = if (prefs.readingReminderTime != null) {
                        try {
                            val parts = prefs.readingReminderTime.split(":")
                            parts[0].toInt() to parts[1].toInt()
                        } catch (e: Exception) {
                            20 to 0
                        }
                    } else {
                        20 to 0
                    }

                    _uiState.update {
                        it.copy(
                            themeMode = prefs.themeMode,
                            isDarkTheme = prefs.themeMode == "DARK",
                            notificationsEnabled = prefs.readingReminderEnabled,
                            reminderHour = hour,
                            reminderMinute = minute
                        )
                    }
                }
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newMode = if (_uiState.value.isDarkTheme) "LIGHT" else "DARK"
            _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme, themeMode = newMode) }
            userPreferencesRepository.updateThemeMode(newMode)
        }
    }

    fun toggleNotifications() {
        val newEnabled = !_uiState.value.notificationsEnabled
        _uiState.update { it.copy(notificationsEnabled = newEnabled) }
        viewModelScope.launch {
            userPreferencesRepository.updateReadingReminderEnabled(newEnabled)
            if (newEnabled) {
                scheduleReminder(_uiState.value.reminderHour, _uiState.value.reminderMinute)
            } else {
                workManager.cancelUniqueWork("reading_reminder_work")
            }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        val time = String.format("%02d:%02d", hour, minute)
        _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute, showTimePicker = false) }
        viewModelScope.launch {
            userPreferencesRepository.updateReadingReminderTime(time)
            scheduleReminder(hour, minute)
        }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showTimePicker = true) }
    }

    fun hideTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0)
        if (target.isBefore(now)) {
            target = target.plusDays(1)
        }
        val delay = Duration.between(now, target).toMillis()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
             .setInitialDelay(delay, TimeUnit.MILLISECONDS)
             .addTag("reading_reminder")
             .build()
             
        workManager.enqueueUniquePeriodicWork(
            "reading_reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    fun showAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun hideAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    fun showDataExportDialog() {
        _uiState.update { it.copy(showDataExportDialog = true) }
    }

    fun hideDataExportDialog() {
        _uiState.update { it.copy(showDataExportDialog = false, exportSuccess = false) }
    }

    fun initializeDefaultShelves() {
        viewModelScope.launch {
            shelfRepository.ensureDefaultShelves()
                .onSuccess {
                    Timber.d("Default shelves initialized successfully")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to initialize default shelves")
                }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            // TODO: Implement actual data export
            // For now, just simulate
            delay(1500)

            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportSuccess = true
                )
            }

            Timber.d("Data export completed")
        }
    }
}
