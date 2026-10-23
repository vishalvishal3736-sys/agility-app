package com.shuttlepulse.agility.di

import android.content.Context
import androidx.room.Room
import com.shuttlepulse.agility.core.audio.CadenceSoundEngine
import com.shuttlepulse.agility.core.timer.MicroPhaseWorkoutTimer
import com.shuttlepulse.agility.data.local.AppDatabase
import com.shuttlepulse.agility.data.repository.UserPreferencesRepository
import com.shuttlepulse.agility.data.repository.WorkoutRepositoryImpl
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import com.shuttlepulse.agility.domain.usecase.AuditFeatureHealthUseCase
import com.shuttlepulse.agility.domain.usecase.GenerateWorkoutTimelineUseCase

object AppModule {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var userPreferencesRepository: UserPreferencesRepository? = null

    @Volatile
    private var workoutRepository: WorkoutRepository? = null

    @Volatile
    private var cadenceSoundEngine: CadenceSoundEngine? = null

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "shuttlepulse_conditioning.db"
            ).fallbackToDestructiveMigration(true).build().also { database = it }
        }
    }

    fun provideUserPreferencesRepository(context: Context): UserPreferencesRepository {
        return userPreferencesRepository ?: synchronized(this) {
            userPreferencesRepository ?: UserPreferencesRepository(context.applicationContext).also {
                userPreferencesRepository = it
            }
        }
    }

    fun provideWorkoutRepository(context: Context): WorkoutRepository {
        return workoutRepository ?: synchronized(this) {
            val db = provideDatabase(context)
            workoutRepository ?: WorkoutRepositoryImpl(
                exerciseDao = db.exerciseDao(),
                workoutDao = db.workoutDao(),
                sessionLogDao = db.sessionLogDao(),
                userPreferencesRepository = provideUserPreferencesRepository(context)
            ).also { workoutRepository = it }
        }
    }

    fun provideCadenceSoundEngine(context: Context): CadenceSoundEngine {
        return cadenceSoundEngine ?: synchronized(this) {
            cadenceSoundEngine ?: CadenceSoundEngine(context.applicationContext).also {
                cadenceSoundEngine = it
            }
        }
    }

    fun provideMicroPhaseWorkoutTimer(): MicroPhaseWorkoutTimer = MicroPhaseWorkoutTimer()

    fun provideGenerateWorkoutTimelineUseCase(): GenerateWorkoutTimelineUseCase =
        GenerateWorkoutTimelineUseCase()

    fun provideAuditFeatureHealthUseCase(): AuditFeatureHealthUseCase =
        AuditFeatureHealthUseCase(provideGenerateWorkoutTimelineUseCase())
}
