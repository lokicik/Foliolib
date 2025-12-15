package com.foliolib.app.domain.usecase.book

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.repository.BookRepository
import javax.inject.Inject

class GetBookByIsbnUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(isbn: String): Result<Book> {
        return bookRepository.getBookByIsbn(isbn)
    }
}
