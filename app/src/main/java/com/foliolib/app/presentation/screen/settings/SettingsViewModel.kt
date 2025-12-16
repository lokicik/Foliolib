package com.foliolib.app.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import android.content.Context
import androidx.work.WorkManager
import com.foliolib.app.domain.repository.BookRepository
import com.foliolib.app.domain.repository.ShelfRepository
import com.foliolib.app.domain.repository.UserPreferencesRepository
import com.foliolib.app.worker.ReminderWorker
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

enum class ExportFormat {
    JSON, CSV
}

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
    val exportSuccess: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.JSON,
    val currentLanguage: String = "en", // en, es, tr
    val showLanguageDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shelfRepository: ShelfRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookRepository: BookRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadCurrentLanguage()
    }

    private fun loadCurrentLanguage() {
        val locales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (!locales.isEmpty) {
            locales.get(0)?.language ?: "en"
        } else {
            "en"
        }
        _uiState.update { it.copy(currentLanguage = currentTag) }
    }

    fun setLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        _uiState.update { it.copy(currentLanguage = languageCode, showLanguageDialog = false) }
    }

    fun showLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun hideLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
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

    fun setExportFormat(format: ExportFormat) {
        _uiState.update { it.copy(exportFormat = format) }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            try {
                val books = bookRepository.getAllBooks().first()
                val format = _uiState.value.exportFormat
                val fileName = "foliolib_export_${System.currentTimeMillis()}.${format.name.lowercase()}"
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                val content = withContext(Dispatchers.Default) {
                    when (format) {
                        ExportFormat.JSON -> {
                            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                            val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, com.foliolib.app.domain.model.Book::class.java)
                            val adapter = moshi.adapter<List<com.foliolib.app.domain.model.Book>>(type)
                            adapter.toJson(books)
                        }
                        ExportFormat.CSV -> {
                            val header = "Title,Authors,ISBN,Status,Rating,Date Added\n"
                            val rows = books.joinToString("\n") { book ->
                                val authors = book.authors.joinToString(";").replace(",", " ")
                                val title = book.title.replace(",", " ")
                                "${title},${authors},${book.isbn ?: ""},${book.readingStatus},${book.rating ?: ""},${book.dateAdded}"
                            }
                            header + rows
                        }
                    }
                }

                withContext(Dispatchers.IO) {
                    file.writeText(content)
                }

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportSuccess = true
                    )
                }
                Timber.d("Data exported to ${file.absolutePath}")
                showExportNotification(file)

            } catch (e: Exception) {
                Timber.e(e, "Export failed")
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportSuccess = false
                    )
                }
            }
        }
    }

    private fun showExportNotification(file: File) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "export_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Export Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, if (file.extension == "json") "application/json" else "text/csv")
            flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Library Exported")
            .setContentText("Tap to open ${file.name}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }
}
