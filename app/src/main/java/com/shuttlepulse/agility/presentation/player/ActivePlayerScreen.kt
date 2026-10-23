package com.shuttlepulse.agility.presentation.player

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.domain.model.DrillArchetype
import com.shuttlepulse.agility.domain.model.PlayerUiState
import com.shuttlepulse.agility.domain.model.SegmentType
import java.util.Locale

@Composable
fun ActivePlayerScreen(
    viewModel: PlayerViewModel,
    onExitPlayer: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showQuitDialog by remember { mutableStateOf(false) }

    // Enforce FLAG_KEEP_SCREEN_ON via DisposableEffect while WorkoutPlayerScreen is active
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            // Keep screen awake or allow normal lifecycle management
        }
    }

    // BackHandler with Quit Workout confirmation dialog when a session is running
    BackHandler(enabled = true) {
        if (state.isRunning && !state.isCompleted) {
            showQuitDialog = true
        } else {
            onExitPlayer()
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            containerColor = ShuttlePulseColors.GymCardElevated,
            titleContentColor = ShuttlePulseColors.TextCrispWhite,
            textContentColor = ShuttlePulseColors.TextSlateMuted,
            title = { Text("Quit Active Conditioning Session?") },
            text = {
                Text("Your current fast-feet and plyometric set is still in progress. Stop timer and exit?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuitDialog = false
                        viewModel.stopAndResetWorkout()
                        onExitPlayer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShuttlePulseColors.ExplosiveBurstCrimson
                    )
                ) {
                    Text("End Workout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Keep Training", color = ShuttlePulseColors.VoltPlyoLime)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShuttlePulseColors.OledGymBlack)
            .testTag("workout_player_screen")
    ) {
        when {
            state.isCompleted -> {
                WorkoutCompletedSummaryView(
                    state = state,
                    onRestart = viewModel::startOrRestartWorkout,
                    onExit = {
                        viewModel.stopAndResetWorkout()
                        onExitPlayer()
                    }
                )
            }

            !state.isRunning -> {
                PreStartPlayerBriefingView(
                    state = state,
                    onSelectPlanMinutes = viewModel::selectPreBuiltPlanByDuration,
                    onStart = viewModel::startOrRestartWorkout,
                    onExit = onExitPlayer
                )
            }

            else -> {
                ActiveWorkoutHudView(
                    state = state,
                    onPauseResume = viewModel::togglePauseResume,
                    onPreviousSegment = viewModel::skipToPreviousSegment,
                    onNextSegment = viewModel::skipToNextSegment,
                    onAddFifteenSecRest = viewModel::addFifteenSecondsRest,
                    onManualBurst = viewModel::triggerManualExplosiveBurst,
                    onRequestQuit = { showQuitDialog = true }
                )
            }
        }
    }
}

