package com.foliolib.app.domain.usecase.shelf

import com.foliolib.app.domain.repository.ShelfRepository
import javax.inject.Inject

class DeleteShelfUseCase @Inject constructor(
    private val shelfRepository: ShelfRepository
) {
    suspend operator fun invoke(shelfId: String): Result<Unit> {
        return shelfRepository.deleteShelf(shelfId)
    }
}
