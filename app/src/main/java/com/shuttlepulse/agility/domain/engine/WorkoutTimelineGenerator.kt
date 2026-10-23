package com.shuttlepulse.agility.domain.engine

import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.MicroCueUrgency
import com.shuttlepulse.agility.domain.model.MicroPhaseCue
import com.shuttlepulse.agility.domain.model.SegmentType
import com.shuttlepulse.agility.domain.model.StancePoseType
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.model.WorkoutCircuitItem
import com.shuttlepulse.agility.domain.model.WorkoutSegment
import javax.inject.Inject
import kotlin.random.Random

/**
 * Compiles a structured [WorkoutCircuit] into an execution timeline of flat [WorkoutSegment]s:
 * - Inserts a 5-second initial prep segment with dominant lead stance preview.
 * - Generates micro-phase cues for each work set (phase-locked combo counts, randomized burst triggers, 50% side switches).
 * - Schedules active rest recovery intervals between consecutive work sets.
 */
class WorkoutTimelineGenerator @Inject constructor() {

    fun generateTimeline(
        circuit: WorkoutCircuit,
        dominantHand: DominantHand,
        bpmScale: Float = 1.0f,
        bpmOffset: Int = 0,
        random: Random = Random.Default
    ): List<WorkoutSegment> {
        val segments = mutableListOf<WorkoutSegment>()
        val items = circuit.items
        if (items.isEmpty()) return emptyList()

        val totalWorkSets = items.sumOf { it.sets.coerceAtLeast(1) }
        var segIdx = 0
        var workSetCounter = 0

        // Initial 5-second Prep Countdown before the first set
        val firstItem = items.first()
        val firstItemBpm = ((firstItem.targetBpm * bpmScale).toInt() + bpmOffset).coerceIn(60, 300)
        segments.add(
            WorkoutSegment(
                segmentIndex = segIdx++,
                type = SegmentType.PREP_COUNTDOWN,
                exercise = firstItem.exercise,
                setNumberWithinExercise = 1,
                totalSetsForExercise = firstItem.sets,
                overallSetIndex = 1,
                totalWorkSetsInWorkout = totalWorkSets,
                durationMs = 5_000L,
                targetBpm = firstItemBpm,
                initialLeadFootBanner = resolveInitialStanceBanner(firstItem.exercise, dominantHand),
                midSetLeadFootBanner = null,
                nextExercisePreviewName = firstItem.exercise.name
            )
        )

        for (itemIndex in items.indices) {
            val item = items[itemIndex]
            val exercise = item.exercise
            val scaledBpm = ((item.targetBpm * bpmScale).toInt() + bpmOffset).coerceIn(60, 300)
            val workDurationMs = (item.workSec.coerceAtLeast(10)) * 1000L
            val restDurationMs = (item.restSec.coerceAtLeast(5)) * 1000L

            for (setNum in 1..item.sets.coerceAtLeast(1)) {
                workSetCounter++
                val microCues = generateMicroCuesForWorkSet(
                    item = item,
                    scaledBpm = scaledBpm,
                    workDurationMs = workDurationMs,
                    dominantHand = dominantHand,
                    random = random
                )

                val initialLead = resolveInitialStanceBanner(exercise, dominantHand)
                val midSetLead = if (exercise.archetype == DrillArchetype.UNILATERAL_POWER &&
                    exercise.isBilateralSwitchMidSet
                ) {
                    dominantHand.leadFootInstruction(isFirstHalfOfSet = false)
                } else {
                    null
                }

                segments.add(
                    WorkoutSegment(
                        segmentIndex = segIdx++,
                        type = SegmentType.WORK_SET,
                        exercise = exercise,
                        setNumberWithinExercise = setNum,
                        totalSetsForExercise = item.sets,
                        overallSetIndex = workSetCounter,
                        totalWorkSetsInWorkout = totalWorkSets,
                        durationMs = workDurationMs,
                        targetBpm = scaledBpm,
                        initialLeadFootBanner = initialLead,
                        midSetLeadFootBanner = midSetLead,
                        nextExercisePreviewName = null,
                        scheduledMicroCues = microCues
                    )
                )

                // Add Rest Recovery segment if there is another work set coming
                val isVeryLastSet = (itemIndex == items.lastIndex && setNum == item.sets)
                if (!isVeryLastSet) {
                    val nextExercise = if (setNum < item.sets) {
                        exercise
                    } else {
                        items[itemIndex + 1].exercise
                    }
                    segments.add(
                        WorkoutSegment(
                            segmentIndex = segIdx++,
                            type = SegmentType.REST_RECOVERY,
                            exercise = nextExercise,
                            setNumberWithinExercise = if (setNum < item.sets) setNum + 1 else 1,
                            totalSetsForExercise = if (setNum < item.sets) item.sets else items[itemIndex + 1].sets,
                            overallSetIndex = workSetCounter,
                            totalWorkSetsInWorkout = totalWorkSets,
                            durationMs = restDurationMs,
                            targetBpm = scaledBpm,
                            initialLeadFootBanner = resolveInitialStanceBanner(nextExercise, dominantHand),
                            midSetLeadFootBanner = null,
                            nextExercisePreviewName = nextExercise.name
                        )
                    )
                }
            }
        }

        return segments
    }

