package com.foliolib.app.domain.usecase.statistics

import com.foliolib.app.domain.model.Statistics
import com.foliolib.app.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOverallStatisticsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(): Flow<Statistics> {
        return statisticsRepository.getOverallStatistics()
    }
}
