package com.foliolib.app.domain.usecase.reading

import com.foliolib.app.domain.repository.ReadingRepository
import javax.inject.Inject

class GetReadingStreakUseCase @Inject constructor(
    private val readingRepository: ReadingRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return readingRepository.getReadingStreak()
    }
}
