package com.example.data

import com.example.data.local.LevelEntity
import com.example.localization.Language
import com.example.model.DirectedEdge
import com.example.model.LevelData
import com.example.model.LevelNode
import com.example.model.NodeType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

object LevelCatalog {

    const val TOTAL_LEVELS = 100

    private val TIER_NAMES_EN = listOf(
        "Harmonic Genesis", // Tier 1 (1..10)
        "Vector Flow",       // Tier 2 (11..20)
        "Lock & Key",        // Tier 3 (21..30)
        "Decaying Echoes",   // Tier 4 (31..40)
        "Ghost Resonance",   // Tier 5 (41..50)
        "Fractal Matrix",    // Tier 6 (51..60)
        "Quantum Gates",     // Tier 7 (61..70)
        "Temporal Drift",    // Tier 8 (71..80)
        "Master Lattice",    // Tier 9 (81..90)
        "Omega Peak"         // Tier 10 (91..100)
    )

    private val TIER_NAMES_TR = listOf(
        "Harmonik Başlangıç", // Tier 1 (1..10)
        "Vektör Akışı",       // Tier 2 (11..20)
        "Kilit & Anahtar",    // Tier 3 (21..30)
        "Sönen Dalgalar",     // Tier 4 (31..40)
        "Gölge Rezonansı",    // Tier 5 (41..50)
        "Fraktal Matris",     // Tier 6 (51..60)
        "Kuantum Kapıları",   // Tier 7 (61..70)
        "Zamansal Akış",      // Tier 8 (71..80)
        "Usta Ağları",        // Tier 9 (81..90)
        "Omega Zirvesi"       // Tier 10 (91..100)
    )

    private val UNIQUE_NAMES_EN = listOf(
        // Tier 1 (1..10)
        "Pure Triangle", "Golden Square", "Resonance Pentagon", "Crystal Hexagon", "Bright Star",
        "Prism Dome", "Twin Loops", "Lapis Ring", "Aura Spark", "Geometric Matrix",
        // Tier 2 (11..20)
        "Vector Stream", "Polar Arc", "Spiral Orbit", "Nebula Spine", "Cosmic Needle",
        "Ion Net", "Vortex Ray", "Zenith Beam", "Pyramid Line", "Infinity Curve",
        // Tier 3 (21..30)
        "Copper Vault", "Silver Gateway", "Sapphire Key", "Ruby Barrier", "Emerald Gate",
        "Mystic Maze", "Hidden Sanctum", "Sacred Portal", "Crystal Seal", "Unlocked Arch",
        // Tier 4 (31..40)
        "Fading Spark", "Vapor Trail", "Fragile Pulse", "Temporal Mark", "Lost Wave",
        "Faint Echo", "Transient Bridge", "Dispersed Photon", "Melting Grid", "Final Reverb",
        // Tier 5 (41..50)
        "Shadow Twin", "Phantom Path", "Invisible Wire", "Illusion Web", "Dark Reflection",
        "Phantom Node", "Reflected Mist", "Stardust Trail", "Shadow Hall", "Broken Mirrors",
        // Tier 6 (51..60)
        "Fractal Tree", "Double Helix", "Dual Core", "Hex Honeycomb", "Crystal Cage",
        "Nested Rings", "Cosmic Blossom", "Fractal Star", "Matrix Weave", "Complex Knot",
        // Tier 7 (61..70)
        "Quantum Tunnel", "Entangled Particle", "Superposition", "Photonic Net", "Hypercube",
        "Multi-Pass", "Dimensional Rift", "Plasma Gate", "Gravity Wave", "Quantum Core",
        // Tier 8 (71..80)
        "Time Loop", "Relativity Line", "Chronos Spiral", "Light Cone", "Time Crystal",
        "Deceleration", "Return Vector", "Time Mirror", "Cosmic Clock", "Interdimensional Flow",
        // Tier 9 (81..90)
        "Master Labyrinth", "Node Metropolis", "Grand Weave", "Intricate Lattice", "Cobweb Strand",
        "Geometric Chaos", "Harmonic Balance", "Supreme Network", "Cosmic Architecture", "Great Portal",
        // Tier 10 (91..100)
        "Zenith Ascent", "Interstellar", "Supernova", "Hyperspace", "Galactic Core",
        "The Crucible", "Cosmos Pulse", "Grand Convergence", "Master Trial", "ECHO OMEGA"
    )

