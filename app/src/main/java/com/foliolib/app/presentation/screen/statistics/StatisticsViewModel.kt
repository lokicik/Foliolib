package com.foliolib.app.presentation.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foliolib.app.domain.model.Statistics
import com.foliolib.app.domain.repository.AuthorStats
import com.foliolib.app.domain.repository.GenreStats
import com.foliolib.app.domain.repository.ReadingTrend
import com.foliolib.app.domain.usecase.statistics.GetFavoriteAuthorsUseCase
import com.foliolib.app.domain.usecase.statistics.GetGenreDistributionUseCase
import com.foliolib.app.domain.usecase.statistics.GetOverallStatisticsUseCase
import com.foliolib.app.domain.usecase.statistics.GetReadingTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val statistics: Statistics? = null,
    val genreDistribution: List<GenreStats> = emptyList(),
    val readingTrends: List<ReadingTrend> = emptyList(),
    val favoriteAuthors: List<AuthorStats> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getOverallStatisticsUseCase: GetOverallStatisticsUseCase,
    private val getGenreDistributionUseCase: GetGenreDistributionUseCase,
    private val getReadingTrendsUseCase: GetReadingTrendsUseCase,
    private val getFavoriteAuthorsUseCase: GetFavoriteAuthorsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            combine(
                getOverallStatisticsUseCase(),
                getGenreDistributionUseCase(),
                getReadingTrendsUseCase(),
                getFavoriteAuthorsUseCase()
            ) { stats, genres, trends, authors ->
                StatisticsUiState(
                    statistics = stats,
                    genreDistribution = genres,
                    readingTrends = trends,
                    favoriteAuthors = authors,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
