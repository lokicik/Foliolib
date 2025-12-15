package com.foliolib.app.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.repository.BookRepository
import com.foliolib.app.domain.usecase.reading.GetReadingStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val getReadingStreakUseCase: GetReadingStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        loadReadingStreak()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                bookRepository.getCurrentlyReadingBooks(),
                bookRepository.getBooksCount(),
                bookRepository.getFinishedBooksCount()
            ) { currentlyReading, totalBooks, finishedBooks ->
                _uiState.update { state ->
                    state.copy(
                        currentlyReading = currentlyReading,
                        totalBooks = totalBooks,
                        finishedBooks = finishedBooks,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    private fun loadReadingStreak() {
        viewModelScope.launch {
            getReadingStreakUseCase()
                .onSuccess { streak ->
                    _uiState.update { it.copy(currentStreak = streak) }
                    Timber.d("Reading streak loaded: $streak days")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load reading streak")
                    _uiState.update { it.copy(currentStreak = 0) }
                }
        }
    }
}
