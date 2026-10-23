package com.shuttlepulse.agility.presentation.home

import androidx.compose.runtime.Composable
import com.shuttlepulse.agility.presentation.dashboard.DashboardScreen
import com.shuttlepulse.agility.presentation.dashboard.DurationFilter

typealias DurationFilter = DurationFilter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLaunchPlayer: () -> Unit,
    onOpenBuilder: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    DashboardScreen(
        viewModel = viewModel,
        onLaunchPlayer = onLaunchPlayer,
        onOpenBuilder = onOpenBuilder,
        onOpenLibrary = onOpenLibrary
    )
}
