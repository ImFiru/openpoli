package dev.openpoli.app

import android.app.Application
import dev.openpoli.app.di.androidPlatformModule
import dev.openpoli.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OpenPoliApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpenPoliApp)
            modules(appModules + androidPlatformModule)
        }
    }
}
