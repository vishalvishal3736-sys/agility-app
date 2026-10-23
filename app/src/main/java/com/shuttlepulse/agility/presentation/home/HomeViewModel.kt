package com.shuttlepulse.agility.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shuttlepulse.agility.core.audio.CadenceSoundEngine
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.SessionLog
import com.shuttlepulse.agility.domain.model.UserPreferences
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeCircuit: WorkoutCircuit? = null,
    val circuits: List<WorkoutCircuit> = emptyList(),
    val recentLogs: List<SessionLog> = emptyList(),
    val preferences: UserPreferences = UserPreferences()
)

class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val cadenceSoundEngine: CadenceSoundEngine
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        workoutRepository.observeActiveCircuit(),
        workoutRepository.observeAllCircuits(),
        workoutRepository.observeSessionLogs(),
        workoutRepository.observeUserPreferences()
    ) { active, circuits, logs, prefs ->
        cadenceSoundEngine.audioDuckingEnabled = prefs.audioDuckingEnabled
        cadenceSoundEngine.metronomeTickEnabled = prefs.metronomeTickEnabled
        cadenceSoundEngine.voiceCalloutsEnabled = prefs.voiceCalloutsEnabled

        HomeUiState(
            activeCircuit = active,
            circuits = circuits,
            recentLogs = logs,
            preferences = prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun setDominantHand(hand: DominantHand) {
        viewModelScope.launch {
            workoutRepository.setDominantHand(hand)
            cadenceSoundEngine.speakCue("${hand.label} selected. ${hand.racketSide} lead.")
        }
    }

    fun selectCircuit(circuit: WorkoutCircuit) {
        viewModelScope.launch {
            workoutRepository.setActiveCircuit(circuit)
        }
    }

    fun deleteCustomCircuit(id: String) {
        viewModelScope.launch {
            workoutRepository.deleteCustomCircuit(id)
        }
    }

    fun updateAudioPrefs(
        duckingEnabled: Boolean,
        metronomeEnabled: Boolean,
        voiceEnabled: Boolean,
        bpmScale: Float
    ) {
        viewModelScope.launch {
            workoutRepository.updateAudioPreferences(
                duckingEnabled = duckingEnabled,
                metronomeEnabled = metronomeEnabled,
                voiceEnabled = voiceEnabled,
                bpmScale = bpmScale
            )
        }
    }

    companion object {
        fun provideFactory(
            workoutRepository: WorkoutRepository,
            cadenceSoundEngine: CadenceSoundEngine
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(workoutRepository, cadenceSoundEngine) as T
            }
        }
    }
}
