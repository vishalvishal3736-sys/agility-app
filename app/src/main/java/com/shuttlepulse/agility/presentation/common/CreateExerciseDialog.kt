package com.shuttlepulse.agility.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.ExerciseCategory
import com.shuttlepulse.agility.domain.model.StancePoseType

@Composable
fun CreateExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: ExerciseCategory,
        archetype: DrillArchetype,
        workSec: Int,
        restSec: Int,
        targetBpm: Int,
        coachingTip: String,
        baseStance: StancePoseType
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var workSec by remember { mutableIntStateOf(30) }
    var restSec by remember { mutableIntStateOf(20) }
    var targetBpm by remember { mutableFloatStateOf(160f) }
    var selectedCategory by remember { mutableStateOf<ExerciseCategory>(ExerciseCategory.FAST_FEET_CHINESE_TAPPING) }
    var selectedArchetype by remember { mutableStateOf<DrillArchetype>(DrillArchetype.RHYTHMIC_CADENCE) }
    var selectedStance by remember { mutableStateOf<StancePoseType>(StancePoseType.WIDE_FAST_TAPPING) }
    var coachingTip by remember { mutableStateOf("Stay on balls of feet, stiff ankles") }
    var nameError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("create_exercise_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = ShuttlePulseColors.GymSurfaceDark,
            border = BorderStroke(1.5.dp, ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = ShuttlePulseColors.VoltPlyoLime,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CUSTOM EXERCISE",
                                style = MaterialTheme.typography.titleLarge,
                                color = ShuttlePulseColors.TextCrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Define custom work/rest duration & cadence",
                                style = MaterialTheme.typography.labelMedium,
                                color = ShuttlePulseColors.FastFeetCyan
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = ShuttlePulseColors.TextSlateMuted
                        )
                    }
                }

                // Exercise Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g. Shadow Net Kill & Recovery") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Please enter a name for the exercise", color = ShuttlePulseColors.ExplosiveBurstCrimson) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_exercise_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShuttlePulseColors.VoltPlyoLime,
                        unfocusedBorderColor = ShuttlePulseColors.GymBorderSubtle,
                        focusedLabelColor = ShuttlePulseColors.VoltPlyoLime,
                        focusedTextColor = ShuttlePulseColors.TextCrispWhite,
                        unfocusedTextColor = ShuttlePulseColors.TextCrispWhite
                    )
                )

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "EXERCISE CATEGORY",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExerciseCategory.entries.forEach { cat ->
                            val selected = selectedCategory == cat
                            FilterChip(
                                selected = selected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.shortLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.22f),
                                    selectedLabelColor = ShuttlePulseColors.FastFeetCyan
                                )
                            )
                        }
                    }
                }

                // Work & Rest Duration Steppers
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymCardElevated),
                    border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "TIME INTERVALS (WORK & REST)",
                            style = MaterialTheme.typography.labelLarge,
                            color = ShuttlePulseColors.VoltPlyoLime,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DurationStepper(
                                label = "Work Time",
                                valueSec = workSec,
                                onDec = { workSec = (workSec - 5).coerceIn(10, 180) },
                                onInc = { workSec = (workSec + 5).coerceIn(10, 180) },
                                accentColor = ShuttlePulseColors.VoltPlyoLime,
                                testTagPrefix = "work_sec"
                            )

                            Surface(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(48.dp),
                                color = ShuttlePulseColors.GymBorderSubtle
                            ) {}

                            DurationStepper(
                                label = "Rest Time",
                                valueSec = restSec,
                                onDec = { restSec = (restSec - 5).coerceIn(5, 120) },
                                onInc = { restSec = (restSec + 5).coerceIn(5, 120) },
                                accentColor = ShuttlePulseColors.FastFeetCyan,
                                testTagPrefix = "rest_sec"
                            )
                        }
                    }
                }

                // Target BPM Cadence
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymCardElevated),
                    border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TARGET CADENCE",
                                style = MaterialTheme.typography.labelLarge,
                                color = ShuttlePulseColors.VoltPlyoLime,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${targetBpm.toInt()} BPM (${(60_000 / targetBpm.toInt().coerceAtLeast(1))}ms/beat)",
                                style = MaterialTheme.typography.titleMedium,
                                color = ShuttlePulseColors.VoltPlyoLime,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = targetBpm,
                            onValueChange = { targetBpm = it },
                            valueRange = 80f..260f,
                            steps = 17,
                            modifier = Modifier.testTag("custom_exercise_bpm_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = ShuttlePulseColors.VoltPlyoLime,
                                activeTrackColor = ShuttlePulseColors.VoltPlyoLime
                            )
                        )
                    }
                }

                // Drill Archetype (Cadence, Burst, Combo, Unilateral)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "EXERCISE MECHANICS TYPE",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DrillArchetype.entries.forEach { arc ->
                            val selected = selectedArchetype == arc
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedArchetype = arc
                                    // Set natural base stance default for the chosen archetype
                                    selectedStance = when (arc) {
                                        DrillArchetype.RHYTHMIC_CADENCE -> StancePoseType.PARALLEL_POGO_BOUNCE
                                        DrillArchetype.BASE_PLUS_REACTIVE_BURST -> StancePoseType.WIDE_FAST_TAPPING
                                        DrillArchetype.COMBO_CYCLE -> StancePoseType.PARALLEL_POGO_BOUNCE
                                        DrillArchetype.UNILATERAL_POWER -> StancePoseType.STAGGERED_SCISSOR_STANCE
                                    }
                                },
                                label = { Text(arc.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                                    selectedLabelColor = ShuttlePulseColors.VoltPlyoLime
                                )
                            )
                        }
                    }
                }

                // Stance Pose Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BASE STANCE POSE",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StancePoseType.entries.forEach { pose ->
                            val selected = selectedStance == pose
                            FilterChip(
                                selected = selected,
                                onClick = { selectedStance = pose },
                                label = { Text(pose.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.20f),
                                    selectedLabelColor = ShuttlePulseColors.FastFeetCyan
                                )
                            )
                        }
                    }
                }

                // Coaching Tip / Form Note
                OutlinedTextField(
                    value = coachingTip,
                    onValueChange = { coachingTip = it },
                    label = { Text("Coaching Form Cue") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShuttlePulseColors.FastFeetCyan,
                        unfocusedBorderColor = ShuttlePulseColors.GymBorderSubtle,
                        focusedLabelColor = ShuttlePulseColors.FastFeetCyan,
                        focusedTextColor = ShuttlePulseColors.TextCrispWhite,
                        unfocusedTextColor = ShuttlePulseColors.TextCrispWhite
                    )
                )

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", color = ShuttlePulseColors.TextSlateMuted)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            onSave(
                                name.trim(),
                                selectedCategory,
                                selectedArchetype,
                                workSec,
                                restSec,
                                targetBpm.toInt(),
                                coachingTip.trim(),
                                selectedStance
                            )
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(50.dp)
                            .testTag("save_custom_exercise_confirm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShuttlePulseColors.VoltPlyoLime,
                            contentColor = ShuttlePulseColors.OledGymBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "SAVE DRILL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationStepper(
    label: String,
    valueSec: Int,
    onDec: () -> Unit,
    onInc: () -> Unit,
    accentColor: Color,
    testTagPrefix: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ShuttlePulseColors.TextSlateMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDec,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_dec")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease $label",
                    tint = ShuttlePulseColors.TextSlateMuted
                )
            }

            Text(
                text = "${valueSec}s",
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onInc,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_inc")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase $label",
                    tint = accentColor
                )
            }
        }
    }
}
