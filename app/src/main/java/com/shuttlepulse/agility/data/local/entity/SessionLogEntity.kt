package com.shuttlepulse.agility.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.SessionLog

@Entity(tableName = "session_logs")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val workoutTitle: String,
    val timestampEpochMs: Long,
    val completedSets: Int,
    val totalWorkSec: Int,
    val totalRestSec: Int,
    val estimatedGroundContacts: Int,
    val explosiveBurstsExecuted: Int,
    val dominantHandName: String
) {
    fun toDomain(): SessionLog {
        return SessionLog(
            id = id,
            workoutTitle = workoutTitle,
            timestampEpochMs = timestampEpochMs,
            completedSets = completedSets,
            totalWorkSec = totalWorkSec,
            totalRestSec = totalRestSec,
            estimatedGroundContacts = estimatedGroundContacts,
            explosiveBurstsExecuted = explosiveBurstsExecuted,
            dominantHandUsed = runCatching { DominantHand.valueOf(dominantHandName) }
                .getOrDefault(DominantHand.RIGHT)
        )
    }

    companion object {
        fun fromDomain(log: SessionLog): SessionLogEntity {
            return SessionLogEntity(
                id = log.id,
                workoutTitle = log.workoutTitle,
                timestampEpochMs = log.timestampEpochMs,
                completedSets = log.completedSets,
                totalWorkSec = log.totalWorkSec,
                totalRestSec = log.totalRestSec,
                estimatedGroundContacts = log.estimatedGroundContacts,
                explosiveBurstsExecuted = log.explosiveBurstsExecuted,
                dominantHandName = log.dominantHandUsed.name
            )
        }
    }
}
