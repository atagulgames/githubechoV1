package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Season2PatternType
import com.example.model.Season2VisualTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Precomputed star node for smooth 60fps cosmic constellation pattern.
 */
private data class CosmicStar(
    val relX: Float,
    val relY: Float,
    val size: Float,
    val phase: Float,
    val isHero: Boolean = false
)

/**
 * Fixed deterministic star points spread harmoniously across canvas space.
 */
private val STATIC_COSMIC_STARS: List<CosmicStar> = listOf(
    CosmicStar(0.12f, 0.15f, 3.2f, 0.4f, true),
    CosmicStar(0.28f, 0.11f, 2.2f, 1.8f),
    CosmicStar(0.42f, 0.19f, 2.8f, 2.6f),
    CosmicStar(0.22f, 0.27f, 3.6f, 3.1f, true),
    CosmicStar(0.68f, 0.14f, 2.4f, 0.9f),
    CosmicStar(0.85f, 0.18f, 3.4f, 4.2f, true),
    CosmicStar(0.74f, 0.28f, 2.0f, 5.1f),
    CosmicStar(0.88f, 0.36f, 2.6f, 1.5f),
    CosmicStar(0.08f, 0.44f, 2.5f, 2.1f),
    CosmicStar(0.18f, 0.52f, 3.8f, 0.2f, true),
    CosmicStar(0.32f, 0.46f, 2.1f, 3.8f),
    CosmicStar(0.52f, 0.42f, 3.0f, 4.7f),
    CosmicStar(0.62f, 0.54f, 2.3f, 2.9f),
    CosmicStar(0.82f, 0.58f, 3.5f, 1.1f, true),
    CosmicStar(0.92f, 0.68f, 2.2f, 3.4f),
    CosmicStar(0.15f, 0.72f, 2.7f, 4.5f),
    CosmicStar(0.25f, 0.82f, 3.3f, 5.3f, true),
    CosmicStar(0.45f, 0.76f, 2.0f, 0.7f),
    CosmicStar(0.55f, 0.88f, 3.1f, 2.4f),
    CosmicStar(0.72f, 0.81f, 2.5f, 3.9f),
    CosmicStar(0.86f, 0.89f, 3.6f, 1.7f, true),
    CosmicStar(0.38f, 0.94f, 2.2f, 4.8f),
    CosmicStar(0.68f, 0.95f, 2.6f, 0.3f),
    CosmicStar(0.50f, 0.25f, 4.0f, 5.8f, true)
)

/**
 * Precomputed ember particle for Solar Flare theme.
 */
private data class SolarEmber(
    val relX: Float,
    val speedY: Float,
    val size: Float,
    val phase: Float
)

private val STATIC_SOLAR_EMBERS: List<SolarEmber> = List(20) { idx ->
    SolarEmber(
        relX = (idx * 0.051f) % 1f,
        speedY = 0.08f + (idx % 5) * 0.02f,
        size = 2f + (idx % 4) * 1.2f,
        phase = idx * 1.35f
    )
}

/**
 * High-performance, animated background pattern component for Season 2.
 */
@Composable
fun Season2BackgroundPatternCanvas(
    theme: Season2VisualTheme,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    alphaMultiplier: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Season2BackgroundTransition")
    val timeSec by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Season2TimeSec"
    )

    Canvas(modifier = modifier) {
        drawSeason2BackgroundPattern(
            theme = theme,
            isDarkTheme = isDarkTheme,
            timeSec = timeSec,
            w = size.width,
            h = size.height,
            alphaMultiplier = alphaMultiplier
        )
    }
}

/**
 * Core DrawScope extension function that renders Season 2 background patterns.
 * Shared between full-screen composable and in-game canvas.
 */
