package com.foliolib.app.domain.usecase.reading

import com.foliolib.app.domain.repository.ReadingRepository
import javax.inject.Inject

class EndReadingSessionUseCase @Inject constructor(
    private val readingRepository: ReadingRepository
) {
    suspend operator fun invoke(sessionId: String, pagesRead: Int): Result<Unit> {
        return readingRepository.endReadingSession(sessionId, pagesRead)
    }
}
