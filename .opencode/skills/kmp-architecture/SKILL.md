---
name: kmp-architecture
description: "Defines project structure, source-set layout, layer boundaries, naming conventions, and Koin dependency injection for Kotlin Multiplatform projects. Use this skill whenever creating a new KMP project, adding a feature, scaffolding a screen or flow, wiring Koin modules, naming a class or file, or deciding which layer and source set a piece of code belongs in. Also use it when reviewing or refactoring code to keep it portable across Android, iOS and desktop."
license: MIT
---

# KMP Architecture

Keep Kotlin Multiplatform projects consistent, portable and easy to navigate.

## 1. Core architecture rules

Clean Architecture layering, expressed per source set:

- `domain` is the business core. Pure Kotlin, no framework.
- `data` handles remote, local and other external data sources.
- `presentation` coordinates state and mapping for the UI.
- `ui` contains Compose screens, reusable components and theme.
- `di` contains Koin module declarations.

Dependency flow always points inward:

`ui -> presentation -> domain <- data`

Rules:

- `domain` must not depend on `android.*`, `java.*`, `platform.*`, Compose, Ktor, Room or Koin APIs.
- `presentation` must not depend on Ktor types, Room 3 types or DTOs.
- `data` may depend on `domain`, never the reverse.
- `ui` stays focused on rendering and user interaction.
- Keep business rules out of UI and out of platform framework classes.
- Everything above lives in `commonMain` unless it is platform glue. See `kmp-platform-boundaries`.

## 2. Recommended project structure

Feature-first, source-set-first. Two Gradle modules: `:shared` (the KMP library) and `:androidApp` (packaging only). Add more modules only when a boundary earns it.

```text
shared/src/
├── commonMain/kotlin/com/company/app/
│   ├── App.kt                        # shared root composable
│   ├── di/
│   │   ├── NetworkModule.kt
│   │   ├── DatabaseModule.kt
│   │   ├── RepositoryModule.kt
│   │   └── DispatcherModule.kt
│   ├── domain/
│   │   ├── model/
│   │   ├── repository/
│   │   └── usecase/
│   ├── data/
│   │   ├── api/model/
│   │   ├── datasource/
│   │   ├── database/          # Room 3 entities + DAOs
│   │   ├── mapper/
│   │   └── repository/
│   ├── presentation/<feature>/
│   └── ui/
│       ├── <feature>/
│       └── theme/
├── androidMain/kotlin/...     # MainActivity, Android actuals
├── iosMain/kotlin/...         # MainViewController, Darwin actuals
└── desktopMain/kotlin/...     # main(), CIO actuals
```

Rules:

- Keep each feature self-contained.
- Place shared abstractions in the lowest common layer possible.
- No `utils/` or `helpers/` dumping grounds.
- Platform entry points live in platform source sets, never in `commonMain`.

## 3. Layer responsibilities

### domain

Pure Kotlin only: entities, value objects, repository interfaces, use cases, domain errors, reusable domain calculators.

### data

Anything outside the app process or outside the pure domain model: Ktor client APIs, DTOs, Room 3 entities and DAOs, local datasource implementations, mappers, repository implementations, caching and sync logic.

### presentation

Screen behavior and state orchestration: ViewModels, `UiState`/`UiAction`/`UiEvent`, screen-only parsers and mappers.

### ui

Rendering only: screens, stateless content composables, reusable components, theme, typography, colors, shapes, previews.

### di

Object graph wiring only: Koin `module { }` declarations, qualifiers, platform-specific module lists.

## 4. Naming conventions

- ViewModel: `[Feature]ViewModel`
- Screen: `[Feature]Screen`
- Screen state: `[Feature]ScreenUiState`
- Content state: `[Feature]UiState`
- Actions: `[Feature]UiAction`
- Events: `[Feature]UiEvent`
- Repository interface: `[Feature]Repository`
- Repository implementation: `[Feature]RepositoryImpl`
- Use case: `[Verb][Subject]UseCase`
- Mapper: `[Source]Mapper` or `[Target]Mapper`
- Parser: `[Type]Parser`
- Provider: `[Name]Provider`
- DTO: `[Entity]Dto`
- Database entity: `[Entity]Entity`
- Koin module: `[Feature]Module`

Rules:

- Names must reveal responsibility. No `Manager`, `Helper`, `Processor`, `Utils`.
- `internal` by default for implementation classes.
- File names match the class they contain.

## 5. Visibility

- `internal` for most implementation classes, ViewModels, use cases, repository implementations.
- `public` only for intended shared APIs.
- `private` for local helpers.
- Kotlin has no package-private visibility; do not pretend it does. Use `internal` scoped to the module.

## 6. Dependency injection with Koin

Constructor injection without annotations. Koin is a container resolved at boundaries, not inside business code.

```kotlin
internal class FeatureViewModel(
    private val observeItems: ObserveItemsUseCase,
    private val mapper: FeatureUiStateMapper,
) : ViewModel()

val featureModule = module {
    viewModelOf(::FeatureViewModel)
    single<FeatureRepository> { FeatureRepositoryImpl(get(), get()) }
    factoryOf(::ObserveItemsUseCase)
}
```

Rules:

- Declare bindings with `single`, `factory`, `viewModel` / `viewModelOf`.
- One module per feature or concern; assemble them in a `modules(...)` list.
- Resolve at the composition boundary with `koinViewModel<T>()` (Compose) or `koinInject<T>()` (non-UI).
- Never call `get()` inside `domain`, or inside `presentation`/`data` bodies. Pass dependencies through constructors.
- No global mutable singletons, no `object` holders.
- No annotation processing: Koin needs no KSP/KAPT.

Full wiring example and the platform module lists: `references/koin.md`.

## 7. Platform entry points

Entry points are platform glue and belong in platform source sets.

- `androidMain`: `MainActivity` calls `startKoin` (or reuses an `Application`), then `setContent { App() }`.
- `iosMain`: `MainViewController()` returns a `ComposeUIViewController { App() }`.
- `desktopMain`: `application { Window { App() } }` and `main()`.

Prefer:

- One `App()` composable as the shared root.
- Compose Navigation for routing, shared across platforms.
- No fragments, no per-platform UI trees.

## 8. Adding a feature

1. Define the feature boundary.
2. Create domain contracts (models, repository interface, use cases).
3. Add data implementations (DTOs, entities, DAOs, mappers, repository impl).
4. Add presentation state and ViewModel.
5. Add UI composables and previews.
6. Register the Koin module.
7. Run the platform-boundaries checklist.

## 9. Review checklist

Layering is correct only if:

- dependencies point inward
- the responsible layer owns the concern
- no framework type leaks into `domain`
- no platform type leaks into `commonMain`
- names match roles
- the code is testable in isolation
- the feature has a preview per screen

Refactor when:

- a ViewModel parses or formats values
- a repository returns DTOs instead of domain models
- UI performs business calculation
- a module mixes unrelated responsibilities
- common code imports `android.*`, `java.*` or `platform.*`

## 10. Decision rules

- Business rule, no UI dependency: `domain`
- Network or persistence: `data`
- Screen orchestration: `presentation`
- Compose rendering: `ui`
- Wiring: `di`
- Platform capability: interface in `commonMain`, implementation in each platform set

When uncertain, choose the lowest layer that can own the logic without leaking framework concerns.
