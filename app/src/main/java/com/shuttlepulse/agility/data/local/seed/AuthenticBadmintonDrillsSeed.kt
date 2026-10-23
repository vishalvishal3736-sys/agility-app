package com.shuttlepulse.agility.data.local.seed

import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.ExerciseCategory
import com.shuttlepulse.agility.domain.model.StancePoseType
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.model.WorkoutCircuitItem

object AuthenticBadmintonDrillsSeed {

    /**
     * 16 Authentic Badminton Off-Court Agility, Fast-Feet Tapping, and Plyometric Drills
     * across all 4 Execution Archetypes.
     */
    val exercises: List<Exercise> = listOf(
        // Category 1: Ankle Stiffness & Plyo Bounce (SSC) — 5 Drills
        Exercise(
            id = "ssc_double_leg_pogo",
            name = "Double-Leg Vertical Pogo Jumps",
            category = ExerciseCategory.ANKLE_STIFFNESS_SSC,
            archetype = DrillArchetype.RHYTHMIC_CADENCE,
            targetBpm = 150,
            defaultWorkSec = 30,
            defaultRestSec = 20,
            coachingCues = listOf(
                "Balls of feet only — heels never touch the floor",
                "Stiff ankles, minimal knee bend (<15°)",
                "Dorsiflex toes up in mid-air to pre-tension Achilles"
            ),
            biomechanicsSummary = "Trains Achilles tendon elastic recoil and minimizes ground contact time (<180ms) for explosive split-step push-offs.",
            baseStancePose = StancePoseType.DOUBLE_LEG_POGO,
            peakActionPose = StancePoseType.DOUBLE_LEG_POGO
        ),
        Exercise(
            id = "ssc_split_stance_pogo",
            name = "Split-Stance Pogo Hops",
            category = ExerciseCategory.ANKLE_STIFFNESS_SSC,
            archetype = DrillArchetype.UNILATERAL_POWER,
            targetBpm = 150,
            defaultWorkSec = 30,
            defaultRestSec = 20,
            isBilateralSwitchMidSet = true,
            coachingCues = listOf(
                "Racket Foot Forward for first 50%, then Switch Sides",
                "Balls of feet only — maintain 30cm front-to-back split",
                "Snap stance swap cleanly on the halfway SWITCH SIDES cue"
            ),
            biomechanicsSummary = "Conditions asymmetric ankle stiffness in the exact badminton defensive/offensive split stance.",
            baseStancePose = StancePoseType.SPLIT_STANCE_RACKET_LEAD,
            peakActionPose = StancePoseType.SPLIT_STANCE_NON_RACKET_LEAD
        ),
        Exercise(
            id = "ssc_4_pogo_1_tuck",
            name = "4 Pogo Hops to 1 Tuck Jump",
            category = ExerciseCategory.ANKLE_STIFFNESS_SSC,
            archetype = DrillArchetype.COMBO_CYCLE,
            targetBpm = 140,
            defaultWorkSec = 35,
            defaultRestSec = 25,
            comboBaseBeats = 4,
            comboExplodeCue = "TUCK JUMP!",
            coachingCues = listOf(
                "Count 1 - 2 - 3 - 4 crisp low ankle bounces",
                "On beat 5 (EXPLODE!), drive knees high to chest",
                "Land immediately into stiff-ankle pogo #1 without pausing"
            ),
            biomechanicsSummary = "Trains transitioning from rhythmic split-step elasticity to maximal jump-smash vertical impulse.",
            baseStancePose = StancePoseType.DOUBLE_LEG_POGO,
            peakActionPose = StancePoseType.TUCK_JUMP_PEAK
        ),
        Exercise(
            id = "ssc_single_leg_lateral_line_pogo",
            name = "Single-Leg Lateral Line Pogos",
            category = ExerciseCategory.ANKLE_STIFFNESS_SSC,
            archetype = DrillArchetype.UNILATERAL_POWER,
            targetBpm = 144,
            defaultWorkSec = 30,
            defaultRestSec = 20,
            isBilateralSwitchMidSet = true,
            coachingCues = listOf(
                "Hop laterally across court tramline on one forefoot",
                "Stiff ankle, zero knee valgus collapse",
                "Switch to support leg immediately at the 50% halfway mark"
            ),
            biomechanicsSummary = "Builds lateral peroneal and subtalar ankle stability for rapid directional push-offs.",
            baseStancePose = StancePoseType.SPLIT_STANCE_RACKET_LEAD,
            peakActionPose = StancePoseType.SPLIT_STANCE_NON_RACKET_LEAD
        ),
        Exercise(
            id = "ssc_quadrant_star_hops",
            name = "4-Quadrant Micro Pogo Cross Hops",
            category = ExerciseCategory.ANKLE_STIFFNESS_SSC,
            archetype = DrillArchetype.RHYTHMIC_CADENCE,
            targetBpm = 156,
            defaultWorkSec = 30,
            defaultRestSec = 20,
            coachingCues = listOf(
                "Hop forward-right-back-left in a compact 25cm cross",
                "Balls of feet only — keep torso centered over the cross",
                "Match every ground contact to the 156 BPM metronome tick"
            ),
            biomechanicsSummary = "Multi-directional forefoot stiffness for micro-adjustments around the center base.",
            baseStancePose = StancePoseType.DOUBLE_LEG_POGO,
            peakActionPose = StancePoseType.DOUBLE_LEG_POGO
        ),

        // Category 2: Fast Feet & Chinese Footwork Tapping — 6 Drills
        Exercise(
            id = "ff_wide_stance_tapping",
            name = "Wide-Stance Fast Tapping",
            category = ExerciseCategory.FAST_FEET_CHINESE_TAPPING,
            archetype = DrillArchetype.RHYTHMIC_CADENCE,
            targetBpm = 240,
            defaultWorkSec = 30,
            defaultRestSec = 20,
            coachingCues = listOf(
                "Wide defensive base, hips sunk low, chest proud",
                "240 BPM machine-gun forefoot pitter-patter",
                "Racket arm held forward in net-interception readiness"
            ),
            biomechanicsSummary = "Signature Chinese National Team neuromuscular drill for maximal motor-unit firing frequency in the calves and hip flexors.",
            baseStancePose = StancePoseType.WIDE_BASE_FAST_TAP,
            peakActionPose = StancePoseType.WIDE_BASE_FAST_TAP
        ),
        Exercise(
            id = "ff_tapping_reactive_burst",
            name = "Fast Tapping + Reactive Lunge/Jump Burst",
            category = ExerciseCategory.FAST_FEET_CHINESE_TAPPING,
            archetype = DrillArchetype.BASE_PLUS_REACTIVE_BURST,
            targetBpm = 240,
            defaultWorkSec = 40,
            defaultRestSec = 25,
            burstMinIntervalMs = 2500L,
            burstMaxIntervalMs = 4500L,
            reactiveBurstCues = listOf(
                "SPLIT STEP!",
                "RACKET LUNGE!",
                "SCISSOR SWITCH!",
                "SPRAWL!",
                "HIGH JUMP!"
            ),
            coachingCues = listOf(
                "Hold 240 BPM fast toe tapping continuously",
                "React instantaneously when the random burst cue fires",
                "Snap back into fast tapping within 1000ms of landing"
            ),
            biomechanicsSummary = "Simulates mid-rally unpredictability: high-frequency base readiness punctured by sudden all-out lunges and jumps.",
            baseStancePose = StancePoseType.WIDE_BASE_FAST_TAP,
            peakActionPose = StancePoseType.DEEP_RACKET_LUNGE
        ),
        Exercise(
            id = "ff_staggered_scissor_switches",
            name = "Staggered Scissor Switches",
            category = ExerciseCategory.FAST_FEET_CHINESE_TAPPING,
            archetype = DrillArchetype.RHYTHMIC_CADENCE,
            targetBpm = 180,
            defaultWorkSec = 30,
            defaultRestSec = 20,
            coachingCues = listOf(
                "Rapidly alternate front and back feet on balls of feet",
                "Keep head level constant — do not bob vertically",
                "Drive elbows tightly in opposition to foot switches"
            ),
            biomechanicsSummary = "Accelerates sagittal plane foot replacement speed for rapid net-to-base recovery.",
            baseStancePose = StancePoseType.SPLIT_STANCE_RACKET_LEAD,
            peakActionPose = StancePoseType.SCISSOR_KICK_ROTATION
        ),
        Exercise(
            id = "ff_in_and_out_split_step",
            name = "In-and-Out Split-Step Fast Feet",
            category = ExerciseCategory.FAST_FEET_CHINESE_TAPPING,
            archetype = DrillArchetype.RHYTHMIC_CADENCE,
            targetBpm = 168,
            defaultWorkSec = 30,
            defaultRestSec = 20,
            coachingCues = listOf(
                "Hop narrow-wide-narrow-wide on the balls of both feet",
                "Pre-tension ankles before the wide split-step contact",
                "Push the floor apart laterally on every wide landing"
            ),
            biomechanicsSummary = "Grooves automatic wide-base split-step adductor/abductor pre-activation.",
            baseStancePose = StancePoseType.WIDE_BASE_FAST_TAP,
            peakActionPose = StancePoseType.DOUBLE_LEG_POGO
        ),
        Exercise(
            id = "ff_rotational_180_taps",
            name = "Fast Tapping + 90° Hip Pivot Snap",
            category = ExerciseCategory.FAST_FEET_CHINESE_TAPPING,
            archetype = DrillArchetype.BASE_PLUS_REACTIVE_BURST,
            targetBpm = 220,
            defaultWorkSec = 35,
            defaultRestSec = 25,
            burstMinIntervalMs = 2500L,
            burstMaxIntervalMs = 4200L,
            reactiveBurstCues = listOf(
                "FOREHAND PIVOT!",
                "BACKHAND PIVOT!",
                "SPLIT STEP!",
                "RACKET LUNGE!"
            ),
            coachingCues = listOf(
                "Fast tap facing the net at 220 BPM",
                "Whip hips 90° toward corner on trigger and snap right back",
                "Keep shoulders square while pelvis rotates underneath"
            ),
            biomechanicsSummary = "Dissociates upper-body stability from rapid pelvic rotation for rear-court preparation.",
            baseStancePose = StancePoseType.WIDE_BASE_FAST_TAP,
            peakActionPose = StancePoseType.SCISSOR_KICK_ROTATION
        ),
        Exercise(
            id = "ff_3_split_1_net_tap",
            name = "3 Split Hops to 1 Net Interception Step",
            category = ExerciseCategory.FAST_FEET_CHINESE_TAPPING,
            archetype = DrillArchetype.COMBO_CYCLE,
            targetBpm = 144,
            defaultWorkSec = 35,
            defaultRestSec = 20,
            comboBaseBeats = 3,
            comboExplodeCue = "RACKET LUNGE!",
            coachingCues = listOf(
                "1 - 2 - 3 elastic split-step hops in base",
                "On beat 4 (EXPLODE!), dart Racket Foot forward into net kill lunge",
                "Rebound off front heel back into split hop #1"
            ),
            biomechanicsSummary = "Syncs split-step rhythm with explosive front-court net kill interception.",
            baseStancePose = StancePoseType.WIDE_BASE_FAST_TAP,
            peakActionPose = StancePoseType.DEEP_RACKET_LUNGE
        ),

        // Category 3: Badminton Explosive Lunge & Rear-Court Power — 5 Drills
        Exercise(
            id = "plyo_rear_scissor_jump",
            name = "Rear-Court Scissor Jump Switches",
            category = ExerciseCategory.EXPLOSIVE_LUNGE_REAR_POWER,
            archetype = DrillArchetype.COMBO_CYCLE,
            targetBpm = 126,
            defaultWorkSec = 35,
            defaultRestSec = 25,
            comboBaseBeats = 2,
            comboExplodeCue = "SCISSOR SWITCH!",
            coachingCues = listOf(
                "2 quick prep bounces with Racket Foot behind",
                "On beat 3, jump and rotate hips 180° mid-air",
                "Land with Non-Racket Foot behind and drive forward immediately"
            ),
            biomechanicsSummary = "Develops round-the-head rear-court scissor kick hip torque and forward push-off momentum.",
            baseStancePose = StancePoseType.SPLIT_STANCE_RACKET_LEAD,
            peakActionPose = StancePoseType.SCISSOR_KICK_ROTATION
        ),
        Exercise(
            id = "plyo_china_jump_bounds",
            name = "China Jump Lateral Bounds",
            category = ExerciseCategory.EXPLOSIVE_LUNGE_REAR_POWER,
            archetype = DrillArchetype.UNILATERAL_POWER,
            targetBpm = 108,
            defaultWorkSec = 30,
            defaultRestSec = 25,
            isBilateralSwitchMidSet = true,
            coachingCues = listOf(
                "Take off and land outward onto your Racket Leg",
                "Absorb landing with vertical torso & stable knee alignment",
                "Switch lead leg at the 50% halfway mark for bilateral balance"
            ),
            biomechanicsSummary = "Specific to forehand rear-court interception ('China Jump'), building eccentric quad/glute braking power.",
            baseStancePose = StancePoseType.SPLIT_STANCE_RACKET_LEAD,
            peakActionPose = StancePoseType.LATERAL_SKATER_STICK
        ),
        Exercise(
            id = "plyo_split_to_deep_lunge",
            name = "Split-Step to Deep Lunge Recovery Hop",
            category = ExerciseCategory.EXPLOSIVE_LUNGE_REAR_POWER,
            archetype = DrillArchetype.COMBO_CYCLE,
            targetBpm = 120,
            defaultWorkSec = 35,
            defaultRestSec = 25,
            comboBaseBeats = 3,
            comboExplodeCue = "DEEP LUNGE!",
            coachingCues = listOf(
                "3 rhythmic split-step bounces in base position",
                "Chest up on lunge — heel-first brake, knee tracks over 2nd toe",
                "Push back to base in a single explosive concentric drive"
            ),
            biomechanicsSummary = "Pairs split-step pre-loading with eccentric heel-to-toe net lunge braking and concentric recovery.",
            baseStancePose = StancePoseType.WIDE_BASE_FAST_TAP,
            peakActionPose = StancePoseType.DEEP_RACKET_LUNGE
        ),
        Exercise(
            id = "plyo_lateral_skater_stick",
            name = "Lateral Skater Bounds with 1-Second Stick",
            category = ExerciseCategory.EXPLOSIVE_LUNGE_REAR_POWER,
            archetype = DrillArchetype.UNILATERAL_POWER,
            targetBpm = 96,
            defaultWorkSec = 30,
            defaultRestSec = 25,
            isBilateralSwitchMidSet = true,
            coachingCues = listOf(
                "Bound laterally onto lead leg and stick landing dead-still",
                "Zero knee wobble — lock pelvis level with the court",
                "Switch lead stabilization leg at the 50% halfway mark"
            ),
            biomechanicsSummary = "Bulletproofs ACL/ankle deceleration mechanics for wide smash-defense lunges.",
            baseStancePose = StancePoseType.LATERAL_SKATER_STICK,
            peakActionPose = StancePoseType.LATERAL_SKATER_STICK
        ),
        Exercise(
            id = "plyo_split_sprawl_jump_smash",
            name = "Defense Sprawl to Vertical Smash Jump",
            category = ExerciseCategory.EXPLOSIVE_LUNGE_REAR_POWER,
            archetype = DrillArchetype.BASE_PLUS_REACTIVE_BURST,
            targetBpm = 200,
            defaultWorkSec = 40,
            defaultRestSec = 30,
            burstMinIntervalMs = 2600L,
            burstMaxIntervalMs = 4400L,
            reactiveBurstCues = listOf(
                "SPRAWL!",
                "HIGH JUMP!",
                "RACKET LUNGE!",
                "SCISSOR SWITCH!"
            ),
            coachingCues = listOf(
                "Fast defensive stance tapping at 200 BPM",
                "Drop hips or explode into vertical smash jump on voice trigger",
                "Recover immediately to active forefoot tapping within 1000ms"
            ),
            biomechanicsSummary = "Trains extreme vertical center-of-mass transitions from diving defense to overhead jump smash.",
            baseStancePose = StancePoseType.WIDE_BASE_FAST_TAP,
            peakActionPose = StancePoseType.TUCK_JUMP_PEAK
        )
    )

