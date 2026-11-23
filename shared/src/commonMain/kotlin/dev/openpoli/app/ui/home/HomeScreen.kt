package dev.openpoli.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.openpoli.app.platform.AppInfo
import dev.openpoli.app.presentation.home.HomeUiAction
import dev.openpoli.app.presentation.home.HomeUiState
import dev.openpoli.app.presentation.home.HomeViewModel
import dev.openpoli.app.presentation.home.ItemUiState
import dev.openpoli.app.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    appInfo: AppInfo = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        platform = appInfo.platform,
        onAction = viewModel::onAction,
    )
}

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    platform: String,
    onAction: (HomeUiAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "OpenPoli on $platform",
            style = MaterialTheme.typography.headlineSmall,
        )

        Button(onClick = { onAction(HomeUiAction.Refresh) }) {
            Text("Refresh")
        }

        if (uiState.isRefreshing) {
            CircularProgressIndicator()
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = uiState.items, key = { it.id }) { item ->
                Text(text = "${item.id}. ${item.title}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    AppTheme {
        HomeContent(
            uiState =
                HomeUiState(
                    items =
                        listOf(
                            ItemUiState(id = 1, title = "Ship the scaffold"),
                            ItemUiState(id = 2, title = "Map Penpot tokens"),
                        ),
                    isLoading = false,
                ),
            platform = "Preview",
            onAction = {},
        )
    }
}
