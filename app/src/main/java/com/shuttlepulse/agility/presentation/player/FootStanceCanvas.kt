package com.shuttlepulse.agility.presentation.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
 * Top-down Foot-Stance Canvas synchronized with [cadencePhase] across all badminton stances:
 * - Parallel Pogo Bounce ([StancePoseType.DOUBLE_LEG_POGO], [StancePoseType.TUCK_JUMP_PEAK])
 * - Wide Alternating Fast Tapping ([StancePoseType.WIDE_BASE_FAST_TAP])
 * - Staggered Scissor Switch ([StancePoseType.SPLIT_STANCE_RACKET_LEAD], [StancePoseType.SPLIT_STANCE_NON_RACKET_LEAD], [StancePoseType.SCISSOR_KICK_ROTATION])
 * - Asymmetric Racket-Leg Lunge ([StancePoseType.DEEP_RACKET_LUNGE], [StancePoseType.LATERAL_SKATER_STICK])
 */
@Composable
fun FootStanceCanvas(
    pose: StancePoseType,
    dominantHand: DominantHand,
    cadencePhase: Float,
    isExplosiveTrigger: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val racketColor = if (isExplosiveTrigger) {
        ShuttlePulseColors.ExplosiveBurstCrimson
    } else {
        ShuttlePulseColors.VoltPlyoLime
    }

    val rLabel = androidx.compose.runtime.remember(textMeasurer, racketColor) {
        textMeasurer.measure(
            "RACKET",
            TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = racketColor
            )
        )
    }

    val sLabel = androidx.compose.runtime.remember(textMeasurer) {
        textMeasurer.measure(
            "SUPPORT",
            TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = ShuttlePulseColors.FastFeetCyan
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(126.dp)
            .testTag("foot_stance_canvas")
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.54f
        val dir = if (dominantHand == DominantHand.RIGHT) 1f else -1f
        val wave = sin(cadencePhase * PI.toFloat())
        val altWave = sin(cadencePhase * 2f * PI.toFloat())

        // Court Base Crosshairs
        drawRoundRect(
            color = ShuttlePulseColors.GymCardElevated.copy(alpha = 0.7f),
            topLeft = Offset(12f, 8f),
            size = Size(w - 24f, h - 16f),
            cornerRadius = CornerRadius(14f, 14f),
            style = Stroke(width = 2f)
        )
        drawLine(
            color = ShuttlePulseColors.GymBorderSubtle,
            start = Offset(cx, 12f),
            end = Offset(cx, h - 12f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )
        drawLine(
            color = ShuttlePulseColors.GymBorderSubtle,
            start = Offset(24f, cy),
            end = Offset(w - 24f, cy),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )

        val racketFootCenter: Offset
        val supportFootCenter: Offset

        when (pose) {
            StancePoseType.DOUBLE_LEG_POGO,
            StancePoseType.TUCK_JUMP_PEAK -> {
                val spread = w * 0.13f
                val bounceY = wave * 8f
                racketFootCenter = Offset(cx + spread * dir, cy - bounceY)
                supportFootCenter = Offset(cx - spread * dir, cy - bounceY)
            }

            StancePoseType.WIDE_BASE_FAST_TAP -> {
                val wideSpread = w * 0.24f
                val tapDelta = altWave * 9f
                racketFootCenter = Offset(cx + wideSpread * dir, cy - tapDelta - 6f)
                supportFootCenter = Offset(cx - wideSpread * dir, cy + tapDelta + 6f)
            }

            StancePoseType.SPLIT_STANCE_RACKET_LEAD,
            StancePoseType.SCISSOR_KICK_ROTATION -> {
                val spread = w * 0.14f
                val stagger = h * 0.21f
                racketFootCenter = Offset(cx + spread * dir, cy - stagger - wave * 5f)
                supportFootCenter = Offset(cx - spread * dir, cy + stagger - wave * 5f)
            }

            StancePoseType.SPLIT_STANCE_NON_RACKET_LEAD -> {
                val spread = w * 0.14f
                val stagger = h * 0.21f
                racketFootCenter = Offset(cx + spread * dir, cy + stagger - wave * 5f)
                supportFootCenter = Offset(cx - spread * dir, cy - stagger - wave * 5f)
            }

            StancePoseType.DEEP_RACKET_LUNGE,
            StancePoseType.LATERAL_SKATER_STICK -> {
                val spread = w * 0.22f
                val lungeReach = h * 0.26f
                racketFootCenter = Offset(cx + spread * dir, cy - lungeReach)
                supportFootCenter = Offset(cx - spread * dir, cy + lungeReach * 0.85f)
            }
        }

        val footW = 36f
        val footH = 54f

        // Draw Support Foot (Cyan)
        drawCircle(
            color = ShuttlePulseColors.FastFeetCyan.copy(alpha = 0.18f + wave * 0.18f),
            radius = 34f + wave * 10f,
            center = supportFootCenter
        )
        drawRoundRect(
            color = ShuttlePulseColors.FastFeetCyan,
            topLeft = Offset(supportFootCenter.x - footW / 2f, supportFootCenter.y - footH / 2f),
            size = Size(footW, footH),
            cornerRadius = CornerRadius(16f, 16f)
        )
        // Forefoot pressure pad highlight
        drawCircle(
            color = ShuttlePulseColors.OledGymBlack,
            radius = 9f,
            center = Offset(supportFootCenter.x, supportFootCenter.y - 11f)
        )

        // Draw Racket Foot (Volt Lime or Burst Crimson)
        drawCircle(
            color = racketColor.copy(alpha = 0.22f + wave * 0.22f),
            radius = 38f + wave * 12f,
            center = racketFootCenter
        )
        drawRoundRect(
            color = racketColor,
            topLeft = Offset(racketFootCenter.x - footW / 2f, racketFootCenter.y - footH / 2f),
            size = Size(footW, footH),
            cornerRadius = CornerRadius(16f, 16f)
        )
        drawCircle(
            color = ShuttlePulseColors.OledGymBlack,
            radius = 9f,
            center = Offset(racketFootCenter.x, racketFootCenter.y - 11f)
        )

        // Labels "R-FOOT" and "S-FOOT"
        drawText(
            textLayoutResult = rLabel,
            topLeft = Offset(
                racketFootCenter.x - rLabel.size.width / 2f,
                racketFootCenter.y + footH / 2f + 2f
            )
        )

        drawText(
            textLayoutResult = sLabel,
            topLeft = Offset(
                supportFootCenter.x - sLabel.size.width / 2f,
                supportFootCenter.y + footH / 2f + 2f
            )
        )
    }
}
