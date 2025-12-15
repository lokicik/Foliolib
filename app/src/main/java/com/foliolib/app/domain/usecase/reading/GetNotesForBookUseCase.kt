package com.foliolib.app.domain.usecase.reading

import com.foliolib.app.domain.model.Note
import com.foliolib.app.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesForBookUseCase @Inject constructor(
    private val readingRepository: ReadingRepository
) {
    operator fun invoke(bookId: String): Flow<List<Note>> {
        return readingRepository.getNotesForBook(bookId)
    }
}
