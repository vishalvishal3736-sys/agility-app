package com.shuttlepulse.agility.di

import android.content.Context
import androidx.room.Room
import com.shuttlepulse.agility.core.timer.MicroPhaseWorkoutTimer
import com.shuttlepulse.agility.data.local.ShuttlePulseDatabase
import com.shuttlepulse.agility.data.preferences.UserPreferencesRepository
import com.shuttlepulse.agility.data.repository.ShuttlePulseRepositoryImpl
import com.shuttlepulse.agility.domain.engine.WorkoutTimelineGenerator
import com.shuttlepulse.agility.domain.repository.ShuttlePulseRepository
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import com.shuttlepulse.agility.domain.usecase.AuditFeatureHealthUseCase
import com.shuttlepulse.agility.domain.usecase.GenerateWorkoutTimelineUseCase
import com.shuttlepulse.agility.presentation.player.CadenceSoundEngine

object AppModule {

    @Volatile
    private var database: ShuttlePulseDatabase? = null

    @Volatile
    private var userPreferencesRepository: UserPreferencesRepository? = null

    @Volatile
    private var shuttlePulseRepository: ShuttlePulseRepository? = null

    @Volatile
    private var cadenceSoundEngine: CadenceSoundEngine? = null

    fun provideDatabase(context: Context): ShuttlePulseDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                ShuttlePulseDatabase::class.java,
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
        return (provideShuttlePulseRepository(context) as WorkoutRepository)
    }

    fun provideShuttlePulseRepository(context: Context): ShuttlePulseRepository {
        return shuttlePulseRepository ?: synchronized(this) {
            val db = provideDatabase(context)
            shuttlePulseRepository ?: ShuttlePulseRepositoryImpl(
                exerciseDao = db.exerciseDao(),
                workoutDao = db.workoutDao(),
                sessionLogDao = db.sessionLogDao(),
                userPreferencesRepository = provideUserPreferencesRepository(context)
            ).also { shuttlePulseRepository = it }
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

    fun provideWorkoutTimelineGenerator(): WorkoutTimelineGenerator = WorkoutTimelineGenerator()

    fun provideGenerateWorkoutTimelineUseCase(): GenerateWorkoutTimelineUseCase =
        GenerateWorkoutTimelineUseCase(provideWorkoutTimelineGenerator())

    fun provideAuditFeatureHealthUseCase(): AuditFeatureHealthUseCase =
        AuditFeatureHealthUseCase(provideGenerateWorkoutTimelineUseCase())
}
