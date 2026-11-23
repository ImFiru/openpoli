# Ktor + Room 3 — full example

Reference for `kmp-patterns`. Everything below is `commonMain` unless a source set is named.

## Dependencies

```kotlin
// commonMain
implementation("io.ktor:ktor-client-core:<ktor>")
implementation("io.ktor:ktor-client-content-negotiation:<ktor>")
implementation("io.ktor:ktor-serialization-kotlinx-json:<ktor>")
implementation("androidx.room3:room3-runtime:3.0.3")
implementation("androidx.sqlite:sqlite-bundled:2.7.1")
ksp("androidx.room3:room3-compiler:3.0.3")

// androidMain
implementation("io.ktor:ktor-client-okhttp:<ktor>")
// iosMain
implementation("io.ktor:ktor-client-darwin:<ktor>")
// jvmMain
implementation("io.ktor:ktor-client-cio:<ktor>")
```

Room 3 publishes `android`, `jvm`, `iosArm64` and `iosSimulatorArm64` variants, so the runtime and DAOs stay in `commonMain`. KSP only — never KAPT, never Java annotation processing. Room 3 generates Kotlin.

## HttpClient engine — expect/actual

```kotlin
// commonMain/data/api/HttpClientFactory.kt
expect fun httpClientEngine(): HttpClientEngineFactory<*>

fun createHttpClient(
    engine: HttpClientEngineFactory<*> = httpClientEngine(),
): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 20_000
    }
    expectSuccess = false
}
```

```kotlin
// androidMain
actual fun httpClientEngine(): HttpClientEngineFactory<*> = OkHttp

// iosMain
actual fun httpClientEngine(): HttpClientEngineFactory<*> = Darwin

// jvmMain
actual fun httpClientEngine(): HttpClientEngineFactory<*> = CIO
```

Rules:

- The engine is the only platform-specific networking code.
- Keep `expectSuccess = false` and map status codes explicitly; thrown exceptions hide the status.
- One `HttpClient` per app, provided by Koin as `single`.

## API and DTOs

```kotlin
@Serializable
data class ItemDto(
    val id: String,
    val title: String,
    val updatedAt: String,
)

internal class KtorItemApi(private val client: HttpClient) : ItemApi {
    override suspend fun getItems(): Result<List<ItemDto>> =
        runCatching {
            val response = client.get("https://api.example.com/items")
            when (response.status.value) {
                in 200..299 -> response.body<List<ItemDto>>()
                401 -> throw DomainException.Unauthorized
                else -> throw DomainException.Http(response.status.value)
            }
        }
}
```

DTOs never leave `data`.

## Room 3 — database and DAO in commonMain

```kotlin
// commonMain/data/database/ItemEntity.kt
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val updatedAt: String,
)

// commonMain/data/database/ItemDao.kt
@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY title")
    fun observeAll(): Flow<List<ItemEntity>>

    @Upsert
    suspend fun upsert(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun clear()
}
```

Rules:

- Entities and DAOs live in `commonMain/data/database`.
- DAOs are `suspend` or return `Flow`; never blocking.
- One DAO per aggregate; no DAO knows about domain models.

## Database builder — expect/actual

```kotlin
// commonMain/data/database/DatabaseFactory.kt
expect fun databasePath(): String

@Database(entities = [ItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

fun createDatabase(): AppDatabase =
    Room.databaseBuilder<AppDatabase>(name = databasePath())
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
```

```kotlin
// androidMain
actual fun databasePath(): String =
    androidContext().getDatabasePath("app.db").absolutePath

// iosMain
actual fun databasePath(): String {
    val dir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )!!
    return "${dir.path}/app.db"
}

// jvmMain
actual fun databasePath(): String {
    val dir = File(System.getProperty("user.dir"), "build/app-data").apply { mkdirs() }
    return File(dir, "app.db").absolutePath
}
```

Notes:

- `BundledSQLiteDriver` everywhere: same SQLite build on every target, no reliance on the host's system SQLite.
- `fallbackToDestructiveMigration(dropAllTables = true)` is a starting point only. Replace it with migrations before shipping.
- On desktop, prefer a real user data directory (`%APPDATA%`, `~/Library/Application Support`, `~/.local/share`) over `build/` once packaging exists.
- iOS actuals need `NSFileManager` from `platform.Foundation` — that import is legal in `iosMain` and forbidden in `commonMain`.
- If the Android actual needs `Context`, resolve it through Koin (`androidContext()`), not a static holder.

## Repository implementation

```kotlin
internal class ItemRepositoryImpl(
    private val api: ItemApi,
    private val dao: ItemDao,
    private val mapper: ItemMapper,
    private val io: CoroutineDispatcher,
) : ItemRepository {

    override fun observeItems(): Flow<List<Item>> =
        dao.observeAll()
            .map { entities -> entities.map(mapper::toDomain) }
            .flowOn(io)

    override suspend fun refresh(): Result<Unit> =
        withContext(io) {
            api.getItems().map { dtos ->
                dao.upsert(dtos.map(mapper::toEntity))
            }
        }
}
```

Rules:

- Database is the source of truth: observe the DAO, refresh in the background.
- `flowOn(io)` for database reads; `withContext(io)` for writes and network mapping.
- `Result` crosses the use-case boundary; exceptions do not.
- If refresh fails, keep serving cached data and return the failure as a value.

## Error mapping

```kotlin
sealed interface DomainException : Throwable {
    data object Unauthorized : DomainException
    data class Http(val code: Int) : DomainException
    data class Serialization(val cause: Throwable) : DomainException
}
```

Map at the boundary:

- Ktor: status codes, `HttpRequestTimeoutException`, `IOException`
- Serialization: `SerializationException`
- Room: `SQLiteException`

The presentation layer maps domain errors to `FeatureError`; the UI never sees an exception type.
