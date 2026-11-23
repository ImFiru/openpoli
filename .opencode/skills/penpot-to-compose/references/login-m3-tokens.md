# Login - M3 Expressive: imported tokens

Source: Penpot file **Material Design 3**, page **Login - M3 Expressive**.
Token catalog: sets `Base Light`, `Base Dark`, `Accent Blue` (active theme `Accent / Blue`).

Imported into `shared/src/commonMain/kotlin/dev/openpoli/app/ui/theme/Theme.kt`.
Raw hex values appear only there.

## Colors

| Penpot token | Light role | Dark role |
| --- | --- | --- |
| `accent` | `primary`, `secondary`, `onPrimaryContainer` | — |
| `accent-soft` | `primaryContainer`, `secondaryContainer` | — |
| `accent-dark` | — | `primary`, `secondary` |
| `on-accent` | `onPrimary`, `onSecondary`, `onError` | — |
| `on-dark-accent` | — | `onPrimary`, `onSecondary`, `onError` |
| `surface` | `background`, `surface` | — |
| `dark-surface` | — | `background`, `surface` |
| `surface-container` | `surfaceVariant`, `surfaceContainer`, `surfaceContainerHigh` | — |
| `dark-surface-container` | — | `surfaceVariant`, `surfaceContainer` |
| `surface-card` | `surfaceContainerLowest` | — |
| `dark-surface-card` | — | `surfaceContainerHigh` |
| `on-surface` | `onBackground`, `onSurface` | — |
| `on-dark-surface` | — | `onBackground`, `onSurface` |
| `on-surface-variant` | `onSurfaceVariant` | — |
| `on-dark-surface-variant` | — | `onSurfaceVariant` |
| `outline` | `outline` | — |
| `dark-outline` | — | `outline` |
| `outline-variant` | `outlineVariant` | — |
| `dark-outline-variant` | — | `outlineVariant` |
| `danger` | `error` | — |
| `dark-danger` | — | `error` |
| `holiday` | `tertiary` + `appColors.holiday` | — |
| `dark-holiday` | — | `tertiary` + `appColors.holiday` |

No Material role exists for these, so they are exposed as `MaterialTheme.appColors`:

`brand-teal`, `brand-orange`, `brand-yellow`, `brand-purple`, `brand-blue`, `brand-ink`,
`dark-circle` (`appColors.circle`) and `on-dark-chip`.

## Typography

Library typographies are Roboto at the M3 scale, line height ×1.2. Each maps to the
matching `Typography` slot:

| Token | Slot | Size | Line | Weight | Tracking |
| --- | --- | --- | --- | --- | --- |
| Display Large | `displayLarge` | 57 | 68 | 400 | 0 |
| Display Medium | `displayMedium` | 45 | 54 | 400 | 0 |
| Display Small | `displaySmall` | 36 | 43 | 400 | 0 |
| Headline Large | `headlineLarge` | 32 | 38 | 400 | 0 |
| Headline Medium | `headlineMedium` | 28 | 34 | 400 | 0 |
| Headline Small | `headlineSmall` | 24 | 29 | 400 | 0 |
| Title Large | `titleLarge` | 22 | 26 | 400 | 0 |
| Title Medium | `titleMedium` | 16 | 19 | 500 | 0.01 |
| Title Small | `titleSmall` | 14 | 17 | 500 | 0.01 |
| Body Large | `bodyLarge` | 16 | 19 | 400 | 0.04 |
| Body Medium | `bodyMedium` | 14 | 17 | 400 | 0.02 |
| Body Small | `bodySmall` | 12 | 14 | 400 | 0.04 |
| Label Large | `labelLarge` | 14 | 17 | 500 | 0.01 |
| Label Medium | `labelMedium` | 12 | 14 | 500 | 0.05 |
| Label Small | `labelSmall` | 11 | 13 | 500 | 0.05 |

## Shapes

Radii sampled from the page's shapes: 4, 8, 16, 24 and 28 dp map to
`extraSmall`, `small`, `medium`, `large`, `extraLarge`.

## Open questions

1. **Font.** The token typographies say Roboto, but every text layer on the Login page uses
   *Google Sans Flex*. Google Sans is not redistributable without the font files, so the
   theme uses `FontFamily.SansSerif` (Roboto on Android, SF on iOS, system sans on desktop).
   To match the design exactly, add the font files and set a `FontFamily` per platform.
2. **Tracking units.** Penpot stores `letterSpacing` as `0.01`–`0.4`. Imported as `sp`. If
   those values were authored in `em`, multiply by the font size instead.
3. **No spacing tokens.** The catalog has colors and typography only, so layout spacing is
   still literal `dp` in composables. If the design adds spacing tokens, extend the theme
   with a `Spacing` object rather than scattering values.

## Re-importing

1. `high_level_overview`, then `execute_code` to read `penpot.library.local.tokens.sets` and
   `penpot.library.local.typographies`.
2. Re-run the mapping above; keep raw values in `Theme.kt` only.
3. `./gradlew check` and compare against `export_shape` of the page.
