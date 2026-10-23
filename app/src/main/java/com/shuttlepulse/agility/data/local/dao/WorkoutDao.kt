package com.shuttlepulse.agility.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shuttlepulse.agility.data.local.entity.WorkoutCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_circuits ORDER BY isPreBuilt DESC, title ASC")
    fun observeAllCircuits(): Flow<List<WorkoutCrossRef>>

    @Query("SELECT COUNT(*) FROM workout_circuits")
    suspend fun getCircuitCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCircuit(circuit: WorkoutCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCircuits(circuits: List<WorkoutCrossRef>)

    @Query("DELETE FROM workout_circuits WHERE id = :id AND isPreBuilt = 0")
    suspend fun deleteCustomCircuit(id: String)
}
