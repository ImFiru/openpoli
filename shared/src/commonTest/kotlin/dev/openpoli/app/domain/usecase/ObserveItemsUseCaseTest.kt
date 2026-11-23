package dev.openpoli.app.domain.usecase

import app.cash.turbine.test
import dev.openpoli.app.domain.model.Item
import dev.openpoli.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FakeItemRepository : ItemRepository {
    private val items = MutableStateFlow<List<Item>>(emptyList())

    var refreshResult: Result<Unit> = Result.success(Unit)

    fun emit(value: List<Item>) {
        items.value = value
    }

    override fun observeItems(): Flow<List<Item>> = items

    override suspend fun refresh(): Result<Unit> = refreshResult
}

internal class ObserveItemsUseCaseTest {
    private val repository = FakeItemRepository()
    private lateinit var sut: ObserveItemsUseCase

    @BeforeTest
    fun setUp() {
        sut = ObserveItemsUseCase(repository)
    }

    @Test
    fun `When repository emits items then use case emits them in order`() =
        runTest {
            // create objects and fakes
            val expected = listOf(Item(id = 1, title = "A"), Item(id = 2, title = "B"))

            // configure fake behavior
            repository.emit(expected)

            // execute
            val actual = sut().first()

            // assert
            assertEquals(expected = expected, actual = actual)
        }

    @Test
    fun `When repository emits twice then every value is observed`() =
        runTest {
            // create objects and fakes
            val first = listOf(Item(id = 1, title = "A"))
            val second = first + Item(id = 2, title = "B")

            // configure fake behavior: StateFlow replays its current value on collect
            repository.emit(first)

            sut().test {
                assertEquals(expected = first, actual = awaitItem())

                repository.emit(second)
                assertEquals(expected = second, actual = awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
