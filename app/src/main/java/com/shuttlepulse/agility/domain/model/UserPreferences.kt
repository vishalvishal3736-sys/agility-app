package com.shuttlepulse.agility.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val dominantHand: DominantHand = DominantHand.RIGHT,
    val audioDuckingEnabled: Boolean = true,
    val metronomeTickEnabled: Boolean = true,
    val voiceCalloutsEnabled: Boolean = true,
    val bpmOffset: Int = 0,               // Exact offset (-20 to +20 BPM)
    val bpmCalibrationScale: Float = 1.0f  // Multiplier (0.80f to 1.20f)
) {
    /**
     * Calculates the effective BPM by applying the calibration scale and the additive offset,
     * clamped to a safe athletic cadence range (60..300 BPM).
     */
    fun calculateEffectiveBpm(baseBpm: Int): Int {
        val scaled = (baseBpm * bpmCalibrationScale).toInt() + bpmOffset
        return scaled.coerceIn(60, 300)
    }
}

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
