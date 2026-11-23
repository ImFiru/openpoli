package dev.openpoli.app.data.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun createDatabase(): AppDatabase {
    val directory = File(System.getProperty("user.home"), ".openpoli").apply { mkdirs() }
    val file = File(directory, "openpoli.db")

    return Room.databaseBuilder<AppDatabase>(name = file.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
