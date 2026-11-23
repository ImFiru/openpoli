package dev.openpoli.app.data.database

import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/** Each platform decides where the database file lives. */
expect fun databasePath(): String

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

fun createDatabase(): AppDatabase =
    Room.databaseBuilder<AppDatabase>(name = databasePath())
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
