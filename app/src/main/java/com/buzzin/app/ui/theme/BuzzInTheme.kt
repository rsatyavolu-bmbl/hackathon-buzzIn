package com.buzzin.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BumbleYellow = Color(0xFFFFC629)
private val BumbleYellowVariant = Color(0xFFFFD74D)

private val LightColorScheme = lightColorScheme(
    primary = BumbleYellow,
    onPrimary = Color.White,
    primaryContainer = BumbleYellowVariant,
    secondary = Color(0xFF6C757D),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
)

private val DarkColorScheme = darkColorScheme(
    primary = BumbleYellow,
    onPrimary = Color.Black,
    primaryContainer = BumbleYellowVariant,
    secondary = Color(0xFF6C757D),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
)

@Composable
fun BuzzInTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
