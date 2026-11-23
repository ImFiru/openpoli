package dev.openpoli.app.data.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/**
 * Room 3 requires a `RoomDatabaseConstructor` on non-Android targets, and keeps
 * `Room.databaseBuilder` in the platform artifacts, so the builder is
 * `expect/actual` per platform.
 */
expect fun createDatabase(): AppDatabase

@Database(entities = [ItemEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

// The Room compiler generates the actual implementations per target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
