package com.shuttlepulse.agility.presentation.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shuttlepulse.agility.data.local.seed.AuthenticBadmintonDrillsSeed
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.ExerciseCategory
import com.shuttlepulse.agility.domain.model.StancePoseType
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.model.WorkoutCircuitItem
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CircuitBuilderUiState(
    val circuitTitle: String = "Custom Pro Footwork Circuit",
    val focusTag: String = "SSC + Reactive Burst",
    val availableExercises: List<Exercise> = AuthenticBadmintonDrillsSeed.exercises,
    val selectedItems: List<WorkoutCircuitItem> = listOf(
        WorkoutCircuitItem(
            exercise = AuthenticBadmintonDrillsSeed.exercises[0],
            sets = 3,
            workSec = 30,
            restSec = 20
        ),
        WorkoutCircuitItem(
            exercise = AuthenticBadmintonDrillsSeed.exercises[5],
            sets = 3,
            workSec = 35,
            restSec = 25
        ),
        WorkoutCircuitItem(
            exercise = AuthenticBadmintonDrillsSeed.exercises[6],
            sets = 3,
            workSec = 40,
            restSec = 25
        )
    ),
    val statusMessage: String? = null
) {
    val totalSets: Int
        get() = selectedItems.sumOf { it.sets }

    val totalDurationSeconds: Int
        get() = selectedItems.sumOf { it.sets * (it.workSec + it.restSec) }

    val totalDurationMinutes: Int
        get() = (totalDurationSeconds + 30) / 60

    val formattedTotalDuration: String
        get() {
            val mins = totalDurationSeconds / 60
            val secs = totalDurationSeconds % 60
            return String.format(java.util.Locale.US, "%dm %02ds", mins, secs)
        }
}

class BuilderViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircuitBuilderUiState())
    val uiState: StateFlow<CircuitBuilderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workoutRepository.observeAllExercises().collect { list ->
                if (list.isNotEmpty()) {
                    _uiState.update { it.copy(availableExercises = list) }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(circuitTitle = title, statusMessage = null) }
    }

    /**
     * Loads a verified 15-Min (900s), 20-Min (1200s), or 25-Min (1500s) preset template
     * into the Custom Circuit Builder so the user can customize and launch it immediately.
     */
    fun applyDurationTemplate(targetMinutes: Int) {
        val targetId = when (targetMinutes) {
            15 -> "circuit_15min_cns_activation"
            20 -> "circuit_20min_chinese_footwork"
            25 -> "circuit_25min_tendon_plyo"
            else -> "circuit_20min_chinese_footwork"
        }
        val template = AuthenticBadmintonDrillsSeed.preBuiltCircuits
            .firstOrNull { it.id == targetId }
            ?: return

        _uiState.update { state ->
            state.copy(
                circuitTitle = "Custom ${targetMinutes}-Min ${template.focusTag} Plan",
                focusTag = template.focusTag,
                selectedItems = template.items,
                statusMessage = "Loaded ${targetMinutes}-Min (${template.formattedTotalDuration}) template"
            )
        }
    }

    fun createAndAddCustomExercise(
        name: String,
        category: ExerciseCategory,
        archetype: DrillArchetype,
        workSec: Int,
        restSec: Int,
        targetBpm: Int,
        coachingTip: String,
        baseStance: com.shuttlepulse.agility.domain.model.StancePoseType
    ) {
        viewModelScope.launch {
            val peakPose = when (baseStance) {
                com.shuttlepulse.agility.domain.model.StancePoseType.DOUBLE_LEG_POGO ->
                    com.shuttlepulse.agility.domain.model.StancePoseType.TUCK_JUMP_PEAK
                com.shuttlepulse.agility.domain.model.StancePoseType.SPLIT_STANCE_RACKET_LEAD ->
                    com.shuttlepulse.agility.domain.model.StancePoseType.DEEP_RACKET_LUNGE
                com.shuttlepulse.agility.domain.model.StancePoseType.WIDE_BASE_FAST_TAP ->
                    com.shuttlepulse.agility.domain.model.StancePoseType.SPLIT_STANCE_RACKET_LEAD
                else -> com.shuttlepulse.agility.domain.model.StancePoseType.TUCK_JUMP_PEAK
            }
            val custom = Exercise(
                id = "custom_drill_${System.currentTimeMillis()}",
                name = name.trim().ifEmpty { "Custom Badminton Drill" },
                category = category,
                archetype = archetype,
                targetBpm = targetBpm.coerceIn(60, 260),
                defaultWorkSec = workSec.coerceIn(10, 180),
                defaultRestSec = restSec.coerceIn(5, 120),
                comboBaseBeats = 4,
                comboExplodeCue = "EXPLODE!",
                burstMinIntervalMs = 2500L,
                burstMaxIntervalMs = 4500L,
                reactiveBurstCues = listOf("SPLIT STEP!", "RACKET LUNGE!", "SCISSOR SWITCH!"),
                isBilateralSwitchMidSet = archetype == DrillArchetype.UNILATERAL_POWER,
                coachingCues = listOf(
                    coachingTip.trim().ifEmpty { "Stay on balls of feet, stiff ankles" },
                    "Maintain continuous cadence and explosive first step"
                ),
                biomechanicsSummary = "Custom drill: ${workSec}s work / ${restSec}s rest @ ${targetBpm} BPM.",
                baseStancePose = baseStance,
                peakActionPose = peakPose
            )
            workoutRepository.saveCustomExercise(custom)
            addExerciseToCircuit(custom)
        }
    }

    fun addExerciseToCircuit(exercise: Exercise) {
        _uiState.update { state ->
            val newItem = WorkoutCircuitItem(
                exercise = exercise,
                sets = 3,
                workSec = exercise.defaultWorkSec,
                restSec = exercise.defaultRestSec,
                targetBpm = exercise.targetBpm
            )
            state.copy(
                selectedItems = state.selectedItems + newItem,
                statusMessage = "Added ${exercise.name}"
            )
        }
    }

    fun removeItemAt(index: Int) {
        _uiState.update { state ->
            if (state.selectedItems.size <= 1) return@update state
            val updated = state.selectedItems.toMutableList().apply { removeAt(index) }
            state.copy(selectedItems = updated)
        }
    }

    fun updateItemSets(index: Int, sets: Int) {
        _uiState.update { state ->
            val list = state.selectedItems.toMutableList()
            val item = list.getOrNull(index) ?: return@update state
            list[index] = item.copy(sets = sets.coerceIn(1, 10))
            state.copy(selectedItems = list)
        }
    }

    fun updateItemWorkSec(index: Int, workSec: Int) {
        _uiState.update { state ->
            val list = state.selectedItems.toMutableList()
            val item = list.getOrNull(index) ?: return@update state
            list[index] = item.copy(workSec = workSec.coerceIn(15, 120))
            state.copy(selectedItems = list)
        }
    }

    fun updateItemRestSec(index: Int, restSec: Int) {
        _uiState.update { state ->
            val list = state.selectedItems.toMutableList()
            val item = list.getOrNull(index) ?: return@update state
            list[index] = item.copy(restSec = restSec.coerceIn(10, 90))
            state.copy(selectedItems = list)
        }
    }

    fun updateItemBpm(index: Int, bpm: Int) {
        _uiState.update { state ->
            val list = state.selectedItems.toMutableList()
            val item = list.getOrNull(index) ?: return@update state
            list[index] = item.copy(targetBpm = bpm.coerceIn(80, 260))
            state.copy(selectedItems = list)
        }
    }

    fun updateItemBurstInterval(index: Int, minSec: Float, maxSec: Float) {
        _uiState.update { state ->
            val list = state.selectedItems.toMutableList()
            val item = list.getOrNull(index) ?: return@update state
            val safeMin = minSec.coerceIn(1.8f, 6.0f)
            val safeMax = maxSec.coerceIn(safeMin + 0.5f, 8.0f)
            list[index] = item.copy(burstMinSec = safeMin, burstMaxSec = safeMax)
            state.copy(selectedItems = list)
        }
    }

    fun saveAndLaunchCircuit(onReadyToLaunch: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val totalMins = state.totalDurationMinutes.coerceAtLeast(5)
            val circuit = WorkoutCircuit(
                id = "custom_${System.currentTimeMillis()}",
                title = state.circuitTitle.trim().ifEmpty { "Custom Badminton Circuit" },
                subtitle = "${state.selectedItems.size} drill blocks · ${state.totalSets} sets · ${state.formattedTotalDuration}",
                durationBadge = "$totalMins MIN · CUSTOM",
                focusTag = state.focusTag,
                items = state.selectedItems,
                isPreBuilt = false
            )
            workoutRepository.saveCustomCircuit(circuit)
            _uiState.update { it.copy(statusMessage = "Saved \"${circuit.title}\"") }
            onReadyToLaunch()
        }
    }

    companion object {
        fun provideFactory(
            workoutRepository: WorkoutRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BuilderViewModel(workoutRepository) as T
            }
        }
    }
}
