package com.shuttlepulse.agility.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SegmentType {
    PREP_COUNTDOWN,
    WORK_SET,
    REST_RECOVERY
}

@Serializable
enum class MicroCueUrgency {
    CADENCE_BEAT,
    COMBO_COUNT,
    EXPLOSIVE_BURST,
    UNILATERAL_SWITCH,
    COACHING_REMINDER
}

@Serializable
data class MicroPhaseCue(
    val timestampOffsetMs: Long,
    val displayBanner: String,
    val spokenCallout: String?,
    val urgency: MicroCueUrgency,
    val beatInCycle: Int = 1,
    val totalBeatsInCycle: Int = 1,
    val poseOverride: StancePoseType? = null
)

@Serializable
data class WorkoutSegment(
    val segmentIndex: Int,
    val type: SegmentType,
    val exercise: Exercise,
    val setNumberWithinExercise: Int,
    val totalSetsForExercise: Int,
    val overallSetIndex: Int,
    val totalWorkSetsInWorkout: Int,
    val durationMs: Long,
    val targetBpm: Int,
    val initialLeadFootBanner: String,
    val midSetLeadFootBanner: String?,
    val nextExercisePreviewName: String?,
    val scheduledMicroCues: List<MicroPhaseCue> = emptyList()
)
