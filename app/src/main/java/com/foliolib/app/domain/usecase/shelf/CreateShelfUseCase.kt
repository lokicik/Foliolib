package com.foliolib.app.domain.usecase.shelf

import com.foliolib.app.domain.model.Shelf
import com.foliolib.app.domain.repository.ShelfRepository
import java.util.UUID
import javax.inject.Inject

class CreateShelfUseCase @Inject constructor(
    private val shelfRepository: ShelfRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String? = null,
        color: String,
        icon: String? = null
    ): Result<Unit> {
        val shelf = Shelf(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            color = color,
            icon = icon,
            isDefault = false,
            sortOrder = 999, // Custom shelves at the end
            createdAt = System.currentTimeMillis(),
            bookCount = 0
        )
        return shelfRepository.createShelf(shelf)
    }
}
