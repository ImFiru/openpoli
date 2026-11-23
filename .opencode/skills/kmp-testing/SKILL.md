---
name: kmp-testing
description: "Defines testing standards for Kotlin Multiplatform projects: kotlin.test in commonTest, kotlinx-coroutines-test, Turbine for Flow, and hand-written fakes instead of mocks. Use this skill whenever writing, reviewing or refactoring unit tests for ViewModels, use cases, mappers, repositories, DAOs or other shared Kotlin code, and when deciding whether a test belongs in commonTest, androidUnitTest or jvmTest."
license: MIT
---

# KMP Testing

Test behavior, not implementation, and keep everything that can be shared in `commonTest`.

## 1. Source-set placement

| Test kind | Source set | Tools |
| --- | --- | --- |
| ViewModel, use case, mapper, repository, fake-driven logic | `commonTest` | `kotlin.test`, `kotlinx-coroutines-test`, Turbine, hand-written fakes |
| Android-specific glue (Context, framework adapters) | `androidUnitTest` | JUnit 5 **or** JUnit 4, MockK allowed |
| Desktop/JVM glue (files, system properties) | `jvmTest` | JUnit 5, MockK allowed |
| iOS glue | `iosTest` | `kotlin.test` only |
| True platform/instrumented behavior | `androidInstrumentedTest` | platform runners |

Rules:

- Default every business test to `commonTest`.
- A test that only runs on one target is a signal the code under test is not portable.
- No MockK, no Mockito, no JUnit 5 in `commonTest` — they do not resolve on iOS.
- JUnit 5 in `androidUnitTest` needs the JUnit 5 Gradle plugin; plain Android unit tests default to JUnit 4.

## 2. Test naming

Descriptive backtick names:

```kotlin
@Test
fun `When amount is valid then it returns parsed value`() { }
```

Rules:

- Condition first, expected result second.
- Read as a sentence, no extra commentary.
- One scenario per test.
- Keep the same style everywhere.

**Backtick support, verified:** the Kotlin 2.4.20 JVM compiler accepts spaces and commas in backtick names and preserves them in bytecode (`When amount is valid then it returns parsed value`). Desktop JVM and Android unit tests are confirmed. iOS cannot be compiled on a Windows host, so it is unverified here; backtick identifiers are a language feature, not a JVM one.

**Fallback rule:** if a target or runner rejects the name, switch the whole file to non-backtick `given/when/then` with underscores:

```kotlin
@Test
fun givenValidAmount_whenParsed_thenReturnsParsedValue() { }
```

Do not mix backticked and non-backticked names inside one test class.

## 3. System under test naming

```kotlin
private lateinit var sut: ExchangeViewModel
```

Rules:

- `sut` in every test file.
- Dependencies named by role (`repository`, `mapper`, `dispatcher`).
- No `mock1`, `dep`, `obj`.

## 4. Test structure

Four blocks:

1. create the subject and its fakes
2. configure fake behavior
3. execute
4. assert

```kotlin
private val repository = FakeItemRepository()
private val sut = ObserveItemsUseCase(repository)

@Test
fun `When repository emits items then use case emits them in order`() = runTest {
    // create objects and fakes
    val expected = listOf(Item(id = "1", title = "A"), Item(id = "2", title = "B"))

    // configure fake behavior
    repository.emit(expected)

    // execute
    val actual = sut.observe().first()

    // assert
    assertEquals(expected = expected, actual = actual.getOrThrow())
}
```

Rules:

- Separate arrange, act, assert.
- `expected` for the expected value, `actual` for the produced value; assert expected against actual.
- Keep fake setup next to the scenario it supports.
- One behavior per test.

## 5. Assertions

- Assert whole objects when the structure matters.
- Assert ordering when ordering matters, contents when contents matter, exact events when events matter.
- Partial assertions only when the test is explicitly about one field.

```kotlin
assertEquals(expected = expectedUiState, actual = actualUiState)
```

`kotlin.test` gives `assertEquals`, `assertTrue`, `assertFailsWith`, `assertNull`, `assertContentEquals`. They work identically on every target.

## 6. Fakes instead of mocks

Write a fake whenever the behavior is simple enough to state in a few lines — which is almost always.

