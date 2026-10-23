package com.shuttlepulse.agility.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.SessionLog
import com.shuttlepulse.agility.domain.model.UserPreferences
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.repository.ShuttlePulseRepository
import com.shuttlepulse.agility.presentation.player.CadenceSoundEngine
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val activeCircuit: WorkoutCircuit? = null,
    val circuits: List<WorkoutCircuit> = emptyList(),
    val recentLogs: List<SessionLog> = emptyList(),
    val preferences: UserPreferences = UserPreferences()
)

open class DashboardViewModel @Inject constructor(
    private val repository: ShuttlePulseRepository,
    private val cadenceSoundEngine: CadenceSoundEngine
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeActiveCircuit(),
        repository.observeAllCircuits(),
        repository.observeSessionLogs(),
        repository.observeUserPreferences()
    ) { active, circuits, logs, prefs ->
        cadenceSoundEngine.audioDuckingEnabled = prefs.audioDuckingEnabled
        cadenceSoundEngine.metronomeTickEnabled = prefs.metronomeTickEnabled
        cadenceSoundEngine.voiceCalloutsEnabled = prefs.voiceCalloutsEnabled

        DashboardUiState(
            activeCircuit = active,
            circuits = circuits,
            recentLogs = logs,
            preferences = prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setDominantHand(hand: DominantHand) {
        viewModelScope.launch {
            repository.setDominantHand(hand)
            cadenceSoundEngine.speakCue("${hand.label} selected. ${hand.racketSide} lead.")
        }
    }

    fun selectCircuit(circuit: WorkoutCircuit) {
        viewModelScope.launch {
            repository.setActiveCircuit(circuit)
        }
    }

    fun deleteCustomCircuit(id: String) {
        viewModelScope.launch {
            repository.deleteCustomCircuit(id)
        }
    }

    fun updateAudioPrefs(
        duckingEnabled: Boolean,
        metronomeEnabled: Boolean,
        voiceEnabled: Boolean,
        bpmScale: Float,
        bpmOffset: Int = uiState.value.preferences.bpmOffset
    ) {
        viewModelScope.launch {
            repository.updateAudioPreferences(
                duckingEnabled = duckingEnabled,
                metronomeEnabled = metronomeEnabled,
                voiceEnabled = voiceEnabled,
                bpmScale = bpmScale,
                bpmOffset = bpmOffset
            )
        }
    }

    companion object {
        fun provideFactory(
            repository: ShuttlePulseRepository,
            cadenceSoundEngine: CadenceSoundEngine
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository, cadenceSoundEngine) as T
            }
        }
    }
}
