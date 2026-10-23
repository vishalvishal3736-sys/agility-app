package com.shuttlepulse.agility.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DrillArchetype(
    val title: String,
    val shortBadge: String,
    val technicalSummary: String
) {
    RHYTHMIC_CADENCE(
        title = "Rhythmic Cadence",
        shortBadge = "BPM METRONOME",
        technicalSummary = "Continuous ground-contact tempo (BPM) to maintain Achilles tendon stiffness and elastic recoil."
    ),
    COMBO_CYCLE(
        title = "Combo Cycle",
        shortBadge = "N-BASE + EXPLODE",
        technicalSummary = "Deterministic micro-pattern: N rhythmic base bounces followed by 1 maximal explosive action on the N+1 beat."
    ),
    BASE_PLUS_REACTIVE_BURST(
        title = "Base + Reactive Burst",
        shortBadge = "REACTIVE BURST",
        technicalSummary = "High-frequency wide-stance fast toe tapping interrupted every X–Y seconds by a random explosive badminton trigger."
    ),
    UNILATERAL_POWER(
        title = "Unilateral Power",
        shortBadge = "UNILATERAL LEAD",
        technicalSummary = "Explicitly cues Racket Foot vs. Non-Racket Foot forward based on Dominant Hand, with automatic mid-set side switch."
    )
}

@Serializable
enum class ExerciseCategory(
    val title: String,
    val shortLabel: String
) {
    ANKLE_STIFFNESS_SSC(
        title = "Ankle Stiffness & Plyo Bounce (SSC)",
        shortLabel = "SSC & Pogo"
    ),
    FAST_FEET_CHINESE_TAPPING(
        title = "Fast Feet & Chinese Footwork Tapping",
        shortLabel = "Chinese Fast-Feet"
    ),
    EXPLOSIVE_LUNGE_REAR_POWER(
        title = "Badminton Explosive Lunge & Rear-Court Power",
        shortLabel = "Lunge & Scissor"
    )
}

@Serializable
enum class DominantHand(
    val label: String,
    val racketSide: String,
    val nonRacketSide: String
) {
    RIGHT(
        label = "Right-Handed",
        racketSide = "Right (Racket) Foot",
        nonRacketSide = "Left (Support) Foot"
    ),
    LEFT(
        label = "Left-Handed",
        racketSide = "Left (Racket) Foot",
        nonRacketSide = "Right (Support) Foot"
    );

    fun leadFootInstruction(isFirstHalfOfSet: Boolean): String {
        return if (isFirstHalfOfSet) {
            "$racketSide Forward"
        } else {
            "$nonRacketSide Forward (Switched)"
        }
    }
}

@Serializable
enum class StancePoseType(val label: String) {
    DOUBLE_LEG_POGO("Vertical Stiff-Ankle Pogo"),
    SPLIT_STANCE_RACKET_LEAD("Split-Stance Racket Foot Lead"),
    SPLIT_STANCE_NON_RACKET_LEAD("Split-Stance Support Foot Lead"),
    WIDE_BASE_FAST_TAP("Wide Badminton Base Fast Tapping"),
    DEEP_RACKET_LUNGE("Extended Deep Forehand Lunge"),
    SCISSOR_KICK_ROTATION("Mid-Air Rear Scissor Hip Switch"),
    TUCK_JUMP_PEAK("Maximal Tuck Jump Peak"),
    LATERAL_SKATER_STICK("Lateral China/Skater Stick")
}
