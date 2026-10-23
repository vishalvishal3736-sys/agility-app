package com.shuttlepulse.agility.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shuttlepulse.agility.R
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.SessionLog
import com.shuttlepulse.agility.domain.model.UserPreferences
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DurationFilter(val label: String) {
    ALL("All"),
    UNDER_10M("Under 10m"),
    TEN_TO_EIGHTEEN("10-18m"),
    TWENTY_PLUS("20m+ Endurance")
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLaunchPlayer: () -> Unit,
    onOpenBuilder: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAudioSettings by remember { mutableStateOf(false) }
    var selectedDurationFilter by remember { mutableStateOf(DurationFilter.ALL) }

    val filteredCircuits = remember(state.circuits, selectedDurationFilter) {
        when (selectedDurationFilter) {
            DurationFilter.ALL -> state.circuits
            DurationFilter.UNDER_10M -> state.circuits.filter { it.totalDurationMinutes < 10 }
            DurationFilter.TEN_TO_EIGHTEEN -> state.circuits.filter { it.totalDurationMinutes in 10..18 }
            DurationFilter.TWENTY_PLUS -> state.circuits.filter { it.totalDurationMinutes >= 20 }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShuttlePulseColors.OledGymBlack)
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ActiveSessionHeroCard(
                circuit = state.activeCircuit,
                allCircuits = state.circuits,
                dominantHand = state.preferences.dominantHand,
                onSelectCircuit = viewModel::selectCircuit,
                onLaunchWorkout = onLaunchPlayer,
                onOpenLibrary = onOpenLibrary
            )
        }

        item {
            DominantHandAndAudioCard(
                preferences = state.preferences,
                expandedAudio = showAudioSettings,
                onToggleAudioExpand = { showAudioSettings = !showAudioSettings },
                onSelectHand = viewModel::setDominantHand,
                onUpdateAudioPrefs = viewModel::updateAudioPrefs
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRO CONDITIONING SESSIONS",
                        style = MaterialTheme.typography.titleLarge,
                        color = ShuttlePulseColors.TextCrispWhite
                    )
                    Text(
                        text = "Chinese fast-feet, SSC pogo bounce & explosive lunge circuits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShuttlePulseColors.TextSlateMuted
                    )
                }

                OutlinedButton(
                    onClick = onOpenBuilder,
                    modifier = Modifier.testTag("open_custom_builder_button"),
                    border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "Build custom circuit",
                        tint = ShuttlePulseColors.FastFeetCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BUILD",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DurationFilter.entries.forEach { filter ->
                    val selected = selectedDurationFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { selectedDurationFilter = filter },
                        modifier = Modifier.testTag("filter_${filter.name.lowercase()}"),
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                            selectedLabelColor = ShuttlePulseColors.VoltPlyoLime,
                            unselectedLabelColor = ShuttlePulseColors.TextSlateMuted
                        )
                    )
                }
            }
        }

        items(filteredCircuits, key = { it.id }) { circuit ->
            WorkoutCircuitCard(
                circuit = circuit,
                isSelected = circuit.id == state.activeCircuit?.id,
                onSelect = { viewModel.selectCircuit(circuit) },
                onStartImmediately = {
                    viewModel.selectCircuit(circuit)
                    onLaunchPlayer()
                },
                onDelete = if (!circuit.isPreBuilt) {
                    { viewModel.deleteCustomCircuit(circuit.id) }
                } else {
                    null
                }
            )
        }

        if (state.recentLogs.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT TRAINING LOGS",
                    style = MaterialTheme.typography.titleLarge,
                    color = ShuttlePulseColors.TextCrispWhite
                )
            }
            items(state.recentLogs.take(4), key = { it.id }) { log ->
                RecentWorkoutLogRow(log = log)
            }
        }
    }
}

