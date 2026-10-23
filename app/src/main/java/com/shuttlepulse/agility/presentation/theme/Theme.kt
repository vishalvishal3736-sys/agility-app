package com.shuttlepulse.agility.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ShuttlePulseColorScheme = darkColorScheme(
    primary = ShuttlePulseColors.PlyoLime,
    onPrimary = ShuttlePulseColors.CourtObsidian,
    primaryContainer = ShuttlePulseColors.GymCardElevated,
    onPrimaryContainer = ShuttlePulseColors.PlyoLime,
    secondary = ShuttlePulseColors.FastFeetCyan,
    onSecondary = ShuttlePulseColors.CourtObsidian,
    secondaryContainer = ShuttlePulseColors.GymCardElevated,
    onSecondaryContainer = ShuttlePulseColors.FastFeetCyan,
    tertiary = ShuttlePulseColors.BurstCrimson,
    onTertiary = ShuttlePulseColors.TextCrispWhite,
    background = ShuttlePulseColors.CourtObsidian,
    onBackground = ShuttlePulseColors.TextCrispWhite,
    surface = ShuttlePulseColors.GymSurfaceDark,
    onSurface = ShuttlePulseColors.TextCrispWhite,
    surfaceVariant = ShuttlePulseColors.GymCardElevated,
    onSurfaceVariant = ShuttlePulseColors.TextSlateMuted,
    outline = ShuttlePulseColors.GymBorderSubtle
)

val ShuttlePulseShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ShuttlePulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ShuttlePulseColors.CourtObsidian.toArgb()
            window.navigationBarColor = ShuttlePulseColors.CourtObsidian.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = ShuttlePulseColorScheme,
        typography = ShuttlePulseTypography,
        shapes = ShuttlePulseShapes,
        content = content
    )
}
