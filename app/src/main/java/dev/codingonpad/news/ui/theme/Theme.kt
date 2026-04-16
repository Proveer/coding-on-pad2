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

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A659E),
    onPrimary = Color.White,
    secondary = Color(0xFF4B8FB1),
    background = Color(0xFFF7F9FB),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7CC4FF),
    onPrimary = Color(0xFF00324E),
    secondary = Color(0xFF9FCBE1),
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF142B3D)
)

@Composable
fun CodingOnPadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
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
