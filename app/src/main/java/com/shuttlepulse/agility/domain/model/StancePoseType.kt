package com.shuttlepulse.agility.domain.model

import kotlinx.serialization.Serializable

/**
 * 6 Core Badminton Biomechanical Stance Types for Real-Time Canvas Kinematics:
 * - [PARALLEL_POGO_BOUNCE]: Symmetric ankle elasticity, heels elevated, Achilles pre-tensioned.
 * - [WIDE_FAST_TAPPING]: Chinese Team low center-of-gravity wide base, rapid alternating toe contacts.
 * - [STAGGERED_SCISSOR_STANCE]: Asymmetric fore/aft stance preparing for rear-court scissor kick or pivot.
 * - [DEEP_RACKET_LUNGE]: Extended forward brake on lead racket heel/midfoot with erect torso.
 * - [LATERAL_SKATER_BOUND]: China jump lateral interception, single-leg absorption and deceleration.
 * - [HIGH_TUCK_EXPLOSION]: Maximal vertical impulse, knees driven to chest on the final combo beat.
 */
@Serializable
enum class StancePoseType(
    val label: String,
    val description: String
) {
    PARALLEL_POGO_BOUNCE(
        label = "Parallel Pogo Bounce",
        description = "Symmetric stiff-ankle bounce on balls of feet; heels elevated."
    ),
    WIDE_FAST_TAPPING(
        label = "Wide Fast Tapping",
        description = "Wide low center-of-gravity badminton ready stance with rapid alternating toe taps."
    ),
    STAGGERED_SCISSOR_STANCE(
        label = "Staggered Scissor Stance",
        description = "Asymmetric fore-aft split stance priming for rear-court hip rotation."
    ),
    DEEP_RACKET_LUNGE(
        label = "Deep Racket Lunge",
        description = "Extended net lunge on dominant racket foot with upright spine recovery."
    ),
    LATERAL_SKATER_BOUND(
        label = "Lateral Skater Bound",
        description = "Sideways China jump bound; single-leg landing stabilization stick."
    ),
    HIGH_TUCK_EXPLOSION(
        label = "High Tuck Explosion",
        description = "Maximal vertical jump explosion with knees driven toward chest."
    );

    companion object {
        // Compatibility aliases for previous seed references
        val DOUBLE_LEG_POGO = PARALLEL_POGO_BOUNCE
        val SPLIT_STANCE_RACKET_LEAD = STAGGERED_SCISSOR_STANCE
        val SPLIT_STANCE_NON_RACKET_LEAD = STAGGERED_SCISSOR_STANCE
        val WIDE_BASE_FAST_TAP = WIDE_FAST_TAPPING
        val SCISSOR_KICK_ROTATION = STAGGERED_SCISSOR_STANCE
        val TUCK_JUMP_PEAK = HIGH_TUCK_EXPLOSION
        val LATERAL_SKATER_STICK = LATERAL_SKATER_BOUND
    }
}
