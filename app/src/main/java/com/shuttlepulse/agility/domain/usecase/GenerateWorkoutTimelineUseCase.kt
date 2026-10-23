package com.shuttlepulse.agility.domain.usecase

import com.shuttlepulse.agility.domain.engine.WorkoutTimelineGenerator
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.model.WorkoutSegment
import javax.inject.Inject
import kotlin.random.Random

class GenerateWorkoutTimelineUseCase @Inject constructor(
    private val timelineGenerator: WorkoutTimelineGenerator = WorkoutTimelineGenerator()
) {
    operator fun invoke(
        circuit: WorkoutCircuit,
        dominantHand: DominantHand,
        bpmScale: Float = 1.0f,
        bpmOffset: Int = 0,
        random: Random = Random.Default
    ): List<WorkoutSegment> {
        return timelineGenerator.generateTimeline(
            circuit = circuit,
            dominantHand = dominantHand,
            bpmScale = bpmScale,
            bpmOffset = bpmOffset,
            random = random
        )
    }
}
