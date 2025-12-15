package com.foliolib.app.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.repository.ShelfRepository
import com.foliolib.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
    val appVersion: String = "1.0.0",
    val isDarkTheme: Boolean = false,
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val notificationsEnabled: Boolean = true,
    val showAboutDialog: Boolean = false,
    val showDataExportDialog: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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
                    _uiState.update {
                        it.copy(
                            themeMode = prefs.themeMode,
                            isDarkTheme = prefs.themeMode == "DARK"
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
        _uiState.update { it.copy(notificationsEnabled = !it.notificationsEnabled) }
        // TODO: Save preference to DataStore
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
            kotlinx.coroutines.delay(1500)

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