@Composable
private fun PreStartPlayerBriefingView(
    state: PlayerUiState,
    onSelectPlanMinutes: (Int) -> Unit,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    val circuit = state.activeCircuit
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ACTIVE PLAYER HUD",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ShuttlePulseColors.VoltPlyoLime
                )
                Text(
                    text = circuit?.title ?: "Badminton Plyometric Session",
                    style = MaterialTheme.typography.titleMedium,
                    color = ShuttlePulseColors.TextCrispWhite
                )
                Text(
                    text = "${circuit?.formattedTotalDuration ?: "15m 00s"} Total  •  ${circuit?.totalSetsCount ?: 18} Work Sets  •  ${circuit?.items?.size ?: 5} Drills",
                    style = MaterialTheme.typography.labelLarge,
                    color = ShuttlePulseColors.FastFeetCyan
                )
            }
            IconButton(onClick = onExit) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close player",
                    tint = ShuttlePulseColors.TextCrispWhite
                )
            }
        }

        // Quick 4 / 8 / 15 / 20 / 25 Min Plan Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(4, 8, 15, 20, 25).forEach { mins ->
                val isCurrentDuration = circuit?.totalDurationMinutes == mins
                OutlinedButton(
                    onClick = { onSelectPlanMinutes(mins) },
                    modifier = Modifier.testTag("player_plan_${mins}min_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isCurrentDuration) {
                            ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.2f)
                        } else {
                            ShuttlePulseColors.GymSurfaceDark
                        },
                        contentColor = if (isCurrentDuration) {
                            ShuttlePulseColors.VoltPlyoLime
                        } else {
                            ShuttlePulseColors.TextCrispWhite
                        }
                    ),
                    border = BorderStroke(
                        width = if (isCurrentDuration) 1.5.dp else 1.dp,
                        color = if (isCurrentDuration) {
                            ShuttlePulseColors.VoltPlyoLime
                        } else {
                            ShuttlePulseColors.GymBorderSubtle
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$mins MIN PLAN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
            border = BorderStroke(1.5.dp, ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StickFigureStanceCanvas(
                    pose = state.activePose,
                    dominantHand = state.userPreferences.dominantHand,
                    cadencePhase = 0.35f,
                    targetBpm = state.currentBpm,
                    isExplosiveTrigger = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp)
                )

                FootStanceCanvas(
                    pose = state.activePose,
                    dominantHand = state.userPreferences.dominantHand,
                    cadencePhase = 0.35f,
                    isExplosiveTrigger = false
                )

                Text(
                    text = "UNILATERAL LEAD: ${state.activeLeadFootBadge.uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = ShuttlePulseColors.FastFeetCyan
                )
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("player_begin_session_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = ShuttlePulseColors.VoltPlyoLime,
                contentColor = ShuttlePulseColors.OledGymBlack
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start workout timer",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START ${circuit?.totalDurationMinutes ?: 15}-MIN CADENCE HUD",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActiveWorkoutHudView(
    state: PlayerUiState,
    onPauseResume: () -> Unit,
    onPreviousSegment: () -> Unit,
    onNextSegment: () -> Unit,
    onAddFifteenSecRest: () -> Unit,
    onManualBurst: () -> Unit,
    onRequestQuit: () -> Unit
) {
    val seg = state.currentSegment
    val isWork = seg?.type == SegmentType.WORK_SET
    val isRest = seg?.type == SegmentType.REST_RECOVERY

    val accentColor = when {
        state.isExplosiveFlashActive -> ShuttlePulseColors.ExplosiveBurstCrimson
        isRest -> ShuttlePulseColors.FastFeetCyan
        seg?.type == SegmentType.PREP_COUNTDOWN -> ShuttlePulseColors.UnilateralSwitchAmber
        else -> ShuttlePulseColors.VoltPlyoLime
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header: Set Counter, Archetype Badge & Quit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ShuttlePulseColors.GymCardElevated,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "SET ${seg?.overallSetIndex ?: 1}/${seg?.totalWorkSetsInWorkout ?: 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.VoltPlyoLime
                    )
                    Text(
                        text = seg?.exercise?.archetype?.shortBadge ?: "CADENCE",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.FastFeetCyan
                    )
                    Text(
                        text = "${state.totalGroundContactsCount} CONTACTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.TextCrispWhite
                    )
                }
            }

            IconButton(
                onClick = onRequestQuit,
                modifier = Modifier.testTag("quit_workout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quit workout",
                    tint = ShuttlePulseColors.TextSlateMuted
                )
            }
        }

        // Giant Countdown & Micro-Phase Callout Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = accentColor.copy(alpha = 0.14f)
            ),
            border = BorderStroke(2.5.dp, accentColor),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = seg?.exercise?.name?.uppercase() ?: "SHUTTLEPULSE",
                    style = MaterialTheme.typography.titleMedium,
                    color = ShuttlePulseColors.TextCrispWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = String.format(Locale.US, "%.1f", state.segmentRemainingMs / 1000f),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 78.sp),
                    color = accentColor
                )

                Text(
                    text = state.activeHeadlineCue,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ShuttlePulseColors.TextCrispWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = ShuttlePulseColors.OledGymBlack.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = state.activeLeadFootBadge,
                        style = MaterialTheme.typography.labelLarge,
                        color = ShuttlePulseColors.VoltPlyoLime,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (seg?.exercise?.archetype == DrillArchetype.COMBO_CYCLE && isWork) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ComboBeatProgressBar(
                        currentStep = state.comboBeatStep,
                        totalSteps = state.comboTotalSteps,
                        isExplosiveBeat = state.isExplosiveFlashActive
                    )
                }
            }
        }

        // Articulated Stick-Figure Stance + Top-Down Foot-Stance Canvas
        Card(
            colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
            border = BorderStroke(1.dp, ShuttlePulseColors.GymBorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StickFigureStanceCanvas(
                    pose = state.activePose,
                    dominantHand = state.userPreferences.dominantHand,
                    cadencePhase = state.cadenceBeatPhase,
                    targetBpm = state.currentBpm,
                    isExplosiveTrigger = state.isExplosiveFlashActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                )

                FootStanceCanvas(
                    pose = state.activePose,
                    dominantHand = state.userPreferences.dominantHand,
                    cadencePhase = state.cadenceBeatPhase,
                    isExplosiveTrigger = state.isExplosiveFlashActive
                )

                Text(
                    text = "COACHING CUE: ${state.activeSubCue}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ShuttlePulseColors.TextSlateMuted
                )
            }
        }

        // Transport & Control Buttons: Previous, Pause/Resume, Skip, +15s Rest, Manual Burst
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousSegment,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ShuttlePulseColors.GymCardElevated)
                    .testTag("player_prev_segment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous segment",
                    tint = ShuttlePulseColors.TextCrispWhite
                )
            }

            Button(
                onClick = onPauseResume,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("player_pause_resume_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isPaused) {
                        ShuttlePulseColors.VoltPlyoLime
                    } else {
                        ShuttlePulseColors.GymCardElevated
                    },
                    contentColor = if (state.isPaused) {
                        ShuttlePulseColors.OledGymBlack
                    } else {
                        ShuttlePulseColors.TextCrispWhite
                    }
                )
            ) {
                Icon(
                    imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Pause or resume"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (state.isPaused) "RESUME" else "PAUSE")
            }

            OutlinedButton(
                onClick = onAddFifteenSecRest,
                modifier = Modifier
                    .height(50.dp)
                    .testTag("player_add_15s_button"),
                border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreTime,
                    contentDescription = "Add 15 seconds",
                    tint = ShuttlePulseColors.FastFeetCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("+15s", color = ShuttlePulseColors.FastFeetCyan)
            }

            OutlinedButton(
                onClick = onManualBurst,
                modifier = Modifier
                    .height(50.dp)
                    .testTag("player_manual_burst_button"),
                border = BorderStroke(1.dp, ShuttlePulseColors.ExplosiveBurstCrimson),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Fire burst trigger",
                    tint = ShuttlePulseColors.ExplosiveBurstCrimson,
                    modifier = Modifier.size(18.dp)
                )
                Text("BURST", color = ShuttlePulseColors.ExplosiveBurstCrimson)
            }

            IconButton(
                onClick = onNextSegment,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ShuttlePulseColors.GymCardElevated)
                    .testTag("player_skip_segment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip segment",
                    tint = ShuttlePulseColors.VoltPlyoLime
                )
            }
        }
    }
}

