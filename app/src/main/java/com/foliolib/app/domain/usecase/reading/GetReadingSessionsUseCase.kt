package com.foliolib.app.domain.usecase.reading

import com.foliolib.app.domain.model.ReadingSession
import com.foliolib.app.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReadingSessionsUseCase @Inject constructor(
    private val readingRepository: ReadingRepository
) {
    operator fun invoke(bookId: String): Flow<List<ReadingSession>> {
        return readingRepository.getAllSessionsForBook(bookId)
    }
}
