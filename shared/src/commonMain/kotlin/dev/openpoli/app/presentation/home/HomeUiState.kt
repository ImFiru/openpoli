package dev.openpoli.app.presentation.home

import androidx.compose.runtime.Immutable

@Immutable
data class ItemUiState(
    val id: Int,
    val title: String,
)

@Immutable
data class HomeUiState(
    val items: List<ItemUiState> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@Immutable
sealed interface HomeUiAction {
    data object Refresh : HomeUiAction
}
