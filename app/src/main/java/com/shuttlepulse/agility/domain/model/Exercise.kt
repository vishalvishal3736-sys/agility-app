package com.shuttlepulse.agility.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val archetype: DrillArchetype,
    val targetBpm: Int,
    val defaultWorkSec: Int,
    val defaultRestSec: Int,
    val comboBaseBeats: Int = 4,
    val comboExplodeCue: String = "EXPLODE!",
    val burstMinIntervalMs: Long = 2800L,
    val burstMaxIntervalMs: Long = 4500L,
    val reactiveBurstCues: List<String> = emptyList(),
    val isBilateralSwitchMidSet: Boolean = false,
    val coachingCues: List<String> = emptyList(),
    val biomechanicsSummary: String = "",
    val baseStancePose: StancePoseType = StancePoseType.DOUBLE_LEG_POGO,
    val peakActionPose: StancePoseType = StancePoseType.TUCK_JUMP_PEAK
) {
    val isCustom: Boolean
        get() = id.startsWith("custom_")
}

