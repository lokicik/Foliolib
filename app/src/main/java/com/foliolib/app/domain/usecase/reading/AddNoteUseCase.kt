package com.foliolib.app.domain.usecase.reading

import com.foliolib.app.domain.model.Note
import com.foliolib.app.domain.repository.ReadingRepository
import java.util.UUID
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val readingRepository: ReadingRepository
) {
    suspend operator fun invoke(
        bookId: String,
        content: String,
        page: Int? = null,
        chapter: String? = null,
        color: String = "#FFEB3B" // Default yellow
    ): Result<Unit> {
        val note = Note(
            id = UUID.randomUUID().toString(),
            bookId = bookId,
            content = content,
            page = page,
            chapter = chapter,
            color = color,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return readingRepository.addNote(note)
    }
}
