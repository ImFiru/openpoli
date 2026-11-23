---
name: kmp-patterns
description: "Defines implementation patterns for Kotlin Multiplatform features: Unidirectional Data Flow, ViewModel design, Flow composition, Result-based error handling, Ktor + Room 3 repositories, use cases, and Compose Multiplatform UI patterns with previews. Use this skill whenever implementing a ViewModel, designing state or action models, writing a repository, DAO or use case, composing Flows, handling errors, or building Compose screens."
license: MIT
---

# KMP Patterns

Implement features with explicit state, reactive flows and testable logic. Code shown here belongs in `commonMain` unless stated otherwise.

## 1. Unidirectional Data Flow

`User action -> ViewModel -> state update -> UI render -> optional event`

Rules:

- Composables do not own business state.
- Composables emit actions upward through callbacks.
- ViewModels own screen behavior and coordinate the flow.
- State flows down from the ViewModel into Compose.
- Events flow out through `SharedFlow` or `Channel`.

```text
UiAction -> onAction() -> internal state -> derived state -> UiState -> Composable
```

## 2. State model design

Prefer explicit types over boolean flags.

```kotlin
@Immutable
internal sealed interface FeatureScreenUiState {
    data object Loading : FeatureScreenUiState
    data class Loaded(val data: FeatureUiState) : FeatureScreenUiState
    data class Error(val error: FeatureError) : FeatureScreenUiState
}

@Immutable
internal data class FeatureUiState(
    val title: String,
    val items: List<ItemUiState>,
    val isRefreshing: Boolean = false,
)

@Immutable
internal sealed interface FeatureUiAction {
    data object Retry : FeatureUiAction
    data class QueryChanged(val value: String) : FeatureUiAction
}

internal sealed interface FeatureUiEvent {
    data class ShowError(val message: String) : FeatureUiEvent
    data object NavigateBack : FeatureUiEvent
}
```

Rules:

- Keep actions raw and user driven.
- Keep events transient.
- Keep `UiState` renderable, no framework types.
- Keep raw typed values in a private input state when the ViewModel needs them.

## 3. ViewModel pattern

ViewModels coordinate; they do not calculate everything.

```kotlin
internal class FeatureViewModel(
    private val observeItems: ObserveItemsUseCase,
    private val mapper: FeatureUiStateMapper,
) : ViewModel() {

    private val _internalState = MutableStateFlow<FeatureInput?>(null)
    private val _uiEvents = MutableSharedFlow<FeatureUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<FeatureUiEvent> = _uiEvents.asSharedFlow()

    val uiState: StateFlow<FeatureScreenUiState> =
        observeItems.observe()
            .map { result -> result.toScreenState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FeatureScreenUiState.Loading,
            )

    fun onAction(action: FeatureUiAction) {
        when (action) {
            FeatureUiAction.Retry -> retry()
            is FeatureUiAction.QueryChanged -> updateQuery(action.value)
        }
    }
}
```

Rules:

- Construction by Koin: `viewModelOf(::FeatureViewModel)`. No DI annotations.
- `viewModelScope` comes from `androidx.lifecycle:lifecycle-viewmodel` (multiplatform). Keep it — but only if that artifact is on `commonMain`; otherwise inject a `CoroutineScope`.
- `stateIn` for `StateFlow` exposure, `WhileSubscribed(...)` for UI-bound streams.
- `onAction(action)` is the single entry point for user input.
- Keep mutable state private; emit events for one-shot effects.
- Do not parse API responses, format display strings or leak DTOs.

## 4. Flow composition

- `combine` when output depends on several latest values.
- `flatMapLatest` when new upstream input must cancel previous work.
- `map` for pure transformations.
- `scan` when the next value depends on the previous.
- `distinctUntilChanged` to stop redundant recomputation.
- `shareIn`/`stateIn` to share upstream work.

Rules:

- Prefer reactive composition over manual callbacks.
- Do not create duplicate subscriptions to the same source.
- Derive flows instead of copying state by hand.