fun DrawScope.drawSeason2BackgroundPattern(
    theme: Season2VisualTheme,
    isDarkTheme: Boolean,
    timeSec: Float,
    w: Float,
    h: Float,
    alphaMultiplier: Float = 1.0f
) {
    // 1. Base Gradient Canvas Fill
    val topColor = theme.getBackgroundTop(isDarkTheme)
    val bottomColor = theme.getBackgroundBottom(isDarkTheme)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(topColor, bottomColor),
            startY = 0f,
            endY = h
        ),
        size = Size(w, h)
    )

    // 2. Specialized Pattern Layer
    when (theme.patternType) {
        Season2PatternType.COSMIC_CONSTELLATION -> {
            drawCosmicConstellationPattern(theme, isDarkTheme, timeSec, w, h, alphaMultiplier)
        }
        Season2PatternType.AURORA_WAVES -> {
            drawAuroraWavesPattern(theme, isDarkTheme, timeSec, w, h, alphaMultiplier)
        }
        Season2PatternType.SOLAR_RINGS -> {
            drawSolarRingsPattern(theme, isDarkTheme, timeSec, w, h, alphaMultiplier)
        }
        Season2PatternType.QUANTUM_GRID -> {
            drawQuantumGridPattern(theme, isDarkTheme, timeSec, w, h, alphaMultiplier)
        }
    }
}

/**
 * Pattern 1: Cosmic Constellation & Nebula Dust
 */