@Composable
private fun WorkoutCompletedSummaryView(
    state: PlayerUiState,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Workout complete",
            tint = ShuttlePulseColors.VoltPlyoLime,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "CONDITIONING SESSION LOGGED",
            style = MaterialTheme.typography.labelLarge,
            color = ShuttlePulseColors.FastFeetCyan
        )
        Text(
            text = state.activeCircuit?.title ?: "ShuttlePulse Workout",
            style = MaterialTheme.typography.headlineLarge,
            color = ShuttlePulseColors.TextCrispWhite,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ShuttlePulseColors.GymSurfaceDark),
            border = BorderStroke(1.5.dp, ShuttlePulseColors.VoltPlyoLime)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FOREFOOT CONTACTS", style = MaterialTheme.typography.labelMedium, color = ShuttlePulseColors.TextSlateMuted)
                    Text("${state.totalGroundContactsCount}", style = MaterialTheme.typography.headlineLarge, color = ShuttlePulseColors.VoltPlyoLime)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EXPLOSIVE BURSTS", style = MaterialTheme.typography.labelMedium, color = ShuttlePulseColors.TextSlateMuted)
                    Text("${state.totalExplosiveTriggersFired}", style = MaterialTheme.typography.headlineLarge, color = ShuttlePulseColors.ExplosiveBurstCrimson)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                border = BorderStroke(1.dp, ShuttlePulseColors.FastFeetCyan)
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = null, tint = ShuttlePulseColors.FastFeetCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text("REPEAT", color = ShuttlePulseColors.FastFeetCyan)
            }
            Button(
                onClick = onExit,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShuttlePulseColors.VoltPlyoLime,
                    contentColor = ShuttlePulseColors.OledGymBlack
                )
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
