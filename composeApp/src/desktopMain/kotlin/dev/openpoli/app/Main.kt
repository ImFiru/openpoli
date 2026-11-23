package dev.openpoli.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.openpoli.app.di.appModules
import dev.openpoli.app.di.desktopPlatformModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModules + desktopPlatformModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "OpenPoli",
    ) {
        App()
    }
}
