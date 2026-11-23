package dev.openpoli.app.domain.usecase

import dev.openpoli.app.domain.model.Item
import dev.openpoli.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

class ObserveItemsUseCase(
    private val repository: ItemRepository,
) {
    operator fun invoke(): Flow<List<Item>> = repository.observeItems()
}

class RefreshItemsUseCase(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.refresh()
}
