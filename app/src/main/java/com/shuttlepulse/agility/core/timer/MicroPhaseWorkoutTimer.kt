package com.shuttlepulse.agility.core.timer

import android.os.SystemClock
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * High-precision monotonic timer anchored on [SystemClock.elapsedRealtime]
 * for zero-drift BPM cadence ticks (`60_000L / targetBpm`), combo cycle counters,
 * 1000ms reactive burst holds, and exact 50% unilateral side switches.
 */
class MicroPhaseWorkoutTimer @Inject constructor() {

    fun nowElapsedRealtimeMs(): Long = SystemClock.elapsedRealtime()

    /**
     * Exact millisecond interval per ground contact for a target BPM:
     * - 150 BPM -> 400ms
     * - 240 BPM -> 250ms
     */
    fun beatIntervalMsForBpm(targetBpm: Int): Long {
        val clamped = targetBpm.coerceIn(40, 320)
        return (60_000L / clamped.toLong()).coerceAtLeast(150L)
    }

    /**
     * Computes the exact beat index and intra-beat normalized phase (0.0f..1.0f)
     * from monotonic elapsed milliseconds.
     */
    fun computeCadenceState(elapsedMs: Long, bpm: Int): Pair<Int, Float> {
        val intervalMs = beatIntervalMsForBpm(bpm)
        val beatCount = (elapsedMs / intervalMs).toInt()
        val phase = ((elapsedMs % intervalMs).toFloat() / intervalMs.toFloat()).coerceIn(0f, 1f)
        return beatCount to phase
    }

    /**
     * Runs a 1-second monotonic `SystemClock.elapsedRealtime()` delta verification
     * (10 x 100ms steps) and returns the average drift per tick in milliseconds.
     */
    suspend fun measureTimerPrecisionJitterMs(): Long {
        val stepMs = 50L
        val iterations = 10
        val start = nowElapsedRealtimeMs()
        var totalDrift = 0L
        var prev = start

        repeat(iterations) {
            delay(stepMs)
            val now = nowElapsedRealtimeMs()
            val delta = now - prev
            prev = now
            totalDrift += abs(delta - stepMs)
        }
        return (totalDrift / iterations).coerceAtLeast(1L)
    }
}
