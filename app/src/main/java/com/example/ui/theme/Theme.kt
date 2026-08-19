package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LinguaXPrimary,
    onPrimary = Color.White,
    primaryContainer = LinguaXPrimaryContainer,
    onPrimaryContainer = LinguaXOnPrimaryContainer,
    secondary = LinguaXSecondary,
    onSecondary = Color.White,
    secondaryContainer = LinguaXSecondaryContainer,
    onSecondaryContainer = LinguaXOnSecondaryContainer,
    tertiary = LinguaXAccent,
    onTertiary = Color(0xFF00363F),
    background = LinguaXBackground,
    onBackground = LinguaXTextPrimary,
    surface = LinguaXSurface,
    onSurface = LinguaXTextPrimary,
    surfaceVariant = LinguaXSurfaceElevated,
    onSurfaceVariant = LinguaXTextSecondary,
    outline = LinguaXBorder,
    outlineVariant = LinguaXBorderLight,
    error = LinguaXError,
    onError = Color.White
)

@Composable
fun LinguaXTheme(
    darkTheme: Boolean = true, // Default to modern premium dark UI
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
