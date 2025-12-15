package com.foliolib.app.domain.usecase.statistics

import com.foliolib.app.domain.repository.AuthorStats
import com.foliolib.app.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteAuthorsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<AuthorStats>> {
        return statisticsRepository.getFavoriteAuthors(limit)
    }
}
