package com.foliolib.app.domain.usecase.shelf

import com.foliolib.app.domain.model.Book
import com.foliolib.app.domain.repository.ShelfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBooksInShelfUseCase @Inject constructor(
    private val shelfRepository: ShelfRepository
) {
    operator fun invoke(shelfId: String): Flow<List<Book>> {
        return shelfRepository.getBooksInShelf(shelfId)
    }
}
