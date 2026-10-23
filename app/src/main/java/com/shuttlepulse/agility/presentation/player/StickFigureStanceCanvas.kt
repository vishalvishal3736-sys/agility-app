package com.shuttlepulse.agility.presentation.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttlepulse.agility.core.ui.theme.JetBrainsMonoFontFamily
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.StancePoseType
import kotlin.math.PI
import kotlin.math.sin

/**
 * Articulated Stick-Figure Biomechanics & Stance Canvas + BPM Cadence Pulse Ring.
 * Highlights the Racket Hand & Racket Foot (#D4FF00 Volt Lime) vs Support Foot (#00F0FF Cyan)
 * according to the user's [DominantHand] preference and current [StancePoseType].
 */
@Composable
fun StickFigureStanceCanvas(
    pose: StancePoseType,
    dominantHand: DominantHand,
    cadencePhase: Float,
    targetBpm: Int,
    isExplosiveTrigger: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val legendText = if (dominantHand == DominantHand.RIGHT) {
        "RACKET SIDE: RIGHT (LIME) · SUPPORT: LEFT (CYAN)"
    } else {
        "RACKET SIDE: LEFT (LIME) · SUPPORT: RIGHT (CYAN)"
    }
    val legendLayout = androidx.compose.runtime.remember(textMeasurer, dominantHand) {
        textMeasurer.measure(
            legendText,
            TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = ShuttlePulseColors.TextSlateMuted
            )
        )
    }

    Box(
        modifier = modifier.testTag("stick_figure_stance_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val floorY = h * 0.84f

            // Background radial cadence aura
            val pulseWave = sin(cadencePhase * PI.toFloat())
            val primaryColor = if (isExplosiveTrigger) {
                ShuttlePulseColors.ExplosiveBurstCrimson
            } else {
                ShuttlePulseColors.VoltPlyoLime
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.16f + pulseWave * 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.48f),
                    radius = (size.minDimension * (0.38f + pulseWave * 0.14f)).coerceAtLeast(20f)
                ),
                radius = size.minDimension * 0.50f,
                center = Offset(w * 0.5f, h * 0.48f)
            )

            // Concentric BPM Cadence Ring
            val ringRadius = (size.minDimension * (0.28f + cadencePhase * 0.18f)).coerceAtLeast(16f)
            drawCircle(
                color = primaryColor.copy(alpha = (1f - cadencePhase) * 0.65f),
                radius = ringRadius,
                center = Offset(w * 0.5f, h * 0.46f),
                style = Stroke(width = if (isExplosiveTrigger) 6f else 3.5f)
            )

            // Court Floor Line + Stiff-Ankle Ground Contact Zone
            drawLine(
                color = ShuttlePulseColors.GymBorderSubtle,
                start = Offset(w * 0.10f, floorY),
                end = Offset(w * 0.90f, floorY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.6f),
                start = Offset(w * 0.25f, floorY + 6f),
                end = Offset(w * 0.75f, floorY + 6f),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f))
            )

            drawArticulatedBadmintonAthlete(
                pose = pose,
                dominantHand = dominantHand,
                pulseWave = pulseWave,
                floorY = floorY
            )

            // Draw Legend Labels for Racket Foot vs Support Foot
            drawText(
                textLayoutResult = legendLayout,
                topLeft = Offset(
                    x = (w - legendLayout.size.width) / 2f,
                    y = h - legendLayout.size.height - 4f
                )
            )
        }

        // Top Stance & BPM Badge Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ShuttlePulseColors.OledGymBlack.copy(alpha = 0.80f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = pose.label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = ShuttlePulseColors.FastFeetCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = if (isExplosiveTrigger) {
                    ShuttlePulseColors.ExplosiveBurstCrimson
                } else {
                    ShuttlePulseColors.VoltPlyoLime.copy(alpha = 0.20f)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isExplosiveTrigger) "EXPLOSIVE PEAK!" else "$targetBpm BPM PULSE",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isExplosiveTrigger) Color.White else ShuttlePulseColors.VoltPlyoLime,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawArticulatedBadmintonAthlete(
    pose: StancePoseType,
    dominantHand: DominantHand,
    pulseWave: Float,
    floorY: Float
) {
    val cx = size.width * 0.50f
    val scale = size.height * 0.0038f
    val bounceOffset = pulseWave * 16f * scale

    // Mirror X if Left-Handed so the Racket side visually flips to the athlete's left
    val dir = if (dominantHand == DominantHand.RIGHT) 1f else -1f

    val hipCenter: Offset
    val shoulderCenter: Offset
    val headCenter: Offset
    val racketKnee: Offset
    val racketFoot: Offset
    val supportKnee: Offset
    val supportFoot: Offset
    val racketHand: Offset
    val supportHand: Offset

    when (pose) {
        StancePoseType.DOUBLE_LEG_POGO -> {
            val lift = bounceOffset * 1.4f
            hipCenter = Offset(cx, floorY - 88f * scale - lift)
            shoulderCenter = Offset(cx, hipCenter.y - 62f * scale)
            headCenter = Offset(cx, shoulderCenter.y - 26f * scale)

            racketKnee = Offset(cx + 16f * scale * dir, floorY - 44f * scale - lift * 0.9f)
            racketFoot = Offset(cx + 18f * scale * dir, floorY - lift * 0.7f)

            supportKnee = Offset(cx - 16f * scale * dir, floorY - 44f * scale - lift * 0.9f)
            supportFoot = Offset(cx - 18f * scale * dir, floorY - lift * 0.7f)

            racketHand = Offset(cx + 38f * scale * dir, shoulderCenter.y + 12f * scale)
            supportHand = Offset(cx - 34f * scale * dir, shoulderCenter.y + 18f * scale)
        }

        StancePoseType.SPLIT_STANCE_RACKET_LEAD -> {
            val lift = bounceOffset
            hipCenter = Offset(cx, floorY - 82f * scale - lift)
            shoulderCenter = Offset(cx + 6f * scale * dir, hipCenter.y - 60f * scale)
            headCenter = Offset(shoulderCenter.x + 2f * scale * dir, shoulderCenter.y - 26f * scale)

            // Racket foot forward
            racketKnee = Offset(cx + 34f * scale * dir, floorY - 42f * scale - lift * 0.6f)
            racketFoot = Offset(cx + 42f * scale * dir, floorY - lift * 0.5f)

            // Support foot staggered back
            supportKnee = Offset(cx - 26f * scale * dir, floorY - 40f * scale - lift * 0.6f)
            supportFoot = Offset(cx - 42f * scale * dir, floorY - lift * 0.5f)

            racketHand = Offset(cx + 48f * scale * dir, shoulderCenter.y - 8f * scale)
            supportHand = Offset(cx - 32f * scale * dir, shoulderCenter.y + 16f * scale)
        }

        StancePoseType.SPLIT_STANCE_NON_RACKET_LEAD -> {
            val lift = bounceOffset
            hipCenter = Offset(cx, floorY - 82f * scale - lift)
            shoulderCenter = Offset(cx - 6f * scale * dir, hipCenter.y - 60f * scale)
            headCenter = Offset(shoulderCenter.x - 2f * scale * dir, shoulderCenter.y - 26f * scale)

            // Support foot forward (switched side)
            supportKnee = Offset(cx + 34f * scale * dir, floorY - 42f * scale - lift * 0.6f)
            supportFoot = Offset(cx + 42f * scale * dir, floorY - lift * 0.5f)

            // Racket foot back
            racketKnee = Offset(cx - 26f * scale * dir, floorY - 40f * scale - lift * 0.6f)
            racketFoot = Offset(cx - 42f * scale * dir, floorY - lift * 0.5f)

            racketHand = Offset(cx + 40f * scale * dir, shoulderCenter.y + 4f * scale)
            supportHand = Offset(cx - 38f * scale * dir, shoulderCenter.y + 8f * scale)
        }

        StancePoseType.WIDE_BASE_FAST_TAP -> {
            val altTap = bounceOffset * 0.45f
            hipCenter = Offset(cx, floorY - 68f * scale)
            shoulderCenter = Offset(cx, hipCenter.y - 56f * scale)
            headCenter = Offset(cx, shoulderCenter.y - 25f * scale)

            racketKnee = Offset(cx + 42f * scale * dir, floorY - 36f * scale - altTap)
            racketFoot = Offset(cx + 54f * scale * dir, floorY - altTap)

            supportKnee = Offset(cx - 42f * scale * dir, floorY - 36f * scale)
            supportFoot = Offset(cx - 54f * scale * dir, floorY)

            racketHand = Offset(cx + 46f * scale * dir, shoulderCenter.y + 6f * scale)
            supportHand = Offset(cx - 42f * scale * dir, shoulderCenter.y + 12f * scale)
        }

        StancePoseType.DEEP_RACKET_LUNGE -> {
            hipCenter = Offset(cx + 12f * scale * dir, floorY - 54f * scale)
            shoulderCenter = Offset(cx + 22f * scale * dir, hipCenter.y - 56f * scale)
            headCenter = Offset(shoulderCenter.x + 4f * scale * dir, shoulderCenter.y - 25f * scale)

            // Deep extended forehand lunge
            racketKnee = Offset(cx + 62f * scale * dir, floorY - 38f * scale)
            racketFoot = Offset(cx + 72f * scale * dir, floorY)

            // Trailing anchor leg
            supportKnee = Offset(cx - 32f * scale * dir, floorY - 24f * scale)
            supportFoot = Offset(cx - 68f * scale * dir, floorY)

            racketHand = Offset(cx + 76f * scale * dir, shoulderCenter.y + 4f * scale)
            supportHand = Offset(cx - 48f * scale * dir, shoulderCenter.y - 8f * scale)
        }

        StancePoseType.SCISSOR_KICK_ROTATION -> {
            val jumpLift = 28f * scale + bounceOffset * 0.8f
            hipCenter = Offset(cx, floorY - 92f * scale - jumpLift)
            shoulderCenter = Offset(cx - 6f * scale * dir, hipCenter.y - 58f * scale)
            headCenter = Offset(shoulderCenter.x, shoulderCenter.y - 25f * scale)

            // Scissor mid-air leg cross
            racketKnee = Offset(cx - 28f * scale * dir, hipCenter.y + 34f * scale)
            racketFoot = Offset(cx - 48f * scale * dir, hipCenter.y + 68f * scale)

            supportKnee = Offset(cx + 30f * scale * dir, hipCenter.y + 28f * scale)
            supportFoot = Offset(cx + 46f * scale * dir, hipCenter.y + 64f * scale)

            racketHand = Offset(cx + 32f * scale * dir, shoulderCenter.y - 36f * scale)
            supportHand = Offset(cx - 38f * scale * dir, shoulderCenter.y + 10f * scale)
        }

        StancePoseType.TUCK_JUMP_PEAK -> {
            val jumpLift = 38f * scale + bounceOffset
            hipCenter = Offset(cx, floorY - 94f * scale - jumpLift)
            shoulderCenter = Offset(cx, hipCenter.y - 56f * scale)
            headCenter = Offset(cx, shoulderCenter.y - 25f * scale)

            // Knees tucked high toward chest
            racketKnee = Offset(cx + 26f * scale * dir, hipCenter.y - 10f * scale)
            racketFoot = Offset(cx + 22f * scale * dir, hipCenter.y + 28f * scale)

            supportKnee = Offset(cx - 26f * scale * dir, hipCenter.y - 10f * scale)
            supportFoot = Offset(cx - 22f * scale * dir, hipCenter.y + 28f * scale)

            racketHand = Offset(cx + 44f * scale * dir, shoulderCenter.y - 18f * scale)
            supportHand = Offset(cx - 44f * scale * dir, shoulderCenter.y - 18f * scale)
        }

        StancePoseType.LATERAL_SKATER_STICK -> {
            hipCenter = Offset(cx + 24f * scale * dir, floorY - 66f * scale)
            shoulderCenter = Offset(cx + 28f * scale * dir, hipCenter.y - 54f * scale)
            headCenter = Offset(shoulderCenter.x, shoulderCenter.y - 24f * scale)

            // Single-leg outside stick on Racket leg
            racketKnee = Offset(cx + 38f * scale * dir, floorY - 34f * scale)
            racketFoot = Offset(cx + 38f * scale * dir, floorY)

            // Counter-balanced airborne support leg
            supportKnee = Offset(cx - 12f * scale * dir, floorY - 38f * scale)
            supportFoot = Offset(cx - 44f * scale * dir, floorY - 26f * scale)

            racketHand = Offset(cx + 58f * scale * dir, shoulderCenter.y + 4f * scale)
            supportHand = Offset(cx - 24f * scale * dir, shoulderCenter.y + 12f * scale)
        }
    }

    val boneStroke = 6.5f

    // 1. Draw Support Leg (Fast-Feet Cyan #00F0FF)
    drawLine(
        color = ShuttlePulseColors.FastFeetCyan,
        start = hipCenter,
        end = supportKnee,
        strokeWidth = boneStroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = ShuttlePulseColors.FastFeetCyan,
        start = supportKnee,
        end = supportFoot,
        strokeWidth = boneStroke,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = ShuttlePulseColors.FastFeetCyan,
        radius = 8f,
        center = supportFoot
    )

    // 2. Draw Spine & Head (Crisp White)
    drawLine(
        color = ShuttlePulseColors.TextCrispWhite,
        start = hipCenter,
        end = shoulderCenter,
        strokeWidth = boneStroke + 1.5f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = ShuttlePulseColors.TextCrispWhite,
        radius = 15f * scale,
        center = headCenter,
        style = Stroke(width = 5f)
    )

    // 3. Draw Support Arm (Cyan)
    drawLine(
        color = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.85f),
        start = shoulderCenter,
        end = supportHand,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    // 4. Draw Racket Leg (Volt Plyo Lime #D4FF00)
    drawLine(
        color = ShuttlePulseColors.VoltPlyoLime,
        start = hipCenter,
        end = racketKnee,
        strokeWidth = boneStroke + 1f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = ShuttlePulseColors.VoltPlyoLime,
        start = racketKnee,
        end = racketFoot,
        strokeWidth = boneStroke + 1f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = ShuttlePulseColors.VoltPlyoLime,
        radius = 9.5f,
        center = racketFoot
    )

    // 5. Draw Racket Arm + Badminton Racket Silhouette (Volt Lime)
    drawLine(
        color = ShuttlePulseColors.VoltPlyoLime,
        start = shoulderCenter,
        end = racketHand,
        strokeWidth = 5.5f,
        cap = StrokeCap.Round
    )
    val racketHeadCenter = Offset(
        x = racketHand.x + 22f * scale * dir,
        y = racketHand.y - 22f * scale
    )
    drawLine(
        color = ShuttlePulseColors.VoltPlyoLime,
        start = racketHand,
        end = Offset(racketHand.x + 12f * scale * dir, racketHand.y - 12f * scale),
        strokeWidth = 3.5f
    )
    drawCircle(
        color = ShuttlePulseColors.VoltPlyoLime,
        radius = 13f * scale,
        center = racketHeadCenter,
        style = Stroke(width = 3.5f)
    )
}

@Composable
fun ComboBeatProgressBar(
    currentStep: Int,
    totalSteps: Int,
    isExplosiveBeat: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
    ) {
        val count = totalSteps.coerceIn(2, 10)
        val gap = 10f
        val barWidth = (size.width - gap * (count - 1)) / count.toFloat()

        for (i in 1..count) {
            val isLastExplodeSlot = (i == count)
            val isActive = (i <= currentStep)
            val slotColor = when {
                isLastExplodeSlot && isExplosiveBeat -> ShuttlePulseColors.ExplosiveBurstCrimson
                isLastExplodeSlot -> ShuttlePulseColors.ExplosiveBurstCrimson.copy(alpha = 0.28f)
                isActive -> ShuttlePulseColors.VoltPlyoLime
                else -> ShuttlePulseColors.GymCardElevated
            }
            drawRoundRect(
                color = slotColor,
                topLeft = Offset((i - 1) * (barWidth + gap), 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }
    }
}
