package dev.openpoli.app.data.mapper

import dev.openpoli.app.data.api.ItemDto
import dev.openpoli.app.data.database.ItemEntity
import dev.openpoli.app.domain.model.Item

internal interface ItemMapper {
    fun toDomain(entity: ItemEntity): Item
    fun toEntity(dto: ItemDto): ItemEntity
}

internal class DefaultItemMapper : ItemMapper {
    override fun toDomain(entity: ItemEntity): Item = Item(id = entity.id, title = entity.title)

    override fun toEntity(dto: ItemDto): ItemEntity = ItemEntity(id = dto.id, title = dto.title)
}
