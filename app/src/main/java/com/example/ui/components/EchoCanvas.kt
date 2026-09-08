package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import com.example.model.CandyCallout
import com.example.model.CandyParticle
import com.example.model.DirectedEdge
import com.example.model.EchoStroke
import com.example.model.EchoTheme
import com.example.model.EchoBeastState
import com.example.model.GameStatus
import com.example.model.Node
import com.example.model.NodeType
import com.example.model.Point
import com.example.model.Segment
import com.example.model.StrokeTheme
import com.example.viewmodel.EchoUiState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private val ColorGridDot = Color(0x3394A3B8)
private val ColorHintGold = Color(0xFFD97706)
private val ColorKeyGold = Color(0xFFD97706)
private val ColorGateLocked = Color(0xFFE11D48)
private val ColorGateUnlocked = Color(0xFF059669)

@Composable
fun EchoCanvas(
    state: EchoUiState,
    onPointerDown: (Point) -> Unit,
    onPointerMove: (Point) -> Unit,
    onPointerUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EchoPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val infiniteTime = rememberInfiniteTransition(label = "EchoTime")
    val timeSec by infiniteTime.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeSec"
    )

    // Shake offset if collision alert is active
    val shakeX = if (state.isCollisionAlertActive) remember(state.isCollisionAlertActive) {
        (Random.nextInt(-14, 14)).dp
    } else 0.dp
    val shakeY = if (state.isCollisionAlertActive) remember(state.isCollisionAlertActive) {
        (Random.nextInt(-14, 14)).dp
    } else 0.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .offset(x = shakeX, y = shakeY)
            .testTag("echo_canvas_container")
    ) {
        val canvasWidth = constraints.maxWidth.toFloat().coerceAtLeast(100f)
        val canvasHeight = constraints.maxHeight.toFloat().coerceAtLeast(100f)

        // Dynamic auto-scaling: compute actual bounds of level nodes so puzzle
        // automatically scales up or down and centers perfectly on ANY device
        val nodes = state.nodes
        val minX = nodes.minOfOrNull { it.x } ?: 60f
        val maxX = nodes.maxOfOrNull { it.x } ?: 300f
        val minY = nodes.minOfOrNull { it.y } ?: 80f
        val maxY = nodes.maxOfOrNull { it.y } ?: 340f

        // Generous margins so outer node rings, numbers, and "KARAKTER" label never clip
        val padX = 52f
        val padY = 64f

        val contentW = (maxX - minX + padX * 2f).coerceAtLeast(160f)
        val contentH = (maxY - minY + padY * 2f).coerceAtLeast(160f)

        val scale = min(canvasWidth / contentW, canvasHeight / contentH).coerceIn(0.4f, 4.0f)

        val contentCenterX = (minX + maxX) / 2f
        val contentCenterY = (minY + maxY) / 2f

        val offsetX = canvasWidth / 2f - contentCenterX * scale
        val offsetY = canvasHeight / 2f - contentCenterY * scale

        fun toVirtual(screen: Offset): Point {
            val vx = (screen.x - offsetX) / scale
            val vy = (screen.y - offsetY) / scale
            return Point(vx, vy)
        }

        fun toScreen(vPoint: Point): Offset {
            return Offset(vPoint.x * scale + offsetX, vPoint.y * scale + offsetY)
        }

        // Cache background grid points to avoid thousands of draw calls per frame
        val gridPoints = remember(canvasWidth, canvasHeight) {
            val points = ArrayList<Offset>(600)
            val step = 44f
            var x = step / 2f
            while (x < canvasWidth) {
                var y = step / 2f
                while (y < canvasHeight) {
                    points.add(Offset(x, y))
                    y += step
                }
                x += step
            }
            points
        }

        val nodeTextPaint = remember {
            android.graphics.Paint().apply {
                isAntiAlias = true
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
        }

        val isRotatingTier = state.level.mechanicType in listOf("ROTATING_WEB", "OMEGA_SYNTHESIS")
        val rotationAngle = if (isRotatingTier) sin(timeSec * 0.75f) * 12f else 0f

        fun unrotate(pos: Offset): Offset {
            if (rotationAngle == 0f) return pos
            val rad = Math.toRadians((-rotationAngle).toDouble())
            val cosA = cos(rad).toFloat()
            val sinA = sin(rad).toFloat()
            val cx = canvasWidth / 2f
            val cy = canvasHeight / 2f
            val dx = pos.x - cx
            val dy = pos.y - cy
            return Offset(cx + dx * cosA - dy * sinA, cy + dx * sinA + dy * cosA)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("echo_canvas")
                .pointerInput(state.gameStatus, scale, offsetX, offsetY, rotationAngle, canvasWidth, canvasHeight) {
                    if (state.gameStatus == GameStatus.PLAYING) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            onPointerDown(toVirtual(unrotate(down.position)))
                            down.consume()

                            do {
                                val event = awaitPointerEvent()
                                val current = event.changes.firstOrNull()
                                if (current != null) {
                                    if (current.pressed) {
                                        onPointerMove(toVirtual(unrotate(current.position)))
                                        current.consume()
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            onPointerUp()
                        }
                    }
                }
        ) {
            withTransform({
                if (rotationAngle != 0f) {
                    rotate(degrees = rotationAngle, pivot = center)
                }
            }) {
                // 1. Grid Background (Single GPU drawPoints instruction for buttery 60+ FPS)
                drawGridBackground(size.width, size.height, state.isDarkTheme, gridPoints)

                // 1.5. Living Web (Yaşayan Ağ) - harmonic breathing connective threads & player tension
                drawLivingWebMesh(
                    nodes = state.nodes,
                    visitedNodeIds = state.visitedNodeIds,
                    currentPointerPos = state.currentPointerPos,
                    adaptiveTendencyQuadrant = state.adaptiveTendencyQuadrant,
                    timeSec = timeSec,
                    isDarkTheme = state.isDarkTheme,
                    toScreen = ::toScreen,
                    scale = scale
                )

                // 1.8. Past Ghost Stroke Labyrinth (Geçmişin Hayaletleri - Yaşayan Labirent)
                drawPastGhostLabyrinth(
                    ghosts = state.pastGhostStrokes,
                    mechanicType = state.level.mechanicType,
                    timeSec = timeSec,
                    toScreen = ::toScreen,
                    scale = scale
                )

                // 1.9. Perfect Echo Ghost Race (Kusursuz Hayaletle Canlı Yarış)
                if (state.isGhostRaceAvailable && state.isGhostRaceActive && state.ghostReplayPoints.isNotEmpty()) {
                    drawPerfectGhostRace(
                        replayPoints = state.ghostReplayPoints,
                        runnerPoint = state.ghostRunnerCurrentPoint,
                        timeSec = timeSec,
                        toScreen = ::toScreen,
                        scale = scale
                    )
                }

                // 2. Directed edge arrows (if level has one-way edges)
                drawDirectedEdgeArrows(
                    edges = state.level.directedEdges,
                    nodes = state.nodes,
                    toScreen = ::toScreen,
                    scale = scale
                )

                // 3. Hint path
                if (state.isHintActive && state.level.hintOrder.isNotEmpty()) {
                    drawHintPath(
                        hintOrder = state.level.hintOrder,
                        nodes = state.nodes,
                        toScreen = ::toScreen,
                        scale = scale
                    )
                }

                // 4. Past Echo Barriers (Collision Obstacles with sway on 3-5 errors & wandering echoes)
                drawEchoBarriers(
                    echoes = state.echoes,
                    echoTheme = state.echoTheme,
                    isShrinkerActive = state.isEchoShrinkerActive,
                    beastState = state.echoBeastState,
                    timeSec = timeSec,
                    mechanicType = state.level.mechanicType,
                    pulseAlpha = pulseAlpha,
                    toScreen = ::toScreen,
                    scale = scale
                )

                // 5. Active Player Stroke with Stroke Theme (and vibrating older rays)
                drawPlayerStroke(
                    segments = state.currentStrokeSegments,
                    currentPointerPos = state.currentPointerPos,
                    visitedNodeIds = state.visitedNodeIds,
                    nodes = state.nodes,
                    strokeTheme = state.strokeTheme,
                    mechanicType = state.level.mechanicType,
                    timeSec = timeSec,
                    toScreen = ::toScreen,
                    scale = scale
                )

                // 6. Game Nodes (with Normal, Key, Gate styles, Character Node 1, Floating Nodes & Dual Entangled)
                drawNodes(
                    nodes = state.nodes,
                    visitedNodeIds = state.visitedNodeIds,
                    collectedKeyIds = state.collectedKeyIds,
                    strokeTheme = state.strokeTheme,
                    isHintActive = state.isHintActive,
                    hintOrder = state.level.hintOrder,
                    pulseAlpha = pulseAlpha,
                    isDrawing = state.isDrawing,
                    isDarkTheme = state.isDarkTheme,
                    mechanicType = state.level.mechanicType,
                    remainingSec = state.levelRemainingTimeSec,
                    timeSec = timeSec,
                    toScreen = ::toScreen,
                    scale = scale,
                    textPaint = nodeTextPaint
                )

                // 6.5. Echo Beast Corruption Aura & DOMINION Mimicry (Yankı Canavarı Atmosferi)
                drawEchoBeastAtmosphere(
                    beastState = state.echoBeastState,
                    timeSec = timeSec,
                    size = size,
                    isDrawing = state.isDrawing,
                    segments = state.currentStrokeSegments,
                    toScreen = ::toScreen,
                    scale = scale
                )

            // 7. Proximity Warning / Electric Glitch
            if (state.isProximityAlertActive) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x33FF2E63), Color.Transparent),
                        center = center,
                        radius = size.width * 0.9f
                    )
                )
            }

            // 8. Violent Collision Flash
            if (state.isCollisionAlertActive) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x99FF2E63), Color(0x33FF2E63), Color.Transparent),
                        center = center,
                        radius = size.width * 0.75f
                    )
                )
            }

            // 9. Candy Crush Style Explosion Particles (Animated & naturally fade out)
            if (state.candyParticles.isNotEmpty()) {
                val now = System.currentTimeMillis()
                state.candyParticles.forEach { p ->
                    val elapsed = (now - p.createdAt).coerceAtLeast(0L)
                    if (elapsed < 1100L) {
                        val t = elapsed / 1000f
                        val currentX = p.x + (p.vx * t * 18f)
                        val currentY = p.y + (p.vy * t * 18f) + (t * t * 25f) // subtle gravity
                        val screenPos = toScreen(Point(currentX, currentY))
                        val currentAlpha = ((1.0f - (elapsed / 1100f)) * p.alpha).coerceIn(0f, 1f)
                        val pColor = p.color.copy(alpha = currentAlpha)

                        if (p.isStar) {
                            drawCircle(
                                color = pColor,
                                radius = (p.size * scale * (1.1f - t * 0.4f)).coerceAtLeast(1f),
                                center = screenPos
                            )
                            val starArm = (p.size * scale * (1.5f - t * 0.5f)).coerceAtLeast(1f)
                            drawLine(
                                color = pColor,
                                start = Offset(screenPos.x - starArm, screenPos.y),
                                end = Offset(screenPos.x + starArm, screenPos.y),
                                strokeWidth = 2.5f * scale
                            )
                            drawLine(
                                color = pColor,
                                start = Offset(screenPos.x, screenPos.y - starArm),
                                end = Offset(screenPos.x, screenPos.y + starArm),
                                strokeWidth = 2.5f * scale
                            )
                        } else {
                            drawCircle(
                                color = pColor,
                                radius = (p.size * scale * (0.9f - t * 0.3f)).coerceAtLeast(0.8f),
                                center = screenPos
                            )
                        }
                    }
                }
            }

            // 10. Candy Crush Pop Callout Text (Animated for 2 seconds, then slowly fades out)
            state.candyCallout?.let { callout ->
                val elapsed = (System.currentTimeMillis() - callout.createdAt).coerceAtLeast(0L)
                if (elapsed < 2900L) {
                    val animScale = when {
                        elapsed < 180L -> 0.6f + (elapsed / 180f) * 0.7f // pop up to 1.3f
                        elapsed < 320L -> 1.3f - ((elapsed - 180L) / 140f) * 0.2f // settle to 1.1f
                        else -> 1.1f + (sin((elapsed - 320L) / 250.0).toFloat() * 0.04f) // gentle pulse
                    }
                    val currentAlpha = when {
                        elapsed < 2000L -> 1.0f // stays for 2 seconds
                        else -> (1.0f - ((elapsed - 2000L) / 900f)).coerceIn(0f, 1f) // then fades out gradually
                    }
                    val upwardFloat = (elapsed / 1000f) * 14f * scale // gentle upward drift
                    val screenPos = toScreen(Point(callout.x, callout.y))
                    val adjustedY = screenPos.y - upwardFloat

                    if (currentAlpha > 0.01f) {
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            textSize = (22f * scale * callout.scale * animScale).coerceIn(16f, 44f)
                            textAlign = android.graphics.Paint.Align.CENTER
                            color = callout.color.copy(alpha = (callout.alpha * currentAlpha).coerceIn(0f, 1f)).toArgb()
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            setShadowLayer(10f * scale, 0f, 3f * scale, android.graphics.Color.BLACK)
                        }
                        val textWidth = paint.measureText(callout.text)
                        val halfW = textWidth / 2f + 16f
                        val clampedX = screenPos.x.coerceIn(halfW, size.width - halfW)
                        val clampedY = adjustedY.coerceIn(paint.textSize + 24f, size.height - 24f)
                        drawContext.canvas.nativeCanvas.drawText(
                            callout.text,
                            clampedX,
                            clampedY,
                            paint
                        )
                    }
                }
            }
        }
    }
}
}