private fun DrawScope.drawCosmicConstellationPattern(
    theme: Season2VisualTheme,
    isDarkTheme: Boolean,
    timeSec: Float,
    w: Float,
    h: Float,
    alphaMultiplier: Float
) {
    val nebulaAlpha = (if (isDarkTheme) 0.18f else 0.10f) * alphaMultiplier

    // 1. Glowing Nebula Clouds (Soft Radial Orbs)
    val neb1Center = Offset(
        w * 0.25f + sin(timeSec * 0.2f) * 40f,
        h * 0.35f + cos(timeSec * 0.18f) * 35f
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.primaryAccent.copy(alpha = nebulaAlpha),
                Color.Transparent
            ),
            center = neb1Center,
            radius = w * 0.65f
        ),
        center = neb1Center,
        radius = w * 0.65f
    )

    val neb2Center = Offset(
        w * 0.75f + cos(timeSec * 0.22f) * 45f,
        h * 0.68f + sin(timeSec * 0.25f) * 40f
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.tertiaryAccent.copy(alpha = nebulaAlpha * 0.85f),
                Color.Transparent
            ),
            center = neb2Center,
            radius = w * 0.55f
        ),
        center = neb2Center,
        radius = w * 0.55f
    )

    // 2. Constellation Star Points & Connecting Filaments
    val starCoords = ArrayList<Offset>(STATIC_COSMIC_STARS.size)
    for (star in STATIC_COSMIC_STARS) {
        val driftX = sin(timeSec * 0.4f + star.phase) * 6f
        val driftY = cos(timeSec * 0.35f + star.phase) * 6f
        val px = (star.relX * w) + driftX
        val py = (star.relY * h) + driftY
        starCoords.add(Offset(px, py))
    }

    // Connect proximate stars with delicate celestial threads
    val maxDist = w * 0.28f
    val lineBaseAlpha = (if (isDarkTheme) 0.16f else 0.10f) * alphaMultiplier
    val starLineColor = theme.primaryAccent

    for (i in 0 until starCoords.size) {
        val p1 = starCoords[i]
        for (j in i + 1 until starCoords.size) {
            val p2 = starCoords[j]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < maxDist) {
                val proximityRatio = 1f - (dist / maxDist)
                val pulse = (sin(timeSec * 1.5f + i + j) * 0.25f + 0.75f)
                val lineAlpha = (proximityRatio * lineBaseAlpha * pulse).coerceIn(0.02f, 0.35f)
                drawLine(
                    color = starLineColor.copy(alpha = lineAlpha),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // Draw individual sparkling star points
    for (i in 0 until starCoords.size) {
        val star = STATIC_COSMIC_STARS[i]
        val center = starCoords[i]
        val twinkle = (sin(timeSec * 2.2f + star.phase) * 0.35f + 0.65f)
        val starRadius = star.size * twinkle
        val starAlpha = (if (isDarkTheme) 0.85f else 0.70f) * twinkle * alphaMultiplier

        // Outer starlight halo
        drawCircle(
            color = theme.secondaryAccent.copy(alpha = starAlpha * 0.35f),
            radius = starRadius * 2.4f,
            center = center
        )
        // Core star
        drawCircle(
            color = (if (isDarkTheme) Color.White else theme.primaryAccent).copy(alpha = starAlpha),
            radius = starRadius,
            center = center
        )

        // 4-point cross glint on hero stars
        if (star.isHero) {
            val glintLen = starRadius * 3.8f
            val glintColor = theme.secondaryAccent.copy(alpha = starAlpha * 0.6f)
            drawLine(
                color = glintColor,
                start = Offset(center.x - glintLen, center.y),
                end = Offset(center.x + glintLen, center.y),
                strokeWidth = 1.2f
            )
            drawLine(
                color = glintColor,
                start = Offset(center.x, center.y - glintLen),
                end = Offset(center.x, center.y + glintLen),
                strokeWidth = 1.2f
            )
        }
    }
}

/**
 * Pattern 2: Multi-sinusoidal Aurora Borealis Wave Ribbons
 */
private fun DrawScope.drawAuroraWavesPattern(
    theme: Season2VisualTheme,
    isDarkTheme: Boolean,
    timeSec: Float,
    w: Float,
    h: Float,
    alphaMultiplier: Float
) {
    val auroraAlpha = (if (isDarkTheme) 0.22f else 0.12f) * alphaMultiplier

    // 3 layered sinusoidal wave curtains flowing vertically across upper and middle viewport
    val bands = listOf(
        Triple(0.20f, theme.primaryAccent, 0.9f),
        Triple(0.38f, theme.secondaryAccent, 1.2f),
        Triple(0.58f, theme.tertiaryAccent, 0.7f)
    )

    for ((index, band) in bands.withIndex()) {
        val (yRatio, color, speed) = band
        val baseY = h * yRatio
        val path = Path()
        val segments = 24
        val stepX = w / segments

        val phase = timeSec * speed + (index * 1.8f)
        path.moveTo(0f, baseY)

        for (seg in 0..segments) {
            val x = seg * stepX
            val wave1 = sin(x * 0.008f + phase) * 35f
            val wave2 = cos(x * 0.015f - phase * 0.8f) * 20f
            val y = baseY + wave1 + wave2
            if (seg == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw flowing curtain glow
        drawPath(
            path = path,
            color = color.copy(alpha = auroraAlpha * 0.75f),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // Draw wider diffuse ribbon under the wave crest
        val ribbonPath = Path().apply {
            addPath(path)
            lineTo(w, baseY + 90f)
            lineTo(0f, baseY + 90f)
            close()
        }
        drawPath(
            path = ribbonPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = auroraAlpha * 0.5f), Color.Transparent),
                startY = baseY - 20f,
                endY = baseY + 100f
            )
        )
    }

    // Ambient floating cyber motes
    for (i in 0 until 16) {
        val phase = i * 2.1f
        val relX = ((i * 0.063f) + sin(timeSec * 0.15f + phase) * 0.04f).coerceIn(0.05f, 0.95f)
        val relY = ((i * 0.058f + 0.15f) + cos(timeSec * 0.2f + phase) * 0.03f).coerceIn(0.1f, 0.9f)
        val moteAlpha = (sin(timeSec * 2f + phase) * 0.25f + 0.55f) * auroraAlpha * 1.4f
        drawCircle(
            color = theme.secondaryAccent.copy(alpha = moteAlpha),
            radius = 2.5f,
            center = Offset(relX * w, relY * h)
        )
    }
}

/**
 * Pattern 3: Concentric Solar Flux Rings & Rising Heat Embers
 */
private fun DrawScope.drawSolarRingsPattern(
    theme: Season2VisualTheme,
    isDarkTheme: Boolean,
    timeSec: Float,
    w: Float,
    h: Float,
    alphaMultiplier: Float
) {
    val solarCenter = Offset(w * 0.5f, h * 0.35f)
    val ringBaseAlpha = (if (isDarkTheme) 0.18f else 0.10f) * alphaMultiplier

    // 1. Central Solar Corona Glow
    val coronaRadius = w * 0.45f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.secondaryAccent.copy(alpha = ringBaseAlpha * 1.5f),
                theme.primaryAccent.copy(alpha = ringBaseAlpha * 0.6f),
                Color.Transparent
            ),
            center = solarCenter,
            radius = coronaRadius
        ),
        center = solarCenter,
        radius = coronaRadius
    )

    // 2. Concentric Expanding Flux Rings
    val ringCount = 5
    val maxRadius = w * 0.85f
    for (i in 0 until ringCount) {
        val ringOffset = (timeSec * 35f + (i * (maxRadius / ringCount))) % maxRadius
        val ringRatio = ringOffset / maxRadius
        val ringAlpha = (1f - ringRatio) * ringBaseAlpha * 1.2f
        drawCircle(
            color = if (i % 2 == 0) theme.primaryAccent.copy(alpha = ringAlpha) else theme.secondaryAccent.copy(alpha = ringAlpha),
            radius = ringOffset,
            center = solarCenter,
            style = Stroke(
                width = 2.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), timeSec * 15f)
            )
        )
    }

    // 3. Upward Drifting Fiery Embers
    for (ember in STATIC_SOLAR_EMBERS) {
        val emberY = (h - ((timeSec * ember.speedY * 500f + ember.phase * 200f) % h))
        val sway = sin(timeSec * 1.8f + ember.phase) * 18f
        val emberX = (ember.relX * w + sway).coerceIn(10f, w - 10f)
        val emberAlpha = (sin(timeSec * 3f + ember.phase) * 0.3f + 0.65f) * ringBaseAlpha * 2f

        drawCircle(
            color = theme.primaryAccent.copy(alpha = emberAlpha),
            radius = ember.size,
            center = Offset(emberX, emberY)
        )
    }
}