    private val UNIQUE_NAMES_TR = listOf(
        // Tier 1 (1..10)
        "Saf Üçgen", "Altın Kare", "Rezonans Beşgeni", "Kristal Altıgen", "Parlak Yıldız",
        "Prizma Kubbesi", "Çifte Halka", "Lapis Çemberi", "Aura Kristali", "Geometrik Matris",
        // Tier 2 (11..20)
        "Vektör Akışı", "Polar Yay", "Sarmal Yörünge", "Nebula Omurgası", "Kozmik İğne",
        "İyon Ağı", "Girdap Vektörü", "Zirve Işını", "Piramit Hattı", "Sonsuzluk Yayı",
        // Tier 3 (21..30)
        "Bakır Kilit", "Gümüş Geçit", "Safir Anahtar", "Yakut Bariyer", "Zümrüt Kapı",
        "Gizemli Labirent", "Gizli Mahzen", "Kutsal Portal", "Kristal Mühür", "Açılan Kemer",
        // Tier 4 (31..40)
        "Sönen Kıvılcım", "Buharlaşan Hat", "Kırılgan Rezonans", "Zamansal İz", "Kayıp Dalga",
        "Sönük Titreşim", "Geçici Köprü", "Dağılan Foton", "Eriyen Ağ", "Son Yankı",
        // Tier 5 (41..50)
        "Gölge İkizi", "Hayalet Yolu", "Görünmez Tel", "İllüzyon Ağı", "Karanlık Yansıma",
        "Fantom Düğümü", "Yansıyan Sis", "Yıldız Tozu", "Gölge Koridoru", "Kırık Aynalar",
        // Tier 6 (51..60)
        "Fraktal Ağacı", "Çift Sarmal", "İkili Çekirdek", "Altıgen Petek", "Kristal Kafes",
        "İç İçe Halkalar", "Kozmik Çiçek", "Fraktal Yıldız", "Matris Örgüsü", "Karmaşık Düğüm",
        // Tier 7 (61..70)
        "Kuantum Tüneli", "Dolanık Parçacık", "Süperpozisyon", "Fotonik Ağ", "Hiper Küp",
        "Çoklu Geçit", "Boyut Kırılması", "Plazma Kapısı", "Çekim Dalgası", "Kuantum Çekirdeği",
        // Tier 8 (71..80)
        "Zaman Döngüsü", "Görecelik Hattı", "Kronos Sarmalı", "Işık Konisi", "Zaman Kristali",
        "Yavaşlayan Akış", "Geri Dönen Hat", "Zaman Aynası", "Kozmik Saat", "Boyutlararası Akış",
        // Tier 9 (81..90)
        "Usta Labirenti", "Düğümler Şehri", "Büyük Dokuma", "Karmaşık Kafes", "Örümcek Ağı",
        "Geometrik Kaos", "Harmonik Denge", "Yüce Ağ", "Kozmik Mimari", "Büyük Ağ Geçidi",
        // Tier 10 (91..100)
        "Zirve Yükselişi", "Yıldızlararası", "Süpernova", "Hiperuzay", "Galaktik Çekirdek",
        "Son Sınav", "Kozmos Nabzı", "Büyük Birleşme", "Usta İmtihanı", "ECHO OMEGA"
    )

    fun getLevelTitle(id: Int, language: Language = Language.EN): String {
        val clampedId = id.coerceIn(1, TOTAL_LEVELS)
        val tierIndex = ((clampedId - 1) / 10).coerceIn(0, 9)
        return if (language == Language.TR) {
            val name = UNIQUE_NAMES_TR.getOrNull(clampedId - 1) ?: "Seviye $clampedId"
            val tier = TIER_NAMES_TR[tierIndex]
            "$name ($tier)"
        } else {
            val name = UNIQUE_NAMES_EN.getOrNull(clampedId - 1) ?: "Level $clampedId"
            val tier = TIER_NAMES_EN[tierIndex]
            "$name ($tier)"
        }
    }