## 5. Error handling

```kotlin
sealed interface FeatureError {
    data object Network : FeatureError
    data object Unauthorized : FeatureError
    data object NotFound : FeatureError
    data class Unknown(val cause: String?) : FeatureError
}
```

Rules:

- `Result<T>` for operations that may fail.
- No thrown exceptions through flow pipelines when a recoverable result model suffices.
- Map low-level failures (Ktor `HttpStatusCode`, SQLite exceptions, serialization errors) into domain errors at the boundary where they happen.
- Keep one-shot UI errors in events; keep durable errors in state.

Patterns:

- initial load failure -> `Error` screen state
- recoverable refresh failure -> keep last good data, emit a snackbar event
- unrecoverable failure -> error state with retry action

## 6. Repository pattern

Repositories expose domain models, never transport or database models.

```kotlin
interface ItemRepository {
    fun observeItems(): Flow<List<Item>>
    suspend fun refresh(): Result<Unit>
}
```

Rules:

- `Flow<Result<T>>` or `suspend fun ...: Result<T>`; pick one style per repository and keep it.
- No DTOs, Ktor types or Room entities in the interface.
- Network, cache, sync and mapping happen in `data`.
- Keep mapping in dedicated mappers.

Full Ktor client factory, Room 3 database, DAO placement and a complete repository implementation: `references/ktor-room.md`.

## 7. Use cases

Names: `FetchItemsUseCase`, `ObserveItemsUseCase`, `SubmitFormUseCase`.

- One public function per use case: `fetchItemsUseCase.fetch()`, `observeItemsUseCase.observe()`.
- Small and focused; the stable boundary between presentation and domain.
- No framework imports.

## 8. Compose patterns

### Screen and content split

```kotlin
@Composable
internal fun FeatureScreen(viewModel: FeatureViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureContent(uiState = uiState, onAction = viewModel::onAction)
}

@Composable
internal fun FeatureContent(
    uiState: FeatureScreenUiState,
    onAction: (FeatureUiAction) -> Unit,
) { /* stateless */ }
```

Rules:

- Screen owns collection and side effects; `FeatureContent` is stateless.
- Never pass a ViewModel into nested components.
- Every screen and every non-trivial component gets a `@Preview`.
- `collectAsStateWithLifecycle()` requires `androidx.lifecycle:lifecycle-runtime-compose` on `commonMain`.

### One-shot events

Collect in `LaunchedEffect` at the screen layer; never inside content.

### Local UI state

Keep purely visual state in Compose: bottom sheet visibility, focus, expansion, animation. Everything else belongs in the ViewModel.

### State hoisting

Hoist to the nearest composable that shares the state; keep content functions parameter-in, callback-out.

### Lists

Stable `key` whenever item identity exists.

### Text input

Local `TextFieldValue` only when cursor or selection must be preserved. Visual transformation for display only; keep raw input separate.

### Theming

No hardcoded colors, type or spacing that exist as design tokens. Tokens come from Penpot and map to `MaterialTheme`; see `penpot-to-compose`.

Material 3 Expressive APIs are version-gated in Compose Multiplatform. Check availability before use, and fall back to standard Material 3 when missing — `penpot-to-compose` lists what is currently present.

## 9. Mapping and calculation

Keep out of composables. Use dedicated classes:

- parser: text -> typed input
- formatter: typed value -> display value
- mapper: model A -> model B
- calculator: domain rules and state derivation

## 10. Review checklist

- Does the ViewModel coordinate rather than compute everything?
- Are actions, state and events distinct?
- Are flows shared and lifecycle aware?
- Is the UI stateless where possible?
- Are errors explicit and mapped?
- Are mapping and formatting extracted?
- Does every screen have a preview?
- Does anything in `commonMain` import a platform package?

Refactor when:

- a composable mutates business state
- a ViewModel formats UI strings
- a repository leaks DTOs or entities
- flow subscriptions are duplicated
- a feature uses flags where sealed state would be clearer
