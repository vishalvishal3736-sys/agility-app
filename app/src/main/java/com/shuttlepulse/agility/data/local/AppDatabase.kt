package com.shuttlepulse.agility.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shuttlepulse.agility.data.local.dao.ExerciseDao
import com.shuttlepulse.agility.data.local.dao.SessionLogDao
import com.shuttlepulse.agility.data.local.dao.WorkoutDao
import com.shuttlepulse.agility.data.local.entity.ExerciseEntity
import com.shuttlepulse.agility.data.local.entity.SessionLogEntity
import com.shuttlepulse.agility.data.local.entity.WorkoutCrossRef

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutCrossRef::class,
        SessionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sessionLogDao(): SessionLogDao
}