```kotlin
internal class FakeItemRepository : ItemRepository {
    private val items = MutableStateFlow<List<Item>>(emptyList())
    var refreshResult: Result<Unit> = Result.success(Unit)

    fun emit(value: List<Item>) { items.value = value }

    override fun observeItems(): Flow<List<Item>> = items

    override suspend fun refresh(): Result<Unit> = refreshResult
}
```

Rules:

- Fakes implement the real interface; the compiler keeps them honest.
- Expose intent-revealing helpers (`emit`, `failWith`), not `verify`-style recording.
- One fake per interface, in `commonTest`, reusable across features.
- MockK is allowed only in `androidUnitTest`/`jvmTest`, and only for types you cannot fake reasonably (final third-party classes).

## 7. Flow testing with Turbine

```kotlin
@Test
fun `When flow emits values then they arrive in order`() = runTest {
    val flow = flowOf(1, 2, 3)

    flow.test {
        assertEquals(expected = 1, actual = awaitItem())
        assertEquals(expected = 2, actual = awaitItem())
        assertEquals(expected = 3, actual = awaitItem())
        awaitComplete()
    }
}
```

Rules:

- Turbine for every `Flow`, `StateFlow` and `SharedFlow` assertion.
- Assert emission order, failure behavior, cancellation, replay.
- Do not skip intermediate emissions that are part of the contract.
- Use `expectNoEvents()` to prove silence.

## 8. Table-driven tests

JUnit 5 parameterization does not exist in `commonTest`. Use a loop:

```kotlin
@Test
fun `When amount is invalid then parser returns null`() {
    val cases = listOf("" to null, "abc" to null, "12,3" to Money(12, 30))

    cases.forEach { (input, expected) ->
        val actual = sut.parse(input)
        assertEquals(expected = expected, actual = actual, message = "input=$input")
    }
}
```

Rules:

- Keep the case list small and readable.
- Always pass the input in the failure `message`.
- `@ParameterizedTest`, `@MethodSource`, `@CsvSource`, `@TestFactory` are allowed only in `jvmTest`/`androidUnitTest`.
- Never use `java.math.BigDecimal` or any `java.*` type in `commonTest`; use `Long` cents, the project's `Money` value class, or `kotlinx.serialization` types.

## 9. Coroutines and dispatchers

- `runTest` for all suspending code.
- Inject dispatchers (constructor or Koin qualifier); never assume `Dispatchers.Main`.
- Prefer `StandardTestDispatcher` and explicit `advanceUntilIdle()`.
- Keep virtual time; no real `delay`.
- In Koin tests, override the module instead of touching globals:

```kotlin
startKoin {
    modules(module {
        single<CoroutineDispatcher>(named("io")) { StandardTestDispatcher(testScheduler) }
    })
}
```

## 10. ViewModel testing

Test through the public API: `onAction`, `uiState`, `uiEvents`.

```kotlin
@Test
fun `When refresh fails then state keeps data and an error event is emitted`() = runTest {
    val expected = FeatureUiEvent.ShowError("offline")
    repository.refreshResult = Result.failure(DomainException.Network)

    sut.uiState.test {
        assertEquals(expected = FeatureScreenUiState.Loading, actual = awaitItem())
        sut.onAction(FeatureUiAction.Retry)
        assertEquals(expected = FeatureScreenUiState.Loaded(data), actual = awaitItem())
        cancelAndIgnoreRemainingEvents()
    }

    sut.uiEvents.test {
        assertEquals(expected = expected, actual = awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

Rules:

- Do not assert private state.
- Assert the state sequence, not just the final value.
- Assert side effects as events.
- Use a test dispatcher for `viewModelScope`.

## 11. Repository, use case, mapper tests

- Use case: assert the input to output contract.
- Mapper: assert exact mapped values, including null and unknown cases.
- Repository: assert the returned domain result for success, failure and empty paths, using a fake `ItemApi` and a fake `ItemDao`.
- Never test generated Room code; test the DAO contract only with an in-memory database when genuinely needed (target-specific test set).

## 12. Review checklist

- Test lives in the narrowest source set that can run it.
- Name explains the behavior and matches the file's style.
- `sut` used consistently.
- Arrange, act, assert separated.
- Expected and actual compared explicitly.
- Data-driven cases use a loop, not JUnit 5 parameterization.
- Flow tests use Turbine, coroutine tests use `runTest`.
- Fakes, not mocks, in `commonTest`.
- No `java.*` type anywhere in `commonTest`.

Full fake, Turbine and dispatcher examples: `references/flow-and-fakes.md`.