/**
 * Pattern 4: Isometric / Orthogonal Quantum Data Grid & Pulse Tracks
 */
private fun DrawScope.drawQuantumGridPattern(
    theme: Season2VisualTheme,
    isDarkTheme: Boolean,
    timeSec: Float,
    w: Float,
    h: Float,
    alphaMultiplier: Float
) {
    val gridAlpha = (if (isDarkTheme) 0.15f else 0.08f) * alphaMultiplier
    val gridSpacing = 64f

    // 1. Subtle Background Grid Lines
    var x = gridSpacing
    while (x < w) {
        drawLine(
            color = theme.darkDotColor.copy(alpha = gridAlpha * 0.5f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += gridSpacing
    }

    var y = gridSpacing
    while (y < h) {
        drawLine(
            color = theme.darkDotColor.copy(alpha = gridAlpha * 0.5f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += gridSpacing
    }

    // 2. High-speed Animated Data Packets Travelling along Grid Tracks
    val packetCount = 6
    for (i in 0 until packetCount) {
        val trackY = ((i + 1) * (h / (packetCount + 1)))
        val packetX = ((timeSec * 220f + i * 180f) % (w + 120f)) - 60f
        val packetColor = if (i % 2 == 0) theme.primaryAccent else theme.secondaryAccent

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    packetColor.copy(alpha = gridAlpha * 2.5f),
                    packetColor.copy(alpha = gridAlpha * 3.5f),
                    Color.Transparent
                ),
                startX = packetX - 60f,
                endX = packetX + 60f
            ),
            start = Offset(packetX - 60f, trackY),
            end = Offset(packetX + 60f, trackY),
            strokeWidth = 2.4f,
            cap = StrokeCap.Round
        )
    }

    // 3. Glowing Diamond Grid Nodes at select intersections
    val cols = (w / gridSpacing).toInt()
    val rows = (h / gridSpacing).toInt()
    for (c in 2..cols step 3) {
        for (r in 2..rows step 4) {
            val nodeX = c * gridSpacing
            val nodeY = r * gridSpacing
            val pulse = sin(timeSec * 2.5f + c * 0.7f + r * 0.9f) * 0.35f + 0.65f
            drawCircle(
                color = theme.primaryAccent.copy(alpha = gridAlpha * 1.8f * pulse),
                radius = 3.5f,
                center = Offset(nodeX, nodeY)
            )
        }
    }
}
