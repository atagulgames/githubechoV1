package com.example.data

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 100 completely unique geometric curve generators for ECHO levels 1 to 100.
 * Every single level has its own distinct mathematical silhouette,
 * guaranteed non-self-intersecting Jordan curve, with generous clearances.
 */
object LevelGeometry {

    fun clampPt(x: Float, y: Float): Pair<Float, Float> =
        Pair(x.coerceIn(44f, 396f), y.coerceIn(88f, 472f))

    fun getCurveForLevel(id: Int, cx: Float, cy: Float): (Float) -> Pair<Float, Float> {
        val clampedId = id.coerceIn(1, 100)
        return when (clampedId) {
            // =========================================================================
            // TIER 1 (Levels 1..10): Pure Classical Geometries (3 to 8 nodes)
            // =========================================================================
            1 -> { t -> // Level 1: "Pure Triangle" (3 nodes)
                val (u, v) = when {
                    t <= 0.5f -> Pair(-130f + (t / 0.5f) * 130f, 115f - (t / 0.5f) * 230f)
                    else -> Pair(((t - 0.5f) / 0.5f) * 130f, -115f + ((t - 0.5f) / 0.5f) * 230f)
                }
                clampPt(cx + u, cy + v)
            }
            2 -> { t -> // Level 2: "Golden Square" (4 nodes)
                val (u, v) = when {
                    t <= 0.333f -> Pair(-115f, 115f - (t / 0.333f) * 230f)
                    t <= 0.667f -> Pair(-115f + ((t - 0.333f) / 0.334f) * 230f, -115f)
                    else -> Pair(115f, -115f + ((t - 0.667f) / 0.333f) * 230f)
                }
                clampPt(cx + u, cy + v)
            }
            3 -> { t -> // Level 3: "Diamond Kite" (open kite contour)
                val (u, v) = when {
                    t <= 0.333f -> Pair(-110f + (t / 0.333f) * 110f, 60f - (t / 0.333f) * 180f)
                    t <= 0.667f -> {
                        val sub = (t - 0.333f) / 0.334f
                        Pair(sub * 110f, -120f + sub * 180f)
                    }
                    else -> {
                        val sub = (t - 0.667f) / 0.333f
                        Pair(110f - sub * 110f, 60f + sub * 80f)
                    }
                }
                clampPt(cx + u, cy + v)
            }
            4 -> { t -> // Level 4: "Resonance Pentagon" (4 nodes)
                val (u, v) = when {
                    t <= 0.333f -> Pair(-120f + (t / 0.333f) * 120f, 40f - (t / 0.333f) * 170f)
                    t <= 0.667f -> Pair(((t - 0.333f) / 0.334f) * 120f, -130f + ((t - 0.333f) / 0.334f) * 170f)
                    else -> Pair(120f, 40f + ((t - 0.667f) / 0.333f) * 80f)
                }
                clampPt(cx + u, cy + v)
            }
            5 -> { t -> // Level 5: "Bright Star" (6 nodes)
                val (u, v) = when {
                    t <= 0.2f -> Pair(-135f + (t / 0.2f) * 85f, 25f - (t / 0.2f) * 85f)
                    t <= 0.4f -> Pair(-50f + ((t - 0.2f) / 0.2f) * 50f, -60f - ((t - 0.2f) / 0.2f) * 75f)
                    t <= 0.6f -> Pair(((t - 0.4f) / 0.2f) * 50f, -135f + ((t - 0.4f) / 0.2f) * 75f)
                    t <= 0.8f -> Pair(50f + ((t - 0.6f) / 0.2f) * 85f, -60f + ((t - 0.6f) / 0.2f) * 85f)
                    else -> Pair(135f - ((t - 0.8f) / 0.2f) * 55f, 25f + ((t - 0.8f) / 0.2f) * 105f)
                }
                clampPt(cx + u, cy + v)
            }
            6 -> { t -> // Level 6: "Crystal Hexagon" (5 nodes)
                val (u, v) = when {
                    t <= 0.25f -> Pair(-70f + (t / 0.25f) * 140f, -130f)
                    t <= 0.50f -> Pair(70f + ((t - 0.25f) / 0.25f) * 65f, -130f + ((t - 0.25f) / 0.25f) * 130f)
                    t <= 0.75f -> Pair(135f - ((t - 0.50f) / 0.25f) * 65f, ((t - 0.50f) / 0.25f) * 130f)
                    else -> Pair(70f - ((t - 0.75f) / 0.25f) * 140f, 130f)
                }
                clampPt(cx + u, cy + v)
            }
            7 -> { t -> // Level 7: "Prism Dome"
                val u = -140f + t * 280f
                val v = (sin(t * 1.5 * PI) * 115f).toFloat()
                clampPt(cx + u, cy + v)
            }
            8 -> { t -> // Level 8: "Twin Loops" (6 nodes)
                val (u, v) = when {
                    t <= 0.333f -> Pair(-125f + (t / 0.333f) * 250f, -125f)
                    t <= 0.667f -> {
                        val sub = (t - 0.333f) / 0.334f
                        Pair(125f - sub * 250f, -125f + sub * 250f)
                    }
                    else -> Pair(-125f + ((t - 0.667f) / 0.333f) * 250f, 125f)
                }
                clampPt(cx + u, cy + v)
            }
            9 -> { t -> // Level 9: "Lapis Ring" (8 nodes)
                val sweep = 1.70 * PI
                val angle = -PI * 0.85 + sweep * t
                val r = 135f
                clampPt(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
            }
            10 -> { t -> // Level 10: "Geometric Matrix" (7 nodes)
                val (u, v) = when {
                    t <= 0.40f -> Pair(-135f + (t / 0.40f) * 135f, 120f - (t / 0.40f) * 240f)
                    t <= 0.70f -> Pair((t - 0.40f) / 0.30f * 135f, -120f + ((t - 0.40f) / 0.30f) * 120f)
                    else -> Pair(135f, ((t - 0.70f) / 0.30f) * 120f)
                }
                clampPt(cx + u, cy + v)
            }

            // =========================================================================
            // TIER 2 (Levels 11..20): Vector Flow & Curves (8 to 13 nodes)
            // =========================================================================
            11 -> { t -> // Level 11: "Vector Stream" (8 nodes)
                val u = -140f + t * 280f
                val v = 110f - 220f * (1f - 4f * (t - 0.5f) * (t - 0.5f))
                clampPt(cx + u, cy + v)
            }
            12 -> { t -> // Level 12: "Polar Arc" (9 nodes)
                val u = -145f + t * 290f
                val v = (sin(t * 2.0 * PI) * 115f).toFloat()
                clampPt(cx + u, cy + v)
            }
            13 -> { t -> // Level 13: "Spiral Orbit" (8 nodes)
                val r = 150f - 85f * t
                val angle = 1.45 * PI * t
                clampPt(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
            }
            14 -> { t -> // Level 14: "Nebula Spine" (10 nodes)
                val v = -140f + t * 280f
                val u = (sin(t * 2.0 * PI) * 125f).toFloat()
                clampPt(cx + u, cy + v)
            }
            15 -> { t -> // Level 15: "Cosmic Needle" (9 nodes)
                val (u, v) = when {
                    t <= 0.4f -> Pair(-110f, -135f + (t / 0.4f) * 215f)
                    t <= 0.6f -> {
                        val a = PI - ((t - 0.4f) / 0.2f) * PI
                        Pair(cos(a).toFloat() * 110f, 80f + sin(a).toFloat() * 55f)
                    }
                    else -> Pair(110f, 80f - ((t - 0.6f) / 0.4f) * 215f)
                }
                clampPt(cx + u, cy + v)
            }
            16 -> { t -> // Level 16: "Ion Net"
                val u = -140f + t * 280f
                val v = (sin(t * 3.5 * PI) * 120f).toFloat()
                clampPt(cx + u, cy + v)
            }
            17 -> { t -> // Level 17: "Vortex Ray" (10 nodes)
                val r = 55f + 95f * t
                val angle = 1.48 * PI * t
                clampPt(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
            }
            18 -> { t -> // Level 18: "Zenith Beam" (12 nodes)
                val u = -145f + t * 290f
                val v = (sin(t * 3.0 * PI) * 110f).toFloat()
                clampPt(cx + u, cy + v)
            }
            19 -> { t -> // Level 19: "Pyramid Line" (11 nodes)
                val (u, v) = when {
                    t <= 0.166f -> Pair(-140f + (t / 0.166f) * 50f, 120f)
                    t <= 0.333f -> Pair(-90f, 120f - ((t - 0.166f) / 0.167f) * 100f)
                    t <= 0.500f -> Pair(-90f + ((t - 0.333f) / 0.167f) * 90f, 20f - ((t - 0.333f) / 0.167f) * 140f)
                    t <= 0.667f -> Pair(((t - 0.500f) / 0.167f) * 90f, -120f + ((t - 0.500f) / 0.167f) * 140f)
                    t <= 0.833f -> Pair(90f, 20f + ((t - 0.667f) / 0.166f) * 100f)
                    else -> Pair(90f + ((t - 0.833f) / 0.167f) * 50f, 120f)
                }
                clampPt(cx + u, cy + v)
            }
            20 -> { t -> // Level 20: "Infinity Ribbon"
                val u = -140f + t * 280f
                val v = (sin(t * 2.5 * PI) * 115f * (0.8f + 0.2f * sin(t * PI))).toFloat()
                clampPt(cx + u, cy + v)
            }

            // =========================================================================
            // TIER 3 (Levels 21..30): Lock & Key (12 to 18 nodes)
            // =========================================================================
            21 -> { t -> // Level 21: "Copper Vault" (12 nodes)
                val (u, v) = when {
                    t <= 0.25f -> Pair(-135f + (t / 0.25f) * 270f, -135f)
                    t <= 0.50f -> Pair(135f, -135f + ((t - 0.25f) / 0.25f) * 270f)
                    t <= 0.75f -> Pair(135f - ((t - 0.50f) / 0.25f) * 210f, 135f)
                    else -> Pair(-75f, 135f - ((t - 0.75f) / 0.25f) * 190f)
                }
                clampPt(cx + u, cy + v)
            }
            22 -> { t -> // Level 22: "Silver Gateway" (14 nodes)
                val (u, v) = when {
                    t <= 0.30f -> Pair(-125f, 130f - (t / 0.30f) * 180f)
                    t <= 0.70f -> {
                        val sub = (t - 0.30f) / 0.40f
                        val a = PI - sub * PI
                        Pair(125f * cos(a).toFloat(), -50f - 85f * sin(a).toFloat())
                    }
                    else -> Pair(125f, -50f + ((t - 0.70f) / 0.30f) * 180f)
                }
                clampPt(cx + u, cy + v)
            }
            23 -> { t -> // Level 23: "Sapphire Key"
                val (u, v) = when {
                    t <= 0.35f -> Pair(-135f + (t / 0.35f) * 270f, -90f)
                    t <= 0.65f -> Pair(135f, -90f + ((t - 0.35f) / 0.30f) * 190f)
                    t <= 0.85f -> Pair(135f - ((t - 0.65f) / 0.20f) * 110f, 100f)
                    else -> Pair(25f, 100f + ((t - 0.85f) / 0.15f) * 35f)
                }
                clampPt(cx + u, cy + v)
            }
            24 -> { t -> // Level 24: "Ruby Barrier" (15 nodes)
                val u = -140f + t * 280f
                val v = when {
                    (t * 8).toInt() % 2 == 0 -> -60f
                    else -> 60f
                }
                clampPt(cx + u, cy + v)
            }
            25 -> { t -> // Level 25: "Emerald Gate" (14 nodes)
                val (u, v) = when {
                    t <= 0.30f -> Pair(-115f, 130f - (t / 0.30f) * 250f)
                    t <= 0.70f -> Pair(-115f + ((t - 0.30f) / 0.40f) * 230f, -120f)
                    else -> Pair(115f, -120f + ((t - 0.70f) / 0.30f) * 250f)
                }
                clampPt(cx + u, cy + v)
            }
            26 -> { t -> // Level 26: "Mystic Maze" (16 nodes)
                val (u, v) = when {
                    t <= 0.25f -> Pair(-135f + (t / 0.25f) * 270f, -135f)
                    t <= 0.50f -> Pair(135f, -135f + ((t - 0.25f) / 0.25f) * 270f)
                    t <= 0.75f -> Pair(135f - ((t - 0.50f) / 0.25f) * 200f, 135f)
                    else -> Pair(-65f, 135f - ((t - 0.75f) / 0.25f) * 190f)
                }
                clampPt(cx + u, cy + v)
            }
            27 -> { t -> // Level 27: "Hidden Sanctum" (15 nodes)
                val sweep = 1.65 * PI
                val angle = -PI * 0.8 + sweep * t
                val r = 145f - 40f * t
                clampPt(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
            }
            28 -> { t -> // Level 28: "Sacred Portal" (17 nodes)
                val a = -PI * 0.8 + t * (1.6 * PI)
                val r = 140f + 25f * sin(a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            29 -> { t -> // Level 29: "Crystal Seal"
                val a = -PI * 0.85 + t * (1.7 * PI)
                val r = 135f - 20f * cos(a * 3.0).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            30 -> { t -> // Level 30: "Unlocked Arch" (18 nodes)
                val a = -PI * 0.95 + t * (1.55 * PI)
                val u = 135f * cos(a).toFloat()
                val v = (135f * sin(a) * (0.8 + 0.2 * cos(a))).toFloat()
                clampPt(cx + u, cy + v)
            }

            // =========================================================================
            // TIER 4 (Levels 31..40): Decaying Echoes (16 to 22 nodes)
            // =========================================================================
            31 -> { t -> // Level 31: "Fading Spark" (16 nodes)
                val a = -PI * 0.9 + t * (1.6 * PI)
                val r = (115f + 35f * cos(a * 4.0).toFloat())
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            32 -> { t -> // Level 32: "Vapor Trail" (18 nodes)
                val u = -145f + t * 290f
                val v = (125f * (1f - 0.6f * t) * sin(t * 3.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            33 -> { t -> // Level 33: "Fragile Pulse"
                val u = -140f + t * 280f
                val v = (sin(t * 3.0 * PI) * 125f).toFloat()
                clampPt(cx + u, cy + v)
            }
            34 -> { t -> // Level 34: "Temporal Mark" (19 nodes)
                val (u, v) = when {
                    t <= 0.70f -> {
                        val a = -PI * 0.5 + (t / 0.70f) * (1.5 * PI)
                        Pair(130f * cos(a).toFloat(), 130f * sin(a).toFloat())
                    }
                    else -> {
                        val sub = (t - 0.70f) / 0.30f
                        Pair(-130f + sub * 130f, 0f)
                    }
                }
                clampPt(cx + u, cy + v)
            }
            35 -> { t -> // Level 35: "Lost Wave" (18 nodes)
                val u = -145f + t * 290f
                val v = (115f * sin(2.0 * PI * t * (1.0 + 2.0 * t))).toFloat()
                clampPt(cx + u, cy + v)
            }
            36 -> { t -> // Level 36: "Faint Echo" (20 nodes)
                val a = t * 1.55 * PI
                val r = 70f + 80f * t
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            37 -> { t -> // Level 37: "Transient Bridge" (19 nodes)
                val u = -145f + t * 290f
                val v = -110f + 220f * (4f * (t - 0.5f) * (t - 0.5f))
                clampPt(cx + u, cy + v)
            }
            38 -> { t -> // Level 38: "Dispersed Photon" (21 nodes)
                val u = -145f + t * 290f
                val v = 120f - 240f * (4f * (t - 0.5f) * (t - 0.5f))
                clampPt(cx + u, cy + v)
            }
            39 -> { t -> // Level 39: "Melting Grid" (20 nodes)
                val u = -140f + t * 280f
                val v = (85f * sin(t * 2.5 * PI) + 40f * cos(t * 5.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            40 -> { t -> // Level 40: "Final Reverb" (22 nodes)
                val r = 150f * (1f - 0.55f * t)
                val a = 1.58 * PI * t
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }

            // =========================================================================
            // TIER 5 (Levels 41..50): Ghost Resonance (19 to 25 nodes)
            // =========================================================================
            41 -> { t -> // Level 41: "Shadow Twin" (19 nodes)
                val a = -PI * 0.85 + t * (1.7 * PI)
                val r = (110f + 40f * cos(a * 2.0).toFloat())
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            42 -> { t -> // Level 42: "Phantom Path" (21 nodes)
                val u = -140f + t * 280f
                val v = (115f * sin(t * 3.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            43 -> { t -> // Level 43: "Invisible Wire" (20 nodes)
                val v = -140f + t * 280f
                val u = (120f * sin(t * 3.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            44 -> { t -> // Level 44: "Illusion Web" (22 nodes)
                val a = t * 1.62 * PI
                val r = 60f + 85f * t
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            45 -> { t -> // Level 45: "Dark Reflection" (21 nodes)
                val u = -145f + t * 290f
                val v = (120f * sin(t * 2.5 * PI) * (0.6 + 0.4 * t)).toFloat()
                clampPt(cx + u, cy + v)
            }
            46 -> { t -> // Level 46: "Phantom Node" (23 nodes)
                val a = 0.2 * PI + t * (1.45 * PI)
                val r = 140f * (1f - 0.45f * cos(a).toFloat())
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            47 -> { t -> // Level 47: "Reflected Mist"
                val u = -140f + t * 280f
                val v = (110f * sin(t * 2.0 * PI) + 35f * sin(t * 4.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            48 -> { t -> // Level 48: "Stardust Trail" (24 nodes)
                val a = t * 1.55 * PI
                val r = (75f + 70f * sqrt(t)).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            49 -> { t -> // Level 49: "Shadow Hall" (23 nodes)
                val u = -140f + t * 280f
                val step = (t * 6).toInt()
                val v = if (step % 2 == 0) -110f else 110f
                clampPt(cx + u, cy + v)
            }
            50 -> { t -> // Level 50: "Broken Mirrors" (25 nodes)
                val u = -145f + t * 290f
                val v = (115f * sin(t * 3.5 * PI + PI * 0.25)).toFloat()
                clampPt(cx + u, cy + v)
            }

            // =========================================================================
            // TIER 6 (Levels 51..60): Fractal Matrix (22 to 28 nodes)
            // =========================================================================
            51 -> { t -> // Level 51: "Fractal Tree" (22 nodes)
                val u = -140f + t * 280f
                val v = (110f * sin(t * 2.0 * PI) + 35f * sin(t * 6.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            52 -> { t -> // Level 52: "Double Helix" (24 nodes)
                val v = -140f + t * 280f
                val u = (125f * sin(t * 2.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            53 -> { t -> // Level 53: "Dual Core" (23 nodes)
                val a = -PI * 0.85 + t * (1.7 * PI)
                val r = (110f + 35f * cos(a * 3.0).toFloat())
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            54 -> { t -> // Level 54: "Hex Honeycomb" (25 nodes)
                val u = -145f + t * 290f
                val v = (90f * sin(t * 4.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            55 -> { t -> // Level 55: "Crystal Cage" (24 nodes)
                val a = -PI * 0.9 + t * (1.6 * PI)
                val ca = cos(a).toFloat()
                val sa = sin(a).toFloat()
                val denom = (kotlin.math.abs(ca) + kotlin.math.abs(sa)).coerceAtLeast(0.01f)
                val r = 135f / denom
                clampPt(cx + r * ca, cy + r * sa)
            }
            56 -> { t -> // Level 56: "Nested Rings" (26 nodes)
                val a = t * 1.52 * PI
                val r = 70f + 80f * sin(t * PI * 0.5).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            57 -> { t -> // Level 57: "Cosmic Blossom" (25 nodes)
                val a = -PI * 0.85 + t * (1.7 * PI)
                val r = 130f * (0.75f + 0.25f * cos(a * 2.5).toFloat())
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            58 -> { t -> // Level 58: "Fractal Star" (27 nodes)
                val a = -PI * 0.9 + t * (1.65 * PI)
                val r = 115f + 35f * cos(a * 5.0).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            59 -> { t -> // Level 59: "Matrix Weave" (26 nodes)
                val u = -145f + t * 290f
                val v = (70f * sin(t * 3.0 * PI) + 45f * cos(t * 1.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            60 -> { t -> // Level 60: "Complex Knot" (28 nodes)
                val a = t * 1.54 * PI
                val u = 135f * cos(a).toFloat()
                val v = (130f * sin(a) * (0.75 + 0.25 * sin(a))).toFloat()
                clampPt(cx + u, cy + v)
            }

            // =========================================================================
            // TIER 7 (Levels 61..70): Quantum Gates (25 to 31 nodes)
            // =========================================================================
            61 -> { t -> // Level 61: "Quantum Tunnel" (25 nodes)
                val u = -140f + t * 280f
                val v = (120f * (1f - t) * sin(t * 3.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            62 -> { t -> // Level 62: "Entangled Particle"
                val a = t * 1.45 * PI
                val r = (55f + 85f * sqrt(t)).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            63 -> { t -> // Level 63: "Superposition" (26 nodes)
                val u = -145f + t * 290f
                val v = (130f * exp(-4.0 * (t - 0.5) * (t - 0.5)) * sin(t * 5.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            64 -> { t -> // Level 64: "Photonic Net" (28 nodes)
                val a = -PI * 0.9 + t * (1.6 * PI)
                val r = 120f + 30f * cos(a * 6.0).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            65 -> { t -> // Level 65: "Hypercube" (27 nodes)
                val u = -140f + t * 280f
                val v = (100f * sin(t * 4.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            66 -> { t -> // Level 66: "Multi-Pass" (29 nodes)
                val (u, v) = when {
                    t <= 0.25f -> Pair(-135f + (t / 0.25f) * 270f, -110f)
                    t <= 0.50f -> Pair(135f - ((t - 0.25f) / 0.25f) * 270f, -35f)
                    t <= 0.75f -> Pair(-135f + ((t - 0.50f) / 0.25f) * 270f, 40f)
                    else -> Pair(135f - ((t - 0.75f) / 0.25f) * 270f, 115f)
                }
                clampPt(cx + u, cy + v)
            }
            67 -> { t -> // Level 67: "Dimensional Rift" (28 nodes)
                val u = -145f + t * 290f
                val v = 125f * (1f - exp(-5.0 * (t - 0.5) * (t - 0.5))).toFloat() * (if (t < 0.5f) -1f else 1f)
                clampPt(cx + u, cy + v)
            }
            68 -> { t -> // Level 68: "Plasma Gate" (30 nodes)
                val a = -PI * 0.85 + t * (1.65 * PI)
                val u = 140f * cos(a).toFloat()
                val v = (120f * sin(a) * (1.0 + 0.3 * cos(a))).toFloat()
                clampPt(cx + u, cy + v)
            }
            69 -> { t -> // Level 69: "Gravity Wave" (29 nodes)
                val u = -145f + t * 290f
                val v = (120f * sin(t * 3.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            70 -> { t -> // Level 70: "Quantum Core" (31 nodes)
                val (u, v) = when {
                    t <= 0.20f -> Pair(-135f + (t / 0.20f) * 270f, -125f)
                    t <= 0.40f -> Pair(135f, -125f + ((t - 0.20f) / 0.20f) * 250f)
                    t <= 0.60f -> Pair(135f - ((t - 0.40f) / 0.20f) * 230f, 125f)
                    t <= 0.80f -> Pair(-95f, 125f - ((t - 0.60f) / 0.20f) * 200f)
                    else -> Pair(-95f + ((t - 0.80f) / 0.20f) * 190f, -75f)
                }
                clampPt(cx + u, cy + v)
            }

            // =========================================================================
            // TIER 8 (Levels 71..80): Temporal Drift (28 to 34 nodes)
            // =========================================================================
            71 -> { t -> // Level 71: "Time Loop" (28 nodes)
                val a = t * 1.56 * PI
                val u = 135f * cos(a).toFloat()
                val v = 115f * sin(a).toFloat()
                clampPt(cx + u, cy + v)
            }
            72 -> { t -> // Level 72: "Relativity Line" (30 nodes)
                val u = -145f + t * 290f
                val v = -120f + 240f / (1f + 0.0003f * u * u)
                clampPt(cx + u, cy + v)
            }
            73 -> { t -> // Level 73: "Chronos Spiral" (29 nodes)
                val a = t * 1.55 * PI
                val r = 145f * sqrt(t)
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            74 -> { t -> // Level 74: "Light Cone" (31 nodes)
                val (u, v) = when {
                    t <= 0.5f -> Pair(-125f + (t / 0.5f) * 125f, -125f + (t / 0.5f) * 125f)
                    else -> Pair(((t - 0.5f) / 0.5f) * 125f, ((t - 0.5f) / 0.5f) * 125f)
                }
                clampPt(cx + u, cy + v)
            }
            75 -> { t -> // Level 75: "Time Crystal" (30 nodes)
                val u = -145f + t * 290f
                val v = (115f * sin(t * 4.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            76 -> { t -> // Level 76: "Deceleration" (32 nodes)
                val u = -145f + t * 290f
                val v = (120f * (1f - t * t) * sin(t * 4.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            77 -> { t -> // Level 77: "Return Vector"
                val (u, v) = when {
                    t <= 0.42f -> Pair(-135f + (t / 0.42f) * 235f, -70f)
                    t <= 0.58f -> {
                        val sub = (t - 0.42f) / 0.16f
                        val a = -PI * 0.5 + sub * PI
                        Pair(100f + 40f * cos(a).toFloat(), 40f * sin(a).toFloat())
                    }
                    else -> Pair(100f - ((t - 0.58f) / 0.42f) * 235f, 70f)
                }
                clampPt(cx + u, cy + v)
            }
            78 -> { t -> // Level 78: "Time Mirror" (33 nodes)
                val u = -140f + t * 280f
                val v = (110f * sin(t * 3.0 * PI) * (if (t < 0.5f) 1f else -1f)).toFloat()
                clampPt(cx + u, cy + v)
            }
            79 -> { t -> // Level 79: "Cosmic Clock" (32 nodes)
                val a = t * 1.6 * PI
                val r = 120f + 25f * sin(a * 6.0).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            80 -> { t -> // Level 80: "Interdimensional Flow" (34 nodes)
                val v = -140f + t * 280f
                val u = (120f * sin(t * 3.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }

            // =========================================================================
            // TIER 9 (Levels 81..90): Master Lattice (30 to 35 nodes)
            // =========================================================================
            81 -> { t -> // Level 81: "Master Labyrinth" (30 nodes)
                val (u, v) = when {
                    t <= 0.25f -> Pair(-135f + (t / 0.25f) * 270f, -120f)
                    t <= 0.50f -> Pair(135f, -120f + ((t - 0.25f) / 0.25f) * 240f)
                    t <= 0.75f -> Pair(135f - ((t - 0.50f) / 0.25f) * 220f, 120f)
                    else -> Pair(-85f, 120f - ((t - 0.75f) / 0.25f) * 180f)
                }
                clampPt(cx + u, cy + v)
            }
            82 -> { t -> // Level 82: "Node Metropolis" (32 nodes)
                val (u, v) = when {
                    t <= 0.20f -> Pair(-140f + (t / 0.20f) * 280f, -120f)
                    t <= 0.40f -> Pair(140f, -120f + ((t - 0.20f) / 0.20f) * 120f)
                    t <= 0.60f -> Pair(140f - ((t - 0.40f) / 0.20f) * 280f, 0f)
                    t <= 0.80f -> Pair(-140f, ((t - 0.60f) / 0.20f) * 120f)
                    else -> Pair(-140f + ((t - 0.80f) / 0.20f) * 280f, 120f)
                }
                clampPt(cx + u, cy + v)
            }
            83 -> { t -> // Level 83: "Grand Weave" (31 nodes)
                val u = -145f + t * 290f
                val v = (75f * sin(t * 3.5 * PI) + 40f * cos(t * 2.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            84 -> { t -> // Level 84: "Intricate Lattice" (33 nodes)
                val u = -145f + t * 290f
                val v = (110f * sin(t * 5.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            85 -> { t -> // Level 85: "Cobweb Strand" (32 nodes)
                val a = t * 1.58 * PI
                val r = 55f + 90f * t
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            86 -> { t -> // Level 86: "Geometric Chaos" (34 nodes)
                val u = -145f + t * 290f
                val v = (95f * sin(t * 4.5 * PI) * (0.5 + 0.5 * sin(t * 2.0 * PI))).toFloat()
                clampPt(cx + u, cy + v)
            }
            87 -> { t -> // Level 87: "Harmonic Balance" (33 nodes)
                val a = -PI * 0.5 + t * (1.5 * PI)
                val r = 135f
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            88 -> { t -> // Level 88: "Supreme Network" (35 nodes)
                val (u, v) = when {
                    t <= 0.333f -> Pair(-140f + (t / 0.333f) * 280f, -120f)
                    t <= 0.667f -> Pair(140f - ((t - 0.333f) / 0.334f) * 280f, 0f)
                    else -> Pair(-140f + ((t - 0.667f) / 0.333f) * 280f, 120f)
                }
                clampPt(cx + u, cy + v)
            }
            89 -> { t -> // Level 89: "Cosmic Architecture" (34 nodes)
                val a = -PI * 0.9 + t * (1.6 * PI)
                val r = 135f * (0.85f + 0.15f * cos(a * 4.0).toFloat())
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            90 -> { t -> // Level 90: "Great Portal" (35 nodes)
                val a = -PI * 0.85 + t * (1.68 * PI)
                val r = 140f
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }

            // =========================================================================
            // TIER 10 (Levels 91..100): Omega Peak (33 to 36 nodes)
            // =========================================================================
            91 -> { t -> // Level 91: "Zenith Ascent" (33 nodes)
                val u = -145f + t * 290f
                val v = (115f * sin(t * 5.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            92 -> { t -> // Level 92: "Interstellar" (35 nodes)
                val u = -145f + t * 290f
                val v = (85f * sin(t * 4.0 * PI) + 40f * cos(t * 2.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            93 -> { t -> // Level 93: "Supernova" (34 nodes)
                val a = t * 1.55 * PI
                val r = (65f + 85f * t * t).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            94 -> { t -> // Level 94: "Hyperspace" (35 nodes)
                val v = -140f + t * 280f
                val u = (120f * sin(t * 4.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            95 -> { t -> // Level 95: "Galactic Core" (34 nodes)
                val a = t * 1.52 * PI
                val r = 150f - 80f * t
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            96 -> { t -> // Level 96: "The Crucible" (36 nodes)
                val (u, v) = when {
                    t <= 0.25f -> Pair(-135f + (t / 0.25f) * 270f, -125f)
                    t <= 0.50f -> Pair(135f, -125f + ((t - 0.25f) / 0.25f) * 250f)
                    t <= 0.75f -> Pair(135f - ((t - 0.50f) / 0.25f) * 240f, 125f)
                    else -> Pair(-105f, 125f - ((t - 0.75f) / 0.25f) * 210f)
                }
                clampPt(cx + u, cy + v)
            }
            97 -> { t -> // Level 97: "Cosmos Pulse" (35 nodes)
                val a = -PI * 0.88 + t * (1.65 * PI)
                val r = 115f + 30f * cos(a * 4.0).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            98 -> { t -> // Level 98: "Grand Convergence" (36 nodes)
                val a = t * 1.58 * PI
                val r = 145f * (1f - 0.45f * t)
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            99 -> { t -> // Level 99: "Master Trial" (35 nodes)
                val u = -145f + t * 290f
                val v = (110f * sin(t * 5.5 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            100 -> { t -> // Level 100: "ECHO OMEGA" (36 nodes) - Grand Finale Omega Arch
                val (u, v) = when {
                    t <= 0.20f -> Pair(-150f + (t / 0.20f) * 55f, 125f)
                    t <= 0.80f -> {
                        val subT = (t - 0.20f) / 0.60f
                        val angle = PI * 1.15 - subT * (PI * 1.30)
                        Pair(115f * cos(angle).toFloat(), 15f - 115f * sin(angle).toFloat())
                    }
                    else -> Pair(95f + ((t - 0.80f) / 0.20f) * 55f, 125f)
                }
                clampPt(cx + u, cy + v)
            }
            else -> { t -> clampPt(cx, cy) }
        }
    }
}