    private fun ex(id: String): Exercise = exercises.first { it.id == id }

    val preBuiltCircuits: List<WorkoutCircuit> = listOf(
        // Plan 1: Exact 15 Minutes (900s total = 18 Sets across 5 Drills)
        WorkoutCircuit(
            id = "circuit_15min_cns_activation",
            title = "15-Min Pre-Court CNS & Fast Feet Activation",
            subtitle = "Neural primer before tournament matches: pogo elasticity, split-step timing, and reactive bursts (15m 00s · 18 Sets).",
            durationBadge = "15 MIN · CNS PRIMER",
            focusTag = "Match Warm-Up & Reactivity",
            items = listOf(
                WorkoutCircuitItem(exercise = ex("ssc_double_leg_pogo"), sets = 4, workSec = 30, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ff_in_and_out_split_step"), sets = 4, workSec = 30, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ssc_split_stance_pogo"), sets = 4, workSec = 30, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ff_tapping_reactive_burst"), sets = 3, workSec = 35, restSec = 15),
                WorkoutCircuitItem(exercise = ex("plyo_split_to_deep_lunge"), sets = 3, workSec = 30, restSec = 20)
            ),
            isPreBuilt = true
        ),
        // Plan 2: Exact 20 Minutes (1,200s total = 22 Sets across 6 Drills)
        WorkoutCircuit(
            id = "circuit_20min_chinese_footwork",
            title = "20-Min Chinese Footwork & Tapping Endurance",
            subtitle = "High-density 240 BPM fast-feet blocks combined with reactive lunge triggers, hip pivots, and scissor switches (20m 00s · 22 Sets).",
            durationBadge = "20 MIN · 240 BPM",
            focusTag = "Chinese National Team Protocol",
            items = listOf(
                WorkoutCircuitItem(exercise = ex("ff_wide_stance_tapping"), sets = 4, workSec = 35, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ff_staggered_scissor_switches"), sets = 4, workSec = 35, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ff_tapping_reactive_burst"), sets = 4, workSec = 40, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ff_rotational_180_taps"), sets = 4, workSec = 35, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ff_3_split_1_net_tap"), sets = 3, workSec = 35, restSec = 15),
                WorkoutCircuitItem(exercise = ex("plyo_rear_scissor_jump"), sets = 3, workSec = 35, restSec = 15)
            ),
            isPreBuilt = true
        ),
        // Plan 3: Exact 25 Minutes (1,500s total = 26 Sets across 7 Drills)
        WorkoutCircuit(
            id = "circuit_25min_tendon_plyo",
            title = "25-Min Tendon Stiffness & Plyometric Explosiveness",
            subtitle = "Maximal stretch-shortening cycle (SSC) pogo combos, 4-quadrant hops, China Jumps, and sprawl-to-smash jumps (25m 00s · 26 Sets).",
            durationBadge = "25 MIN · SSC PLYO",
            focusTag = "Achilles Stiffness & Rear Power",
            items = listOf(
                WorkoutCircuitItem(exercise = ex("ssc_double_leg_pogo"), sets = 4, workSec = 35, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ssc_4_pogo_1_tuck"), sets = 4, workSec = 40, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ssc_single_leg_lateral_line_pogo"), sets = 4, workSec = 35, restSec = 20),
                WorkoutCircuitItem(exercise = ex("ssc_quadrant_star_hops"), sets = 4, workSec = 35, restSec = 20),
                WorkoutCircuitItem(exercise = ex("plyo_china_jump_bounds"), sets = 4, workSec = 35, restSec = 25),
                WorkoutCircuitItem(exercise = ex("plyo_rear_scissor_jump"), sets = 3, workSec = 40, restSec = 20),
                WorkoutCircuitItem(exercise = ex("plyo_split_sprawl_jump_smash"), sets = 3, workSec = 40, restSec = 20)
            ),
            isPreBuilt = true
        )
    )
}