private fun DrawScope.drawGridBackground(
    w: Float,
    h: Float,
    isDarkTheme: Boolean,
    gridPoints: List<Offset>
) {
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    drawRect(color = bgColor)
    val dotColor = if (isDarkTheme) Color(0x33475569) else Color(0x3394A3B8)
    drawPoints(
        points = gridPoints,
        pointMode = PointMode.Points,
        color = dotColor,
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDirectedEdgeArrows(
    edges: List<DirectedEdge>,
    nodes: List<Node>,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    for (edge in edges) {
        val fromNode = nodes.firstOrNull { it.id == edge.fromId }
        val toNode = nodes.firstOrNull { it.id == edge.toId }
        if (fromNode != null && toNode != null) {
            val p1 = toScreen(fromNode.toPoint())
            val p2 = toScreen(toNode.toPoint())

            // Midpoint
            val midX = (p1.x + p2.x) / 2f
            val midY = (p1.y + p2.y) / 2f

            val angle = atan2(p2.y - p1.y, p2.x - p1.x)
            val arrowSize = 12f * (scale / 1.5f).coerceAtLeast(1f)

            // Draw arrow head pointing toward p2
            val path = Path().apply {
                moveTo(midX, midY)
                lineTo(
                    (midX - arrowSize * cos(angle - Math.PI / 6)).toFloat(),
                    (midY - arrowSize * sin(angle - Math.PI / 6)).toFloat()
                )
                moveTo(midX, midY)
                lineTo(
                    (midX - arrowSize * cos(angle + Math.PI / 6)).toFloat(),
                    (midY - arrowSize * sin(angle + Math.PI / 6)).toFloat()
                )
            }
            drawPath(path, color = Color(0x9900F0FF), style = Stroke(width = 3f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawEchoBarriers(
    echoes: List<EchoStroke>,
    echoTheme: EchoTheme,
    isShrinkerActive: Boolean,
    beastState: EchoBeastState,
    timeSec: Float,
    mechanicType: String,
    pulseAlpha: Float,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val widthFactor = if (isShrinkerActive) 0.5f else 1.0f
    val isSwaying = beastState.level >= 2 || mechanicType in listOf("WANDERING_ECHOES", "OMEGA_SYNTHESIS")

    for (echo in echoes) {
        val lifeRatio = (echo.remainingAttempts.toFloat() / echo.maxLifetime).coerceIn(0.2f, 1f)
        val alpha = if (echo.isGhost) 0.20f else (lifeRatio * pulseAlpha)

        val glowColor = echoTheme.glowColor.copy(alpha = alpha * 0.5f)
        val barrierColor = echoTheme.echoColor.copy(alpha = alpha)

        val glowWidth = 14f * widthFactor * (scale / 1.5f).coerceAtLeast(1f)
        val coreWidth = 5f * widthFactor * (scale / 1.5f).coerceAtLeast(1f)

        // 3-5 errors (ENRAGED) or WANDERING_ECHOES: beams undulate and sway
        val swaySpeed = if (beastState.level >= 3) 3.2f else 2.0f
        val swayAmp = (if (beastState.level >= 3) 8.5f else 5.0f) * scale

        for (seg in echo.segments) {
            val rawStart = toScreen(seg.p1)
            val rawEnd = toScreen(seg.p2)

            val swayX = if (isSwaying) sin(timeSec * swaySpeed + echo.id * 1.4f) * swayAmp else 0f
            val swayY = if (isSwaying) cos(timeSec * (swaySpeed * 0.9f) + echo.id * 1.7f) * swayAmp else 0f

            val start = Offset(rawStart.x + swayX, rawStart.y + swayY)
            val end = Offset(rawEnd.x - swayX * 0.8f, rawEnd.y + swayY * 0.8f)

            // Neon glow aura
            drawLine(
                color = glowColor,
                start = start,
                end = end,
                strokeWidth = glowWidth,
                cap = StrokeCap.Round
            )

            // Core lethal laser
            drawLine(
                color = barrierColor,
                start = start,
                end = end,
                strokeWidth = coreWidth,
                cap = StrokeCap.Round
            )

            // Echo endpoint danger nodes
            drawCircle(
                color = barrierColor,
                radius = 3.5f * widthFactor * (scale / 1.5f).coerceAtLeast(1f),
                center = start
            )
            drawCircle(
                color = barrierColor,
                radius = 3.5f * widthFactor * (scale / 1.5f).coerceAtLeast(1f),
                center = end
            )
        }
    }
}

private fun DrawScope.drawPlayerStroke(
    segments: List<Segment>,
    currentPointerPos: Point?,
    visitedNodeIds: List<Int>,
    nodes: List<Node>,
    strokeTheme: StrokeTheme,
    mechanicType: String,
    timeSec: Float,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val glowWidth = 16f * (scale / 1.5f).coerceAtLeast(1f)
    val coreWidth = 6f * (scale / 1.5f).coerceAtLeast(1f)

    // Invisible Rays mechanic: pulse visibility of active beam
    val isInvisibleRays = mechanicType in listOf("INVISIBLE_RAYS", "OMEGA_SYNTHESIS")
    val rayAlpha = if (isInvisibleRays) (sin(timeSec * 3.8f) * 0.45f + 0.55f).coerceIn(0.12f, 1f) else 1f
    val currentGlowColor = strokeTheme.glowColor.copy(alpha = strokeTheme.glowColor.alpha * rayAlpha)
    val currentPrimaryColor = strokeTheme.primaryColor.copy(alpha = strokeTheme.primaryColor.alpha * rayAlpha)

    // Older rays vibrate under cosmic tension ("Eski ışınlar titreşsin")
    for ((idx, seg) in segments.withIndex()) {
        val start = toScreen(seg.p1)
        val end = toScreen(seg.p2)

        val ageRatio = ((segments.size - idx).toFloat() / segments.size.coerceAtLeast(1)).coerceIn(0.15f, 1f)
        val vibOffset = sin(timeSec * 28f + idx * 4.2f) * (ageRatio * 2.4f * scale)
        val dx = end.x - start.x
        val dy = end.y - start.y
        val len = hypot(dx, dy).coerceAtLeast(1f)
        val midX = (start.x + end.x) * 0.5f + (-dy / len) * vibOffset
        val midY = (start.y + end.y) * 0.5f + (dx / len) * vibOffset

        val segPath = Path().apply {
            moveTo(start.x, start.y)
            quadraticBezierTo(midX, midY, end.x, end.y)
        }

        drawPath(
            path = segPath,
            color = currentGlowColor,
            style = Stroke(width = glowWidth, cap = StrokeCap.Round)
        )
        drawPath(
            path = segPath,
            color = currentPrimaryColor,
            style = Stroke(width = coreWidth, cap = StrokeCap.Round)
        )
    }

    if (currentPointerPos != null && visitedNodeIds.isNotEmpty()) {
        val lastNodeId = visitedNodeIds.last()
        val lastNode = nodes.firstOrNull { it.id == lastNodeId }
        if (lastNode != null) {
            val start = toScreen(lastNode.toPoint())
            val end = toScreen(currentPointerPos)

            drawLine(
                color = currentGlowColor,
                start = start,
                end = end,
                strokeWidth = glowWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = currentPrimaryColor,
                start = start,
                end = end,
                strokeWidth = coreWidth,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = currentGlowColor,
                radius = 18f * (scale / 1.5f).coerceAtLeast(1f),
                center = end,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = currentPrimaryColor,
                radius = 5f * (scale / 1.5f).coerceAtLeast(1f),
                center = end
            )
        }
    }
}

private fun DrawScope.drawHintPath(
    hintOrder: List<Int>,
    nodes: List<Node>,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    for (i in 0 until hintOrder.size - 1) {
        val fromId = hintOrder[i]
        val toId = hintOrder[i + 1]
        val fromNode = nodes.firstOrNull { it.id == fromId }
        val toNode = nodes.firstOrNull { it.id == toId }
        if (fromNode != null && toNode != null) {
            val p1 = toScreen(fromNode.toPoint())
            val p2 = toScreen(toNode.toPoint())
            drawLine(
                color = ColorHintGold.copy(alpha = 0.65f),
                start = p1,
                end = p2,
                strokeWidth = 3f * (scale / 1.5f).coerceAtLeast(1f),
                pathEffect = dashEffect,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawNodes(
    nodes: List<Node>,
    visitedNodeIds: List<Int>,
    collectedKeyIds: Set<Int>,
    strokeTheme: StrokeTheme,
    isHintActive: Boolean,
    hintOrder: List<Int>,
    pulseAlpha: Float,
    isDrawing: Boolean,
    isDarkTheme: Boolean,
    mechanicType: String,
    remainingSec: Int,
    timeSec: Float,
    toScreen: (Point) -> Offset,
    scale: Float,
    textPaint: android.graphics.Paint
) {
    val nodeDensityFactor = if (nodes.size >= 28) 0.65f else if (nodes.size >= 16) 0.82f else 1.0f
    val baseRadius = 20f * (scale / 1.5f).coerceAtLeast(1f) * nodeDensityFactor

    val isFloatingTier = mechanicType in listOf("FLOATING_NODES", "OMEGA_SYNTHESIS")
    val isDecayingTier = mechanicType in listOf("DECAYING_NODES", "OMEGA_SYNTHESIS")
    val isDecoyTier = mechanicType in listOf("DECOY_TARGETS", "OMEGA_SYNTHESIS")
    val isDualEntangledTier = mechanicType in listOf("DUAL_ENTANGLED_WEB", "OMEGA_SYNTHESIS")

    for (node in nodes) {
        val basePos = toScreen(node.toPoint())
        val isVisited = visitedNodeIds.contains(node.id)

        // Living Web node shifting ("Bazı düğümler yer değiştirsin")
        val driftAmp = if (isFloatingTier) 8.5f * scale else 3.2f * scale
        val floatDx = if (!isVisited) sin(timeSec * 1.8f + node.id * 1.5f) * driftAmp else 0f
        val floatDy = if (!isVisited) cos(timeSec * 1.5f + node.id * 1.3f) * driftAmp else 0f
        val pos = Offset(basePos.x + floatDx, basePos.y + floatDy)

        val isCharacterStart = node.id == 1

        // Character starting node special glowing pulse & beacon
        if (isCharacterStart) {
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.28f * pulseAlpha),
                radius = baseRadius * (1.75f + 0.25f * (1f - pulseAlpha)),
                center = pos
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                radius = baseRadius * 1.35f,
                center = pos,
                style = Stroke(width = 3.5f)
            )

            // When idle before drawing, show "KARAKTER" tag above Node 1
            if (visitedNodeIds.isEmpty() && !isDrawing) {
                drawContext.canvas.nativeCanvas.apply {
                    val labelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#0284C7")
                        textSize = baseRadius * 0.75f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText("KARAKTER", pos.x, pos.y - baseRadius * 1.6f, labelPaint)
                }
            }
        }

        // Highlight next target node in the sequence (sıra numarası ve hedef rehberi)
        val lastVisited = visitedNodeIds.lastOrNull()
        val nextExpectedId = if (lastVisited == null) {
            1
        } else if (hintOrder.isNotEmpty()) {
            val idx = hintOrder.indexOf(lastVisited)
            if (idx != -1 && idx + 1 < hintOrder.size) hintOrder[idx + 1] else lastVisited + 1
        } else {
            lastVisited + 1
        }
        val isNextTarget = isDrawing && node.id == nextExpectedId
        if (isNextTarget) {
            drawCircle(
                color = Color(0xFF0284C7).copy(alpha = 0.30f * pulseAlpha),
                radius = baseRadius * (1.6f + 0.25f * (1f - pulseAlpha)),
                center = pos
            )
            drawCircle(
                color = Color(0xFF0284C7),
                radius = baseRadius * 1.25f,
                center = pos,
                style = Stroke(width = 2.5f)
            )
        }

        when (node.type) {
            NodeType.KEY -> {
                // Key Node: Golden Amber styling with Diamond shape
                val isCollected = collectedKeyIds.contains(node.keyForGateId)
                val keyColor = if (isCollected) strokeTheme.primaryColor else ColorKeyGold

                drawCircle(
                    color = keyColor.copy(alpha = 0.35f),
                    radius = baseRadius * 1.5f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = baseRadius,
                    center = pos
                )
                drawCircle(
                    color = keyColor,
                    radius = baseRadius,
                    center = pos,
                    style = Stroke(width = 3.5f)
                )

                // Diamond icon in center
                val path = Path().apply {
                    moveTo(pos.x, pos.y - baseRadius * 0.55f)
                    lineTo(pos.x + baseRadius * 0.55f, pos.y)
                    lineTo(pos.x, pos.y + baseRadius * 0.55f)
                    lineTo(pos.x - baseRadius * 0.55f, pos.y)
                    close()
                }
                drawPath(path, color = keyColor.copy(alpha = 0.4f))
            }

            NodeType.GATE -> {
                // Gate Node: Locked Red border until key gathered
                val isUnlocked = collectedKeyIds.contains(node.keyForGateId) ||
                        collectedKeyIds.contains(node.id) ||
                        (node.keyForGateId == -1 && collectedKeyIds.isNotEmpty())
                val gateColor = if (isUnlocked) ColorGateUnlocked else ColorGateLocked

                drawCircle(
                    color = gateColor.copy(alpha = 0.25f),
                    radius = baseRadius * 1.5f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF131A2A),
                    radius = baseRadius,
                    center = pos
                )
                drawCircle(
                    color = gateColor,
                    radius = baseRadius,
                    center = pos,
                    style = Stroke(width = 3.5f)
                )

                // Lock core symbol
                drawCircle(
                    color = gateColor.copy(alpha = 0.35f),
                    radius = baseRadius * 0.45f,
                    center = pos
                )
            }

            NodeType.NORMAL -> {
                if (isVisited) {
                    drawCircle(
                        color = strokeTheme.glowColor,
                        radius = baseRadius * 1.5f,
                        center = pos
                    )
                    drawCircle(
                        color = strokeTheme.primaryColor,
                        radius = baseRadius,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius * 0.35f,
                        center = pos
                    )
                } else if (isCharacterStart) {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFF0284C7),
                        radius = baseRadius,
                        center = pos,
                        style = Stroke(width = 3.5f)
                    )
                } else {
                    val isAlphaDimension = node.id % 2 == 1
                    val dimColor = if (isDualEntangledTier) {
                        if (isAlphaDimension) Color(0xFF00E5FF) else Color(0xFFD946EF)
                    } else null

                    if (dimColor != null) {
                        drawCircle(
                            color = dimColor.copy(alpha = 0.25f),
                            radius = baseRadius * 1.55f,
                            center = pos
                        )
                    }

                    val unvisitedNodeBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                    val unvisitedNodeBorder = dimColor ?: if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1)
                    drawCircle(
                        color = unvisitedNodeBg,
                        radius = baseRadius,
                        center = pos
                    )
                    drawCircle(
                        color = unvisitedNodeBorder,
                        radius = baseRadius,
                        center = pos,
                        style = Stroke(width = if (dimColor != null) 3.5f else 2.5f)
                    )
                }
            }
        }

        // Decaying Nodes mechanic: remaining time circle outline
        if (isDecayingTier && !isVisited) {
            val decayRatio = (remainingSec / 60f).coerceIn(0f, 1f)
            drawArc(
                color = Color(0xFFEF4444),
                startAngle = -90f,
                sweepAngle = 360f * decayRatio,
                useCenter = false,
                topLeft = Offset(pos.x - baseRadius * 1.35f, pos.y - baseRadius * 1.35f),
                size = Size(baseRadius * 2.7f, baseRadius * 2.7f),
                style = Stroke(width = 2.5f * (scale / 1.5f).coerceAtLeast(1f), cap = StrokeCap.Round)
            )
        }

        // Decoy Target mechanic: dashed warning aura
        val isDecoy = isDecoyTier && node.id > 1 && node.id % 4 == 0
        if (isDecoy && !isVisited) {
            drawCircle(
                color = Color(0xFFF43F5E).copy(alpha = 0.45f * pulseAlpha),
                radius = baseRadius * 1.4f,
                center = pos,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), timeSec * 15f)
                )
            )
        }

        // Draw node number (1, 2, 3... N) or hint step
        val displayText = if (isHintActive) {
            val stepIndex = hintOrder.indexOf(node.id)
            if (stepIndex != -1) (stepIndex + 1).toString() else node.id.toString()
        } else {
            node.id.toString()
        }

        val textColor = when {
            isVisited -> android.graphics.Color.WHITE
            isCharacterStart -> android.graphics.Color.parseColor("#0284C7")
            node.type == NodeType.KEY -> android.graphics.Color.parseColor("#D97706")
            node.type == NodeType.GATE -> {
                val isUnlocked = collectedKeyIds.contains(node.keyForGateId) ||
                        collectedKeyIds.contains(node.id) ||
                        (node.keyForGateId == -1 && collectedKeyIds.isNotEmpty())
                if (isUnlocked) android.graphics.Color.parseColor("#059669") else android.graphics.Color.parseColor("#E11D48")
            }
            else -> if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#0F172A")
        }
        textPaint.color = textColor
        textPaint.textSize = baseRadius * 1.05f
        val textY = pos.y - ((textPaint.descent() + textPaint.ascent()) / 2f)
        drawContext.canvas.nativeCanvas.drawText(displayText, pos.x, textY, textPaint)
    }
}

// -------------------------------------------------------------
// Living Web & Ghost System Helpers
// -------------------------------------------------------------

private fun DrawScope.drawLivingWebMesh(
    nodes: List<Node>,
    visitedNodeIds: List<Int>,
    currentPointerPos: Point?,
    adaptiveTendencyQuadrant: Int?,
    timeSec: Float,
    isDarkTheme: Boolean,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    if (nodes.size < 2) return
    val baseColor = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)

    // Quadrant cognitive resonance aura reflecting player's past hesitation tendency
    val quadCenter = when (adaptiveTendencyQuadrant) {
        1 -> Offset(size.width * 0.72f, size.height * 0.28f) // top-right
        2 -> Offset(size.width * 0.28f, size.height * 0.28f) // top-left
        3 -> Offset(size.width * 0.28f, size.height * 0.72f) // bottom-left
        4 -> Offset(size.width * 0.72f, size.height * 0.72f) // bottom-right
        else -> null
    }
    if (quadCenter != null) {
        val quadGlow = (sin(timeSec * 2.2f) * 0.04f + 0.06f).coerceIn(0.02f, 0.12f)
        drawCircle(
            color = Color(0xFFA855F7).copy(alpha = quadGlow),
            radius = size.width * 0.38f,
            center = quadCenter
        )
    }

    val dragPos = currentPointerPos?.let { toScreen(it) }

    for (i in 0 until nodes.size - 1) {
        val n1 = nodes[i]
        val n2 = nodes[i + 1]
        val rawP1 = toScreen(n1.toPoint())
        val rawP2 = toScreen(n2.toPoint())
        val distSq = (rawP1.x - rawP2.x) * (rawP1.x - rawP2.x) + (rawP1.y - rawP2.y) * (rawP1.y - rawP2.y)
        if (distSq < (340f * scale) * (340f * scale)) {
            // Living web: slow harmonic undulation of beams
            val slowPhase = timeSec * 1.6f + (i * 0.75f)
            val beamShiftX = sin(slowPhase) * (2.8f * scale)
            val beamShiftY = cos(slowPhase * 0.85f) * (2.8f * scale)
            val p1 = Offset(rawP1.x + beamShiftX, rawP1.y + beamShiftY)
            val p2 = Offset(rawP2.x - beamShiftX * 0.7f, rawP2.y + beamShiftY * 0.7f)

            // Dynamic tension stretch towards active drag cursor ("Ağ oyuncunun hamlelerine göre yeniden şekillensin")
            val midX: Float
            val midY: Float
            if (dragPos != null) {
                val baseMidX = (p1.x + p2.x) * 0.5f
                val baseMidY = (p1.y + p2.y) * 0.5f
                val dCursorX = dragPos.x - baseMidX
                val dCursorY = dragPos.y - baseMidY
                val curDist = hypot(dCursorX, dCursorY)
                val pullRadius = 160f * scale
                if (curDist < pullRadius && curDist > 1f) {
                    val pullRatio = (1f - (curDist / pullRadius)) * 14f * scale
                    midX = baseMidX + (dCursorX / curDist) * pullRatio
                    midY = baseMidY + (dCursorY / curDist) * pullRatio
                } else {
                    midX = baseMidX
                    midY = baseMidY
                }
            } else {
                midX = (p1.x + p2.x) * 0.5f
                midY = (p1.y + p2.y) * 0.5f
            }

            val ripple = (sin(timeSec * 2.5f + i * 0.8f) * 0.04f + 0.07f).coerceIn(0.02f, 0.14f)
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                quadraticBezierTo(midX, midY, p2.x, p2.y)
            }
            drawPath(
                path = path,
                color = baseColor.copy(alpha = ripple),
                style = Stroke(
                    width = 1.3f * scale,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * scale, 6f * scale), timeSec * 10f)
                )
            )
        }
    }
}

