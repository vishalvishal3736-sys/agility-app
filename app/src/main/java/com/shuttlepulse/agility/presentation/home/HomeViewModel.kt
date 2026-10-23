package com.shuttlepulse.agility.presentation.home

import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import com.shuttlepulse.agility.presentation.dashboard.DashboardUiState
import com.shuttlepulse.agility.presentation.dashboard.DashboardViewModel
import com.shuttlepulse.agility.presentation.player.CadenceSoundEngine
import javax.inject.Inject

typealias HomeUiState = DashboardUiState

class HomeViewModel @Inject constructor(
    repository: WorkoutRepository,
    cadenceSoundEngine: CadenceSoundEngine
) : DashboardViewModel(repository, cadenceSoundEngine)
