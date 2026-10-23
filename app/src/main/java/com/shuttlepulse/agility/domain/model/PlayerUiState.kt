package com.shuttlepulse.agility.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val dominantHand: DominantHand = DominantHand.RIGHT,
    val audioDuckingEnabled: Boolean = true,
    val metronomeTickEnabled: Boolean = true,
    val voiceCalloutsEnabled: Boolean = true,
    val bpmCalibrationScale: Float = 1.0f
)

@Serializable
data class SessionLog(
    val id: Long = 0L,
    val workoutTitle: String,
    val timestampEpochMs: Long,
    val completedSets: Int,
    val totalWorkSec: Int,
    val totalRestSec: Int,
    val estimatedGroundContacts: Int,
    val explosiveBurstsExecuted: Int,
    val dominantHandUsed: DominantHand
)

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
