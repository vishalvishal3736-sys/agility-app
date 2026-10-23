package com.shuttlepulse.agility.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.ExerciseCategory
import com.shuttlepulse.agility.domain.model.StancePoseType
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import com.shuttlepulse.agility.domain.model.WorkoutCircuitItem
import com.shuttlepulse.agility.domain.repository.WorkoutRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val allExercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val selectedCategory: ExerciseCategory? = null,
    val selectedExercise: Exercise? = null,
    val showPeakPosePreview: Boolean = false,
    val dominantHand: DominantHand = DominantHand.RIGHT
)

class LibraryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val selectedCategoryFlow = MutableStateFlow<ExerciseCategory?>(null)
    private val selectedExerciseIdFlow = MutableStateFlow<String?>(null)
    private val showPeakPoseFlow = MutableStateFlow(false)

    val uiState: StateFlow<LibraryUiState> = combine(
        workoutRepository.observeAllExercises(),
        workoutRepository.observeUserPreferences(),
        selectedCategoryFlow,
        selectedExerciseIdFlow,
        showPeakPoseFlow
    ) { exercises, prefs, category, selectedId, showPeak ->
        val filtered = if (category == null) {
            exercises
        } else {
            exercises.filter { it.category == category }
        }
        val selected = filtered.find { it.id == selectedId }
            ?: filtered.firstOrNull()
            ?: exercises.firstOrNull()

        LibraryUiState(
            allExercises = exercises,
            filteredExercises = filtered,
            selectedCategory = category,
            selectedExercise = selected,
            showPeakPosePreview = showPeak,
            dominantHand = prefs.dominantHand
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun selectCategory(category: ExerciseCategory?) {
        selectedCategoryFlow.value = category
    }

    fun selectExercise(exercise: Exercise) {
        selectedExerciseIdFlow.value = exercise.id
        showPeakPoseFlow.value = false
    }

    fun togglePosePreview() {
        showPeakPoseFlow.value = !showPeakPoseFlow.value
    }

    fun createCustomExercise(
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
            selectedExerciseIdFlow.value = custom.id
        }
    }

    fun deleteCustomExercise(exerciseId: String) {
        viewModelScope.launch {
            workoutRepository.deleteCustomExercise(exerciseId)
            if (selectedExerciseIdFlow.value == exerciseId) {
                selectedExerciseIdFlow.value = null
            }
        }
    }

    fun launchSingleDrillQuickSet(exercise: Exercise, onReadyToLaunch: () -> Unit) {
        viewModelScope.launch {
            val singleDrillCircuit = WorkoutCircuit(
                id = "quick_drill_${exercise.id}",
                title = "Quick Focus: ${exercise.name}",
                subtitle = exercise.biomechanicsSummary,
                durationBadge = "3 SETS · ${exercise.targetBpm} BPM",
                focusTag = exercise.archetype.shortBadge,
                items = listOf(
                    WorkoutCircuitItem(
                        exercise = exercise,
                        sets = 3,
                        workSec = exercise.defaultWorkSec,
                        restSec = exercise.defaultRestSec,
                        targetBpm = exercise.targetBpm
                    )
                ),
                isPreBuilt = false
            )
            workoutRepository.setActiveCircuit(singleDrillCircuit)
            onReadyToLaunch()
        }
    }

    companion object {
        fun provideFactory(
            workoutRepository: WorkoutRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LibraryViewModel(workoutRepository) as T
            }
        }
    }
}
