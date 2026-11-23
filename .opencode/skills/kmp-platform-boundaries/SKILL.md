---
name: kmp-platform-boundaries
description: "Defines which code belongs in commonMain, androidMain, iosMain and desktopMain/jvmMain, the expect/actual rules for platform capabilities, the imports forbidden in shared code, and the build checklist to run after every feature on Android, iOS simulator and desktop. Use this skill whenever adding platform code, creating an expect/actual pair, importing a framework API, or finishing a feature and verifying it builds on all targets."
---

# KMP Platform Boundaries

Shared code stays shared. Platform code is thin, explicit and replaceable.

## 1. Targets

| Target | Declared as | Source set |
| --- | --- | --- |
| Android | `androidTarget()` | `androidMain` |
| iOS | `iosArm64()`, `iosSimulatorArm64()` | `iosMain` (via `iosMain.dependsOn`) |
| Desktop | `jvm("desktop")` | `desktopMain` |

Note: `jvm("desktop")` produces `desktopMain` / `desktopTest`. If a project declares plain `jvm()`, the source set is `jvmMain` / `jvmTest`. Use one convention consistently — this skill writes `desktopMain` because the target is `jvm("desktop")`.

No native desktop targets, no web. Do not add targets speculatively; each one is a permanent build cost.

## 2. What lives where

| Concern | Source set |
| --- | --- |
| Compose UI, ViewModels, domain, data, DI modules | `commonMain` |
| Android `Activity`, `Application`, `Context`-bound actuals | `androidMain` |
| `MainViewController`, `NSFileManager`, Darwin engine | `iosMain` |
| `main()`, `Window`, `File`-based paths, CIO engine | `desktopMain` |
| Shared tests | `commonTest` |
| Platform glue tests | `androidUnitTest`, `iosTest`, `desktopTest` |

Rules:

- If code compiles in `commonMain`, it belongs in `commonMain`. Moving portable code into a platform set is a regression.
- Platform sets contain only: entry points, engine selection, path resolution, and implementations of `commonMain` interfaces.
- No shared file may be duplicated per platform with copy-pasted bodies.

## 3. Forbidden imports in commonMain

```text
android.*
androidx.room.*            // Room 2 — use androidx.room3.* instead
java.*
javax.*
platform.*                 // Kotlin/Native platform libraries
okhttp3.*
io.ktor.client.engine.darwin.*
io.ktor.client.engine.okhttp.*
org.jetbrains.skiko.*      // desktop-only
NS*.kt / kotlinx.cinterop.*
```

Check mechanically:

```bash
rg -n "^import (android|java|javax|platform)\." composeApp/src/commonMain && echo "VIOLATION"
```

`java.*` is the easiest rule to break by accident: `java.util.UUID`, `java.time.*`, `java.math.BigDecimal`, `java.io.File`. Use `kotlin.uuid.Uuid`, `kotlinx-datetime`, `kotlin.time`, `Long` cents or a `Money` value class, and a `Path`-style interface in `commonMain`.

## 4. Platform capabilities: interface first

Platform features go behind a `commonMain` interface. `expect/actual` is for thin leaves, not feature surfaces.

```kotlin
// commonMain/platform/FilePicker.kt
interface FilePicker {
    suspend fun pickFile(mimeTypes: List<String>): PickedFile?
}

data class PickedFile(val name: String, val bytes: ByteArray)
```

Implementations are supplied per platform through Koin:

```kotlin
// androidMain, iosMain, desktopMain
internal class AndroidFilePicker(private val launcher: ActivityResultLauncher<...>) : FilePicker
internal class IosFilePicker(private val controller: UIViewController) : FilePicker
internal class DesktopFilePicker : FilePicker
```

| Capability | Interface in commonMain | Implementation |
| --- | --- | --- |
| File picker | `FilePicker` | Activity result / `UIDocumentPicker` / `JFileChooser` |
| Notifications | `Notifications` | `NotificationManager` / `UNUserNotificationCenter` / tray |
| Permissions | `PermissionRequester` | runtime permissions / `AVFoundation` / no-op |
| Window management | `WindowController` | `WindowInsetsController` / `UIWindow` / `Window` state |
| Sharing | `Sharer` | `Intent` / `UIActivityViewController` / desktop share |

Rules:

- Interface in `commonMain`, implementation never referenced by shared code.
- Pass platform objects (`Context`, `UIViewController`) into the implementation, never upward.
- Provide a no-op or documented fallback where a platform genuinely lacks the capability.

## 5. expect/actual rules

Use for narrow, stateless leaves:

```kotlin
expect fun httpClientEngine(): HttpClientEngineFactory<*>
expect fun databasePath(): String
```

Rules:

- Every `expect` has an `actual` in all three platform sets. The build fails otherwise — good.
- Keep the `expect` surface tiny: a function or a property, not a class with a rich API.
- `expect class` only when construction genuinely differs; prefer an interface + DI.
- Never expose a platform type in an `expect` signature reachable from `commonMain` call sites.
- `actual typealias` is fine when a platform library already provides the type.
- Experimental `-Xexpect-actual-classes` warning means the design is drifting; prefer an interface.

## 6. After every feature: checklist

Run from the repo root. Module name is `composeApp`; adjust if the project differs.

```bash
# 1. shared + Android
./gradlew :composeApp:assembleDebug

# 2. shared unit tests (runs commonTest on every target)
./gradlew :composeApp:testDebugUnitTest :composeApp:desktopTest

# 3. desktop app
./gradlew :composeApp:run

# 4. iOS — macOS hosts only
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 :composeApp:iosSimulatorArm64Test
```

On a non-macOS host, **skip step 4**. Apple targets are disabled there, so the tasks do
not exist and Gradle reports `task not found` — that is expected, not a failure. Android
and desktop still prove the shared code is coherent; iOS linking is verified in CI or on
a Mac before release.

iOS also needs `binaries.framework { baseName = "ComposeApp"; isStatic = true }` on each
Apple target, plus an Xcode project that consumes it. The framework configuration is
already present; the Xcode project is not.

Also confirm:

- [ ] `rg -n "^import (android|java|javax|platform)\." composeApp/src/commonMain` returns nothing
- [ ] every new `expect` has three `actual`s
- [ ] every screen has a `@Preview`
- [ ] no design token hardcoded (colors, type, dp) — see `penpot-to-compose`
- [ ] Koin modules registered in the platform `startKoin` list
- [ ] no unrelated file touched

## 7. Failure playbook

| Symptom | Cause | Fix |
| --- | --- | --- |
| `Unresolved reference` in `commonMain` | platform API used | move behind interface, implement per platform |
| `Expected declaration 'X' has no actual` | missing target | add the actual to every platform set |
| iOS links but Android fails | Android actual uses an API the engine/version lacks | check minSdk and the actual |
| Desktop works, iOS crashes at runtime | `java.*` leaked into shared bytecode | grep the imports, replace with KMP types |
| Common test passes on JVM, fails on iOS | JVM-only API in `commonTest` | remove `java.*` from tests too |
