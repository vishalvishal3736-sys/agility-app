package com.shuttlepulse.agility.domain.model

data class PlayerUiState(
    val activeCircuit: WorkoutCircuit? = null,
    val segments: List<WorkoutSegment> = emptyList(),
    val currentSegmentIndex: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val segmentRemainingMs: Long = 0L,
    val segmentElapsedMs: Long = 0L,
    val totalWorkoutElapsedMs: Long = 0L,
    val currentBpm: Int = 150,
    // Real-time Cadence Pulse & Micro-Phase Telemetry
    val cadenceBeatPhase: Float = 0f,           // 0.0..1.0 sawtooth within each BPM beat
    val currentBeatCounter: Int = 0,
    val comboBeatStep: Int = 1,
    val comboTotalSteps: Int = 5,
    val activeHeadlineCue: String = "GET READY",
    val activeSubCue: String = "Balls of feet only · Stiff ankles",
    val activeLeadFootBadge: String = "Right (Racket) Foot Forward",
    val activePose: StancePoseType = StancePoseType.DOUBLE_LEG_POGO,
    val isExplosiveFlashActive: Boolean = false,
    val totalGroundContactsCount: Int = 0,
    val totalExplosiveTriggersFired: Int = 0,
    val userPreferences: UserPreferences = UserPreferences()
) {
    val currentSegment: WorkoutSegment?
        get() = segments.getOrNull(currentSegmentIndex)
}
