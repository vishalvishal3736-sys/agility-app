package com.shuttlepulse.agility.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shuttlepulse.agility.core.audio.CadenceSoundEngine
import com.shuttlepulse.agility.core.timer.MicroPhaseWorkoutTimer
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.MicroCueUrgency
import com.shuttlepulse.agility.domain.model.PlayerUiState
import com.shuttlepulse.agility.domain.model.SegmentType
import com.shuttlepulse.agility.domain.model.SessionLog
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.model.WorkoutSegment
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import com.shuttlepulse.agility.domain.usecase.GenerateWorkoutTimelineUseCase
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val cadenceSoundEngine: CadenceSoundEngine,
    private val workoutTimer: MicroPhaseWorkoutTimer,
    private val generateWorkoutTimelineUseCase: GenerateWorkoutTimelineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var timerLoopJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                workoutRepository.observeActiveCircuit(),
                workoutRepository.observeUserPreferences()
            ) { circuit, prefs ->
                circuit to prefs
            }.collect { (circuit, prefs) ->
                cadenceSoundEngine.audioDuckingEnabled = prefs.audioDuckingEnabled
                cadenceSoundEngine.metronomeTickEnabled = prefs.metronomeTickEnabled
                cadenceSoundEngine.voiceCalloutsEnabled = prefs.voiceCalloutsEnabled

                val circuitChanged = _uiState.value.activeCircuit?.id != circuit.id
                if (circuitChanged) {
                    timerLoopJob?.cancel()
                }

                if (!_uiState.value.isRunning || circuitChanged) {
                    val timeline = generateWorkoutTimelineUseCase(
                        circuit = circuit,
                        dominantHand = prefs.dominantHand,
                        bpmScale = prefs.bpmCalibrationScale
                    )
                    val firstSeg = timeline.firstOrNull()
                    _uiState.update {
                        it.copy(
                            activeCircuit = circuit,
                            segments = timeline,
                            currentSegmentIndex = 0,
                            segmentElapsedMs = 0L,
                            segmentRemainingMs = firstSeg?.durationMs ?: 5_000L,
                            currentBpm = firstSeg?.targetBpm ?: 150,
                            activeHeadlineCue = firstSeg?.exercise?.name ?: circuit.title,
                            activeSubCue = firstSeg?.exercise?.coachingCues?.firstOrNull()
                                ?: "Balls of feet only · Stiff ankles",
                            activeLeadFootBadge = firstSeg?.initialLeadFootBanner
                                ?: prefs.dominantHand.leadFootInstruction(true),
                            activePose = firstSeg?.exercise?.baseStancePose
                                ?: it.activePose,
                            isRunning = false,
                            isPaused = false,
                            isCompleted = false,
                            isExplosiveFlashActive = false,
                            userPreferences = prefs
                        )
                    }
                } else {
                    _uiState.update { it.copy(userPreferences = prefs) }
                }
            }
        }
    }

    fun selectPreBuiltPlanByDuration(minutes: Int) {
        val targetId = when (minutes) {
            15 -> "circuit_15min_cns_activation"
            20 -> "circuit_20min_chinese_footwork"
            25 -> "circuit_25min_tendon_plyo"
            else -> "circuit_15min_cns_activation"
        }
        val circuit = com.shuttlepulse.agility.data.local.seed.AuthenticBadmintonDrillsSeed.preBuiltCircuits
            .firstOrNull { it.id == targetId }
            ?: return
        viewModelScope.launch {
            workoutRepository.setActiveCircuit(circuit)
        }
    }

    fun startOrRestartWorkout() {
        timerLoopJob?.cancel()
        val state = _uiState.value
        val circuit = state.activeCircuit ?: return
        val prefs = state.userPreferences
        val timeline = generateWorkoutTimelineUseCase(
            circuit = circuit,
            dominantHand = prefs.dominantHand,
            bpmScale = prefs.bpmCalibrationScale
        )
        if (timeline.isEmpty()) return

        _uiState.update {
            it.copy(
                segments = timeline,
                currentSegmentIndex = 0,
                isRunning = true,
                isPaused = false,
                isCompleted = false,
                totalWorkoutElapsedMs = 0L,
                totalGroundContactsCount = 0,
                totalExplosiveTriggersFired = 0
            )
        }
        launchSegmentRunner(startIndex = 0)
    }

    fun togglePauseResume() {
        if (!_uiState.value.isRunning || _uiState.value.isCompleted) return
        val nextPaused = !_uiState.value.isPaused
        _uiState.update {
            it.copy(
                isPaused = nextPaused,
                activeHeadlineCue = if (nextPaused) "WORKOUT PAUSED" else it.activeHeadlineCue
            )
        }
        if (nextPaused) {
            cadenceSoundEngine.speakCue("Paused")
        } else {
            cadenceSoundEngine.speakCue("Resume")
        }
    }

    fun skipToNextSegment() {
        val state = _uiState.value
        val nextIdx = state.currentSegmentIndex + 1
        timerLoopJob?.cancel()
        if (nextIdx >= state.segments.size) {
            completeWorkoutAndSaveLog()
        } else {
            if (!state.isRunning) {
                _uiState.update { it.copy(isRunning = true, isPaused = false, isCompleted = false) }
            }
            launchSegmentRunner(startIndex = nextIdx)
        }
    }

    fun skipToPreviousSegment() {
        val state = _uiState.value
        val prevIdx = (state.currentSegmentIndex - 1).coerceAtLeast(0)
        timerLoopJob?.cancel()
        if (!state.isRunning) {
            _uiState.update { it.copy(isRunning = true, isPaused = false, isCompleted = false) }
        }
        launchSegmentRunner(startIndex = prevIdx)
    }

    /**
     * Adds +15 seconds to the active segment (especially useful during Rest Recovery)
     * without spawning duplicate coroutine timers.
     */
    fun addFifteenSecondsRest() {
        val state = _uiState.value
        val currentSeg = state.currentSegment ?: return
        val updatedDuration = currentSeg.durationMs + 15_000L
        val updatedSegments = state.segments.toMutableList().apply {
            set(state.currentSegmentIndex, currentSeg.copy(durationMs = updatedDuration))
        }
        _uiState.update {
            it.copy(
                segments = updatedSegments,
                segmentRemainingMs = it.segmentRemainingMs + 15_000L,
                activeHeadlineCue = "+15S RECOVERY ADDED"
            )
        }
        cadenceSoundEngine.speakCue("Added fifteen seconds")
    }

    fun triggerManualExplosiveBurst() {
        val seg = _uiState.value.currentSegment ?: return
        val triggerText = seg.exercise.reactiveBurstCues.randomOrNull()
            ?: seg.exercise.comboExplodeCue
        cadenceSoundEngine.playExplosiveBurstWhistle(triggerText.removeSuffix("!"))
        _uiState.update {
            it.copy(
                activeHeadlineCue = triggerText,
                activePose = seg.exercise.peakActionPose,
                isExplosiveFlashActive = true,
                totalExplosiveTriggersFired = it.totalExplosiveTriggersFired + 1
            )
        }
    }

    fun stopAndResetWorkout() {
        timerLoopJob?.cancel()
        cadenceSoundEngine.stopSpeaking()
        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isCompleted = false,
                currentSegmentIndex = 0,
                isExplosiveFlashActive = false,
                activeHeadlineCue = "READY TO LAUNCH"
            )
        }
    }

    private fun launchSegmentRunner(startIndex: Int) {
        timerLoopJob?.cancel()
        timerLoopJob = viewModelScope.launch {
            var segIndex = startIndex
            while (isActive && segIndex < _uiState.value.segments.size) {
                val segment = _uiState.value.segments[segIndex]
                runSingleSegment(segIndex, segment)
                segIndex++
            }
            if (isActive && segIndex >= _uiState.value.segments.size) {
                completeWorkoutAndSaveLog()
            }
        }
    }

    private suspend fun runSingleSegment(segIndex: Int, initialSegment: WorkoutSegment) {
        var elapsedInSegMs = 0L
        var lastTickRealtimeMs = workoutTimer.nowElapsedRealtimeMs()
        var lastBeatIndex = -1
        val firedCueIndices = mutableSetOf<Int>()
        var lastCountdownSecondSpoken = -1
        var lastFlashStartedAtMs = -1L

        // Announce Segment Start concisely so TTS finishes well before the 3-2-1 countdown pips
        when (initialSegment.type) {
            SegmentType.PREP_COUNTDOWN -> {
                cadenceSoundEngine.speakCue("Ready: ${initialSegment.exercise.name}")
            }
            SegmentType.REST_RECOVERY -> {
                val nextName = initialSegment.nextExercisePreviewName ?: initialSegment.exercise.name
                cadenceSoundEngine.playExplosiveBurstWhistle("Rest. Next: $nextName")
            }
            SegmentType.WORK_SET -> {
                // Micro-cue at 0ms + beat 0 handle the immediate start sound
            }
        }

        _uiState.update {
            it.copy(
                currentSegmentIndex = segIndex,
                segmentElapsedMs = 0L,
                segmentRemainingMs = initialSegment.durationMs,
                currentBpm = initialSegment.targetBpm,
                activeHeadlineCue = when (initialSegment.type) {
                    SegmentType.PREP_COUNTDOWN -> "PREP · ${initialSegment.exercise.name.uppercase()}"
                    SegmentType.REST_RECOVERY -> "ACTIVE RECOVERY REST"
                    SegmentType.WORK_SET -> initialSegment.exercise.name.uppercase()
                },
                activeSubCue = initialSegment.exercise.coachingCues.firstOrNull()
                    ?: "Balls of feet only · Stiff ankles",
                activeLeadFootBadge = initialSegment.initialLeadFootBanner,
                activePose = initialSegment.exercise.baseStancePose,
                isExplosiveFlashActive = false
            )
        }

        // Reset monotonic reference right after initial state setup so frame 0 has 0ms drift
        lastTickRealtimeMs = workoutTimer.nowElapsedRealtimeMs()
        var lastUiEmitRealtimeMs = 0L

        while (true) {
            val latestSeg = _uiState.value.segments.getOrNull(segIndex) ?: initialSegment
            val targetDurationMs = latestSeg.durationMs
            val nowRealtime = workoutTimer.nowElapsedRealtimeMs()
            val deltaMs = (nowRealtime - lastTickRealtimeMs).coerceAtLeast(0L)
            lastTickRealtimeMs = nowRealtime

            if (!_uiState.value.isPaused) {
                elapsedInSegMs = (elapsedInSegMs + deltaMs).coerceAtMost(targetDurationMs)
                val remainingMs = (targetDurationMs - elapsedInSegMs).coerceAtLeast(0L)

                if (latestSeg.type == SegmentType.WORK_SET) {
                    // 1. Fire scheduled MicroPhaseCues (Combo counts, Reactive Bursts, Unilateral 50% Switch) first
                    val cues = latestSeg.scheduledMicroCues
                    for (cIdx in cues.indices) {
                        if (cIdx !in firedCueIndices && elapsedInSegMs >= cues[cIdx].timestampOffsetMs) {
                            firedCueIndices.add(cIdx)
                            val cue = cues[cIdx]
                            if (cue.urgency == MicroCueUrgency.EXPLOSIVE_BURST ||
                                cue.urgency == MicroCueUrgency.UNILATERAL_SWITCH
                            ) {
                                lastFlashStartedAtMs = elapsedInSegMs
                            }
                            handleMicroPhaseCueFired(latestSeg, cue)
                        }
                    }

                    // Auto-clear explosive flash after 950ms for Unilateral Switch or Manual Burst
                    if (_uiState.value.isExplosiveFlashActive &&
                        lastFlashStartedAtMs >= 0L &&
                        elapsedInSegMs - lastFlashStartedAtMs >= 950L &&
                        latestSeg.exercise.archetype != DrillArchetype.COMBO_CYCLE
                    ) {
                        lastFlashStartedAtMs = -1L
                        _uiState.update { s ->
                            s.copy(
                                isExplosiveFlashActive = false,
                                activePose = latestSeg.exercise.baseStancePose
                            )
                        }
                    }

                    // 2. Compute exact monotonic BPM beat index and intra-beat animation phase
                    val (beatIdx, beatPhase) = workoutTimer.computeCadenceState(
                        elapsedMs = elapsedInSegMs,
                        bpm = latestSeg.targetBpm
                    )

                    if (beatIdx > lastBeatIndex) {
                        lastBeatIndex = beatIdx
                        val archetype = latestSeg.exercise.archetype
                        val isDuringFlashHold = _uiState.value.isExplosiveFlashActive &&
                            (elapsedInSegMs - lastFlashStartedAtMs in 0L..850L)

                        // Play cadence tick when appropriate (COMBO_CYCLE plays ticks via COMBO_COUNT cues;
                        // BASE_PLUS_REACTIVE_BURST pauses fast-tapping ticks during the 1000ms burst hold)
                        if (archetype != DrillArchetype.COMBO_CYCLE && !isDuringFlashHold) {
                            cadenceSoundEngine.playCadenceTick(isAccentBeat = (beatIdx % 4 == 0))
                        }

                        _uiState.update { s ->
                            s.copy(
                                currentBeatCounter = beatIdx,
                                totalGroundContactsCount = s.totalGroundContactsCount + 1
                            )
                        }
                    }

                    // Smooth UI throttle (~35fps) to eliminate recomposition thrashing and stutter
                    val shouldEmitUi = (nowRealtime - lastUiEmitRealtimeMs >= 28L) ||
                        (elapsedInSegMs >= targetDurationMs)
                    if (shouldEmitUi) {
                        lastUiEmitRealtimeMs = nowRealtime
                        _uiState.update { s ->
                            s.copy(
                                segmentElapsedMs = elapsedInSegMs,
                                segmentRemainingMs = remainingMs,
                                totalWorkoutElapsedMs = s.totalWorkoutElapsedMs + deltaMs,
                                cadenceBeatPhase = beatPhase
                            )
                        }
                    }
                } else {
                    // PREP_COUNTDOWN or REST_RECOVERY: Synchronized 3 - 2 - 1 audio pips & HUD banner
                    val secLeft = ((remainingMs + 999L) / 1000L).toInt()
                    if (secLeft in 1..3 && secLeft != lastCountdownSecondSpoken) {
                        lastCountdownSecondSpoken = secLeft
                        if (secLeft == 3) {
                            // Ensure any Rest/Prep TTS preview stops before the 3-2-1 countdown pips
                            cadenceSoundEngine.stopSpeaking()
                        }
                        cadenceSoundEngine.playCountdownPip(isFinalSecond = (secLeft == 1))
                    }

                    val countdownBanner = if (secLeft in 1..3) {
                        "GET SET · $secLeft"
                    } else if (latestSeg.type == SegmentType.PREP_COUNTDOWN) {
                        "PREP · ${latestSeg.exercise.name.uppercase()}"
                    } else {
                        "NEXT: ${(latestSeg.nextExercisePreviewName ?: latestSeg.exercise.name).uppercase()}"
                    }

                    val shouldEmitUi = (nowRealtime - lastUiEmitRealtimeMs >= 28L) ||
                        (elapsedInSegMs >= targetDurationMs)
                    if (shouldEmitUi) {
                        lastUiEmitRealtimeMs = nowRealtime
                        _uiState.update { s ->
                            s.copy(
                                segmentElapsedMs = elapsedInSegMs,
                                segmentRemainingMs = remainingMs,
                                totalWorkoutElapsedMs = s.totalWorkoutElapsedMs + deltaMs,
                                activeHeadlineCue = countdownBanner,
                                cadenceBeatPhase = ((elapsedInSegMs % 1000L) / 1000f)
                            )
                        }
                    }
                }

                if (elapsedInSegMs >= targetDurationMs) break
            }

            // 8ms polling (~120Hz) for tight sub-10ms audio-visual beat alignment
            delay(8L)
        }
    }

    private fun handleMicroPhaseCueFired(
        segment: WorkoutSegment,
        cue: com.shuttlepulse.agility.domain.model.MicroPhaseCue
    ) {
        when (cue.urgency) {
            MicroCueUrgency.EXPLOSIVE_BURST -> {
                cadenceSoundEngine.playExplosiveBurstWhistle(cue.spokenCallout)
                _uiState.update { s ->
                    s.copy(
                        activeHeadlineCue = cue.displayBanner,
                        comboBeatStep = cue.beatInCycle,
                        comboTotalSteps = cue.totalBeatsInCycle,
                        activePose = cue.poseOverride ?: segment.exercise.peakActionPose,
                        isExplosiveFlashActive = true,
                        totalExplosiveTriggersFired = s.totalExplosiveTriggersFired + 1
                    )
                }
            }

            MicroCueUrgency.UNILATERAL_SWITCH -> {
                cadenceSoundEngine.playUnilateralSwitchCue(cue.spokenCallout ?: "Switch sides")
                _uiState.update { s ->
                    s.copy(
                        activeHeadlineCue = cue.displayBanner,
                        activeLeadFootBadge = segment.midSetLeadFootBanner ?: s.activeLeadFootBadge,
                        activePose = cue.poseOverride ?: segment.exercise.peakActionPose,
                        isExplosiveFlashActive = true
                    )
                }
            }

            MicroCueUrgency.COMBO_COUNT -> {
                cadenceSoundEngine.playCadenceTick(isAccentBeat = (cue.beatInCycle == 1))
                _uiState.update { s ->
                    s.copy(
                        activeHeadlineCue = cue.displayBanner,
                        comboBeatStep = cue.beatInCycle,
                        comboTotalSteps = cue.totalBeatsInCycle,
                        activePose = cue.poseOverride ?: segment.exercise.baseStancePose,
                        isExplosiveFlashActive = false
                    )
                }
            }

            MicroCueUrgency.CADENCE_BEAT,
            MicroCueUrgency.COACHING_REMINDER -> {
                cue.spokenCallout?.let { cadenceSoundEngine.speakCue(it) }
                val tipIdx = (cue.timestampOffsetMs / 5000L).toInt()
                val nextTip = segment.exercise.coachingCues.let { list ->
                    if (list.isEmpty()) "Balls of feet only · Stiff ankles"
                    else list[tipIdx % list.size]
                }
                _uiState.update { s ->
                    s.copy(
                        activeHeadlineCue = cue.displayBanner,
                        activeSubCue = nextTip,
                        activePose = cue.poseOverride ?: segment.exercise.baseStancePose,
                        isExplosiveFlashActive = false
                    )
                }
            }
        }
    }

    private fun completeWorkoutAndSaveLog() {
        val state = _uiState.value
        val circuit = state.activeCircuit ?: return
        cadenceSoundEngine.playExplosiveBurstWhistle("Workout complete! Great footwork session.")

        val completedWorkSets = state.segments.count { it.type == SegmentType.WORK_SET }
        val log = SessionLog(
            workoutTitle = circuit.title,
            timestampEpochMs = System.currentTimeMillis(),
            completedSets = completedWorkSets.coerceAtLeast(1),
            totalWorkSec = circuit.totalWorkSeconds,
            totalRestSec = circuit.totalRestSeconds,
            estimatedGroundContacts = state.totalGroundContactsCount.coerceAtLeast(120),
            explosiveBurstsExecuted = state.totalExplosiveTriggersFired.coerceAtLeast(12),
            dominantHandUsed = state.userPreferences.dominantHand
        )

        viewModelScope.launch {
            workoutRepository.logCompletedSession(log)
        }

        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isCompleted = true,
                activeHeadlineCue = "SESSION COMPLETE!",
                isExplosiveFlashActive = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerLoopJob?.cancel()
    }

    companion object {
        fun provideFactory(
            workoutRepository: WorkoutRepository,
            cadenceSoundEngine: CadenceSoundEngine,
            workoutTimer: MicroPhaseWorkoutTimer,
            generateWorkoutTimelineUseCase: GenerateWorkoutTimelineUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerViewModel(
                    workoutRepository = workoutRepository,
                    cadenceSoundEngine = cadenceSoundEngine,
                    workoutTimer = workoutTimer,
                    generateWorkoutTimelineUseCase = generateWorkoutTimelineUseCase
                ) as T
            }
        }
    }
}
