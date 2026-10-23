package com.shuttlepulse.agility.data.repository

import com.shuttlepulse.agility.data.local.dao.ExerciseDao
import com.shuttlepulse.agility.data.local.dao.SessionLogDao
import com.shuttlepulse.agility.data.local.dao.WorkoutDao
import com.shuttlepulse.agility.domain.repository.ShuttlePulseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ShuttlePulseRepositoryImpl @Inject constructor(
    exerciseDao: ExerciseDao,
    workoutDao: WorkoutDao,
    sessionLogDao: SessionLogDao,
    userPreferencesRepository: UserPreferencesRepository
) : WorkoutRepositoryImpl(
    exerciseDao = exerciseDao,
    workoutDao = workoutDao,
    sessionLogDao = sessionLogDao,
    userPreferencesRepository = userPreferencesRepository
), ShuttlePulseRepository
