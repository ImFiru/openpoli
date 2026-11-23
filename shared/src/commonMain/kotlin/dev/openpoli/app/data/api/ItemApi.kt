package dev.openpoli.app.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: Int,
    val title: String,
)

interface ItemApi {
    suspend fun fetchItems(): Result<List<ItemDto>>
}

internal class KtorItemApi(
    private val client: HttpClient,
    private val baseUrl: String = "https://jsonplaceholder.typicode.com",
) : ItemApi {
    override suspend fun fetchItems(): Result<List<ItemDto>> = runCatching {
        val response = client.get("$baseUrl/todos")
        check(response.status.isSuccess()) { "HTTP ${response.status.value}" }
        response.body<List<ItemDto>>()
    }
}
