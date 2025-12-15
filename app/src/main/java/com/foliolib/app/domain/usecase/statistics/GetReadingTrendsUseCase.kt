package com.foliolib.app.domain.usecase.statistics

import com.foliolib.app.domain.repository.ReadingTrend
import com.foliolib.app.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReadingTrendsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(months: Int = 12): Flow<List<ReadingTrend>> {
        return statisticsRepository.getReadingTrendsByMonth(months)
    }
}
