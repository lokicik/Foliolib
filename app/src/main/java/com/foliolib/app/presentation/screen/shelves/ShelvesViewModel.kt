package com.foliolib.app.presentation.screen.shelves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.model.Shelf
import com.foliolib.app.domain.usecase.shelf.CreateShelfUseCase
import com.foliolib.app.domain.usecase.shelf.DeleteShelfUseCase
import com.foliolib.app.domain.usecase.shelf.GetAllShelvesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ShelvesUiState(
    val shelves: List<Shelf> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val newShelfName: String = "",
    val newShelfDescription: String = "",
    val newShelfColor: String = "#8B5CF6", // Default violet
    val newShelfIcon: String = "folder"
)

@HiltViewModel
class ShelvesViewModel @Inject constructor(
    private val getAllShelvesUseCase: GetAllShelvesUseCase,
    private val createShelfUseCase: CreateShelfUseCase,
    private val deleteShelfUseCase: DeleteShelfUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShelvesUiState())
    val uiState: StateFlow<ShelvesUiState> = _uiState.asStateFlow()

    init {
        loadShelves()
    }

    private fun loadShelves() {
        viewModelScope.launch {
            getAllShelvesUseCase().collect { shelves ->
                _uiState.update { state ->
                    state.copy(
                        shelves = shelves,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = false,
                newShelfName = "",
                newShelfDescription = "",
                newShelfColor = "#8B5CF6",
                newShelfIcon = "folder"
            )
        }
    }

    fun updateNewShelfName(name: String) {
        _uiState.update { it.copy(newShelfName = name) }
    }

    fun updateNewShelfDescription(description: String) {
        _uiState.update { it.copy(newShelfDescription = description) }
    }

    fun updateNewShelfColor(color: String) {
        _uiState.update { it.copy(newShelfColor = color) }
    }

    fun createShelf() {
        viewModelScope.launch {
            val name = _uiState.value.newShelfName
            val description = _uiState.value.newShelfDescription.takeIf { it.isNotBlank() }
            val color = _uiState.value.newShelfColor
            val icon = _uiState.value.newShelfIcon

            if (name.isNotBlank()) {
                createShelfUseCase(
                    name = name,
                    description = description,
                    color = color,
                    icon = icon
                ).onSuccess {
                    Timber.d("Shelf created successfully")
                    hideCreateDialog()
                }.onFailure { error ->
                    Timber.e(error, "Failed to create shelf")
                    _uiState.update { it.copy(error = error.message) }
                }
            }
        }
    }

    fun deleteShelf(shelfId: String) {
        viewModelScope.launch {
            deleteShelfUseCase(shelfId)
                .onSuccess {
                    Timber.d("Shelf deleted successfully")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to delete shelf")
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