    private fun resolveInitialStanceBanner(
        exercise: Exercise,
        dominantHand: DominantHand
    ): String {
        return when (exercise.archetype) {
            DrillArchetype.UNILATERAL_POWER -> dominantHand.leadFootInstruction(isFirstHalfOfSet = true)
            DrillArchetype.BASE_PLUS_REACTIVE_BURST -> "Wide Base · ${dominantHand.racketSide} Slightly Ahead"
            DrillArchetype.COMBO_CYCLE -> "Elastic Base · ${exercise.comboBaseBeats} Bounces + ${exercise.comboExplodeCue}"
            DrillArchetype.RHYTHMIC_CADENCE -> "Symmetric Balls-of-Feet Rebound (${exercise.targetBpm} BPM)"
        }
    }

    private fun generateMicroCuesForWorkSet(
        item: WorkoutCircuitItem,
        scaledBpm: Int,
        workDurationMs: Long,
        dominantHand: DominantHand,
        random: Random
    ): List<MicroPhaseCue> {
        val exercise = item.exercise
        val cues = mutableListOf<MicroPhaseCue>()

        when (exercise.archetype) {
            DrillArchetype.RHYTHMIC_CADENCE -> {
                cues.add(
                    MicroPhaseCue(
                        timestampOffsetMs = 0L,
                        displayBanner = "${scaledBpm} BPM CADENCE LOCK",
                        spokenCallout = "Go!",
                        urgency = MicroCueUrgency.CADENCE_BEAT,
                        poseOverride = exercise.baseStancePose
                    )
                )
                if (workDurationMs >= 14_000L) {
                    cues.add(
                        MicroPhaseCue(
                            timestampOffsetMs = workDurationMs / 2L,
                            displayBanner = "HOLD TEMPO · STIFF ANKLES!",
                            spokenCallout = "Stiff ankles!",
                            urgency = MicroCueUrgency.COACHING_REMINDER,
                            poseOverride = exercise.baseStancePose
                        )
                    )
                }
            }

            DrillArchetype.COMBO_CYCLE -> {
                val baseBeats = exercise.comboBaseBeats.coerceIn(2, 8)
                val totalCycleBeats = baseBeats + 1
                val beatIntervalMs = (60_000L / scaledBpm.coerceIn(60, 260).toLong()).coerceAtLeast(220L)
                var cursorMs = 0L
                var stepInCycle = 1

                while (cursorMs < workDurationMs - 250L) {
                    val isExplodeBeat = (stepInCycle == totalCycleBeats)
                    if (isExplodeBeat) {
                        cues.add(
                            MicroPhaseCue(
                                timestampOffsetMs = cursorMs,
                                displayBanner = exercise.comboExplodeCue,
                                spokenCallout = exercise.comboExplodeCue.removeSuffix("!"),
                                urgency = MicroCueUrgency.EXPLOSIVE_BURST,
                                beatInCycle = stepInCycle,
                                totalBeatsInCycle = totalCycleBeats,
                                poseOverride = exercise.peakActionPose
                            )
                        )
                        cursorMs += (beatIntervalMs * 2L)
                        stepInCycle = 1
                    } else {
                        cues.add(
                            MicroPhaseCue(
                                timestampOffsetMs = cursorMs,
                                displayBanner = "BOUNCE $stepInCycle / $baseBeats",
                                spokenCallout = null,
                                urgency = MicroCueUrgency.COMBO_COUNT,
                                beatInCycle = stepInCycle,
                                totalBeatsInCycle = totalCycleBeats,
                                poseOverride = exercise.baseStancePose
                            )
                        )
                        cursorMs += beatIntervalMs
                        stepInCycle++
                    }
                }
            }

            DrillArchetype.BASE_PLUS_REACTIVE_BURST -> {
                cues.add(
                    MicroPhaseCue(
                        timestampOffsetMs = 0L,
                        displayBanner = "FAST FEET TAPPING (${scaledBpm} BPM)",
                        spokenCallout = "Fast feet!",
                        urgency = MicroCueUrgency.CADENCE_BEAT,
                        poseOverride = StancePoseType.WIDE_FAST_TAPPING
                    )
                )
                val minMs = (item.burstMinSec * 1000f).toLong().coerceAtLeast(1800L)
                val maxMs = (item.burstMaxSec * 1000f).toLong().coerceAtLeast(minMs + 400L)
                val pool = exercise.reactiveBurstCues.ifEmpty {
                    listOf("SPLIT STEP!", "RACKET LUNGE!", "SCISSOR SWITCH!", "HIGH JUMP!", "SPRAWL!")
                }

                var cursorMs = random.nextLong(minMs, maxMs + 1)
                while (cursorMs < workDurationMs - 1200L) {
                    val trigger = pool[random.nextInt(pool.size)]
                    val burstPose = when {
                        trigger.contains("LUNGE", ignoreCase = true) -> StancePoseType.DEEP_RACKET_LUNGE
                        trigger.contains("SCISSOR", ignoreCase = true) -> StancePoseType.STAGGERED_SCISSOR_STANCE
                        trigger.contains("JUMP", ignoreCase = true) -> StancePoseType.HIGH_TUCK_EXPLOSION
                        trigger.contains("SPRAWL", ignoreCase = true) -> StancePoseType.WIDE_FAST_TAPPING
                        else -> StancePoseType.STAGGERED_SCISSOR_STANCE
                    }
                    cues.add(
                        MicroPhaseCue(
                            timestampOffsetMs = cursorMs,
                            displayBanner = trigger,
                            spokenCallout = trigger.removeSuffix("!"),
                            urgency = MicroCueUrgency.EXPLOSIVE_BURST,
                            poseOverride = burstPose
                        )
                    )
                    if (cursorMs + 1000L < workDurationMs - 400L) {
                        cues.add(
                            MicroPhaseCue(
                                timestampOffsetMs = cursorMs + 1000L,
                                displayBanner = "FAST TAPPING!",
                                spokenCallout = null,
                                urgency = MicroCueUrgency.CADENCE_BEAT,
                                poseOverride = StancePoseType.WIDE_FAST_TAPPING
                            )
                        )
                    }
                    cursorMs += random.nextLong(minMs, maxMs + 1)
                }
            }

            DrillArchetype.UNILATERAL_POWER -> {
                val firstHalfLead = dominantHand.leadFootInstruction(isFirstHalfOfSet = true)
                val shortFirstLead = if (dominantHand == DominantHand.RIGHT) {
                    "Right leg lead!"
                } else {
                    "Left leg lead!"
                }
                cues.add(
                    MicroPhaseCue(
                        timestampOffsetMs = 0L,
                        displayBanner = firstHalfLead.uppercase(),
                        spokenCallout = shortFirstLead,
                        urgency = MicroCueUrgency.CADENCE_BEAT,
                        poseOverride = StancePoseType.STAGGERED_SCISSOR_STANCE
                    )
                )
                if (exercise.isBilateralSwitchMidSet && workDurationMs >= 10_000L) {
                    val secondHalfLead = dominantHand.leadFootInstruction(isFirstHalfOfSet = false)
                    val shortSecondLead = if (dominantHand == DominantHand.RIGHT) {
                        "Switch sides! Left leg lead!"
                    } else {
                        "Switch sides! Right leg lead!"
                    }
                    cues.add(
                        MicroPhaseCue(
                            timestampOffsetMs = workDurationMs / 2L,
                            displayBanner = "SWITCH SIDES! · ${secondHalfLead.uppercase()}",
                            spokenCallout = shortSecondLead,
                            urgency = MicroCueUrgency.UNILATERAL_SWITCH,
                            poseOverride = StancePoseType.STAGGERED_SCISSOR_STANCE
                        )
                    )
                }
            }
        }

        return cues.sortedBy { it.timestampOffsetMs }
    }
}
