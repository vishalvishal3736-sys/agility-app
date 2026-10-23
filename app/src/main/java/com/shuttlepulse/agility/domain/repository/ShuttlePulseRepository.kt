package com.shuttlepulse.agility.domain.repository

import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.SessionLog
import com.shuttlepulse.agility.domain.model.UserPreferences
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import kotlinx.coroutines.flow.Flow

interface ShuttlePulseRepository {
    fun observeAllExercises(): Flow<List<Exercise>>
    fun observeAllCircuits(): Flow<List<WorkoutCircuit>>
    fun observeActiveCircuit(): Flow<WorkoutCircuit>
    fun observeSessionLogs(): Flow<List<SessionLog>>
    fun observeUserPreferences(): Flow<UserPreferences>

    suspend fun getExerciseById(id: String): Exercise?
    suspend fun saveCustomExercise(exercise: Exercise)
    suspend fun deleteCustomExercise(id: String)
    suspend fun saveCustomCircuit(circuit: WorkoutCircuit)
    suspend fun deleteCustomCircuit(id: String)
    suspend fun setActiveCircuit(circuit: WorkoutCircuit)
    suspend fun logCompletedSession(sessionLog: SessionLog): Long
    suspend fun setDominantHand(hand: DominantHand)
    suspend fun updateAudioPreferences(
        duckingEnabled: Boolean,
        metronomeEnabled: Boolean,
        voiceEnabled: Boolean,
        bpmScale: Float,
        bpmOffset: Int = 0
    )
    suspend fun ensureSeeded()
}
