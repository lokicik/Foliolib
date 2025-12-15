package com.foliolib.app.domain.usecase.book

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.repository.BookRepository
import timber.log.Timber
import javax.inject.Inject

class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(book: Book): Result<Unit> {
        return try {
            bookRepository.insertBook(book)
            Timber.d("Book added successfully: ${book.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding book")
            Result.failure(e)
        }
    }
}
