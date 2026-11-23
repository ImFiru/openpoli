package dev.openpoli.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.openpoli.app.domain.usecase.ObserveItemsUseCase
import dev.openpoli.app.domain.usecase.RefreshItemsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

internal class HomeViewModel(
    observeItems: ObserveItemsUseCase,
    private val refreshItems: RefreshItemsUseCase,
) : ViewModel() {
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> =
        combine(observeItems(), isRefreshing, errorMessage) { items, refreshing, error ->
            HomeUiState(
                items = items.map { ItemUiState(id = it.id, title = it.title) },
                isLoading = false,
                isRefreshing = refreshing,
                errorMessage = error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(),
        )

    fun onAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            refreshItems().onFailure { errorMessage.value = it.message ?: "Refresh failed" }
            isRefreshing.value = false
        }
    }
}
