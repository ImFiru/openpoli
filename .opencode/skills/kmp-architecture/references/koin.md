# Koin wiring — full example

Reference for `kmp-architecture`. Koin 4.x, multiplatform, no annotation processing.

## Dependencies

```kotlin
// commonMain
implementation("io.insert-koin:koin-core:4.1.1")
implementation("io.insert-koin:koin-compose:4.1.1")
implementation("io.insert-koin:koin-compose-viewmodel:4.1.1")

// androidMain
implementation("io.insert-koin:koin-android:4.1.1")
```

`koin-compose-viewmodel` is what makes `koinViewModel()` available on every target, including iOS and desktop. Without it you get `koin-compose` only, and no ViewModel scoping.

## Modules

```kotlin
// commonMain/di/DispatcherModule.kt
val dispatcherModule = module {
    single<CoroutineDispatcher>(named("io")) { Dispatchers.Default }
    single<CoroutineDispatcher>(named("main")) { Dispatchers.Main }
}

// commonMain/di/NetworkModule.kt
val networkModule = module {
    single<HttpClient> { createHttpClient() }
    single<ItemApi> { KtorItemApi(get()) }
}

// commonMain/di/DatabaseModule.kt
val databaseModule = module {
    single<AppDatabase> { createDatabase() }
    single<ItemDao> { get<AppDatabase>().itemDao() }
}

// commonMain/di/RepositoryModule.kt
val repositoryModule = module {
    single<ItemRepository> { ItemRepositoryImpl(get(), get(), get()) }
}

// feature module
val featureModule = module {
    viewModelOf(::FeatureViewModel)
    factoryOf(::ObserveItemsUseCase)
}
```

Keep declarations in `di/`, one file per concern, and assemble them once:

```kotlin
val appModules = listOf(
    dispatcherModule,
    networkModule,
    databaseModule,
    repositoryModule,
    featureModule,
)
```

## Starting Koin per platform

```kotlin
// androidMain
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModules, androidPlatformModule)
        }
    }
}

// iosMain
fun initKoin() = startKoin { modules(appModules, iosPlatformModule) }
fun MainViewController() = ComposeUIViewController { App() }

// jvmMain
fun main() = application {
    startKoin { modules(appModules, jvmPlatformModule) }
    Window(onCloseRequest = ::exitApplication) { App() }
}
```

Platform modules hold only the actuals:

```kotlin
// androidMain
val androidPlatformModule = module {
    single<Notifications> { AndroidNotifications(androidContext()) }
}

// iosMain
val iosPlatformModule = module {
    single<Notifications> { IosNotifications() }
}

// jvmMain
val jvmPlatformModule = module {
    single<Notifications> { DesktopNotifications() }
}
```

## Resolving in Compose

```kotlin
@Composable
internal fun FeatureScreen(
    viewModel: FeatureViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureContent(uiState = uiState, onAction = viewModel::onAction)
}
```

Rules:

- `koinViewModel()` at the screen boundary only.
- Pass `uiState` and callbacks down; never pass the container or the ViewModel deeper.
- For non-Compose entry points, `koinInject<T>()` inside a composable or `KoinPlatform.getKoin().get<T>()` at the platform boundary.
- Inside `domain`, `data` and `presentation` bodies, `get()` is forbidden — take constructor parameters.

## Qualifiers

```kotlin
single<CoroutineDispatcher>(named("io")) { Dispatchers.Default }

internal class ItemRepositoryImpl(
    private val api: ItemApi,
    private val dao: ItemDao,
    @Named("io") private val io: CoroutineDispatcher,
) : ItemRepository
```

Use qualifiers sparingly. If a type needs three qualifiers, the design is probably wrong.
