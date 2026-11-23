package dev.openpoli.app.data.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.context.GlobalContext

actual fun createDatabase(): AppDatabase {
    val context = GlobalContext.get().get<Context>()
    val file = context.getDatabasePath("openpoli.db")
    file.parentFile?.mkdirs()

    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = file.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
