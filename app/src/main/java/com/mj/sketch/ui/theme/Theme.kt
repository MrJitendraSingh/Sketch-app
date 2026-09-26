package com.mj.sketch.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = DarkGrey700,
    onPrimaryContainer = PureWhite,
    secondary = LightGrey200,
    onSecondary = PureBlack,
    secondaryContainer = MediumGrey600,
    onSecondaryContainer = PureWhite,
    tertiary = LightGrey300,
    onTertiary = PureBlack,
    background = DarkGrey900,
    onBackground = PureWhite,
    surface = DarkGrey900,
    onSurface = PureWhite,
    surfaceVariant = DarkGrey800,
    onSurfaceVariant = LightGrey300,
    surfaceContainerHigh = DarkGrey700,
    error = PureWhite,
    onError = PureBlack,
    errorContainer = MediumGrey600,
    onErrorContainer = PureWhite,
    outline = LightGrey400
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = LightGrey200,
    onPrimaryContainer = PureBlack,
    secondary = DarkGrey700,
    onSecondary = PureWhite,
    secondaryContainer = LightGrey100,
    onSecondaryContainer = PureBlack,
    tertiary = MediumGrey600,
    onTertiary = PureWhite,
    background = LightGrey100,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = LightGrey100,
    onSurfaceVariant = DarkGrey700,
    surfaceContainerHigh = LightGrey200,
    error = PureBlack,
    onError = PureWhite,
    errorContainer = LightGrey300,
    onErrorContainer = PureBlack,
    outline = MediumGrey500
)

@Composable
fun SketchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Enforce strict black and white theme without dynamic wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
