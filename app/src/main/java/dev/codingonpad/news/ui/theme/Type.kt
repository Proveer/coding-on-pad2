package dev.codingonpad.news.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Premium news-app type scale: system serif for display + titles, sans for body.
 * Serif maps to Noto Serif on most Android devices; Sans maps to Roboto.
 */
val CodingOnPadTypography: Typography = run {
    val serif = FontFamily.Serif
    val sans = FontFamily.SansSerif
    val base = Typography()
    base.copy(
        displayLarge = base.displayLarge.copy(
            fontFamily = serif, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.6).sp
        ),
        headlineLarge = base.headlineLarge.copy(
            fontFamily = serif, fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp
        ),
        headlineMedium = base.headlineMedium.copy(
            fontFamily = serif, fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp
        ),
        headlineSmall = base.headlineSmall.copy(
            fontFamily = serif, fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp
        ),
        titleLarge = base.titleLarge.copy(
            fontFamily = serif, fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp
        ),
        titleMedium = base.titleMedium.copy(
            fontFamily = serif, fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.1).sp
        ),
        titleSmall = base.titleSmall.copy(
            fontFamily = sans, fontWeight = FontWeight.Medium,
            fontSize = 14.sp, lineHeight = 20.sp
        ),
        bodyLarge = base.bodyLarge.copy(
            fontFamily = sans, fontSize = 16.sp, lineHeight = 24.sp
        ),
        bodyMedium = base.bodyMedium.copy(
            fontFamily = sans, fontSize = 14.sp, lineHeight = 20.sp
        ),
        bodySmall = base.bodySmall.copy(
            fontFamily = sans, fontSize = 13.sp, lineHeight = 18.sp
        ),
        labelLarge = base.labelLarge.copy(
            fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
        ),
        labelMedium = base.labelMedium.copy(
            fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 12.sp
        ),
        labelSmall = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp
        )
    )
}
