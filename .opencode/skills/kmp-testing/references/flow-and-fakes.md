# Flow, fakes and dispatchers — full examples

Reference for `kmp-testing`. All code is `commonTest`.

## Dependencies

```kotlin
// commonTest
implementation(kotlin("test"))
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:<coroutines>")
implementation("app.cash.turbine:turbine:<turbine>")
```

```kotlin
// androidUnitTest / jvmTest only
implementation("io.mockk:mockk:<mockk>")
implementation("org.junit.jupiter:junit-jupiter:<junit5>")
```

## A fake with controllable failure and emissions

```kotlin
internal class FakeItemRepository : ItemRepository {
    private val items = MutableStateFlow<List<Item>>(emptyList())
    private val refreshCalls = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    var refreshResult: Result<Unit> = Result.success(Unit)
    var emitDelayMillis: Long = 0

    fun emit(value: List<Item>) { items.value = value }

    fun failRefresh(error: Throwable) { refreshResult = Result.failure(error) }

    fun refreshCount(): Int = refreshCalls.replayCache.size

    override fun observeItems(): Flow<List<Item>> =
        items.onEach { if (emitDelayMillis > 0) delay(emitDelayMillis) }

    override suspend fun refresh(): Result<Unit> {
        refreshCalls.tryEmit(Unit)
        return refreshResult
    }
}
```

Rules:

- Reuse one fake per interface; add fields, not subclasses.
- Never record call order unless the contract genuinely cares about it.
- Reset state in `@BeforeTest` if the fake is a field of a shared class.

## Turbine patterns

### StateFlow: assert every state

```kotlin
@Test
fun `When data loads then state goes loading to loaded`() = runTest {
    repository.emit(listOf(Item("1", "A")))

    sut.uiState.test {
        assertEquals(expected = FeatureScreenUiState.Loading, actual = awaitItem())
        val actual = awaitItem()
        assertIs<FeatureScreenUiState.Loaded>(actual)
        assertEquals(expected = 1, actual = actual.data.items.size)
        cancelAndIgnoreRemainingEvents()
    }
}
```

### SharedFlow events: assert exact events

```kotlin
@Test
fun `When retry fails then a show error event is emitted`() = runTest {
    repository.failRefresh(DomainException.Network)

    sut.uiEvents.test {
        sut.onAction(FeatureUiAction.Retry)
        assertEquals(
            expected = FeatureUiEvent.ShowError("offline"),
            actual = awaitItem(),
        )
        expectNoEvents()
    }
}
```

### Proving silence and completion

```kotlin
flow.test {
    expectNoEvents()
    awaitComplete()
}
```

### Error contract

```kotlin
flow.test {
    val actual = awaitError()
    assertIs<DomainException.Http>(actual)
}
```

## Dispatcher injection

Production code should not hardcode `Dispatchers.IO`; take a dispatcher.

```kotlin
internal class ItemRepositoryImpl(
    private val api: ItemApi,
    private val dao: ItemDao,
    private val io: CoroutineDispatcher,
) : ItemRepository
```

Test it with the scheduler's dispatcher:

```kotlin
@Test
fun `When refresh succeeds then DAO receives mapped entities`() = runTest {
    val dao = FakeItemDao()
    val sut = ItemRepositoryImpl(FakeItemApi(), dao, StandardTestDispatcher(testScheduler))

    val actual = sut.refresh()

    assertTrue(actual.isSuccess)
    assertEquals(expected = listOf(ItemEntity("1", "A", "now")), actual = dao.stored())
}
```

## Koin overrides in tests

```kotlin
@Test
fun `When repository fails then state is error`() = runTest {
    val fake = FakeItemRepository().apply { failRefresh(DomainException.Network) }

    startKoin {
        modules(
            module {
                single<ItemRepository> { fake }
                single<CoroutineDispatcher>(named("io")) { StandardTestDispatcher(testScheduler) }
            },
        )
    }

    val sut: FeatureViewModel = getKoin().get()
    // assert
    stopKoin()
}
```

Rules:

- Prefer constructor injection in tests; use Koin overrides only when the test is about wiring.
- Always `stopKoin()` in `@AfterTest`.
- Never touch `Dispatchers.setMain` when the ViewModel takes a dispatcher.

## ViewModel with `viewModelScope`

`viewModelScope` uses `Dispatchers.Main.immediate`. In `commonTest` there is no Main dispatcher, so either:

1. inject the scope/dispatcher (preferred), or
2. call `Dispatchers.setMain(StandardTestDispatcher(testScheduler))` in `@BeforeTest` and `resetMain()` in `@AfterTest` — `kotlinx-coroutines-test` supports this on every target.

## Test file skeleton

```kotlin
internal class FeatureViewModelTest {
    private val repository = FakeItemRepository()
    private lateinit var sut: FeatureViewModel

    @BeforeTest
    fun setUp() {
        sut = FeatureViewModel(ObserveItemsUseCase(repository), FeatureUiStateMapper())
    }

    @Test
    fun `When items load then state is loaded`() = runTest { /* ... */ }
}
```
