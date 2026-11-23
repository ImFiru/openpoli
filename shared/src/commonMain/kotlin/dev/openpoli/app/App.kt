package dev.openpoli.app

import androidx.compose.runtime.Composable
import dev.openpoli.app.ui.home.HomeScreen
import dev.openpoli.app.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        HomeScreen()
    }
}