private fun DrawScope.drawPastGhostLabyrinth(
    ghosts: List<List<Point>>,
    mechanicType: String,
    timeSec: Float,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    if (ghosts.isEmpty()) return
    val isCumulativeGhosts = mechanicType in listOf("CUMULATIVE_GHOSTS", "OMEGA_SYNTHESIS")

    ghosts.forEachIndexed { strokeIdx, strokePts ->
        if (strokePts.size < 2) return@forEachIndexed
        val path = Path()
        val p0 = toScreen(strokePts[0])
        path.moveTo(p0.x, p0.y)
        for (i in 1 until strokePts.size) {
            val pi = toScreen(strokePts[i])
            path.lineTo(pi.x, pi.y)
        }

        // Wide ethereal outer purple glow
        drawPath(
            path = path,
            color = Color(0x22A855F7),
            style = Stroke(
                width = 12f * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Translucent semi-transparent ghost ray (Ghost Labyrinth)
        val rayAlpha = if (isCumulativeGhosts) 0.50f else 0.35f
        drawPath(
            path = path,
            color = Color(0xFFC084FC).copy(alpha = rayAlpha),
            style = Stroke(
                width = 4.5f * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f * scale, 10f * scale), timeSec * 20f + strokeIdx * 8f)
            )
        )

        // Memory wisps wandering along the ghost labyrinth
        val t = ((timeSec * 0.38f + strokeIdx * 0.25f) % 1f)
        val sampleIdx = (t * (strokePts.size - 1)).toInt().coerceIn(0, strokePts.size - 1)
        val spiritPt = toScreen(strokePts[sampleIdx])
        drawCircle(
            color = Color(0x66C4B5FD),
            radius = 7.5f * scale,
            center = spiritPt
        )
        drawCircle(
            color = Color(0xCCEDE9FE),
            radius = 3.2f * scale,
            center = spiritPt
        )
    }
}

