package dev.codingonpad.news.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.codingonpad.news.ui.ThemePreference

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A659E),
    onPrimary = Color.White,
    secondary = Color(0xFF4B8FB1),
    background = Color(0xFFF6F8FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6ECF2),
    onSurfaceVariant = Color(0xFF4E5A66)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7CC4FF),
    onPrimary = Color(0xFF00324E),
    secondary = Color(0xFF9FCBE1),
    background = Color(0xFF0B1622),
    surface = Color(0xFF142433),
    surfaceVariant = Color(0xFF1E3144),
    onSurface = Color(0xFFE4ECF3),
    onSurfaceVariant = Color(0xFFB5C4D1)
)

@Composable
fun CodingOnPadTheme(
    themePreference: ThemePreference = ThemePreference.AUTO,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.AUTO -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
