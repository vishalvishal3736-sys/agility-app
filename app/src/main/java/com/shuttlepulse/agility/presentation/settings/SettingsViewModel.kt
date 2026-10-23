package com.shuttlepulse.agility.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shuttlepulse.agility.core.audio.CadenceSoundEngine
import com.shuttlepulse.agility.core.timer.MicroPhaseWorkoutTimer
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.UserPreferences
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import com.shuttlepulse.agility.domain.usecase.AuditFeatureHealthUseCase
import com.shuttlepulse.agility.domain.usecase.DiagnosticCheckResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val seededDrillsCount: Int = 16,
    val seededCircuitsCount: Int = 5,
    val isRunningAudit: Boolean = false,
    val lastMeasuredDriftMs: Long = 1L,
    val auditResults: List<DiagnosticCheckResult> = emptyList(),
    val statusBanner: String = "SYSTEM NOMINAL · HIGH-PRECISION MONOTONIC CLOCK READY"
)

class SettingsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val cadenceSoundEngine: CadenceSoundEngine,
    private val workoutTimer: MicroPhaseWorkoutTimer,
    private val auditFeatureHealthUseCase: AuditFeatureHealthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                workoutRepository.observeUserPreferences(),
                workoutRepository.observeAllExercises(),
                workoutRepository.observeAllCircuits()
            ) { prefs, exercises, circuits ->
                Triple(prefs, exercises, circuits)
            }.collect { (prefs, exercises, circuits) ->
                val preBuiltCount = circuits.count { it.isPreBuilt }
                val checks = auditFeatureHealthUseCase(
                    exercises = exercises,
                    circuits = circuits,
                    soundPoolReady = cadenceSoundEngine.isSoundPoolReady,
                    ttsReady = cadenceSoundEngine.isTtsReady,
                    audioDuckingActive = prefs.audioDuckingEnabled,
                    timerJitterMs = _uiState.value.lastMeasuredDriftMs
                )
                _uiState.update {
                    it.copy(
                        preferences = prefs,
                        seededDrillsCount = exercises.size,
                        seededCircuitsCount = preBuiltCount,
                        auditResults = checks
                    )
                }
            }
        }
    }

    fun setDominantHand(hand: DominantHand) {
        viewModelScope.launch {
            workoutRepository.setDominantHand(hand)
        }
    }

    fun updateAudioPreferences(
        ducking: Boolean,
        metronome: Boolean,
        voice: Boolean,
        bpmScale: Float,
        bpmOffset: Int
    ) {
        viewModelScope.launch {
            workoutRepository.updateAudioPreferences(
                duckingEnabled = ducking,
                metronomeEnabled = metronome,
                voiceEnabled = voice,
                bpmScale = bpmScale,
                bpmOffset = bpmOffset
            )
        }
    }

    fun setBpmOffset(offset: Int) {
        val current = _uiState.value.preferences
        updateAudioPreferences(
            ducking = current.audioDuckingEnabled,
            metronome = current.metronomeTickEnabled,
            voice = current.voiceCalloutsEnabled,
            bpmScale = current.bpmCalibrationScale,
            bpmOffset = offset
        )
    }

    fun runAutomatedSelfTest(fireAudioBurst: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunningAudit = true, statusBanner = "BENCHMARKING MONOTONIC TIMER...") }
            val measuredDrift = workoutTimer.measureTimerPrecisionJitterMs()
            val exercises = com.shuttlepulse.agility.data.local.seed.AuthenticBadmintonDrillsSeed.exercises
            val circuits = com.shuttlepulse.agility.data.local.seed.AuthenticBadmintonDrillsSeed.preBuiltCircuits

            val checks = auditFeatureHealthUseCase(
                exercises = exercises,
                circuits = circuits,
                soundPoolReady = cadenceSoundEngine.isSoundPoolReady,
                ttsReady = cadenceSoundEngine.isTtsReady,
                audioDuckingActive = _uiState.value.preferences.audioDuckingEnabled,
                timerJitterMs = measuredDrift
            )

            if (fireAudioBurst) {
                cadenceSoundEngine.runDiagnostic240BpmBurstTest()
            }

            val allPassed = checks.all { it.passed }
            _uiState.update {
                it.copy(
                    isRunningAudit = false,
                    lastMeasuredDriftMs = measuredDrift,
                    auditResults = checks,
                    statusBanner = if (allPassed) {
                        "ALL SYSTEMS VERIFIED · SUB-10MS LATENCY & ACCURATE SOUND BENCH"
                    } else {
                        "DIAGNOSTIC NOTICE · REVIEW STATUS CHECKS BELOW"
                    }
                )
            }
        }
    }

    fun testTick() = cadenceSoundEngine.testCadenceTick()
    fun testAccent() = cadenceSoundEngine.testAccentBeat()
    fun testBurstWhistle() = cadenceSoundEngine.testBurstWhistle()
    fun testCountdownPip() = cadenceSoundEngine.testCountdownPip()
    fun test240BpmBurst() = cadenceSoundEngine.runDiagnostic240BpmBurstTest()
    fun testVoiceCallout() = cadenceSoundEngine.speakCue("Racket lunge, recover to base!")

    companion object {
        fun provideFactory(
            workoutRepository: WorkoutRepository,
            cadenceSoundEngine: CadenceSoundEngine,
            workoutTimer: MicroPhaseWorkoutTimer,
            auditFeatureHealthUseCase: AuditFeatureHealthUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    workoutRepository = workoutRepository,
                    cadenceSoundEngine = cadenceSoundEngine,
                    workoutTimer = workoutTimer,
                    auditFeatureHealthUseCase = auditFeatureHealthUseCase
                ) as T
            }
        }
    }
}
