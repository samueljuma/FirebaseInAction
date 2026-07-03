package com.samueljuma.firebaseinaction.presentation.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = White,
    primaryContainer = AccentBlueContainer,
    onPrimaryContainer = OnAccentBlueContainer,
    secondary = InkMuted,
    onSecondary = White,
    secondaryContainer = PaperWarm2,
    onSecondaryContainer = InkWarm,
    background = PaperWhite,
    onBackground = InkWarm,
    surface = PaperWhite,
    onSurface = InkWarm,
    surfaceVariant = PaperWarm2,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = PaperWhite,
    surfaceContainerLow = PaperWarm1,
    surfaceContainer = PaperWarm2,
    surfaceContainerHigh = PaperWarm3,
    surfaceContainerHighest = PaperWarm4,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = White,
    scrim = Black
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueDark,
    onPrimary = OnAccentDarkInk,
    primaryContainer = AccentBlueContainerDark,
    onPrimaryContainer = OnAccentBlueContainerDark,
    secondary = MistMuted,
    onSecondary = Coal,
    secondaryContainer = Coal600,
    onSecondaryContainer = Mist,
    background = Coal,
    onBackground = Mist,
    surface = Coal,
    onSurface = Mist,
    surfaceVariant = Coal600,
    onSurfaceVariant = MistMuted,
    surfaceContainerLowest = Coal900,
    surfaceContainerLow = Coal800,
    surfaceContainer = Coal700,
    surfaceContainerHigh = Coal600,
    surfaceContainerHighest = Coal500,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    scrim = Black
)

/**
 * App theme. Follows the system light/dark setting with a fixed, brand-consistent
 * Notion-style palette (no dynamic color, so the look is identical across devices).
 *
 * Also drives the system-bar icon appearance: with an edge-to-edge, transparent
 * status/navigation bar, the icons must contrast with the app background — dark
 * icons in light mode, light icons in dark mode.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
