---
name: penpot-to-compose
description: "Turns a Penpot design into Compose Multiplatform code: reads frames through the Penpot MCP, maps design tokens to MaterialTheme, generates stateless composables with state hoisting, adds previews, and verifies the result. Use this skill whenever implementing a screen from Penpot, extracting colors, typography or spacing tokens, or checking whether a Material 3 Expressive API is available in the Compose Multiplatform material3 artifact."
---

# Penpot to Compose

The Penpot file is the design source of truth. Code follows the design; the design does not follow the code.

## 1. Read the design first

| Step | Tool |
| --- | --- |
| Orient yourself | `high_level_overview` |
| Look up exact API calls | `penpot_api_info` |
| Read frames, layers, text styles, fills | `execute_code` |
| See the result | `export_shape` (`shapeId = "selection"` or `"page"`) |

Workflow:

1. `high_level_overview` before any plugin code.
2. `penpot_api_info` for the exact object shape — never guess property names.
3. Inspect read-only first: frame names, layer tree, text styles, color fills, spacing.
4. Report what you found before writing code.
5. Export the frame to PNG so you and the user look at the same thing.

Rules:

- Never invent a token that is not in the file. If a value repeats without a named style, propose adding the token to the design instead of hardcoding it.
- Do not modify the Penpot file unless asked; this skill is design -> code.
- If the plugin is not connected, ask the user to open the Penpot MCP plugin and connect; do not retry blindly.

## 2. Map tokens to MaterialTheme

| Penpot | Compose Multiplatform |
| --- | --- |
| Color styles / fills | `lightColorScheme` / `darkColorScheme` in the app theme |
| Text styles | `Typography` (`displayLarge` … `labelSmall`) |
| Spacing variables | `MaterialTheme.spacing` (custom extension) |
| Corner radii | `Shapes` or a spacing/shape extension |
| Elevation | `CardDefaults` / `Surface` elevation tokens |

```kotlin
// ui/theme/Theme.kt
private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFFFBFDF7),
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}
```

Rules:

- `Color(0x…)`, `dp` and `sp` literals are allowed only in the theme file.
- Everywhere else use `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.spacing.*`.
- Dark theme gets its own scheme from the same design, not an inverted guess.
- A token used twice and missing from the theme becomes a theme entry first.

## 3. Build stateless composables

```kotlin
@Composable
internal fun FeatureContent(
    uiState: FeatureUiState,
    onAction: (FeatureUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(MaterialTheme.spacing.md)) {
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun FeatureScreen(viewModel: FeatureViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureContent(uiState = uiState, onAction = viewModel::onAction)
}
```

Rules:

- Content composables are stateless: parameters in, callbacks out.
- State is hoisted to the screen; local visual state stays local.
- No ViewModel below the screen level.
- No business logic or formatting in composables; use mappers and formatters.
- Every screen and non-trivial component gets a preview.

## 4. Previews

```kotlin
@Preview
@Composable
private fun FeatureContentPreview() {
    AppTheme {
        FeatureContent(
            uiState = FeatureUiState(title = "Preview", items = emptyList()),
            onAction = {},
        )
    }
}
```

Use `@Preview` from `androidx.compose.ui.tooling.preview` (Compose Multiplatform re-exports
it; `org.jetbrains.compose.ui.tooling.preview.Preview` is deprecated). Preview the theme,
the loading, empty and error states — not just the happy path.

## 5. Verify against the design

1. `export_shape` the frame again after implementation.
2. Compare spacing, type scale, color roles, corner radii.
3. Report deviations instead of silently "fixing" the design.

## 6. Material 3 Expressive — verify before use

Compose Multiplatform ships material3 on its own version line, so an API that exists in Jetpack Compose may be missing here.

**Verified on `org.jetbrains.compose.material3:material3:1.13.0-rc01`** (artifact inspection, `commonMain` sources):

| API | Status |
| --- | --- |
| `ExperimentalMaterial3ExpressiveApi` | present |
| `MotionScheme` | present |
| `MaterialShapes` | present |
| `FloatingToolbar` | present |
| `ButtonGroup` | present |
| `SplitButton` | present |
| `ToggleButton` | present |
| `LoadingIndicator` | present |
| `MaterialExpressiveTheme` | **absent** |
| Flexible top app bars (`LargeFlexibleTopAppBar`, …) | **absent** — only tokens exist |

Consequences:

- Use the listed components in `commonMain`, annotated with `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`.
- Do **not** call `MaterialExpressiveTheme`; use `MaterialTheme` and, where supported, `MotionScheme.expressive()`.
- For flexible top app bars, use standard `TopAppBar` / `MediumTopAppBar` with `TopAppBarScrollBehavior`.
- If the design needs a component that is missing, either fall back to standard Material 3 or wrap a platform-specific implementation in `expect/actual` — and note the fallback in the PR.

How to re-verify (do this instead of trusting a blog post):

```bash
# list the artifact that is actually on your classpath
find ~/.gradle/caches/modules-2 -name "material3-*-sources.jar" | head -1
unzip -l <jar> | grep -Ei "MaterialShapes|FloatingToolbar|ButtonGroup|LoadingIndicator|Expressive"
```

Rules:

- A component is available only if it is in the artifact the project resolves.
- Never ship an `@OptIn` for an API you have not compiled against.
- Record the material3 version in the PR when using expressive APIs.

## 7. Checklist

- [ ] design read through the MCP, not from memory
- [ ] tokens mapped to `MaterialTheme`, no stray literals outside the theme
- [ ] stateless content composable, state hoisted to the screen
- [ ] previews for default, loading, empty and error
- [ ] expressive APIs verified in the resolved artifact
- [ ] `export_shape` compared against the implementation
- [ ] builds on Android (and desktop, and iOS where the host allows)
