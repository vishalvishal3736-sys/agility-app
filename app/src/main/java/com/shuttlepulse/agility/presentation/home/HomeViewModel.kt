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
) : DashboardViewModel(repository, cadenceSoundEngine) {
    companion object {
        fun provideFactory(
            workoutRepository: WorkoutRepository,
            cadenceSoundEngine: CadenceSoundEngine
        ): androidx.lifecycle.ViewModelProvider.Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(workoutRepository, cadenceSoundEngine) as T
            }
        }
    }
}
