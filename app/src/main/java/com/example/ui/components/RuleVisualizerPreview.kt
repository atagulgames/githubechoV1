package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.model.VisualPreviewType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RuleVisualizerPreview(
    previewType: VisualPreviewType,
    accentColor: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rule_preview_anim")

    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_progress"
    )

    val pingPong by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ping_pong"
    )

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val borderColor = accentColor.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            when (previewType) {
                VisualPreviewType.ORBITAL_SHIFTING_NODES -> {
                    // Draw elliptical orbits
                    drawOval(
                        color = accentColor.copy(alpha = 0.2f),
                        topLeft = Offset(cx - 70f, cy - 36f),
                        size = Size(140f, 72f),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )

                    // 3 moving nodes on orbit
                    for (i in 0..2) {
                        val angle = (animProgress * 2 * PI + i * (2 * PI / 3f)).toFloat()
                        val nx = cx + cos(angle) * 70f
                        val ny = cy + sin(angle) * 36f

                        // Orbit line to center
                        drawLine(
                            color = accentColor.copy(alpha = 0.3f),
                            start = Offset(cx, cy),
                            end = Offset(nx, ny),
                            strokeWidth = 1f
                        )

                        // Outer halo
                        drawCircle(
                            color = accentColor.copy(alpha = 0.25f),
                            radius = 16f,
                            center = Offset(nx, ny)
                        )
                        // Core node
                        drawCircle(
                            color = accentColor,
                            radius = 8f,
                            center = Offset(nx, ny)
                        )
                    }
                    // Center core
                    drawCircle(color = Color.White, radius = 5f, center = Offset(cx, cy))
                }

                VisualPreviewType.INVISIBLE_LINES_FADE -> {
                    val p1 = Offset(w * 0.2f, cy)
                    val p2 = Offset(w * 0.8f, cy)

                    // Fade alpha: visible -> fading -> near invisible -> reappears
                    val fadeAlpha = ((1f - pingPong) * 0.9f + 0.08f).coerceIn(0.08f, 1f)

                    // Connected stroke with fade
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(accentColor.copy(alpha = fadeAlpha), Color(0xFF38BDF8).copy(alpha = fadeAlpha))
                        ),
                        start = p1,
                        end = p2,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    // Stealth particles along the line
                    for (k in 1..4) {
                        val px = p1.x + (p2.x - p1.x) * (k / 5f)
                        val sparkAlpha = (pingPong * 0.7f).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color.White.copy(alpha = sparkAlpha),
                            radius = 3f,
                            center = Offset(px, cy + sin(animProgress * 2 * PI + k).toFloat() * 6f)
                        )
                    }

                    // Two anchored nodes
                    listOf(p1, p2).forEachIndexed { idx, pt ->
                        drawCircle(color = accentColor.copy(alpha = 0.3f), radius = 18f, center = pt)
                        drawCircle(color = accentColor, radius = 10f, center = pt)
                        drawCircle(color = Color.White, radius = 4f, center = pt)
                    }
                }

                VisualPreviewType.DECAYING_RING -> {
                    val nodeCenter = Offset(cx, cy)
                    val maxRadius = 34f
                    val sweep = (1f - animProgress) * 360f

                    // Warning ring background
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.15f),
                        radius = maxRadius,
                        center = nodeCenter
                    )

                    // Depleting ring arc
                    drawArc(
                        color = if (animProgress > 0.65f) Color(0xFFEF4444) else Color(0xFFF59E0B),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(nodeCenter.x - maxRadius, nodeCenter.y - maxRadius),
                        size = Size(maxRadius * 2, maxRadius * 2),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Central node core
                    drawCircle(
                        color = Color(0xFFEC4899),
                        radius = 14f,
                        center = nodeCenter
                    )
                    drawCircle(color = Color.White, radius = 5f, center = nodeCenter)
                }

                VisualPreviewType.WANDERING_ECHO_DRIFT -> {
                    // Safe nodes
                    val n1 = Offset(w * 0.25f, cy - 25f)
                    val n2 = Offset(w * 0.75f, cy + 25f)
                    drawLine(color = Color(0xFF0284C7).copy(alpha = 0.4f), start = n1, end = n2, strokeWidth = 2f)
                    listOf(n1, n2).forEach {
                        drawCircle(color = Color(0xFF0284C7), radius = 8f, center = it)
                    }

                    // Floating red echo beam moving up and down
                    val driftY = cy + sin(animProgress * 2 * PI).toFloat() * 24f
                    val echoStart = Offset(w * 0.35f, driftY - 12f)
                    val echoEnd = Offset(w * 0.65f, driftY + 12f)

                    // Crimson hazard glow
                    drawLine(
                        color = Color(0xFFEF4444).copy(alpha = 0.35f),
                        start = echoStart,
                        end = echoEnd,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFDC2626),
                        start = echoStart,
                        end = echoEnd,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                VisualPreviewType.PHANTOM_DECOY -> {
                    // Real node on left (stable, radiant)
                    val realPos = Offset(w * 0.32f, cy)
                    drawCircle(
                        color = Color(0xFF14B8A6).copy(alpha = 0.3f),
                        radius = 22f + pingPong * 4f,
                        center = realPos
                    )
                    drawCircle(color = Color(0xFF14B8A6), radius = 12f, center = realPos)
                    drawCircle(color = Color.White, radius = 4f, center = realPos)

                    // Decoy node on right (jittery, dashed illusion)
                    val jitterX = sin(animProgress * 12 * PI).toFloat() * 3f
                    val decoyPos = Offset(w * 0.68f + jitterX, cy)
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                        radius = 20f,
                        center = decoyPos,
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                    )
                    drawCircle(
                        color = Color(0xFFD97706).copy(alpha = 0.7f),
                        radius = 10f,
                        center = decoyPos,
                        style = Stroke(width = 2f)
                    )
                }

                VisualPreviewType.VECTOR_REVERSE_FLOW -> {
                    val start = Offset(w * 0.22f, cy)
                    val end = Offset(w * 0.78f, cy)

                    // Main vector path line
                    drawLine(
                        color = Color(0xFF2563EB).copy(alpha = 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = 3f
                    )

                    // Moving vector chevrons along line
                    for (k in 0..2) {
                        val t = (animProgress + k / 3f) % 1f
                        val arrowX = start.x + (end.x - start.x) * t
                        val arrowPath = Path().apply {
                            moveTo(arrowX - 8f, cy - 8f)
                            lineTo(arrowX + 4f, cy)
                            lineTo(arrowX - 8f, cy + 8f)
                        }
                        drawPath(
                            path = arrowPath,
                            color = Color(0xFF60A5FA),
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                        )
                    }

                    // Nodes
                    drawCircle(color = Color(0xFF2563EB), radius = 9f, center = start)
                    drawCircle(color = Color(0xFF2563EB), radius = 9f, center = end)
                }

                VisualPreviewType.ROTATING_CANVAS -> {
                    val angle = animProgress * 360f

                    rotate(degrees = angle, pivot = Offset(cx, cy)) {
                        // Rotating square mesh
                        drawRect(
                            color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                            topLeft = Offset(cx - 32f, cy - 32f),
                            size = Size(64f, 64f),
                            style = Stroke(width = 2f)
                        )

                        // 4 rotating corner nodes
                        val corners = listOf(
                            Offset(cx - 32f, cy - 32f),
                            Offset(cx + 32f, cy - 32f),
                            Offset(cx + 32f, cy + 32f),
                            Offset(cx - 32f, cy + 32f)
                        )
                        corners.forEach {
                            drawCircle(color = Color(0xFFF59E0B), radius = 6f, center = it)
                        }
                    }

                    // Center anchor
                    drawCircle(color = Color.White, radius = 4f, center = Offset(cx, cy))
                }

                VisualPreviewType.DUAL_PORTAL -> {
                    val keyPt = Offset(w * 0.28f, cy)
                    val gatePt = Offset(w * 0.72f, cy)

                    // Inter-dimensional bridge
                    drawLine(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.4f),
                        start = keyPt,
                        end = gatePt,
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    // Dimension Alpha Key (Golden)
                    drawCircle(color = Color(0xFFF59E0B).copy(alpha = 0.3f), radius = 18f, center = keyPt)
                    drawCircle(color = Color(0xFFF59E0B), radius = 10f, center = keyPt)
                    drawCircle(color = Color.White, radius = 4f, center = keyPt)

                    // Dimension Beta Gate (Violet)
                    drawCircle(color = Color(0xFFA855F7).copy(alpha = 0.3f), radius = 22f, center = gatePt)
                    drawRect(
                        color = Color(0xFFA855F7),
                        topLeft = Offset(gatePt.x - 10f, gatePt.y - 10f),
                        size = Size(20f, 20f),
                        style = Stroke(width = 3f)
                    )
                }

                VisualPreviewType.CUMULATIVE_GHOST_TRAIL -> {
                    // Cumulative ghost trails waving
                    for (layer in 0..2) {
                        val path = Path()
                        val yOffset = (layer - 1) * 16f
                        path.moveTo(w * 0.15f, cy + yOffset)
                        path.cubicTo(
                            w * 0.38f, cy + yOffset - 22f * pingPong,
                            w * 0.62f, cy + yOffset + 22f * pingPong,
                            w * 0.85f, cy + yOffset
                        )
                        drawPath(
                            path = path,
                            color = Color(0xFFA855F7).copy(alpha = 0.35f + layer * 0.15f),
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )
                    }

                    // Wisps
                    val wispX = w * 0.15f + (w * 0.7f) * animProgress
                    val wispY = cy + sin(animProgress * 4 * PI).toFloat() * 12f
                    drawCircle(color = Color(0xFFE879F9), radius = 5f, center = Offset(wispX, wispY))
                }

                VisualPreviewType.OMEGA_NEXUS, VisualPreviewType.PULSING_LIVING_WEB, VisualPreviewType.COMPLEX_LATTICE -> {
                    // Living pulsing web
                    val points = listOf(
                        Offset(w * 0.22f, cy + 18f),
                        Offset(cx, cy - 22f),
                        Offset(w * 0.78f, cy + 18f)
                    )

                    // Elastic tension undulation
                    val waveY = sin(animProgress * 2 * PI).toFloat() * 8f
                    val mid = Offset(cx, cy - 22f + waveY)

                    val webPath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        quadraticTo(mid.x, mid.y, points[2].x, points[2].y)
                    }

                    drawPath(
                        path = webPath,
                        brush = Brush.linearGradient(
                            listOf(accentColor, Color(0xFF38BDF8))
                        ),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Living web nodes
                    points.forEachIndexed { i, pt ->
                        val p = if (i == 1) mid else pt
                        drawCircle(color = accentColor.copy(alpha = 0.3f), radius = 16f, center = p)
                        drawCircle(color = accentColor, radius = 9f, center = p)
                        drawCircle(color = Color.White, radius = 3.5f, center = p)
                    }
                }
            }
        }
    }
}
