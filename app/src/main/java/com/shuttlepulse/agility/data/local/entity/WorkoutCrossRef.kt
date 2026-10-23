package com.shuttlepulse.agility.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.model.WorkoutCircuitItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "workout_circuits")
data class WorkoutCrossRef(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val durationBadge: String,
    val focusTag: String,
    val itemsJson: String,
    val isPreBuilt: Boolean
) {
    fun toDomain(json: Json = Json { ignoreUnknownKeys = true }): WorkoutCircuit {
        val items = runCatching {
            json.decodeFromString<List<WorkoutCircuitItem>>(itemsJson)
        }.getOrDefault(emptyList())
        return WorkoutCircuit(
            id = id,
            title = title,
            subtitle = subtitle,
            durationBadge = durationBadge,
            focusTag = focusTag,
            items = items,
            isPreBuilt = isPreBuilt
        )
    }

    companion object {
        fun fromDomain(circuit: WorkoutCircuit, json: Json = Json): WorkoutCrossRef {
            return WorkoutCrossRef(
                id = circuit.id,
                title = circuit.title,
                subtitle = circuit.subtitle,
                durationBadge = circuit.durationBadge,
                focusTag = circuit.focusTag,
                itemsJson = json.encodeToString(circuit.items),
                isPreBuilt = circuit.isPreBuilt
            )
        }
    }
}
