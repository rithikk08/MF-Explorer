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

val DarkBg         = Color(0xFF0F1015)
val SurfaceDark    = Color(0xFF16181F)
val SurfaceCard    = Color(0xFF1E2029)
val AccentGold     = Color(0xFF8B7FE8)
val AccentGoldDim  = Color(0xFF524B99)
val PositiveGreen  = Color(0xFF3ECFA0)
val NegativeRed    = Color(0xFFFF6B6B)
val TextPrimary    = Color(0xFFF2F3F7)
val TextSecondary  = Color(0xFF8890A6)
val TextTertiary   = Color(0xFF484F66)
val ChartLine      = Color(0xFF8B7FE8)
val DividerColor   = Color(0xFF22252F)

val CardAccents = listOf(
    Color(0xFF3ECFA0),
    Color(0xFF8B7FE8),
    Color(0xFF56B4D3),
    Color(0xFFE07B5A),
    Color(0xFFE8A84B),
    Color(0xFFB39DDB),
)

val FolderAccents = listOf(
    Color(0xFF3ECFA0),
    Color(0xFF8B7FE8),
    Color(0xFF56B4D3),
    Color(0xFFE07B5A),
    Color(0xFFE8A84B),
    Color(0xFFB39DDB),
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

// for dark mode
fun darkCustomColors() = CustomAppColors(
    darkBg        = Color(0xFF0F1015),
    surfaceDark   = Color(0xFF16181F),
    surfaceCard   = Color(0xFF1E2029),
    accentGold    = Color(0xFF8B7FE8),
    accentGoldDim = Color(0xFF524B99),
    positiveGreen = Color(0xFF3ECFA0),
    negativeRed   = Color(0xFFFF6B6B),
    textPrimary   = Color(0xFFF2F3F7),
    textSecondary = Color(0xFF8890A6),
    textTertiary  = Color(0xFF484F66),
    chartLine     = Color(0xFF8B7FE8),
    dividerColor  = Color(0xFF22252F),
    cardAccents   = CardAccents,
    folderAccents = FolderAccents
)
// light mode
fun lightCustomColors() = CustomAppColors(
    darkBg        = Color(0xFFF4F4F8),
    surfaceDark   = Color(0xFFFFFFFF),
    surfaceCard   = Color(0xFFFFFFFF),
    accentGold    = Color(0xFF6C61D4),
    accentGoldDim = Color(0xFF3D378A),
    positiveGreen = Color(0xFF28A882),
    negativeRed   = Color(0xFFE84545),
    textPrimary   = Color(0xFF12131A),
    textSecondary = Color(0xFF585F78),
    textTertiary  = Color(0xFF9298AF),
    chartLine     = Color(0xFF6C61D4),
    dividerColor  = Color(0xFFE2E3EC),
    cardAccents   = CardAccents,
    folderAccents = FolderAccents
)

val LocalCustomColors = staticCompositionLocalOf { lightCustomColors() }