    /**
     * Generates a complete set of 100 unique, guaranteed playable puzzle levels.
     * Every level has mathematically distinct geometry, non-crossing solution path,
     * and generous node spacing.
     */
    fun create100Levels(): List<LevelEntity> {
        val list = ArrayList<LevelEntity>(TOTAL_LEVELS)

        for (id in 1..TOTAL_LEVELS) {
            val levelData = buildLevelData(id)
            list.add(
                LevelEntity(
                    id = id,
                    title = levelData.title,
                    gridSize = levelData.gridSize,
                    nodesJson = serializeNodes(levelData.nodes),
                    edgesJson = serializeEdges(levelData.directedEdges),
                    parEchoes = levelData.parEchoes,
                    hintOrderJson = levelData.hintOrder.joinToString(","),
                    mechanicType = levelData.mechanicType,
                    decayLifetime = levelData.decayLifetime,
                    isGhostEchoes = levelData.isGhostEchoes,
                    description = levelData.description,
                    stars = 0,
                    isCompleted = false,
                    isUnlocked = id == 1,
                    bestEchoCount = -1
                )
            )
        }

        return list
    }

    fun create250Levels(): List<LevelEntity> = create100Levels()

    private fun serializeNodes(nodes: List<LevelNode>): String {
        return nodes.joinToString(";") {
            "${it.id}:${(it.x * 10).roundToInt() / 10f}:${(it.y * 10).roundToInt() / 10f}:${it.type.name}:${it.keyForGateId}"
        }
    }

