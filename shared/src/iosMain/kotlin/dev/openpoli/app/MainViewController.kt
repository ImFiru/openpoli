package dev.openpoli.app

import androidx.compose.ui.window.ComposeUIViewController
import dev.openpoli.app.di.appModules
import dev.openpoli.app.di.iosPlatformModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun initKoin() {
    startKoin {
        modules(appModules + iosPlatformModule)
    }
}

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
