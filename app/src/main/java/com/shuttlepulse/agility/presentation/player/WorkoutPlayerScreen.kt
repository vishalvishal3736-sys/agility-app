package com.shuttlepulse.agility.presentation.player

import androidx.compose.runtime.Composable

@Composable
fun WorkoutPlayerScreen(
    viewModel: PlayerViewModel,
    onExitPlayer: () -> Unit
) {
    ActivePlayerScreen(viewModel = viewModel, onExitPlayer = onExitPlayer)
}
