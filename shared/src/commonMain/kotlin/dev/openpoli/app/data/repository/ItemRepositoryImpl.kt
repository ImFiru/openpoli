package dev.openpoli.app.data.repository

import dev.openpoli.app.data.api.ItemApi
import dev.openpoli.app.data.database.ItemDao
import dev.openpoli.app.data.mapper.ItemMapper
import dev.openpoli.app.domain.model.Item
import dev.openpoli.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ItemRepositoryImpl(
    private val api: ItemApi,
    private val dao: ItemDao,
    private val mapper: ItemMapper,
) : ItemRepository {

    override fun observeItems(): Flow<List<Item>> =
        dao.observeAll().map { entities -> entities.map(mapper::toDomain) }

    override suspend fun refresh(): Result<Unit> =
        api.fetchItems().map { dtos ->
            dao.upsert(dtos.map(mapper::toEntity))
        }
}
