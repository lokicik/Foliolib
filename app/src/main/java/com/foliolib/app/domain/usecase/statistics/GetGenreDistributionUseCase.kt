package com.foliolib.app.domain.usecase.statistics

import com.foliolib.app.domain.repository.GenreStats
import com.foliolib.app.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGenreDistributionUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(): Flow<List<GenreStats>> {
        return statisticsRepository.getGenreDistribution()
    }
}
