package com.shuttlepulse.agility.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shuttlepulse.agility.data.local.dao.CircuitDao
import com.shuttlepulse.agility.data.local.dao.ExerciseDao
import com.shuttlepulse.agility.data.local.dao.WorkoutDao
import com.shuttlepulse.agility.data.local.dao.WorkoutLogDao
import com.shuttlepulse.agility.data.local.dao.SessionLogDao
import com.shuttlepulse.agility.data.local.entity.CircuitEntity
import com.shuttlepulse.agility.data.local.entity.ExerciseEntity
import com.shuttlepulse.agility.data.local.entity.SessionLogEntity
import com.shuttlepulse.agility.data.local.entity.WorkoutCrossRef
import com.shuttlepulse.agility.data.local.entity.WorkoutLogEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutCrossRef::class,
        SessionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ShuttlePulseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sessionLogDao(): SessionLogDao

    // Blueprint aliases
    fun circuitDao(): CircuitDao = workoutDao()
    fun workoutLogDao(): WorkoutLogDao = sessionLogDao()
}