@Composable
private fun ActiveSessionHeroCard(
    circuit: WorkoutCircuit?,
    allCircuits: List<WorkoutCircuit>,
    dominantHand: DominantHand,
    onSelectCircuit: (WorkoutCircuit) -> Unit,
    onLaunchWorkout: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_quick_start_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
        border = BorderStroke(1.5.dp, ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_court),
                    contentDescription = "ShuttlePulse badminton plyometrics banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ShuttlePulseColors.GymSurfaceDark.copy(alpha = 0.94f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = ShuttlePulseColors.VoltPlyoLime,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "SHUTTLEPULSE · ${circuit?.totalDurationMinutes ?: 15} MIN PLAN",
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.OledGymBlack,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = ShuttlePulseColors.OledGymBlack.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan)
                    ) {
                        Text(
                            text = dominantHand.label.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.FastFeetCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = circuit?.title ?: "15-Min Pre-Court CNS & Fast Feet Activation",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ShuttlePulseColors.TextCrispWhite
                    )
                    Text(
                        text = circuit?.subtitle
                            ?: "Pogo elasticity, Chinese 240 BPM fast-feet tapping, and reactive burst triggers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShuttlePulseColors.TextSlateMuted,
                        maxLines = 2
                    )
                }
            }

            // 1-Tap Plan Duration Switcher (4 / 8 / 15 / 20 / 25 MIN)
            val preBuiltPlans = allCircuits.filter { it.isPreBuilt }
            if (preBuiltPlans.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    preBuiltPlans.forEach { plan ->
                        val mins = plan.totalDurationMinutes
                        val selected = plan.id == circuit?.id
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCircuit(plan) },
                            modifier = Modifier.testTag("quick_plan_${mins}min_chip"),
                            label = {
                                Text(
                                    text = "$mins MIN (${plan.totalSetsCount}S)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.24f),
                                selectedLabelColor = ShuttlePulseColors.VoltPlyoLime
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroStatItem("DURATION", circuit?.formattedTotalDuration ?: "15m 00s")
                HeroStatItem("DRILL BLOCKS", "${circuit?.items?.size ?: 5} Drills")
                HeroStatItem("TOTAL SETS", "${circuit?.totalSetsCount ?: 18} Sets")
                HeroStatItem("LEAD STANCE", dominantHand.racketSide.substringBefore(" "))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onLaunchWorkout,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(52.dp)
                        .testTag("quick_start_workout_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShuttlePulseColors.VoltPlyoLime,
                        contentColor = ShuttlePulseColors.OledGymBlack
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch workout player HUD"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "START ${circuit?.totalDurationMinutes ?: 15}-MIN HUD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenLibrary,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(52.dp)
                        .testTag("browse_drill_library_button"),
                    border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "16 DRILLS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ShuttlePulseColors.TextSlateMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = ShuttlePulseColors.VoltPlyoLime
        )
    }
}

@Composable
private fun DominantHandAndAudioCard(
    preferences: UserPreferences,
    expandedAudio: Boolean,
    onToggleAudioExpand: () -> Unit,
    onSelectHand: (DominantHand) -> Unit,
    onUpdateAudioPrefs: (Boolean, Boolean, Boolean, Float, Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
        border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsTennis,
                        contentDescription = "Dominant racket hand",
                        tint = ShuttlePulseColors.VoltPlyoLime
                    )
                    Column {
                        Text(
                            text = "DOMINANT RACKET HAND (UNILATERAL CUES)",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShuttlePulseColors.TextCrispWhite
                        )
                        Text(
                            text = "Cues ${preferences.dominantHand.racketSide} Forward & mid-set side switch",
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.TextSlateMuted
                        )
                    }
                }
                IconButton(
                    onClick = onToggleAudioExpand,
                    modifier = Modifier.testTag("toggle_audio_ducking_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Audio and BPM calibration",
                        tint = ShuttlePulseColors.FastFeetCyan
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DominantHand.entries.forEach { hand ->
                    val selected = preferences.dominantHand == hand
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectHand(hand) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dominant_hand_${hand.name.lowercase()}"),
                        label = {
                            Text(
                                text = "${hand.label} (${hand.racketSide.substringBefore(" ")})",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                            selectedLabelColor = ShuttlePulseColors.VoltPlyoLime
                        )
                    )
                }
            }

            if (expandedAudio) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background Music AudioFocus Ducking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShuttlePulseColors.TextCrispWhite
                        )
                        Text(
                            text = "Uses AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK during spoken burst triggers",
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.TextSlateMuted
                        )
                    }
                    Switch(
                        checked = preferences.audioDuckingEnabled,
                        onCheckedChange = {
                            onUpdateAudioPrefs(
                                it,
                                preferences.metronomeTickEnabled,
                                preferences.voiceCalloutsEnabled,
                                preferences.bpmCalibrationScale,
                                preferences.bpmOffset
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ShuttlePulseColors.OledGymBlack,
                            checkedTrackColor = ShuttlePulseColors.VoltPlyoLime
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SoundPool Zero-Latency BPM Metronome Tick",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShuttlePulseColors.TextCrispWhite
                    )
                    Switch(
                        checked = preferences.metronomeTickEnabled,
                        onCheckedChange = {
                            onUpdateAudioPrefs(
                                preferences.audioDuckingEnabled,
                                it,
                                preferences.voiceCalloutsEnabled,
                                preferences.bpmCalibrationScale,
                                preferences.bpmOffset
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ShuttlePulseColors.OledGymBlack,
                            checkedTrackColor = ShuttlePulseColors.FastFeetCyan
                        )
                    )
                }

                Column {
                    Text(
                        text = String.format(
                            Locale.US,
                            "Global BPM Cadence Multiplier: %.0f%%",
                            preferences.bpmCalibrationScale * 100f
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan
                    )
                    Slider(
                        value = preferences.bpmCalibrationScale,
                        onValueChange = {
                            onUpdateAudioPrefs(
                                preferences.audioDuckingEnabled,
                                preferences.metronomeTickEnabled,
                                preferences.voiceCalloutsEnabled,
                                it,
                                preferences.bpmOffset
                            )
                        },
                        valueRange = 0.80f..1.20f,
                        colors = SliderDefaults.colors(
                            thumbColor = ShuttlePulseColors.FastFeetCyan,
                            activeTrackColor = ShuttlePulseColors.FastFeetCyan
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCircuitCard(
    circuit: WorkoutCircuit,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onStartImmediately: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("circuit_card_${circuit.id}"),
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
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${circuit.durationBadge} (${circuit.formattedTotalDuration})",
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.VoltPlyoLime,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${circuit.totalSetsCount} SETS · ${circuit.focusTag.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ShuttlePulseColors.FastFeetCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete custom circuit",
                                tint = ShuttlePulseColors.ExplosiveBurstCrimson
                            )
                        }
                    }
                    Button(
                        onClick = onStartImmediately,
                        modifier = Modifier.testTag("train_circuit_${circuit.id}"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) {
                                ShuttlePulseColors.VoltPlyoLime
                            } else {
                                ShuttlePulseColors.GymCardElevated
                            },
                            contentColor = if (isSelected) {
                                ShuttlePulseColors.OledGymBlack
                            } else {
                                ShuttlePulseColors.VoltPlyoLime
                            }
                        )
                    ) {
                        Text("TRAIN", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Text(
                text = circuit.title,
                style = MaterialTheme.typography.titleLarge,
                color = ShuttlePulseColors.TextCrispWhite
            )
            Text(
                text = circuit.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ShuttlePulseColors.TextSlateMuted
            )

            // List drill pills in circuit
            Text(
                text = circuit.items.joinToString("  •  ") { "${it.sets}x ${it.exercise.name}" },
                style = MaterialTheme.typography.labelMedium,
                color = ShuttlePulseColors.FastFeetCyan,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun RecentWorkoutLogRow(log: SessionLog) {
    val fmt = SimpleDateFormat("MMM d · HH:mm", Locale.US)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
        border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.workoutTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = ShuttlePulseColors.TextCrispWhite
                )
                Text(
                    text = "${fmt.format(Date(log.timestampEpochMs))} · ${log.dominantHandUsed.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ShuttlePulseColors.TextSlateMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${log.estimatedGroundContacts} Contacts",
                    style = MaterialTheme.typography.titleSmall,
                    color = ShuttlePulseColors.VoltPlyoLime
                )
                Text(
                    text = "${log.explosiveBurstsExecuted} Bursts · ${log.completedSets} Sets",
                    style = MaterialTheme.typography.labelMedium,
                    color = ShuttlePulseColors.FastFeetCyan
                )
            }
        }
    }
}
