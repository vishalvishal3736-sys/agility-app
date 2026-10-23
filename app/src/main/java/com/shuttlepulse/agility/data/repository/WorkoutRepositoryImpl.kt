package com.shuttlepulse.agility.data.repository

import com.shuttlepulse.agility.data.local.dao.ExerciseDao
import com.shuttlepulse.agility.data.local.dao.SessionLogDao
import com.shuttlepulse.agility.data.local.dao.WorkoutDao
import com.shuttlepulse.agility.data.local.entity.ExerciseEntity
import com.shuttlepulse.agility.data.local.entity.SessionLogEntity
import com.shuttlepulse.agility.data.local.entity.WorkoutCrossRef
import com.shuttlepulse.agility.data.local.seed.AuthenticBadmintonDrillsSeed
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.SessionLog
import com.shuttlepulse.agility.domain.model.UserPreferences
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val sessionLogDao: SessionLogDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : WorkoutRepository {

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In-memory hot state flow so selecting 15-min, 20-min, or 25-min plans is instantaneous (0ms race condition)
    private val _activeCircuitState = MutableStateFlow(
        AuthenticBadmintonDrillsSeed.preBuiltCircuits.first()
    )

    init {
        repoScope.launch {
            ensureSeeded()
            userPreferencesRepository.activeCircuitFlow.collect { savedCircuit ->
                // Upgrade any stale pre-built circuit from DataStore to the latest seed definition
                val canonical = AuthenticBadmintonDrillsSeed.preBuiltCircuits.find {
                    it.id == savedCircuit.id
                } ?: savedCircuit
                _activeCircuitState.value = canonical
            }
        }
    }

    override fun observeAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.observeAllExercises()
            .onStart { ensureSeeded() }
            .map { list ->
                if (list.isEmpty()) {
                    AuthenticBadmintonDrillsSeed.exercises
                } else {
                    list.map { it.toDomain() }
                }
            }
    }

    override fun observeAllCircuits(): Flow<List<WorkoutCircuit>> {
        return workoutDao.observeAllCircuits()
            .onStart { ensureSeeded() }
            .map { list ->
                if (list.isEmpty()) {
                    AuthenticBadmintonDrillsSeed.preBuiltCircuits
                } else {
                    list.map { it.toDomain() }
                }
            }
    }

    override fun observeActiveCircuit(): Flow<WorkoutCircuit> {
        return _activeCircuitState.asStateFlow()
    }

    override fun observeSessionLogs(): Flow<List<SessionLog>> {
        return sessionLogDao.observeSessionLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeUserPreferences(): Flow<UserPreferences> {
        return userPreferencesRepository.userPreferencesFlow
    }

    override suspend fun getExerciseById(id: String): Exercise? = withContext(Dispatchers.IO) {
        ensureSeeded()
        exerciseDao.getExerciseById(id)?.toDomain()
            ?: AuthenticBadmintonDrillsSeed.exercises.find { it.id == id }
    }

    override suspend fun saveCustomExercise(exercise: Exercise) = withContext(Dispatchers.IO) {
        exerciseDao.insert(ExerciseEntity.fromDomain(exercise))
    }

    override suspend fun deleteCustomExercise(id: String) = withContext(Dispatchers.IO) {
        exerciseDao.deleteExerciseById(id)
    }

    override suspend fun saveCustomCircuit(circuit: WorkoutCircuit) = withContext(Dispatchers.IO) {
        _activeCircuitState.value = circuit
        workoutDao.insertCircuit(WorkoutCrossRef.fromDomain(circuit))
        userPreferencesRepository.saveActiveCircuit(circuit)
    }

    override suspend fun deleteCustomCircuit(id: String) = withContext(Dispatchers.IO) {
        workoutDao.deleteCustomCircuit(id)
    }

    override suspend fun setActiveCircuit(circuit: WorkoutCircuit) {
        // Update hot in-memory StateFlow immediately so navigating to PlayerScreen sees the new plan right away
        _activeCircuitState.value = circuit
        withContext(Dispatchers.IO) {
            userPreferencesRepository.saveActiveCircuit(circuit)
        }
    }

    override suspend fun logCompletedSession(sessionLog: SessionLog): Long = withContext(Dispatchers.IO) {
        sessionLogDao.insertLog(SessionLogEntity.fromDomain(sessionLog))
    }

    override suspend fun setDominantHand(hand: DominantHand) {
        userPreferencesRepository.setDominantHand(hand)
    }

    override suspend fun updateAudioPreferences(
        duckingEnabled: Boolean,
        metronomeEnabled: Boolean,
        voiceEnabled: Boolean,
        bpmScale: Float
    ) {
        userPreferencesRepository.updateAudioPreferences(
            duckingEnabled = duckingEnabled,
            metronomeEnabled = metronomeEnabled,
            voiceEnabled = voiceEnabled,
            bpmScale = bpmScale
        )
    }

    override suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        // Always upsert all 16 authentic exercises and 3 pre-built (15m, 20m, 25m) circuits
        exerciseDao.insertAll(
            AuthenticBadmintonDrillsSeed.exercises.map { ExerciseEntity.fromDomain(it) }
        )
        workoutDao.insertCircuits(
            AuthenticBadmintonDrillsSeed.preBuiltCircuits.map { WorkoutCrossRef.fromDomain(it) }
        )
        if (sessionLogDao.getLogCount() == 0) {
            val now = System.currentTimeMillis()
            val sampleLog = SessionLog(
                workoutTitle = "15-Min Pre-Court CNS & Fast Feet Activation",
                timestampEpochMs = now - 86_400_000L,
                completedSets = 18,
                totalWorkSec = 555,
                totalRestSec = 345,
                estimatedGroundContacts = 1480,
                explosiveBurstsExecuted = 38,
                dominantHandUsed = DominantHand.RIGHT
            )
            sessionLogDao.insertLog(SessionLogEntity.fromDomain(sampleLog))
        }
    }
}
