package dev.openpoli.app.data.database

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val title: String,
)

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY id")
    fun observeAll(): Flow<List<ItemEntity>>

    @Upsert
    suspend fun upsert(items: List<ItemEntity>)
}
