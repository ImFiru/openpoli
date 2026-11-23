package dev.openpoli.app.domain.repository

import dev.openpoli.app.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observeItems(): Flow<List<Item>>

    suspend fun refresh(): Result<Unit>
}
