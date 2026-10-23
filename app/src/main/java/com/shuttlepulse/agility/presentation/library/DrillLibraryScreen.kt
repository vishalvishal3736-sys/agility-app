package com.shuttlepulse.agility.presentation.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.Exercise
import com.shuttlepulse.agility.domain.model.ExerciseCategory
import com.shuttlepulse.agility.presentation.player.FootStanceCanvas
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shuttlepulse.agility.presentation.common.CreateExerciseDialog
import com.shuttlepulse.agility.presentation.player.FootStanceCanvas
import com.shuttlepulse.agility.presentation.player.StickFigureStanceCanvas

@Composable
fun DrillLibraryScreen(
    viewModel: LibraryViewModel,
    onLaunchQuickDrill: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateExerciseDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name, cat, arc, workS, restS, bpm, tip, stance ->
                showCreateDialog = false
                viewModel.createCustomExercise(
                    name = name,
                    category = cat,
                    archetype = arc,
                    workSec = workS,
                    restSec = restS,
                    targetBpm = bpm,
                    coachingTip = tip,
                    baseStance = stance
                )
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShuttlePulseColors.OledGymBlack)
            .testTag("drill_library_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DRILL ENCYCLOPEDIA",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ShuttlePulseColors.TextCrispWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.allExercises.size} drills · Tap + to create your own custom exercise",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShuttlePulseColors.TextSlateMuted
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("library_create_exercise_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShuttlePulseColors.VoltPlyoLime,
                        contentColor = ShuttlePulseColors.OledGymBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom exercise",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CREATE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("All (${state.allExercises.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                        selectedLabelColor = ShuttlePulseColors.VoltPlyoLime
                    )
                )
                ExerciseCategory.entries.forEach { cat ->
                    val selected = state.selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat.shortLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                            selectedLabelColor = ShuttlePulseColors.VoltPlyoLime
                        )
                    )
                }
            }
        }

        // Selected Exercise Interactive Stance & Form Checklist Spotlight
        state.selectedExercise?.let { selected ->
            item {
                ExerciseBiomechanicsSpotlightCard(
                    exercise = selected,
                    dominantHand = state.dominantHand,
                    showPeakPose = state.showPeakPosePreview,
                    onTogglePose = viewModel::togglePosePreview,
                    onDeleteCustom = if (selected.isCustom) {
                        { viewModel.deleteCustomExercise(selected.id) }
                    } else null,
                    onStartQuickSet = {
                        viewModel.launchSingleDrillQuickSet(selected, onLaunchQuickDrill)
                    }
                )
            }
        }

        items(state.filteredExercises, key = { it.id }) { exercise ->
            val isSelected = exercise.id == state.selectedExercise?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectExercise(exercise) }
                    .testTag("library_drill_card_${exercise.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        ShuttlePulseColors.GymCardElevated
                    } else {
                        ShuttlePulseColors.GymSurfaceDark
                    }
                ),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) {
                        ShuttlePulseColors.VoltPlyoLime
                    } else {
                        ShuttlePulseColors.GymBorderSubtle
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (exercise.isCustom) {
                                Surface(
                                    color = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "CUSTOM DRILL",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ShuttlePulseColors.VoltPlyoLime,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Surface(
                                color = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = exercise.archetype.shortBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ShuttlePulseColors.FastFeetCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${exercise.targetBpm} BPM",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ShuttlePulseColors.VoltPlyoLime,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = ShuttlePulseColors.TextCrispWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = exercise.coachingCues.firstOrNull().orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShuttlePulseColors.TextSlateMuted,
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${exercise.defaultWorkSec}s WORK",
                                style = MaterialTheme.typography.labelLarge,
                                color = ShuttlePulseColors.VoltPlyoLime,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${exercise.defaultRestSec}s REST",
                                style = MaterialTheme.typography.labelMedium,
                                color = ShuttlePulseColors.TextSlateMuted
                            )
                        }

                        if (exercise.isCustom) {
                            IconButton(
                                onClick = { viewModel.deleteCustomExercise(exercise.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete custom drill",
                                    tint = ShuttlePulseColors.ExplosiveBurstCrimson
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseBiomechanicsSpotlightCard(
    exercise: Exercise,
    dominantHand: DominantHand,
    showPeakPose: Boolean,
    onTogglePose: () -> Unit,
    onDeleteCustom: (() -> Unit)?,
    onStartQuickSet: () -> Unit
) {
    val activePose = if (showPeakPose) exercise.peakActionPose else exercise.baseStancePose

    Card(
        colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
        border = BorderStroke(1.5.dp, ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = ShuttlePulseColors.VoltPlyoLime,
                            fontWeight = FontWeight.Bold
                        )
                        if (exercise.isCustom) {
                            Surface(
                                color = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "CUSTOM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShuttlePulseColors.VoltPlyoLime,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${exercise.category.title} · ${exercise.archetype.title} · ${exercise.defaultWorkSec}s work / ${exercise.defaultRestSec}s rest",
                        style = MaterialTheme.typography.labelMedium,
                        color = ShuttlePulseColors.FastFeetCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDeleteCustom != null) {
                        IconButton(onClick = onDeleteCustom) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete custom drill",
                                tint = ShuttlePulseColors.ExplosiveBurstCrimson
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onTogglePose,
                        border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Toggle base vs peak stance",
                            tint = ShuttlePulseColors.FastFeetCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showPeakPose) "PEAK POSE" else "BASE POSE",
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.FastFeetCyan
                        )
                    }
                }
            }

            StickFigureStanceCanvas(
                pose = activePose,
                dominantHand = dominantHand,
                cadencePhase = if (showPeakPose) 0.75f else 0.25f,
                targetBpm = exercise.targetBpm,
                isExplosiveTrigger = showPeakPose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            FootStanceCanvas(
                pose = activePose,
                dominantHand = dominantHand,
                cadencePhase = if (showPeakPose) 0.75f else 0.25f,
                isExplosiveTrigger = showPeakPose
            )

            Text(
                text = exercise.biomechanicsSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = ShuttlePulseColors.TextCrispWhite
            )

            Text(
                text = "MANDATORY FORM CHECKLIST",
                style = MaterialTheme.typography.labelLarge,
                color = ShuttlePulseColors.VoltPlyoLime
            )
            exercise.coachingCues.forEach { cue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ShuttlePulseColors.VoltPlyoLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = cue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShuttlePulseColors.TextCrispWhite
                    )
                }
            }

            Button(
                onClick = onStartQuickSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("launch_single_drill_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShuttlePulseColors.VoltPlyoLime,
                    contentColor = ShuttlePulseColors.OledGymBlack
                )
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LAUNCH 3-SET FOCUS BLOCK (${exercise.targetBpm} BPM)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
