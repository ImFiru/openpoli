package dev.openpoli.app.data.database

import java.io.File

actual fun databasePath(): String {
    val directory = File(System.getProperty("user.home"), ".openpoli").apply { mkdirs() }
    return File(directory, "openpoli.db").absolutePath
}
