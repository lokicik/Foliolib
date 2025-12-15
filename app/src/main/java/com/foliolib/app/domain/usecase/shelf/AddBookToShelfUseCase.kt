package com.foliolib.app.domain.usecase.shelf

import com.foliolib.app.domain.repository.ShelfRepository
import javax.inject.Inject

class AddBookToShelfUseCase @Inject constructor(
    private val shelfRepository: ShelfRepository
) {
    suspend operator fun invoke(bookId: String, shelfId: String): Result<Unit> {
        return shelfRepository.addBookToShelf(bookId, shelfId)
    }
}
