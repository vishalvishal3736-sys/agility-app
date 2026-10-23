package com.shuttlepulse.agility.presentation.builder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.WorkoutCircuitItem
import com.shuttlepulse.agility.presentation.common.CreateExerciseDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale

@Composable
fun CircuitBuilderScreen(
    viewModel: BuilderViewModel,
    onLaunchBuiltCircuit: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateExerciseDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name, cat, arc, workS, restS, bpm, tip, stance ->
                showCreateDialog = false
                viewModel.createAndAddCustomExercise(
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
            .testTag("circuit_builder_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CUSTOM CIRCUIT BUILDER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ShuttlePulseColors.TextCrispWhite
                )
                Text(
                    text = "Chain authentic badminton drills or start from a 15, 20, or 25-minute template",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShuttlePulseColors.TextSlateMuted
                )

                Surface(
                    color = ShuttlePulseColors.GymCardElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL PLAN: ${state.formattedTotalDuration}",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShuttlePulseColors.VoltPlyoLime
                        )
                        Text(
                            text = "${state.totalSets} SETS • ${state.selectedItems.size} DRILLS",
                            style = MaterialTheme.typography.labelLarge,
                            color = ShuttlePulseColors.FastFeetCyan
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 20, 25).forEach { mins ->
                        val isActiveTemplate = state.totalDurationMinutes == mins
                        AssistChip(
                            onClick = { viewModel.applyDurationTemplate(mins) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("builder_template_${mins}min_chip"),
                            label = {
                                Text(
                                    text = "$mins MIN PLAN",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isActiveTemplate) {
                                    ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f)
                                } else {
                                    ShuttlePulseColors.GymSurfaceDark
                                },
                                labelColor = if (isActiveTemplate) {
                                    ShuttlePulseColors.VoltPlyoLime
                                } else {
                                    ShuttlePulseColors.TextCrispWhite
                                }
                            )
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.circuitTitle,
                onValueChange = viewModel::updateTitle,
                label = { Text("Circuit Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("circuit_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShuttlePulseColors.VoltPlyoLime,
                    unfocusedBorderColor = ShuttlePulseColors.GymBorderSubtle,
                    focusedLabelColor = ShuttlePulseColors.VoltPlyoLime,
                    focusedTextColor = ShuttlePulseColors.TextCrispWhite,
                    unfocusedTextColor = ShuttlePulseColors.TextCrispWhite
                )
            )
        }

        // Quick Add Drill Strip from Seeded and Custom Exercises
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APPEND DRILLS TO CIRCUIT",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan
                    )
                    TextButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("builder_open_create_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = ShuttlePulseColors.VoltPlyoLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ CREATE DRILL",
                            color = ShuttlePulseColors.VoltPlyoLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { showCreateDialog = true },
                        label = { Text("★ + NEW CUSTOM DRILL") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.20f),
                            labelColor = ShuttlePulseColors.VoltPlyoLime
                        ),
                        border = BorderStroke(1.dp, ShuttlePulseColors.VoltPlyoLime)
                    )
                    state.availableExercises.forEach { ex ->
                        AssistChip(
                            onClick = { viewModel.addExerciseToCircuit(ex) },
                            label = { Text("+ ${ex.name}${if (ex.isCustom) " (Custom)" else ""}") },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = if (ex.isCustom) ShuttlePulseColors.FastFeetCyan else ShuttlePulseColors.VoltPlyoLime
                            )
                        )
                    }
                }
            }
        }

        itemsIndexed(state.selectedItems) { idx, item ->
            CircuitBuilderItemCard(
                index = idx,
                item = item,
                onRemove = { viewModel.removeItemAt(idx) },
                onSetsChange = { viewModel.updateItemSets(idx, it) },
                onWorkSecChange = { viewModel.updateItemWorkSec(idx, it) },
                onRestSecChange = { viewModel.updateItemRestSec(idx, it) },
                onBpmChange = { viewModel.updateItemBpm(idx, it) },
                onBurstIntervalChange = { minS, maxS ->
                    viewModel.updateItemBurstInterval(idx, minS, maxS)
                }
            )
        }

        item {
            Button(
                onClick = { viewModel.saveAndLaunchCircuit(onLaunchBuiltCircuit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_and_launch_circuit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShuttlePulseColors.VoltPlyoLime,
                    contentColor = ShuttlePulseColors.OledGymBlack
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE & LAUNCH CUSTOM CIRCUIT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CircuitBuilderItemCard(
    index: Int,
    item: WorkoutCircuitItem,
    onRemove: () -> Unit,
    onSetsChange: (Int) -> Unit,
    onWorkSecChange: (Int) -> Unit,
    onRestSecChange: (Int) -> Unit,
    onBpmChange: (Int) -> Unit,
    onBurstIntervalChange: (Float, Float) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
        border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${index + 1} · ${item.exercise.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ShuttlePulseColors.TextCrispWhite
                    )
                    Surface(
                        color = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.exercise.archetype.shortBadge,
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.FastFeetCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove block",
                        tint = ShuttlePulseColors.ExplosiveBurstCrimson
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactStepper(
                    label = "Sets",
                    valueText = "${item.sets}",
                    onDec = { onSetsChange(item.sets - 1) },
                    onInc = { onSetsChange(item.sets + 1) }
                )
                CompactStepper(
                    label = "Work",
                    valueText = "${item.workSec}s",
                    onDec = { onWorkSecChange(item.workSec - 5) },
                    onInc = { onWorkSecChange(item.workSec + 5) }
                )
                CompactStepper(
                    label = "Rest",
                    valueText = "${item.restSec}s",
                    onDec = { onRestSecChange(item.restSec - 5) },
                    onInc = { onRestSecChange(item.restSec + 5) }
                )
            }

            Column {
                Text(
                    text = "Target Cadence: ${item.targetBpm} BPM (${60_000 / item.targetBpm.coerceAtLeast(1)}ms / contact)",
                    style = MaterialTheme.typography.labelMedium,
                    color = ShuttlePulseColors.VoltPlyoLime
                )
                Slider(
                    value = item.targetBpm.toFloat(),
                    onValueChange = { onBpmChange(it.toInt()) },
                    valueRange = 90f..260f,
                    colors = SliderDefaults.colors(
                        thumbColor = ShuttlePulseColors.VoltPlyoLime,
                        activeTrackColor = ShuttlePulseColors.VoltPlyoLime
                    )
                )
            }

            if (item.exercise.archetype == DrillArchetype.BASE_PLUS_REACTIVE_BURST) {
                Text(
                    text = String.format(
                        Locale.US,
                        "Reactive Burst Trigger Window: %.1fs – %.1fs (1000ms hold)",
                        item.burstMinSec,
                        item.burstMaxSec
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = ShuttlePulseColors.ExplosiveBurstCrimson
                )
                Slider(
                    value = item.burstMinSec,
                    onValueChange = { onBurstIntervalChange(it, it + 1.8f) },
                    valueRange = 2.0f..5.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ShuttlePulseColors.ExplosiveBurstCrimson,
                        activeTrackColor = ShuttlePulseColors.ExplosiveBurstCrimson
                    )
                )
            }
        }
    }
}

@Composable
private fun CompactStepper(
    label: String,
    valueText: String,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDec, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease $label",
                tint = ShuttlePulseColors.TextSlateMuted
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = ShuttlePulseColors.TextSlateMuted)
            Text(valueText, style = MaterialTheme.typography.titleSmall, color = ShuttlePulseColors.VoltPlyoLime)
        }
        IconButton(onClick = onInc, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase $label",
                tint = ShuttlePulseColors.VoltPlyoLime
            )
        }
    }
}
