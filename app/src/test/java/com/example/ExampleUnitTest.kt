package com.example

import com.shuttlepulse.agility.core.timer.MicroPhaseWorkoutTimer
import com.shuttlepulse.agility.data.local.seed.AuthenticBadmintonDrillsSeed
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.MicroCueUrgency
import com.shuttlepulse.agility.domain.model.SegmentType
import com.shuttlepulse.agility.domain.usecase.AuditFeatureHealthUseCase
import com.shuttlepulse.agility.domain.usecase.GenerateWorkoutTimelineUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val generateTimeline = GenerateWorkoutTimelineUseCase()
    private val auditHealth = AuditFeatureHealthUseCase(generateTimeline)
    private val timer = MicroPhaseWorkoutTimer()

    @Test
    fun seedContainsSixteenExercisesAndThreePrograms() {
        assertEquals(16, AuthenticBadmintonDrillsSeed.exercises.size)
        assertEquals(3, AuthenticBadmintonDrillsSeed.preBuiltCircuits.size)
        val archetypes = AuthenticBadmintonDrillsSeed.exercises.map { it.archetype }.toSet()
        assertEquals(4, archetypes.size)
    }

    @Test
    fun bpmIntervalCalculation_matches150And240Bpm() {
        assertEquals(400L, timer.beatIntervalMsForBpm(150))
        assertEquals(250L, timer.beatIntervalMsForBpm(240))
    }

    @Test
    fun unilateralDrill_switchesSidesAt50PercentAndHonorsDominantHand() {
        val circuit = AuthenticBadmintonDrillsSeed.preBuiltCircuits.first()
        val rightSegs = generateTimeline(circuit, DominantHand.RIGHT)
        val leftSegs = generateTimeline(circuit, DominantHand.LEFT)

        val unilateralWorkRight = rightSegs.first {
            it.type == SegmentType.WORK_SET &&
                it.exercise.archetype == DrillArchetype.UNILATERAL_POWER
        }
        val unilateralWorkLeft = leftSegs.first {
            it.type == SegmentType.WORK_SET &&
                it.exercise.archetype == DrillArchetype.UNILATERAL_POWER
        }

        assertTrue(unilateralWorkRight.initialLeadFootBanner.contains("Right (Racket)"))
        assertTrue(unilateralWorkLeft.initialLeadFootBanner.contains("Left (Racket)"))

        val switchCue = unilateralWorkRight.scheduledMicroCues.first {
            it.urgency == MicroCueUrgency.UNILATERAL_SWITCH
        }
        assertEquals(unilateralWorkRight.durationMs / 2L, switchCue.timestampOffsetMs)
    }

    @Test
    fun preBuiltWorkoutPlans_matchExact15_20_25MinuteDurationsAndGenerateValidTimelines() {
        val circuits = AuthenticBadmintonDrillsSeed.preBuiltCircuits
        val plan15 = circuits.first { it.id == "circuit_15min_cns_activation" }
        val plan20 = circuits.first { it.id == "circuit_20min_chinese_footwork" }
        val plan25 = circuits.first { it.id == "circuit_25min_tendon_plyo" }

        assertEquals(900, plan15.totalDurationSeconds)
        assertEquals(15, plan15.totalDurationMinutes)
        assertEquals("15m 00s", plan15.formattedTotalDuration)
        assertEquals(18, plan15.totalSetsCount)

        assertEquals(1200, plan20.totalDurationSeconds)
        assertEquals(20, plan20.totalDurationMinutes)
        assertEquals("20m 00s", plan20.formattedTotalDuration)
        assertEquals(22, plan20.totalSetsCount)

        assertEquals(1500, plan25.totalDurationSeconds)
        assertEquals(25, plan25.totalDurationMinutes)
        assertEquals("25m 00s", plan25.formattedTotalDuration)
        assertEquals(26, plan25.totalSetsCount)

        // Ensure all 3 plans generate valid work/rest/prep timelines
        listOf(plan15, plan20, plan25).forEach { plan ->
            val timeline = generateTimeline(plan, DominantHand.RIGHT)
            val workSets = timeline.count { it.type == SegmentType.WORK_SET }
            assertEquals(plan.totalSetsCount, workSets)
        }
    }

    @Test
    fun comboCycleCues_arePhaseLockedToExactBpmBeatIntervals() {
        val circuit = AuthenticBadmintonDrillsSeed.preBuiltCircuits.first()
        val timeline = generateTimeline(circuit, DominantHand.RIGHT)
        val comboWorkSeg = timeline.first {
            it.type == SegmentType.WORK_SET &&
                it.exercise.archetype == DrillArchetype.COMBO_CYCLE
        }
        val beatIntervalMs = timer.beatIntervalMsForBpm(comboWorkSeg.targetBpm)
        assertTrue(comboWorkSeg.scheduledMicroCues.isNotEmpty())
        comboWorkSeg.scheduledMicroCues.forEach { cue ->
            assertEquals(
                "Combo cue at ${cue.timestampOffsetMs}ms must align with beatIntervalMs=${beatIntervalMs}ms",
                0L,
                cue.timestampOffsetMs % beatIntervalMs
            )
        }
    }

    @Test
    fun customExercise_isCustomFlagAndCustomDurationsWork() {
        val custom = com.shuttlepulse.agility.domain.model.Exercise(
            id = "custom_test_12345",
            name = "Shadow Net Kill & Recover",
            category = com.shuttlepulse.agility.domain.model.ExerciseCategory.FAST_FEET_CHINESE_TAPPING,
            archetype = DrillArchetype.BASE_PLUS_REACTIVE_BURST,
            targetBpm = 200,
            defaultWorkSec = 45,
            defaultRestSec = 20,
            reactiveBurstCues = listOf("NET KILL!", "RECOVER!"),
            coachingCues = listOf("Stiff ankles, quick racket recovery")
        )

        assertTrue(custom.isCustom)
        assertEquals(45, custom.defaultWorkSec)
        assertEquals(20, custom.defaultRestSec)
        assertEquals(200, custom.targetBpm)

        // Seed exercise is not custom
        val seedEx = AuthenticBadmintonDrillsSeed.exercises.first()
        org.junit.Assert.assertFalse(seedEx.isCustom)

        // Generate timeline with custom exercise in a circuit
        val customCircuit = com.shuttlepulse.agility.domain.model.WorkoutCircuit(
            id = "circuit_custom_test",
            title = "My Custom Agility Test",
            subtitle = "Custom drill test",
            durationBadge = "3m",
            focusTag = "Custom",
            items = listOf(
                com.shuttlepulse.agility.domain.model.WorkoutCircuitItem(
                    exercise = custom,
                    sets = 2,
                    workSec = 45,
                    restSec = 20
                )
            ),
            isPreBuilt = false
        )

        val timeline = generateTimeline(customCircuit, DominantHand.RIGHT)
        val workSets = timeline.filter { it.type == SegmentType.WORK_SET }
        assertEquals(2, workSets.size)
        assertEquals(45_000L, workSets.first().durationMs)
    }

    @Test
    fun auditFeatureHealth_passesAllChecks() {
        val checks = auditHealth(
            exercises = AuthenticBadmintonDrillsSeed.exercises,
            circuits = AuthenticBadmintonDrillsSeed.preBuiltCircuits,
            soundPoolReady = true,
            ttsReady = true,
            audioDuckingActive = true,
            timerJitterMs = 2L
        )
        assertTrue(checks.all { it.passed })
    }
}

