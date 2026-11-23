# Ktor + Room 3 — full example

Reference for `kmp-patterns`. Everything below is `commonMain` unless a source set is named.

## Dependencies

Versions live in `gradle/libs.versions.toml`, never inline. The block below is a
starting catalog; the versions were current when written and should be bumped
together, not one at a time.

```toml
# gradle/libs.versions.toml
[versions]
ktor = "3.6.1"
room3 = "3.0.3"
sqlite = "2.7.1"
kotlin = "2.4.20"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
room3-runtime = { module = "androidx.room3:room3-runtime", version.ref = "room3" }
room3-compiler = { module = "androidx.room3:room3-compiler", version.ref = "room3" }
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }
```

```kotlin
// commonMain
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
implementation(libs.room3.runtime)
implementation(libs.androidx.sqlite.bundled)
ksp(libs.room3.compiler)

// androidMain
implementation(libs.ktor.client.okhttp)
// iosMain
implementation(libs.ktor.client.darwin)
// desktopMain
implementation(libs.ktor.client.cio)
```

Rules:

- KSP and the Room 3 compiler must match the Kotlin version you compile with; bump them in the same commit as Kotlin.
- Ktor engine artifacts are per target; never add `ktor-client-okhttp` outside `androidMain`.
- One catalog entry per module: no version strings in `build.gradle.kts`.

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

// desktopMain
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

// desktopMain
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
