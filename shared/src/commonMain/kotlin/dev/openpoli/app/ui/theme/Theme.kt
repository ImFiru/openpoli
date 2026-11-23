package dev.openpoli.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens imported from the Penpot file "Material Design 3", page
 * "Login - M3 Expressive". Sets: Base Light, Base Dark and Accent Blue (active theme).
 *
 * Raw values live only here. Everything else reads MaterialTheme roles.
 */
private object Token {
    // Base Light
    val Surface = Color(0xFFFAF8FF)
    val SurfaceContainer = Color(0xFFE7E0EC)
    val SurfaceCard = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1B1B1F)
    val OnSurfaceVariant = Color(0xFF56535C)
    val Outline = Color(0xFF6E7488)
    val OutlineVariant = Color(0xFFE8E4F0)
    val Holiday = Color(0xFF8A6A89)
    val Danger = Color(0xFFB3261E)

    // Base Dark
    val DarkSurface = Color(0xFF141218)
    val DarkSurfaceContainer = Color(0xFF2B2930)
    val DarkSurfaceCard = Color(0xFF2B2930)
    val OnDarkSurface = Color(0xFFE6E0E9)
    val OnDarkSurfaceVariant = Color(0xFFCAC4D0)
    val DarkOutline = Color(0xFF6B6572)
    val DarkOutlineVariant = Color(0xFF3A3742)
    val DarkHoliday = Color(0xFFC9A7C6)
    val DarkDanger = Color(0xFFFFB4AB)
    val DarkCircle = Color(0xFF35323C)

    // Accent Blue
    val Accent = Color(0xFF2A4174)
    val AccentSoft = Color(0xFFDCE6F5)
    val AccentDark = Color(0xFFB0C6FF)

    // Shared accents and ink
    val OnAccent = Color(0xFFFFFFFF)
    val OnDarkAccent = Color(0xFF0B1B33)
    val BrandTeal = Color(0xFF5EE0CC)
    val BrandOrange = Color(0xFFFFB77C)
    val BrandYellow = Color(0xFFFFD25E)
    val BrandPurple = Color(0xFFD7B5FF)
    val BrandBlue = Color(0xFF8FC9FF)
    val BrandInk = Color(0xFF101418)
}

/** Brand and semantic colors that Material 3 has no role for. */
@Immutable
data class AppColors(
    val holiday: Color,
    val onDarkChip: Color,
    val circle: Color,
    val brandTeal: Color,
    val brandOrange: Color,
    val brandYellow: Color,
    val brandPurple: Color,
    val brandBlue: Color,
    val brandInk: Color,
)

private val LightAppColors =
    AppColors(
        holiday = Token.Holiday,
        onDarkChip = Token.OnAccent,
        circle = Token.SurfaceContainer,
        brandTeal = Token.BrandTeal,
        brandOrange = Token.BrandOrange,
        brandYellow = Token.BrandYellow,
        brandPurple = Token.BrandPurple,
        brandBlue = Token.BrandBlue,
        brandInk = Token.BrandInk,
    )

private val DarkAppColors =
    AppColors(
        holiday = Token.DarkHoliday,
        onDarkChip = Token.OnAccent,
        circle = Token.DarkCircle,
        brandTeal = Token.BrandTeal,
        brandOrange = Token.BrandOrange,
        brandYellow = Token.BrandYellow,
        brandPurple = Token.BrandPurple,
        brandBlue = Token.BrandBlue,
        brandInk = Token.BrandInk,
    )

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

private val LightColors =
    lightColorScheme(
        primary = Token.Accent,
        onPrimary = Token.OnAccent,
        primaryContainer = Token.AccentSoft,
        onPrimaryContainer = Token.Accent,
        secondary = Token.Accent,
        onSecondary = Token.OnAccent,
        secondaryContainer = Token.AccentSoft,
        onSecondaryContainer = Token.Accent,
        tertiary = Token.Holiday,
        onTertiary = Token.OnAccent,
        background = Token.Surface,
        onBackground = Token.OnSurface,
        surface = Token.Surface,
        onSurface = Token.OnSurface,
        surfaceVariant = Token.SurfaceContainer,
        onSurfaceVariant = Token.OnSurfaceVariant,
        surfaceContainerLowest = Token.SurfaceCard,
        surfaceContainer = Token.SurfaceContainer,
        surfaceContainerHigh = Token.SurfaceContainer,
        outline = Token.Outline,
        outlineVariant = Token.OutlineVariant,
        error = Token.Danger,
        onError = Token.OnAccent,
    )

private val DarkColors =
    darkColorScheme(
        primary = Token.AccentDark,
        onPrimary = Token.OnDarkAccent,
        primaryContainer = Token.DarkSurfaceContainer,
        onPrimaryContainer = Token.OnDarkSurface,
        secondary = Token.AccentDark,
        onSecondary = Token.OnDarkAccent,
        secondaryContainer = Token.DarkSurfaceContainer,
        onSecondaryContainer = Token.OnDarkSurface,
        tertiary = Token.DarkHoliday,
        onTertiary = Token.OnDarkAccent,
        background = Token.DarkSurface,
        onBackground = Token.OnDarkSurface,
        surface = Token.DarkSurface,
        onSurface = Token.OnDarkSurface,
        surfaceVariant = Token.DarkSurfaceContainer,
        onSurfaceVariant = Token.OnDarkSurfaceVariant,
        surfaceContainerLowest = Token.DarkSurface,
        surfaceContainer = Token.DarkSurfaceContainer,
        surfaceContainerHigh = Token.DarkSurfaceCard,
        outline = Token.DarkOutline,
        outlineVariant = Token.DarkOutlineVariant,
        error = Token.DarkDanger,
        onError = Token.OnDarkAccent,
    )

/**
 * Typography tokens from the Penpot library (Roboto, 1.2 line height).
 *
 * The Login page itself draws text with "Google Sans Flex"; the token set says Roboto.
 * Google Sans Flex cannot be bundled without the font files, so both map to the platform
 * sans for now. See references/login-m3-tokens.md.
 */
private fun textStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Double = 0.0,
): TextStyle =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

private val AppTypography =
    Typography(
        displayLarge = textStyle(57, 68),
        displayMedium = textStyle(45, 54),
        displaySmall = textStyle(36, 43),
        headlineLarge = textStyle(32, 38),
        headlineMedium = textStyle(28, 34),
        headlineSmall = textStyle(24, 29),
        titleLarge = textStyle(22, 26),
        titleMedium = textStyle(16, 19, FontWeight.Medium, 0.01),
        titleSmall = textStyle(14, 17, FontWeight.Medium, 0.01),
        bodyLarge = textStyle(16, 19, letterSpacing = 0.04),
        bodyMedium = textStyle(14, 17, letterSpacing = 0.02),
        bodySmall = textStyle(12, 14, letterSpacing = 0.04),
        labelLarge = textStyle(14, 17, FontWeight.Medium, 0.01),
        labelMedium = textStyle(12, 14, FontWeight.Medium, 0.05),
        labelSmall = textStyle(11, 13, FontWeight.Medium, 0.05),
    )

/** Radii sampled from the Login page: menus and cards use 16, sheets and pills 24-28. */
private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
