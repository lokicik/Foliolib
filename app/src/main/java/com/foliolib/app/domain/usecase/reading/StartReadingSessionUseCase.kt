package com.foliolib.app.domain.usecase.reading

import com.foliolib.app.domain.model.ReadingSession
import com.foliolib.app.domain.repository.ReadingRepository
import javax.inject.Inject

class StartReadingSessionUseCase @Inject constructor(
    private val readingRepository: ReadingRepository
) {
    suspend operator fun invoke(bookId: String): Result<ReadingSession> {
        return readingRepository.startReadingSession(bookId)
    }
}