    fun deserializeNodes(raw: String): List<LevelNode> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { part ->
            val tokens = part.split(":")
            if (tokens.size >= 3) {
                val id = tokens[0].toIntOrNull() ?: 1
                val x = tokens[1].toFloatOrNull() ?: 0f
                val y = tokens[2].toFloatOrNull() ?: 0f
                val type = if (tokens.size >= 4) {
                    try { NodeType.valueOf(tokens[3]) } catch (_: Exception) { NodeType.NORMAL }
                } else NodeType.NORMAL
                val keyForGateId = if (tokens.size >= 5) tokens[4].toIntOrNull() ?: -1 else -1
                LevelNode(id, x, y, type, keyForGateId)
            } else null
        }
    }

    private fun serializeEdges(edges: List<DirectedEdge>): String {
        return edges.joinToString(";") { "${it.fromId}>${it.toId}" }
    }

    fun deserializeEdges(raw: String): List<DirectedEdge> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { part ->
            val tokens = part.split(">")
            if (tokens.size == 2) {
                val from = tokens[0].toIntOrNull()
                val to = tokens[1].toIntOrNull()
                if (from != null && to != null) DirectedEdge(from, to) else null
            } else null
        }
    }

    fun entityToLevelData(entity: LevelEntity): LevelData {
        val hintOrder = if (entity.hintOrderJson.isNotBlank()) {
            entity.hintOrderJson.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else emptyList()

        return LevelData(
            levelId = entity.id,
            title = entity.title,
            gridSize = entity.gridSize,
            nodes = deserializeNodes(entity.nodesJson),
            directedEdges = deserializeEdges(entity.edgesJson),
            parEchoes = entity.parEchoes,
            hintOrder = hintOrder,
            mechanicType = entity.mechanicType,
            decayLifetime = entity.decayLifetime,
            isGhostEchoes = entity.isGhostEchoes,
            description = entity.description
        )
    }

    fun buildLevelData(id: Int): LevelData {
        val clampedId = id.coerceIn(1, TOTAL_LEVELS)
        val tierIndex = ((clampedId - 1) / 10).coerceIn(0, 9)

        // Default title in English (as requested: "Herşey başlangıçta ingilizce olacak")
        val title = getLevelTitle(clampedId, Language.EN)

        // Dynamic node count from 3 to 36 with smooth difficulty progression
        val nodeCount = (3 + ((clampedId - 1) * 33.0 / 99.0).roundToInt()).coerceIn(3, 36)

        val centerX = 180f
        val centerY = 230f

        // Key & Gate mechanics: Key appears along the first half/third, Gate is at the final node
        val hasKeyGate = (tierIndex == 2 || tierIndex == 6 || (tierIndex == 9 && clampedId % 2 == 0)) && nodeCount >= 4
        val keyIndex = if (hasKeyGate) (nodeCount / 3).coerceIn(2, nodeCount - 1) else -1
        val gateIndex = if (hasKeyGate) nodeCount else -1

        // 12 distinct open, non-self-intersecting geometric archetypes.
        // Every archetype forms an open curve where Node 1 and the final node (nodeCount)
        // are widely separated (80-140px), eliminating false collisions when reaching the goal!
        val shapeArchetype = (clampedId - 1) % 12

        fun clampPt(x: Float, y: Float): Pair<Float, Float> =
            Pair(x.coerceIn(48f, 312f), y.coerceIn(88f, 372f))

        val curveFn: (Float) -> Pair<Float, Float> = when (shapeArchetype) {
            0 -> {
                // Archetype 0: Open Celestial Arc (sweeps ~280 degrees, leaving wide ~80 deg opening)
                val rx = 115f + (clampedId * 5 % 15)
                val ry = 125f + (clampedId * 7 % 15)
                val baseAngle = ((clampedId * 37) % 360) * (PI / 180.0)
                val sweep = 1.55 * PI
                { t ->
                    val angle = baseAngle + sweep * t
                    clampPt(centerX + rx * cos(angle).toFloat(), centerY + ry * sin(angle).toFloat())
                }
            }
            1 -> {
                // Archetype 1: Inward Archimedean Spiral (Monotonically decreasing radius)
                val startR = 130f
                val endR = 56f
                val baseAngle = ((clampedId * 53) % 360) * (PI / 180.0)
                val turns = 1.35 * PI
                { t ->
                    val r = startR + (endR - startR) * t
                    val angle = baseAngle + turns * t
                    clampPt(centerX + r * cos(angle).toFloat(), centerY + r * sin(angle).toFloat())
                }
            }
            2 -> {
                // Archetype 2: Open Diamond / Rhombus Horseshoe (Level 51 Archetype)
                // Sweeps 279 degrees around 3 of the 4 diamond quadrants, leaving a wide 120px gap
                // between Node 1 and Node 20. Reaching Node 20 is completely unobstructed!
                val w = 118f + (clampedId % 4) * 4f
                val h = 130f + (clampedId % 3) * 5f
                val rot = ((clampedId * 29) % 360) * (PI / 180.0)
                val sweep = 1.55 * PI
                { t ->
                    val angle = rot + sweep * t
                    val ca = cos(angle).toFloat()
                    val sa = sin(angle).toFloat()
                    val denom = (abs(ca) + abs(sa)).coerceAtLeast(0.001f)
                    clampPt(centerX + (w * ca / denom), centerY + (h * sa / denom))
                }
            }
            3 -> {
                // Archetype 3: Harmonic Sine Wave Meander
                val w = 122f
                val h = 118f
                val waves = 1.5
                val rot = ((clampedId * 17) % 360) * (PI / 180.0)
                { t ->
                    val u = -w + t * 2f * w
                    val v = (sin(t * waves * PI) * (h * 0.6)).toFloat()
                    val rx = (u * cos(rot) - v * sin(rot)).toFloat()
                    val ry = (u * sin(rot) + v * cos(rot)).toFloat()
                    clampPt(centerX + rx, centerY + ry)
                }
            }
            4 -> {
                // Archetype 4: S-Serpentine Track
                val w = 118f
                val h = 125f
                { t ->
                    val u = (sin(t * 3.0 * PI) * w).toFloat()
                    val v = -h + t * 2f * h
                    clampPt(centerX + u, centerY + v)
                }
            }
            5 -> {
                // Archetype 5: Spiral Outward (Nebula Spire)
                val startR = 56f
                val endR = 130f
                val baseAngle = ((clampedId * 31) % 360) * (PI / 180.0)
                val turns = 1.35 * PI
                { t ->
                    val r = startR + (endR - startR) * t
                    val angle = baseAngle + turns * t
                    clampPt(centerX + r * cos(angle).toFloat(), centerY + r * sin(angle).toFloat())
                }
            }
            6 -> {
                // Archetype 6: U-Shaped Canyon (Hairpin Track)
                val w = 108f
                val h = 128f
                val rot = ((clampedId * 23) % 360) * (PI / 180.0)
                { t ->
                    val (u, v) = when {
                        t <= 0.4f -> Pair(-w, -h + (t / 0.4f) * (1.5f * h))
                        t <= 0.6f -> {
                            val angle = PI - ((t - 0.4f) / 0.2f) * PI
                            Pair(cos(angle).toFloat() * w, 0.5f * h + sin(angle).toFloat() * (0.5f * h))
                        }
                        else -> Pair(w, 0.5f * h - ((t - 0.6f) / 0.4f) * (1.5f * h))
                    }
                    val rx = (u * cos(rot) - v * sin(rot)).toFloat()
                    val ry = (u * sin(rot) + v * cos(rot)).toFloat()
                    clampPt(centerX + rx, centerY + ry)
                }
            }
            7 -> {
                // Archetype 7: Diagonal Ascending Ribbon
                val w = 120f
                val h = 125f
                { t ->
                    val u = -w + t * 2f * w
                    val v = -h + t * 2f * h + (sin(t * 2.0 * PI) * 35.0).toFloat()
                    clampPt(centerX + u, centerY + v)
                }
            }
            8 -> {
                // Archetype 8: Open Hexagonal Crown (5 sides, 6th side open)
                val rad = 125f
                val rot = ((clampedId * 23) % 360) * (PI / 180.0)
                val sweep = 1.60 * PI
                val secAngle = PI / 6.0
                { t ->
                    val angle = rot + sweep * t
                    val ca = cos(angle).toFloat()
                    val sa = sin(angle).toFloat()
                    val phi = (angle % (2.0 * secAngle)) - secAngle
                    val r = (rad * cos(secAngle) / cos(phi).coerceAtLeast(0.001)).toFloat()
                    clampPt(centerX + r * ca, centerY + r * sa)
                }
            }
            9 -> {
                // Archetype 9: Horseshoe Arch (Open U loop)
                val rx = 118f
                val ry = 128f
                val baseAngle = PI * 0.15
                val sweep = PI * 1.7
                { t ->
                    val angle = baseAngle + sweep * t
                    clampPt(centerX + rx * cos(angle).toFloat(), centerY + ry * sin(angle).toFloat())
                }
            }
            10 -> {
                // Archetype 10: Smooth S-Curve Wave
                val w = 120f
                val h = 125f
                val rot = ((clampedId * 37) % 360) * (PI / 180.0)
                { t ->
                    val u = -w + t * 2f * w
                    val v = (sin(t * PI * 2.0 - PI / 2.0) * (h * 0.55)).toFloat()
                    val rx = (u * cos(rot) - v * sin(rot)).toFloat()
                    val ry = (u * sin(rot) + v * cos(rot)).toFloat()
                    clampPt(centerX + rx, centerY + ry)
                }
            }
            else -> {
                // Archetype 11: Crescent Ribbon
                val r = 125f
                val baseAngle = ((clampedId * 43) % 360) * (PI / 180.0)
                val span = 1.45 * PI
                { t ->
                    val angle = baseAngle + span * t
                    val curR = r - (20.0 * sin(t * PI)).toFloat()
                    clampPt(centerX + curR * cos(angle).toFloat(), centerY + curR * sin(angle).toFloat())
                }
            }
        }

        val sampledPoints = sampleCurve(nodeCount, curveFn)
        val nodes = ArrayList<LevelNode>(nodeCount)
        for (i in 1..nodeCount) {
            val pt = sampledPoints[i - 1]
            val type = when (i) {
                keyIndex -> NodeType.KEY
                gateIndex -> NodeType.GATE
                else -> NodeType.NORMAL
            }
            val keyForGate = if (type == NodeType.KEY || type == NodeType.GATE) gateIndex else -1
            nodes.add(LevelNode(i, pt.first, pt.second, type, keyForGate))
        }

        // Mechanic classification for the 10 tiers
        val mechanicType = when (tierIndex) {
            0 -> "STANDARD"
            1 -> "ONE_WAY"
            2 -> "KEY_GATE"
            3 -> "DECAYING_ECHO"
            4 -> "GHOST_ECHO"
            5 -> "FRAKTAL_MATRIX"
            6 -> "KEY_GATE"
            7 -> "DECAYING_ECHO"
            8 -> "MASTER_NETWORK"
            9 -> if (clampedId == 100) "OMEGA" else if (clampedId % 2 == 0) "KEY_GATE" else "DECAYING_ECHO"
            else -> "STANDARD"
        }

        val decayLifetime = when (mechanicType) {
            "DECAYING_ECHO" -> 5 + (clampedId % 3)
            "OMEGA" -> 6
            else -> 0
        }

        val isGhostEchoes = mechanicType == "GHOST_ECHO" || (tierIndex >= 7 && clampedId % 3 == 0)

        // Directed edges always follow forward solution order: from i to i+1
        val directedEdges = if (mechanicType == "ONE_WAY" || tierIndex == 6) {
            val edges = ArrayList<DirectedEdge>()
            for (i in 1 until nodeCount) {
                if (i % 2 == 1) {
                    edges.add(DirectedEdge(i, i + 1))
                }
            }
            edges
        } else {
            emptyList()
        }

        val hintOrder = (1..nodeCount).toList()
        val parEchoes = (nodeCount / 3).coerceIn(1, 4)

        val description = if (clampedId == 100) {
            "GRAND FINALE: Complete the 100th and final puzzle of the ECHO universe to claim total mastery!"
        } else {
            "Level $clampedId: Connect all $nodeCount nodes with a single continuous stroke without colliding with echoes!"
        }

        return LevelData(
            levelId = clampedId,
            title = title,
            gridSize = 4,
            nodes = nodes,
            directedEdges = directedEdges,
            parEchoes = parEchoes,
            hintOrder = hintOrder,
            mechanicType = mechanicType,
            decayLifetime = decayLifetime,
            isGhostEchoes = isGhostEchoes,
            description = description
        )
    }

    private fun sampleCurve(count: Int, curveFn: (Float) -> Pair<Float, Float>): List<Pair<Float, Float>> {
        val fineSteps = 400
        val finePts = ArrayList<Pair<Float, Float>>(fineSteps)
        for (step in 0 until fineSteps) {
            val t = step.toFloat() / (fineSteps - 1).coerceAtLeast(1)
            finePts.add(curveFn(t))
        }

        val cumLens = ArrayList<Float>(fineSteps)
        cumLens.add(0f)
        var totalLen = 0f
        for (i in 0 until fineSteps - 1) {
            val p1 = finePts[i]
            val p2 = finePts[i + 1]
            val d = hypot(p2.first - p1.first, p2.second - p1.second)
            totalLen += d
            cumLens.add(totalLen)
        }

        val resampled = ArrayList<Pair<Float, Float>>(count)
        val stepLen = totalLen / (count - 1).coerceAtLeast(1)

        resampled.add(finePts.first())
        var curIdx = 0
        for (i in 1 until count - 1) {
            val targetLen = i * stepLen
            while (curIdx < fineSteps - 1 && cumLens[curIdx + 1] < targetLen) {
                curIdx++
            }
            val segLen = cumLens[curIdx + 1] - cumLens[curIdx]
            val t = if (segLen <= 0f) 0f else (targetLen - cumLens[curIdx]) / segLen
            val p1 = finePts[curIdx]
            val p2 = finePts[curIdx + 1]
            val x = p1.first + t * (p2.first - p1.first)
            val y = p1.second + t * (p2.second - p1.second)
            resampled.add(Pair(x, y))
        }
        resampled.add(finePts.last())
        return resampled
    }
}
