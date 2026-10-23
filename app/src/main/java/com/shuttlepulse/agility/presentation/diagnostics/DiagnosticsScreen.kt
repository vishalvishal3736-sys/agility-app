package com.shuttlepulse.agility.presentation.diagnostics

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
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.shuttlepulse.agility.domain.usecase.DiagnosticCheckResult

@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShuttlePulseColors.OledGymBlack)
            .testTag("diagnostics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SYSTEM HEALTH & AUDIO/TIMER DIAGNOSTICS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ShuttlePulseColors.TextCrispWhite
                )
                Text(
                    text = state.statusBanner,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShuttlePulseColors.VoltPlyoLime
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
                border = BorderStroke(1.5.dp, ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DiagStat("DB SEED", "${state.seededExerciseCount} Drills / ${state.seededProgramCount} Pro")
                        DiagStat("TIMER DRIFT", "${state.lastMeasuredDriftMs}ms avg")
                        DiagStat(
                            "OVERALL",
                            if (state.results.all { it.passed }) "ALL PASS" else "CHECK"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.runAutomatedSelfTest(fireAudioBurst = true) },
                            enabled = !state.isRunningAudit,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("run_automated_self_test_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShuttlePulseColors.VoltPlyoLime,
                                contentColor = ShuttlePulseColors.OledGymBlack
                            )
                        ) {
                            Icon(imageVector = Icons.Default.BuildCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isRunningAudit) "TESTING..." else "RUN AUTOMATED SELF-TEST",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = viewModel::test240BpmMetronomeAndWhistle,
                            modifier = Modifier
                                .weight(0.8f)
                                .height(50.dp)
                                .testTag("test_240bpm_audio_button"),
                            border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = ShuttlePulseColors.FastFeetCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("240 BPM", color = ShuttlePulseColors.FastFeetCyan)
                        }
                    }
                }
            }
        }

        items(state.results, key = { it.id }) { check ->
            DiagnosticCheckCard(check = check)
        }
    }
}

@Composable
private fun DiagStat(label: String, value: String) {
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
