package dev.openpoli.app.di

import dev.openpoli.app.platform.AppInfo
import org.koin.core.module.Module
import org.koin.dsl.module

val androidPlatformModule: Module =
    module {
        single { AppInfo(platform = "Android") }
    }
