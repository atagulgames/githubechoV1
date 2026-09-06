package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class CandyParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val isStar: Boolean
)

data class CandyCallout(
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float,
    var scale: Float,
    val createdAt: Long = System.currentTimeMillis()
)

object CandyEffectsFactory {
    private val CANDY_COLORS = listOf(
        Color(0xFFFFB703), // Amber Gold
        Color(0xFFFB8500), // Orange Crush
        Color(0xFF06D6A0), // Mint Emerald
        Color(0xFF118AB2), // Vivid Blue
        Color(0xFFEF476F), // Strawberry Pink
        Color(0xFF8338EC), // Royal Purple
        Color(0xFFFF006E)  // Magenta Spark
    )

    fun createNodeExplosion(cx: Float, cy: Float, count: Int = 18): List<CandyParticle> {
        val particles = ArrayList<CandyParticle>(count)
        for (i in 0 until count) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextFloat() * 7f + 3f
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()
            val color = CANDY_COLORS[i % CANDY_COLORS.size]
            val size = Random.nextFloat() * 6f + 4f
            val isStar = i % 3 == 0

            particles.add(
                CandyParticle(
                    x = cx,
                    y = cy,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = size,
                    alpha = 1.0f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 18f,
                    isStar = isStar
                )
            )
        }
        return particles
    }

    fun createConfettiVictory(width: Float, height: Float, count: Int = 50): List<CandyParticle> {
        val particles = ArrayList<CandyParticle>(count)
        for (i in 0 until count) {
            val x = Random.nextFloat() * width
            val y = Random.nextFloat() * (height * 0.4f)
            val vx = (Random.nextFloat() - 0.5f) * 6f
            val vy = Random.nextFloat() * 4f + 2f
            val color = CANDY_COLORS[i % CANDY_COLORS.size]
            val size = Random.nextFloat() * 7f + 5f

            particles.add(
                CandyParticle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = size,
                    alpha = 1.0f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 12f,
                    isStar = i % 2 == 0
                )
            )
        }
        return particles
    }

    fun getCalloutText(connectedCount: Int): Pair<String, Color> {
        return when (connectedCount) {
            2 -> "GÜZEL! ✨" to Color(0xFF38BDF8)
            3 -> "HARİKA! 💫" to Color(0xFF34D399)
            4 -> "MÜKEMMEL! 🌟" to Color(0xFFFBBF24)
            5 -> "ŞAHANE! 🍬" to Color(0xFFF472B6)
            6 -> "HARMONİK CRUSH! 🔥" to Color(0xFFFB923C)
            else -> "EFSANEVİ KOMBO! 👑" to Color(0xFFA78BFA)
        }
    }
}
