package com.lockguard.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lockguard.app.ui.theme.ElectricBlue
import com.lockguard.app.ui.theme.ElectricCyan
import com.lockguard.app.ui.theme.Navy800
import com.lockguard.app.ui.theme.Navy900

/**
 * Minimalist shield combined with camera-eye symbol logo.
 * Features an animated glowing iris pulse.
 */
@Composable
fun ShieldLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    isAnimated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val width = size.toPx()
            val height = size.toPx()

            // 1. Draw outer minimalist shield
            val shieldPath = Path().apply {
                moveTo(width * 0.5f, height * 0.12f)
                cubicTo(
                    width * 0.68f, height * 0.12f,
                    width * 0.88f, height * 0.18f,
                    width * 0.88f, height * 0.18f
                )
                cubicTo(
                    width * 0.88f, height * 0.48f,
                    width * 0.82f, height * 0.78f,
                    width * 0.5f, height * 0.92f
                )
                cubicTo(
                    width * 0.18f, height * 0.78f,
                    width * 0.12f, height * 0.48f,
                    width * 0.12f, height * 0.18f
                )
                cubicTo(
                    width * 0.12f, height * 0.18f,
                    width * 0.32f, height * 0.12f,
                    width * 0.5f, height * 0.12f
                )
                close()
            }

            // Shield gradient background
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Navy800, Navy900)
                )
            )

            // Shield electric outline
            drawPath(
                path = shieldPath,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricCyan, ElectricBlue)
                ),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 2. Camera-Eye Outer Aperture Ring
            val eyeCenter = Offset(width * 0.5f, height * 0.48f)
            val outerRadius = width * 0.20f

            drawCircle(
                color = Navy900,
                radius = outerRadius,
                center = eyeCenter
            )

            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(ElectricCyan, ElectricBlue, ElectricCyan),
                    center = eyeCenter
                ),
                radius = outerRadius,
                center = eyeCenter,
                style = Stroke(width = 3.dp.toPx())
            )

            // 3. Camera Pupil & Iris with Pulse
            val irisRadius = width * 0.12f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricCyan, ElectricBlue),
                    center = eyeCenter,
                    radius = irisRadius
                ),
                radius = irisRadius,
                center = eyeCenter
            )

            // Optical Center Glow
            drawCircle(
                color = Color.White,
                radius = width * 0.035f,
                center = Offset(eyeCenter.x + width * 0.03f, eyeCenter.y - height * 0.03f)
            )
        }
    }
}
