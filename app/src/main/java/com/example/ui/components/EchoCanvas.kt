package com.example.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.model.GameStatus
import com.example.model.Node
import com.example.model.NodeType
import com.example.model.Point
import com.example.model.Segment
import com.example.model.StrokeTheme
import com.example.viewmodel.EchoUiState
import kotlin.math.atan2
import kotlin.math.cos
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

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("echo_canvas")
                .pointerInput(state.gameStatus, scale, offsetX, offsetY) {
                    if (state.gameStatus == GameStatus.PLAYING) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            onPointerDown(toVirtual(down.position))
                            down.consume()

                            do {
                                val event = awaitPointerEvent()
                                val current = event.changes.firstOrNull()
                                if (current != null) {
                                    if (current.pressed) {
                                        onPointerMove(toVirtual(current.position))
                                        current.consume()
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            onPointerUp()
                        }
                    }
                }
        ) {
            // 1. Grid Background (Single GPU drawPoints instruction for buttery 60+ FPS)
            drawGridBackground(size.width, size.height, state.isDarkTheme, gridPoints)

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

            // 4. Past Echo Barriers (Collision Obstacles with Echo Theme & Decaying opacity)
            drawEchoBarriers(
                echoes = state.echoes,
                echoTheme = state.echoTheme,
                isShrinkerActive = state.isEchoShrinkerActive,
                pulseAlpha = pulseAlpha,
                toScreen = ::toScreen,
                scale = scale
            )

            // 5. Active Player Stroke with Stroke Theme
            drawPlayerStroke(
                segments = state.currentStrokeSegments,
                currentPointerPos = state.currentPointerPos,
                visitedNodeIds = state.visitedNodeIds,
                nodes = state.nodes,
                strokeTheme = state.strokeTheme,
                toScreen = ::toScreen,
                scale = scale
            )

            // 6. Game Nodes (with Normal, Key, Gate styles, and Character Node 1)
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
                toScreen = ::toScreen,
                scale = scale,
                textPaint = nodeTextPaint
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
    pulseAlpha: Float,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val widthFactor = if (isShrinkerActive) 0.5f else 1.0f

    for (echo in echoes) {
        val lifeRatio = (echo.remainingAttempts.toFloat() / echo.maxLifetime).coerceIn(0.2f, 1f)
        val alpha = if (echo.isGhost) 0.20f else (lifeRatio * pulseAlpha)

        val glowColor = echoTheme.glowColor.copy(alpha = alpha * 0.5f)
        val barrierColor = echoTheme.echoColor.copy(alpha = alpha)

        val glowWidth = 14f * widthFactor * (scale / 1.5f).coerceAtLeast(1f)
        val coreWidth = 5f * widthFactor * (scale / 1.5f).coerceAtLeast(1f)

        for (seg in echo.segments) {
            val start = toScreen(seg.p1)
            val end = toScreen(seg.p2)

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
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val glowWidth = 16f * (scale / 1.5f).coerceAtLeast(1f)
    val coreWidth = 6f * (scale / 1.5f).coerceAtLeast(1f)

    for (seg in segments) {
        val start = toScreen(seg.p1)
        val end = toScreen(seg.p2)

        drawLine(
            color = strokeTheme.glowColor,
            start = start,
            end = end,
            strokeWidth = glowWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = strokeTheme.primaryColor,
            start = start,
            end = end,
            strokeWidth = coreWidth,
            cap = StrokeCap.Round
        )
    }

    if (currentPointerPos != null && visitedNodeIds.isNotEmpty()) {
        val lastNodeId = visitedNodeIds.last()
        val lastNode = nodes.firstOrNull { it.id == lastNodeId }
        if (lastNode != null) {
            val start = toScreen(lastNode.toPoint())
            val end = toScreen(currentPointerPos)

            drawLine(
                color = strokeTheme.glowColor,
                start = start,
                end = end,
                strokeWidth = glowWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeTheme.primaryColor,
                start = start,
                end = end,
                strokeWidth = coreWidth,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = strokeTheme.glowColor,
                radius = 18f * (scale / 1.5f).coerceAtLeast(1f),
                center = end,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = strokeTheme.primaryColor,
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
    toScreen: (Point) -> Offset,
    scale: Float,
    textPaint: android.graphics.Paint
) {
    val baseRadius = 20f * (scale / 1.5f).coerceAtLeast(1f)

    for (node in nodes) {
        val pos = toScreen(node.toPoint())
        val isVisited = visitedNodeIds.contains(node.id)
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
                    val unvisitedNodeBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                    val unvisitedNodeBorder = if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1)
                    drawCircle(
                        color = unvisitedNodeBg,
                        radius = baseRadius,
                        center = pos
                    )
                    drawCircle(
                        color = unvisitedNodeBorder,
                        radius = baseRadius,
                        center = pos,
                        style = Stroke(width = 2.5f)
                    )
                }
            }
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
