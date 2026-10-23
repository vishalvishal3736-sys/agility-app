package com.shuttlepulse.agility.domain.model

import java.util.Locale
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutCircuitItem(
    val exercise: Exercise,
    val sets: Int = 2,
    val workSec: Int = exercise.defaultWorkSec,
    val restSec: Int = exercise.defaultRestSec,
    val targetBpm: Int = exercise.targetBpm,
    val burstMinSec: Float = exercise.burstMinIntervalMs / 1000f,
    val burstMaxSec: Float = exercise.burstMaxIntervalMs / 1000f
)

@Serializable
data class WorkoutCircuit(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationBadge: String,
    val focusTag: String,
    val items: List<WorkoutCircuitItem>,
    val isPreBuilt: Boolean = true
) {
    val totalWorkSeconds: Int
        get() = items.sumOf { it.sets * it.workSec }

    val totalRestSeconds: Int
        get() = items.sumOf { it.sets * it.restSec }

    val totalDurationSeconds: Int
        get() = totalWorkSeconds + totalRestSeconds

    val totalDurationMinutes: Int
        get() = (totalDurationSeconds + 30) / 60

    val formattedTotalDuration: String
        get() {
            val mins = totalDurationSeconds / 60
            val secs = totalDurationSeconds % 60
            return if (secs == 0) {
                "${mins}m 00s"
            } else {
                String.format(Locale.US, "%dm %02ds", mins, secs)
            }
        }

    val totalSetsCount: Int
        get() = items.sumOf { it.sets }
}

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
