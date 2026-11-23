package dev.openpoli.app.data.database

import android.content.Context
import org.koin.core.context.GlobalContext

actual fun databasePath(): String {
    val context = GlobalContext.get().get<Context>()
    val file = context.getDatabasePath("openpoli.db")
    file.parentFile?.mkdirs()
    return file.absolutePath
}
