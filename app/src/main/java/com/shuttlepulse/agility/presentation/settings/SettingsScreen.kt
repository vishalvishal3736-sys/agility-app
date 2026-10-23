package com.shuttlepulse.agility.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.usecase.DiagnosticCheckResult
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = state.preferences

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShuttlePulseColors.OledGymBlack)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SETTINGS & SYSTEM HEALTH",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ShuttlePulseColors.TextCrispWhite
                )
                Text(
                    text = "Athlete stance calibration, zero-latency audio engine, and monotonic timer benchmarks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShuttlePulseColors.TextSlateMuted
                )
            }
        }

        // Section 1: Athlete Biomechanics & Cadence Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
                border = BorderStroke(1.5.dp, ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsTennis,
                            contentDescription = null,
                            tint = ShuttlePulseColors.VoltPlyoLime
                        )
                        Text(
                            text = "ATHLETE BIOMECHANICS & STANCE",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShuttlePulseColors.VoltPlyoLime
                        )
                    }

                    Text(
                        text = "Dominant Racket Hand (Controls Lead-Foot Cues & Canvas Stance)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShuttlePulseColors.TextCrispWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DominantHand.entries.forEach { hand ->
                            val selected = prefs.dominantHand == hand
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setDominantHand(hand) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_dominant_hand_${hand.name.lowercase()}"),
                                label = {
                                    Text(
                                        text = "${hand.label} (${hand.racketSide.substringBefore(" ")})",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.22f),
                                    selectedLabelColor = ShuttlePulseColors.VoltPlyoLime,
                                    labelColor = ShuttlePulseColors.TextSlateMuted
                                )
                            )
                        }
                    }

                    // Cadence Offset Slider (-20 to +20 BPM)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BPM Cadence Offset",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShuttlePulseColors.TextCrispWhite
                            )
                            val offsetStr = if (prefs.bpmOffset >= 0) "+${prefs.bpmOffset}" else "${prefs.bpmOffset}"
                            Text(
                                text = "$offsetStr BPM (e.g., 150 -> ${150 + prefs.bpmOffset} BPM)",
                                style = MaterialTheme.typography.labelLarge,
                                color = ShuttlePulseColors.FastFeetCyan
                            )
                        }
                        Slider(
                            value = prefs.bpmOffset.toFloat(),
                            onValueChange = { viewModel.setBpmOffset(it.toInt()) },
                            valueRange = -20f..20f,
                            steps = 39,
                            colors = SliderDefaults.colors(
                                thumbColor = ShuttlePulseColors.FastFeetCyan,
                                activeTrackColor = ShuttlePulseColors.FastFeetCyan
                            )
                        )
                    }
                }
            }
        }

        // Section 2: Audio & Voice Engine Toggles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
                border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = ShuttlePulseColors.FastFeetCyan
                        )
                        Text(
                            text = "AUDIO ENGINE & FOCUS MANAGEMENT",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShuttlePulseColors.FastFeetCyan
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Music Ducking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShuttlePulseColors.TextCrispWhite
                            )
                            Text(
                                text = "Ducks Spotify/Apple Music during explosive voice cues via AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK",
                                style = MaterialTheme.typography.labelMedium,
                                color = ShuttlePulseColors.TextSlateMuted
                            )
                        }
                        Switch(
                            checked = prefs.audioDuckingEnabled,
                            onCheckedChange = {
                                viewModel.updateAudioPreferences(
                                    it,
                                    prefs.metronomeTickEnabled,
                                    prefs.voiceCalloutsEnabled,
                                    prefs.bpmCalibrationScale,
                                    prefs.bpmOffset
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fast Mixer BPM Cadence Ticks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShuttlePulseColors.TextCrispWhite
                            )
                            Text(
                                text = "Zero-latency 880Hz ticks & 1320Hz downbeat accents via SoundPool and AudioTrack",
                                style = MaterialTheme.typography.labelMedium,
                                color = ShuttlePulseColors.TextSlateMuted
                            )
                        }
                        Switch(
                            checked = prefs.metronomeTickEnabled,
                            onCheckedChange = {
                                viewModel.updateAudioPreferences(
                                    prefs.audioDuckingEnabled,
                                    it,
                                    prefs.voiceCalloutsEnabled,
                                    prefs.bpmCalibrationScale,
                                    prefs.bpmOffset
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Spoken Athletic Voice Coaching",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShuttlePulseColors.TextCrispWhite
                            )
                            Text(
                                text = "Snappy 1.35x speech rate for mid-set commands: 'EXPLODE!', 'SWITCH SIDES!', 'RACKET LUNGE!'",
                                style = MaterialTheme.typography.labelMedium,
                                color = ShuttlePulseColors.TextSlateMuted
                            )
                        }
                        Switch(
                            checked = prefs.voiceCalloutsEnabled,
                            onCheckedChange = {
                                viewModel.updateAudioPreferences(
                                    prefs.audioDuckingEnabled,
                                    prefs.metronomeTickEnabled,
                                    it,
                                    prefs.bpmCalibrationScale,
                                    prefs.bpmOffset
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShuttlePulseColors.OledGymBlack,
                                checkedTrackColor = ShuttlePulseColors.VoltPlyoLime
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Live Audio & Haptic Test Bench
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
                border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = ShuttlePulseColors.ExplosiveBurstCrimson
                        )
                        Text(
                            text = "LIVE AUDIO & HAPTIC TEST BENCH",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShuttlePulseColors.ExplosiveBurstCrimson
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::testTick,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ShuttlePulseColors.VoltPlyoLime)
                        ) {
                            Text("880Hz Tick", color = ShuttlePulseColors.VoltPlyoLime)
                        }
                        OutlinedButton(
                            onClick = viewModel::testAccent,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ShuttlePulseColors.VoltPlyoLime)
                        ) {
                            Text("1320Hz Accent", color = ShuttlePulseColors.VoltPlyoLime)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::testBurstWhistle,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ShuttlePulseColors.ExplosiveBurstCrimson)
                        ) {
                            Text("Whistle Burst", color = ShuttlePulseColors.ExplosiveBurstCrimson)
                        }
                        OutlinedButton(
                            onClick = viewModel::testCountdownPip,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan)
                        ) {
                            Text("660Hz Pip", color = ShuttlePulseColors.FastFeetCyan)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::test240BpmBurst,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan)
                        ) {
                            Text("240 BPM Burst", color = ShuttlePulseColors.FastFeetCyan)
                        }
                        OutlinedButton(
                            onClick = viewModel::testVoiceCallout,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ShuttlePulseColors.RecoveryMint)
                        ) {
                            Text("Voice Cue (1.35x)", color = ShuttlePulseColors.RecoveryMint)
                        }
                    }
                }
            }
        }

        // Section 4: Monotonic Timer Precision & Self-Test Suite
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
                border = BorderStroke(1.5.dp, ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = ShuttlePulseColors.VoltPlyoLime
                        )
                        Text(
                            text = "MONOTONIC TIMER & SYSTEM HEALTH",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShuttlePulseColors.VoltPlyoLime
                        )
                    }

                    Text(
                        text = state.statusBanner,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShuttlePulseColors.TextCrispWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatLabel("DB SEED", "${state.seededDrillsCount} Drills / ${state.seededCircuitsCount} Plans")
                        StatLabel("TIMER DRIFT", "${state.lastMeasuredDriftMs}ms avg")
                        StatLabel(
                            "STATUS",
                            if (state.auditResults.all { it.passed }) "ALL PASS" else "CHECK"
                        )
                    }

                    Button(
                        onClick = { viewModel.runAutomatedSelfTest(fireAudioBurst = true) },
                        enabled = !state.isRunningAudit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_settings_self_test_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShuttlePulseColors.VoltPlyoLime,
                            contentColor = ShuttlePulseColors.OledGymBlack
                        )
                    ) {
                        Icon(imageVector = Icons.Default.BuildCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRunningAudit) "RUNNING BENCHMARKS..." else "RUN AUTOMATED SELF-TEST",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        items(state.auditResults, key = { it.id }) { check ->
            DiagnosticCheckCard(check = check)
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
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
private fun DiagnosticCheckCard(check: DiagnosticCheckResult) {
    val badgeColor = if (check.passed) {
        ShuttlePulseColors.RecoveryMint
    } else {
        ShuttlePulseColors.ExplosiveBurstCrimson
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("diag_card_${check.id}"),
        colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = check.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ShuttlePulseColors.TextCrispWhite
                )
                Text(
                    text = check.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShuttlePulseColors.TextSlateMuted
                )
            }

            Surface(
                color = badgeColor.copy(alpha = 0.20f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, badgeColor)
            ) {
                Text(
                    text = if (check.passed) "PASS" else "FAIL",
                    style = MaterialTheme.typography.labelLarge,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
