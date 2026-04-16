package dev.codingonpad.news.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.codingonpad.news.ui.ThemePreference

/**
 * Custom Indigo-seeded scheme. Skips Material You dynamic color to keep a
 * distinct brand identity instead of matching the device wallpaper.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF2F4FC7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE2FF),
    onPrimaryContainer = Color(0xFF001352),
    secondary = Color(0xFF585E72),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE2F9),
    onSecondaryContainer = Color(0xFF151B2C),
    tertiary = Color(0xFF745471),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFBFBFE),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFBFBFE),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE2E2EC),
    onSurfaceVariant = Color(0xFF45464F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F4FA),
    surfaceContainer = Color(0xFFEFEEF5),
    surfaceContainerHigh = Color(0xFFE9E8F0),
    surfaceContainerHighest = Color(0xFFE3E2EA),
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C6D0),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9C4FF),
    onPrimary = Color(0xFF032484),
    primaryContainer = Color(0xFF203EAD),
    onPrimaryContainer = Color(0xFFDDE2FF),
    secondary = Color(0xFFC0C6DD),
    onSecondary = Color(0xFF2A3042),
    secondaryContainer = Color(0xFF404659),
    onSecondaryContainer = Color(0xFFDCE2F9),
    tertiary = Color(0xFFE2BBDD),
    onTertiary = Color(0xFF432740),
    background = Color(0xFF111217),
    onBackground = Color(0xFFE4E2E9),
    surface = Color(0xFF111217),
    onSurface = Color(0xFFE4E2E9),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C6D0),
    surfaceContainerLowest = Color(0xFF0B0C11),
    surfaceContainerLow = Color(0xFF191A1F),
    surfaceContainer = Color(0xFF1D1E23),
    surfaceContainerHigh = Color(0xFF27292E),
    surfaceContainerHighest = Color(0xFF323339),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF45464F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun CodingOnPadTheme(
    themePreference: ThemePreference = ThemePreference.AUTO,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.AUTO -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = CodingOnPadTypography,
        content = content
    )
}
