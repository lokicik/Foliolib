package com.foliolib.app.domain.usecase.shelf

import com.foliolib.app.domain.model.Shelf
import com.foliolib.app.domain.repository.ShelfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllShelvesUseCase @Inject constructor(
    private val shelfRepository: ShelfRepository
) {
    operator fun invoke(): Flow<List<Shelf>> {
        return shelfRepository.getAllShelves()
    }
}
