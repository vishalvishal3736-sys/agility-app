package com.shuttlepulse.agility.presentation.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shuttlepulse.agility.core.audio.CadenceSoundEngine
import com.shuttlepulse.agility.core.timer.MicroPhaseWorkoutTimer
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import com.shuttlepulse.agility.domain.usecase.AuditFeatureHealthUseCase
import com.shuttlepulse.agility.domain.usecase.DiagnosticCheckResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiagnosticsUiState(
    val isRunningAudit: Boolean = false,
    val lastMeasuredDriftMs: Long = 2L,
    val seededExerciseCount: Int = 16,
    val seededProgramCount: Int = 3,
    val results: List<DiagnosticCheckResult> = emptyList(),
    val statusBanner: String = "Ready to run full 7-point self-test audit."
)

class DiagnosticsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val cadenceSoundEngine: CadenceSoundEngine,
    private val workoutTimer: MicroPhaseWorkoutTimer,
    private val auditFeatureHealthUseCase: AuditFeatureHealthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    init {
        runAutomatedSelfTest(fireAudioBurst = false)
    }

    fun runAutomatedSelfTest(fireAudioBurst: Boolean = true) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunningAudit = true,
                    statusBanner = "Executing database seed check, 1s elapsedRealtime drift test & 240 BPM audio synthesis..."
                )
            }

            workoutRepository.ensureSeeded()
            val exercises = workoutRepository.observeAllExercises().first()
            val circuits = workoutRepository.observeAllCircuits().first()

            if (fireAudioBurst) {
                cadenceSoundEngine.runDiagnostic240BpmBurstTest()
            }

            val measuredDriftMs = workoutTimer.measureTimerPrecisionJitterMs()
            val checks = auditFeatureHealthUseCase(
                exercises = exercises,
                circuits = circuits,
                soundPoolReady = cadenceSoundEngine.isSoundPoolReady || cadenceSoundEngine.isAudioTrackReady,
                ttsReady = cadenceSoundEngine.isTtsReady,
                audioDuckingActive = cadenceSoundEngine.audioDuckingEnabled,
                timerJitterMs = measuredDriftMs
            )

            val passCount = checks.count { it.passed }
            _uiState.update {
                it.copy(
                    isRunningAudit = false,
                    lastMeasuredDriftMs = measuredDriftMs,
                    seededExerciseCount = exercises.size,
                    seededProgramCount = circuits.count { c -> c.isPreBuilt },
                    results = checks,
                    statusBanner = "Self-Test Complete: $passCount/${checks.size} Subsystems PASS (Drift: ${measuredDriftMs}ms)"
                )
            }
        }
    }

    fun test240BpmMetronomeAndWhistle() {
        cadenceSoundEngine.runDiagnostic240BpmBurstTest()
    }

    companion object {
        fun provideFactory(
            workoutRepository: WorkoutRepository,
            cadenceSoundEngine: CadenceSoundEngine,
            workoutTimer: MicroPhaseWorkoutTimer,
            auditFeatureHealthUseCase: AuditFeatureHealthUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DiagnosticsViewModel(
                    workoutRepository = workoutRepository,
                    cadenceSoundEngine = cadenceSoundEngine,
                    workoutTimer = workoutTimer,
                    auditFeatureHealthUseCase = auditFeatureHealthUseCase
                ) as T
            }
        }
    }
}
