package com.shuttlepulse.agility.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.ExerciseCategory
import com.shuttlepulse.agility.domain.model.StancePoseType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryName: String,
    val archetypeName: String,
    val targetBpm: Int,
    val defaultWorkSec: Int,
    val defaultRestSec: Int,
    val comboBaseBeats: Int,
    val comboExplodeCue: String,
    val burstMinIntervalMs: Long,
    val burstMaxIntervalMs: Long,
    val reactiveBurstCuesJson: String,
    val isBilateralSwitchMidSet: Boolean,
    val coachingCuesJson: String,
    val biomechanicsSummary: String,
    val baseStancePoseName: String,
    val peakActionPoseName: String
) {
    fun toDomain(json: Json = Json { ignoreUnknownKeys = true }): Exercise {
        val burstCues = runCatching {
            json.decodeFromString<List<String>>(reactiveBurstCuesJson)
        }.getOrDefault(emptyList())

        val cues = runCatching {
            json.decodeFromString<List<String>>(coachingCuesJson)
        }.getOrDefault(emptyList())

        return Exercise(
            id = id,
            name = name,
            category = runCatching { ExerciseCategory.valueOf(categoryName) }
                .getOrDefault(ExerciseCategory.ANKLE_STIFFNESS_SSC),
            archetype = runCatching { DrillArchetype.valueOf(archetypeName) }
                .getOrDefault(DrillArchetype.RHYTHMIC_CADENCE),
            targetBpm = targetBpm,
            defaultWorkSec = defaultWorkSec,
            defaultRestSec = defaultRestSec,
            comboBaseBeats = comboBaseBeats,
            comboExplodeCue = comboExplodeCue,
            burstMinIntervalMs = burstMinIntervalMs,
            burstMaxIntervalMs = burstMaxIntervalMs,
            reactiveBurstCues = burstCues,
            isBilateralSwitchMidSet = isBilateralSwitchMidSet,
            coachingCues = cues,
            biomechanicsSummary = biomechanicsSummary,
            baseStancePose = runCatching { StancePoseType.valueOf(baseStancePoseName) }
                .getOrDefault(StancePoseType.DOUBLE_LEG_POGO),
            peakActionPose = runCatching { StancePoseType.valueOf(peakActionPoseName) }
                .getOrDefault(StancePoseType.TUCK_JUMP_PEAK)
        )
    }

    companion object {
        fun fromDomain(exercise: Exercise, json: Json = Json): ExerciseEntity {
            return ExerciseEntity(
                id = exercise.id,
                name = exercise.name,
                categoryName = exercise.category.name,
                archetypeName = exercise.archetype.name,
                targetBpm = exercise.targetBpm,
                defaultWorkSec = exercise.defaultWorkSec,
                defaultRestSec = exercise.defaultRestSec,
                comboBaseBeats = exercise.comboBaseBeats,
                comboExplodeCue = exercise.comboExplodeCue,
                burstMinIntervalMs = exercise.burstMinIntervalMs,
                burstMaxIntervalMs = exercise.burstMaxIntervalMs,
                reactiveBurstCuesJson = json.encodeToString(exercise.reactiveBurstCues),
                isBilateralSwitchMidSet = exercise.isBilateralSwitchMidSet,
                coachingCuesJson = json.encodeToString(exercise.coachingCues),
                biomechanicsSummary = exercise.biomechanicsSummary,
                baseStancePoseName = exercise.baseStancePose.name,
                peakActionPoseName = exercise.peakActionPose.name
            )
        }
    }
}
