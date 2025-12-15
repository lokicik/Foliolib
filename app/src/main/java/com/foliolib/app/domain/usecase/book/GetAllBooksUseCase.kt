package com.foliolib.app.domain.usecase.book

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(): Flow<List<Book>> {
        return bookRepository.getAllBooks()
    }
}
