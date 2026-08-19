package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// LinguaX Premium Dark Visual Tokens
// Matching reference high-craft design
// ==========================================

// Canvas & Surfaces
val LinguaXBackground = Color(0xFF070B19)
val LinguaXBackgroundCanvas = Color(0xFF050814)
val LinguaXSurface = Color(0xFF0E1428)
val LinguaXSurfaceElevated = Color(0xFF141C38)
val LinguaXSurfaceHighlight = Color(0xFF1D284F)
val LinguaXSurfaceGlass = Color(0xD90E1428)
val LinguaXSurfaceGlassLight = Color(0x66182449)
val LinguaXSurfaceCard = Color(0xFF121933)

// Brand Core Accents
val LinguaXPrimary = Color(0xFF4F7CFF)
val LinguaXPrimaryLight = Color(0xFF7B9DFF)
val LinguaXPrimaryDark = Color(0xFF2C55D6)
val LinguaXPrimaryContainer = Color(0xFF1E2D56)
val LinguaXOnPrimaryContainer = Color(0xFFD6E2FF)

val LinguaXSecondary = Color(0xFF6C5CE7)
val LinguaXSecondaryLight = Color(0xFF8E82F7)
val LinguaXSecondaryDark = Color(0xFF503FD4)
val LinguaXSecondaryContainer = Color(0xFF282354)
val LinguaXOnSecondaryContainer = Color(0xFFE4E0FF)

val LinguaXAccent = Color(0xFF22D3EE)
val LinguaXAccentLight = Color(0xFF67E8F9)
val LinguaXAccentCyan = Color(0xFF06B6D4)

// Gamification Tokens (Trophy, Streaks, XP, Hearts)
val LinguaXGold = Color(0xFFFFB300)
val LinguaXGoldLight = Color(0xFFFFD54F)
val LinguaXGoldDark = Color(0xFFFF8F00)
val LinguaXAccentGold = Color(0xFFFFB300)
val LinguaXXP = Color(0xFFFFC107)

val LinguaXStreak = Color(0xFFFF5722)
val LinguaXFlame = Color(0xFFFF7043)
val LinguaXAccentFlame = Color(0xFFFF5722)
val LinguaXHearts = Color(0xFFFF2A6D)

// Status Colors
val LinguaXSuccess = Color(0xFF22C55E)
val LinguaXSuccessLight = Color(0xFF4ADE80)
val LinguaXSuccessGreen = Color(0xFF22C55E)

val LinguaXWarning = Color(0xFFF59E0B)
val LinguaXWarningLight = Color(0xFFFBBF24)

val LinguaXError = Color(0xFFEF4444)
val LinguaXErrorLight = Color(0xFFF87171)

// Typography Colors
val LinguaXTextPrimary = Color(0xFFFFFFFF)
val LinguaXTextSecondary = Color(0xFF94A3B8)
val LinguaXTextTertiary = Color(0xFF64748B)
val LinguaXTextMuted = Color(0xFF475569)

// Borders & Dividers
val LinguaXBorder = Color(0xFF1E293B)
val LinguaXBorderLight = Color(0xFF2E3D5C)
val LinguaXBorderGlow = Color(0x664F7CFF)
val LinguaXCardBorder = Color(0x334F7CFF)

// ==========================================
// Premium 3D Depth Brushes & Gradients
// ==========================================

val LinguaXPrimaryGradient = Brush.horizontalGradient(
    listOf(LinguaXPrimary, LinguaXSecondary)
)

val LinguaXButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF4F7CFF), Color(0xFF6C5CE7))
)

val LinguaXAccentGradient = Brush.horizontalGradient(
    listOf(LinguaXAccent, LinguaXPrimary)
)

val LinguaXCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF161F3D), Color(0xFF0E1428))
)

val LinguaXGlassGradient = Brush.verticalGradient(
    listOf(Color(0xE61B264B), Color(0xCC0E1428))
)

val LinguaXGoldGradient = Brush.linearGradient(
    listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
)

val LinguaXFlameGradient = Brush.linearGradient(
    listOf(Color(0xFFFF8A65), Color(0xFFFF3D00))
)

val LinguaXGreenGradient = Brush.linearGradient(
    listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
)

val LinguaXPurpleGradient = Brush.linearGradient(
    listOf(Color(0xFF9D85FF), Color(0xFF6C5CE7))
)

val LinguaXCyanGradient = Brush.linearGradient(
    listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
)

val LinguaXTrophyCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF1A2346), Color(0xFF0F162F))
)

val LinguaXPillGradient = Brush.verticalGradient(
    listOf(Color(0x334F7CFF), Color(0x1A1E293B))
)

val LinguaXBorderGradient = Brush.linearGradient(
    listOf(
        Color(0xFF4F7CFF).copy(alpha = 0.55f),
        Color(0xFF22D3EE).copy(alpha = 0.25f),
        Color(0xFF1E293B).copy(alpha = 0.40f)
    )
)

