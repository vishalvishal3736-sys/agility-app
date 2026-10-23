package com.shuttlepulse.agility.core.ui.theme

import androidx.compose.runtime.Composable
import com.shuttlepulse.agility.presentation.theme.ShuttlePulseColors as PresentationColors
import com.shuttlepulse.agility.presentation.theme.ShuttlePulseTheme as PresentationTheme
import com.shuttlepulse.agility.presentation.theme.TekoFontFamily as PresentationTeko
import com.shuttlepulse.agility.presentation.theme.SpaceGroteskFontFamily as PresentationSpaceGrotesk
import com.shuttlepulse.agility.presentation.theme.JetBrainsMonoFontFamily as PresentationJetBrainsMono

// Backwards-compatible aliases for core.ui.theme package
typealias ShuttlePulseColors = PresentationColors

val TekoFontFamily = PresentationTeko
val SpaceGroteskFontFamily = PresentationSpaceGrotesk
val JetBrainsMonoFontFamily = PresentationJetBrainsMono

@Composable
fun ShuttlePulseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    PresentationTheme(darkTheme = darkTheme, content = content)
}
