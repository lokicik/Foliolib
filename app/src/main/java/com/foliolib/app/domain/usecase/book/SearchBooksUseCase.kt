package com.foliolib.app.domain.usecase.book

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.repository.BookRepository
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(query: String): Result<List<Book>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return bookRepository.searchBooks(query)
    }
}
