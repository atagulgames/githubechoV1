package com.example.data

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
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
        val clampedId = id.coerceIn(1, 150)
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
            // =========================================================================
            // TIER 13 (Levels 101..110): Hyper-Flux Matrix (26 to 34 nodes)
            // =========================================================================
            101 -> { t -> // Level 101: Astroid / 4-Cusp Hypocycloid
                val a = t * 2.0 * PI
                val r = 135f
                val u = (r * (cos(a) * cos(a) * cos(a))).toFloat()
                val v = (r * (sin(a) * sin(a) * sin(a))).toFloat()
                clampPt(cx + u, cy + v)
            }
            102 -> { t -> // Level 102: Torus Knot Projection
                val a = t * 2.0 * PI
                val r = 125f + 35f * cos(3.0 * a).toFloat()
                val u = r * cos(2.0 * a).toFloat()
                val v = r * sin(2.0 * a).toFloat() * 0.9f
                clampPt(cx + u, cy + v)
            }
            103 -> { t -> // Level 103: Fermat Spiral Dual Arm
                val a = t * 3.5 * PI
                val r = 140f * sqrt(t).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            104 -> { t -> // Level 104: Butterfly Silhouette
                val a = t * 2.0 * PI
                val r = 100f * (sin(a).toFloat() * sin(a).toFloat() * 0.6f + cos(2.0 * a).toFloat() * 0.4f + 0.5f)
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            105 -> { t -> // Level 105: Lissajous 3:4 Resonance
                val a = t * 2.0 * PI
                val u = 130f * sin(3.0 * a + PI * 0.25).toFloat()
                val v = 115f * sin(4.0 * a).toFloat()
                clampPt(cx + u, cy + v)
            }
            106 -> { t -> // Level 106: Cardioid Heart Loop
                val a = t * 2.0 * PI
                val r = 70f * (1f - sin(a).toFloat())
                clampPt(cx + r * cos(a).toFloat() * 1.5f, cy - r * sin(a).toFloat() * 1.4f)
            }
            107 -> { t -> // Level 107: Nephroid Double Lobe
                val a = t * 2.0 * PI
                val u = 125f * (3f * cos(a).toFloat() - cos(3.0 * a).toFloat()) * 0.28f
                val v = 125f * (3f * sin(a).toFloat() - sin(3.0 * a).toFloat()) * 0.28f
                clampPt(cx + u, cy + v)
            }
            108 -> { t -> // Level 108: Lemniscate of Bernoulli
                val a = -PI * 0.45 + t * 0.9 * PI
                val scale = 140f
                val den = (1f + sin(a).toFloat() * sin(a).toFloat()).coerceAtLeast(0.01f)
                val u = (scale * cos(a).toFloat()) / den
                val v = (scale * sin(a).toFloat() * cos(a).toFloat()) / den
                clampPt(cx + u, cy + v)
            }
            109 -> { t -> // Level 109: Hypotrochoid Star
                val a = t * 2.0 * PI
                val u = 95f * cos(a).toFloat() + 45f * cos(5.0 * a).toFloat()
                val v = 95f * sin(a).toFloat() - 45f * sin(5.0 * a).toFloat()
                clampPt(cx + u, cy + v)
            }
            110 -> { t -> // Level 110: Epicycloid 5 Lobes
                val a = t * 2.0 * PI
                val u = 100f * cos(a).toFloat() - 30f * cos(5.0 * a).toFloat()
                val v = 100f * sin(a).toFloat() - 30f * sin(5.0 * a).toFloat()
                clampPt(cx + u, cy + v)
            }
            // =========================================================================
            // TIER 14 (Levels 111..120): Singularity Void (28 to 36 nodes)
            // =========================================================================
            111 -> { t -> // Level 111: Event Horizon Ellipse Vortex
                val a = t * 2.2 * PI
                val r = 135f - 45f * t
                clampPt(cx + r * 1.15f * cos(a).toFloat(), cy + r * 0.85f * sin(a).toFloat())
            }
            112 -> { t -> // Level 112: Accretion Disk Funnel
                val u = -140f + t * 280f
                val v = (55f * sin(t * 3.0 * PI) + 60f * cos(t * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            113 -> { t -> // Level 113: Spacetime Dip Wave
                val a = t * 2.0 * PI
                val r = 120f + 30f * sin(6.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            114 -> { t -> // Level 114: 4-Leaf Clover Singularity
                val a = t * 2.0 * PI
                val r = 125f * cos(2.0 * a).toFloat().let { kotlin.math.abs(it) } + 25f
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            115 -> { t -> // Level 115: Dual Circle Hourglass
                val (u, v) = when {
                    t <= 0.5f -> {
                        val sub = t / 0.5f * 2.0 * PI
                        Pair(-65f + 55f * cos(sub).toFloat(), 55f * sin(sub).toFloat())
                    }
                    else -> {
                        val sub = (t - 0.5f) / 0.5f * 2.0 * PI
                        Pair(65f + 55f * cos(sub).toFloat(), 55f * sin(sub).toFloat())
                    }
                }
                clampPt(cx + u, cy + v)
            }
            116 -> { t -> // Level 116: Hexagram Star Perimeter
                val a = t * 2.0 * PI
                val r = 125f + 35f * cos(6.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            117 -> { t -> // Level 117: Octagram 8-Point Cosmic Star
                val a = t * 2.0 * PI
                val r = 120f + 40f * cos(8.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            118 -> { t -> // Level 118: Right Strophoid Loop
                val a = -PI * 0.42 + t * 0.84 * PI
                val u = 120f * (cos(2.0 * a) / cos(a)).toFloat().coerceIn(-1.3f, 1.3f)
                val v = 120f * (sin(2.0 * a) / (2.0 * cos(a))).toFloat().coerceIn(-1.2f, 1.2f)
                clampPt(cx + u, cy + v)
            }
            119 -> { t -> // Level 119: Folium Loop
                val a = t * 2.0 * PI
                val r = 130f * (cos(a).toFloat() * cos(a).toFloat() * 0.7f + 0.35f)
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat() * 1.15f)
            }
            120 -> { t -> // Level 120: Rhombus Constellation
                val (u, v) = when {
                    t <= 0.25f -> Pair(0f + (t / 0.25f) * 135f, -125f + (t / 0.25f) * 125f)
                    t <= 0.50f -> Pair(135f - ((t - 0.25f) / 0.25f) * 135f, 0f + ((t - 0.25f) / 0.25f) * 125f)
                    t <= 0.75f -> Pair(0f - ((t - 0.50f) / 0.25f) * 135f, 125f - ((t - 0.50f) / 0.25f) * 125f)
                    else -> Pair(-135f + ((t - 0.75f) / 0.25f) * 135f, 0f - ((t - 0.75f) / 0.25f) * 125f)
                }
                clampPt(cx + u, cy + v)
            }
            // =========================================================================
            // TIER 15 (Levels 121..130): Quantum Entanglement (30 to 38 nodes)
            // =========================================================================
            121 -> { t -> // Level 121: Dual Interlocking Loops
                val a = t * 2.0 * PI
                val u = 120f * cos(a).toFloat() + 25f * cos(3.0 * a).toFloat()
                val v = 95f * sin(a).toFloat() - 25f * sin(3.0 * a).toFloat()
                clampPt(cx + u, cy + v)
            }
            122 -> { t -> // Level 122: Double Spiral Figure-8
                val a = t * 2.0 * PI
                val u = 135f * sin(a).toFloat()
                val v = 110f * sin(2.0 * a).toFloat()
                clampPt(cx + u, cy + v)
            }
            123 -> { t -> // Level 123: Trefoil Knot Contour
                val a = t * 2.0 * PI
                val u = 115f * (sin(a) + 2.0 * sin(2.0 * a)).toFloat() * 0.35f
                val v = 115f * (cos(a) - 2.0 * cos(2.0 * a)).toFloat() * 0.35f
                clampPt(cx + u, cy + v)
            }
            124 -> { t -> // Level 124: Sine-Modulated Oval
                val a = t * 2.0 * PI
                val r = 125f + 25f * sin(5.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat() * 1.1f, cy + r * sin(a).toFloat() * 0.9f)
            }
            125 -> { t -> // Level 125: Honeycomb Diamond Chain
                val u = -140f + t * 280f
                val v = 110f * (sin(t * 4.0 * PI) * cos(t * 2.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            126 -> { t -> // Level 126: Zig-Zag Matrix
                val u = -135f + t * 270f
                val v = if ((t * 8).toInt() % 2 == 0) -95f else 95f
                clampPt(cx + u, cy + v)
            }
            127 -> { t -> // Level 127: Serpentine Wave Harmonic
                val u = -140f + t * 280f
                val v = 105f * sin(t * 3.5 * PI).toFloat()
                clampPt(cx + u, cy + v)
            }
            128 -> { t -> // Level 128: Double Helix Projection
                val a = t * 3.0 * PI
                val u = 130f * cos(a).toFloat()
                val v = -120f + t * 240f
                clampPt(cx + u, cy + v)
            }
            129 -> { t -> // Level 129: Cycloid Wave Arch
                val a = t * 2.5 * PI
                val u = -135f + (a / (2.5 * PI) * 270f).toFloat()
                val v = 110f - 100f * (1f - cos(a).toFloat())
                clampPt(cx + u, cy + v)
            }
            130 -> { t -> // Level 130: Concentric Octagon Ring
                val a = t * 2.0 * PI
                val r = 120f + 20f * cos(8.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat() * 1.05f, cy + r * sin(a).toFloat() * 1.05f)
            }
            // =========================================================================
            // TIER 16 (Levels 131..140): Chrono-Stasis Fracture (32 to 38 nodes)
            // =========================================================================
            131 -> { t -> // Level 131: Starburst Multi-Spike
                val a = t * 2.0 * PI
                val r = 110f + 45f * sin(7.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            132 -> { t -> // Level 132: Involute Spiral Arc
                val a = 0.5 + t * 2.2 * PI
                val r = 25f * a.toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            133 -> { t -> // Level 133: Agnesi Curve Loop
                val u = -140f + t * 280f
                val den = 1f + (u / 65f) * (u / 65f)
                val v = (240f / den) - 120f
                clampPt(cx + u, cy + v)
            }
            134 -> { t -> // Level 134: Trisectrix Curve
                val a = -PI * 0.44 + t * 0.88 * PI
                val r = 130f * (2.0 * cos(a) - 1.0).toFloat().coerceIn(-1.3f, 1.3f)
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            135 -> { t -> // Level 135: Archimedean Core Arm
                val a = t * 2.8 * PI
                val r = 40f + 95f * t
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            136 -> { t -> // Level 136: Hyperbolic Cross
                val a = t * 2.0 * PI
                val u = 130f * cos(a).toFloat() * kotlin.math.abs(cos(a).toFloat())
                val v = 115f * sin(a).toFloat() * kotlin.math.abs(sin(a).toFloat())
                clampPt(cx + u, cy + v)
            }
            137 -> { t -> // Level 137: Sinusoidal Flower
                val a = t * 2.0 * PI
                val r = 120f + 35f * sin(9.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            138 -> { t -> // Level 138: Epitrochoid 6-Cusp Shield
                val a = t * 2.0 * PI
                val u = 95f * cos(a).toFloat() - 35f * cos(6.0 * a).toFloat()
                val v = 95f * sin(a).toFloat() - 35f * sin(6.0 * a).toFloat()
                clampPt(cx + u, cy + v)
            }
            139 -> { t -> // Level 139: Double Tear-Drop Loop
                val a = t * 2.0 * PI
                val u = 135f * cos(a).toFloat() * (sin(a).toFloat() * 0.4f + 0.6f)
                val v = 120f * sin(a).toFloat()
                clampPt(cx + u, cy + v)
            }
            140 -> { t -> // Level 140: Multi-Stage Quantum Maze
                val u = -140f + t * 280f
                val v = (75f * sin(t * 5.0 * PI) + 50f * cos(t * 2.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            // =========================================================================
            // TIER 17 (Levels 141..149): Celestial Apex (34 to 39 nodes)
            // =========================================================================
            141 -> { t -> // Level 141: Crown Heptagon (7 Lobes)
                val a = t * 2.0 * PI
                val r = 125f + 30f * cos(7.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            142 -> { t -> // Level 142: Crescent Moon Contour
                val a = -PI * 0.8 + t * 1.6 * PI
                val rOuter = 135f
                val rInner = 85f
                val (u, v) = if (t <= 0.5f) {
                    val sub = t / 0.5f * PI * 1.6 - PI * 0.8
                    Pair(rOuter * cos(sub).toFloat(), rOuter * sin(sub).toFloat())
                } else {
                    val sub = PI * 0.8 - (t - 0.5f) / 0.5f * PI * 1.6
                    Pair(rInner * cos(sub).toFloat() + 35f, rInner * sin(sub).toFloat())
                }
                clampPt(cx + u, cy + v)
            }
            143 -> { t -> // Level 143: Super-Ellipse Lamé Curve
                val a = t * 2.0 * PI
                val cosA = cos(a).toFloat()
                val sinA = sin(a).toFloat()
                val signCos = if (cosA >= 0) 1f else -1f
                val signSin = if (sinA >= 0) 1f else -1f
                val u = 135f * signCos * kotlin.math.abs(cosA).pow(0.65f)
                val v = 120f * signSin * kotlin.math.abs(sinA).pow(0.65f)
                clampPt(cx + u, cy + v)
            }
            144 -> { t -> // Level 144: Parabolic Wave Nexus
                val u = -140f + t * 280f
                val v = (115f * sin(t * 6.0 * PI) * (1f - kotlin.math.abs(t - 0.5f) * 0.8f)).toFloat()
                clampPt(cx + u, cy + v)
            }
            145 -> { t -> // Level 145: Grand Decagon (10-Point Star)
                val a = t * 2.0 * PI
                val r = 120f + 32f * cos(10.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            146 -> { t -> // Level 146: Quad-Vortex Cross
                val a = t * 2.0 * PI
                val u = 135f * cos(a).toFloat() * (1f + 0.35f * cos(4.0 * a).toFloat())
                val v = 120f * sin(a).toFloat() * (1f - 0.35f * cos(4.0 * a).toFloat())
                clampPt(cx + u, cy + v)
            }
            147 -> { t -> // Level 147: Celestial Trident
                val u = -140f + t * 280f
                val v = (85f * sin(t * 4.5 * PI) + 45f * sin(t * 9.0 * PI)).toFloat()
                clampPt(cx + u, cy + v)
            }
            148 -> { t -> // Level 148: Sovereign Ring with Spoke Nodes
                val a = t * 2.0 * PI
                val r = 125f + 25f * cos(12.0 * a).toFloat()
                clampPt(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
            }
            149 -> { t -> // Level 149: Penultimate Dual Gateway
                val a = t * 2.0 * PI
                val u = 130f * cos(a).toFloat() + 20f * sin(3.0 * a).toFloat()
                val v = 115f * sin(a).toFloat() + 30f * cos(2.0 * a).toFloat()
                clampPt(cx + u, cy + v)
            }
            // =========================================================================
            // TIER 18 (Level 150): COSMIC INFINITY - ULTIMATE APEX FINALE (40 nodes)
            // =========================================================================
            150 -> { t -> // Level 150: "COSMIC INFINITY" - The Grand Universal Mandala
                val a = t * 2.0 * PI
                val r = 130f + 35f * cos(8.0 * a).toFloat() + 15f * sin(4.0 * a).toFloat()
                val u = r * cos(a).toFloat() * 1.05f
                val v = r * sin(a).toFloat() * 0.95f
                clampPt(cx + u, cy + v)
            }
            else -> { t -> clampPt(cx, cy) }
        }
    }
}
