package com.foliolib.app.domain.usecase.book

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.repository.BookRepository
import timber.log.Timber
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(book: Book): Result<Unit> {
        return try {
            bookRepository.deleteBook(book)
            Timber.d("Book deleted successfully: ${book.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting book")
            Result.failure(e)
        }
    }
}
