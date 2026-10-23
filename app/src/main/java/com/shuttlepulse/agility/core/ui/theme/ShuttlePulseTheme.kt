package com.shuttlepulse.agility.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttlepulse.agility.R

object ShuttlePulseColors {
    val OledGymBlack = Color(0xFF04060A)
    val GymSurfaceDark = Color(0xFF0B111C)
    val GymCardElevated = Color(0xFF131D2E)
    val GymBorderSubtle = Color(0xFF21324D)

    val VoltPlyoLime = Color(0xFFD4FF00)
    val FastFeetCyan = Color(0xFF00F0FF)
    val ExplosiveBurstCrimson = Color(0xFFFF2A5F)
    val UnilateralSwitchAmber = Color(0xFFFFB800)
    val RecoveryMint = Color(0xFF00E676)

    val TextCrispWhite = Color(0xFFF8FAFC)
    val TextSlateMuted = Color(0xFF94A3B8)
}

val TekoFontFamily = FontFamily(
    Font(R.font.teko, FontWeight.Normal),
    Font(R.font.teko, FontWeight.Bold)
)

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

private val ShuttlePulseColorScheme = darkColorScheme(
    primary = ShuttlePulseColors.VoltPlyoLime,
    onPrimary = Color(0xFF060900),
    primaryContainer = Color(0xFF263300),
    onPrimaryContainer = ShuttlePulseColors.VoltPlyoLime,
    secondary = ShuttlePulseColors.FastFeetCyan,
    onSecondary = Color(0xFF001E24),
    secondaryContainer = Color(0xFF062C36),
    onSecondaryContainer = ShuttlePulseColors.FastFeetCyan,
    tertiary = ShuttlePulseColors.ExplosiveBurstCrimson,
    onTertiary = Color.White,
    background = ShuttlePulseColors.OledGymBlack,
    onBackground = ShuttlePulseColors.TextCrispWhite,
    surface = ShuttlePulseColors.GymSurfaceDark,
    onSurface = ShuttlePulseColors.TextCrispWhite,
    surfaceVariant = ShuttlePulseColors.GymCardElevated,
    onSurfaceVariant = ShuttlePulseColors.TextSlateMuted,
    outline = ShuttlePulseColors.GymBorderSubtle
)

val ShuttlePulseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = TekoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 84.sp,
        lineHeight = 80.sp,
        letterSpacing = 1.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = TekoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 56.sp,
        letterSpacing = 1.sp
    ),
    displaySmall = TextStyle(
        fontFamily = TekoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.8.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp
    )
)

val ShuttlePulseShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun ShuttlePulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShuttlePulseColorScheme,
        typography = ShuttlePulseTypography,
        shapes = ShuttlePulseShapes,
        content = content
    )
}