private fun DrawScope.drawPerfectGhostRace(
    replayPoints: List<Point>,
    runnerPoint: Point?,
    timeSec: Float,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    if (replayPoints.size < 2) return
    val path = Path()
    val p0 = toScreen(replayPoints[0])
    path.moveTo(p0.x, p0.y)
    for (i in 1 until replayPoints.size) {
        val pi = toScreen(replayPoints[i])
        path.lineTo(pi.x, pi.y)
    }

    // Golden celestial ideal path
    drawPath(
        path = path,
        color = Color(0x50F59E0B),
        style = Stroke(
            width = 3.5f * scale,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f * scale, 8f * scale), timeSec * 22f)
        )
    )

    // Animated gold ghost runner
    val currentPt = runnerPoint ?: replayPoints.firstOrNull()
    if (currentPt != null) {
        val sc = toScreen(currentPt)
        val pulseR = (sin(timeSec * 6f) * 2f + 8f) * scale
        drawCircle(
            color = Color(0x40F59E0B),
            radius = pulseR * 1.8f,
            center = sc
        )
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = pulseR,
            center = sc
        )
        drawCircle(
            color = Color(0xFFFFFBEB),
            radius = pulseR * 0.45f,
            center = sc
        )
    }
}

private fun DrawScope.drawEchoBeastAtmosphere(
    beastState: EchoBeastState,
    timeSec: Float,
    size: Size,
    isDrawing: Boolean,
    segments: List<Segment>,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    if (beastState.level == 0) return
    when (beastState) {
        EchoBeastState.AWAKENING -> {
            val alpha = (sin(timeSec * 3f) * 0.12f + 0.18f).coerceIn(0.05f, 0.35f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFFF59E0B).copy(alpha = alpha)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.85f
                )
            )
        }
        EchoBeastState.ENRAGED -> {
            val alpha = (sin(timeSec * 5f) * 0.18f + 0.32f).coerceIn(0.1f, 0.55f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFFEF4444).copy(alpha = alpha)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.82f
                )
            )
        }
        EchoBeastState.DOMINION -> {
            val alpha = (sin(timeSec * 4f) * 0.22f + 0.48f).coerceIn(0.25f, 0.75f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF881337).copy(alpha = alpha)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.78f
                )
            )
            val eyeCenter = Offset(size.width / 2f, 32f)
            val eyeGlow = (sin(timeSec * 6f) * 0.3f + 0.7f).coerceIn(0.4f, 1f)
            val eyeColor = Color(0xFFEF4444).copy(alpha = eyeGlow)
            drawLine(
                color = eyeColor,
                start = Offset(eyeCenter.x - 22f, eyeCenter.y),
                end = Offset(eyeCenter.x - 6f, eyeCenter.y - 4f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = eyeColor,
                start = Offset(eyeCenter.x + 6f, eyeCenter.y - 4f),
                end = Offset(eyeCenter.x + 22f, eyeCenter.y),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Beast DOMINION mimicry: corrupted crimson shadow trail mimicking player's active path
            if (isDrawing && segments.isNotEmpty()) {
                val mimicAlpha = (sin(timeSec * 7f) * 0.15f + 0.35f).coerceIn(0.15f, 0.55f)
                val mimicShift = sin(timeSec * 4f) * 8f * scale
                for (seg in segments) {
                    val s = toScreen(seg.p1)
                    val e = toScreen(seg.p2)
                    drawLine(
                        color = Color(0xFFDC2626).copy(alpha = mimicAlpha),
                        start = Offset(s.x + mimicShift, s.y - mimicShift),
                        end = Offset(e.x + mimicShift, e.y - mimicShift),
                        strokeWidth = 3.5f * scale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * scale, 6f * scale), timeSec * 30f)
                    )
                }
            }
        }
        else -> {}
    }
}
