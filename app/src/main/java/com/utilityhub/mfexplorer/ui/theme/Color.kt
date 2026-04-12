package com.utilityhub.mfexplorer.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Immutable

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val DarkBg        = Color(0xFF080C12)
val SurfaceDark   = Color(0xFF0E1420)
val SurfaceCard   = Color(0xFF141C2E)
val AccentGold    = Color(0xFF4D9FFF)
val AccentGoldDim = Color(0xFF1A5099)
val PositiveGreen = Color(0xFF00D4AA)
val NegativeRed   = Color(0xFFFF4D6A)
val TextPrimary   = Color(0xFFE8EDF5)
val TextSecondary = Color(0xFF6878A0)
val TextTertiary  = Color(0xFF374060)
val ChartLine     = Color(0xFF4D9FFF)
val DividerColor  = Color(0xFF161E30)

val CardAccents = listOf(
    Color(0xFF4D9FFF),  // electric blue
    Color(0xFF00D4AA),  // cyan-green
    Color(0xFF7B8FFF),  // periwinkle
)

val FolderAccents = listOf(
    Color(0xFF4D9FFF),
    Color(0xFF00D4AA),
    Color(0xFF7B8FFF),
)

@Immutable
data class CustomAppColors(
    val darkBg: Color,
    val surfaceDark: Color,
    val surfaceCard: Color,
    val accentGold: Color,
    val accentGoldDim: Color,
    val positiveGreen: Color,
    val negativeRed: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val chartLine: Color,
    val dividerColor: Color,
    val cardAccents: List<Color>,
    val folderAccents: List<Color>
)

fun darkCustomColors() = CustomAppColors(
    darkBg        = Color(0xFF080C12),
    surfaceDark   = Color(0xFF0E1420),
    surfaceCard   = Color(0xFF141C2E),
    accentGold    = Color(0xFF4D9FFF),
    accentGoldDim = Color(0xFF1A5099),
    positiveGreen = Color(0xFF00D4AA),
    negativeRed   = Color(0xFFFF4D6A),
    textPrimary   = Color(0xFFE8EDF5),
    textSecondary = Color(0xFF6878A0),
    textTertiary  = Color(0xFF374060),
    chartLine     = Color(0xFF4D9FFF),
    dividerColor  = Color(0xFF161E30),
    cardAccents   = listOf(
        Color(0xFF4D9FFF),
        Color(0xFF629FAD),
        Color(0xFF7B8FFF),
    ),
    folderAccents = listOf(
        Color(0xFF4D9FFF),
        Color(0xFF629FAD),
        Color(0xFF7B8FFF),
    )
)

fun lightCustomColors() = CustomAppColors(
    darkBg        = Color(0xFFF0F4FA),
    surfaceDark   = Color(0xFFFFFFFF),
    surfaceCard   = Color(0xFFFFFFFF),
    accentGold    = Color(0xFF1A7AE8),
    accentGoldDim = Color(0xFF0D4A9E),
    positiveGreen = Color(0xFF00A888),
    negativeRed   = Color(0xFFE8364F),
    textPrimary   = Color(0xFF0A1020),
    textSecondary = Color(0xFF4A5878),
    textTertiary  = Color(0xFF8A96B0),
    chartLine     = Color(0xFF1A7AE8),
    dividerColor  = Color(0xFFDDE4F0),
    cardAccents   = listOf(
        Color(0xFF1A7AE8),
        Color(0xFF629FAD),
        Color(0xFF5060CC),
    ),
    folderAccents = listOf(
        Color(0xFF1A7AE8),
        Color(0xFF629FAD),
        Color(0xFF5060CC),
    )
)

val LocalCustomColors = staticCompositionLocalOf { lightCustomColors() }