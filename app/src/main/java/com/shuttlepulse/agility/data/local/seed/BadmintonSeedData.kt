package com.shuttlepulse.agility.data.local.seed

import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.WorkoutCircuit

object BadmintonSeedData {
    val exercises: List<Exercise>
        get() = AuthenticBadmintonDrillsSeed.exercises

    val preBuiltCircuits: List<WorkoutCircuit>
        get() = AuthenticBadmintonDrillsSeed.preBuiltCircuits
}
