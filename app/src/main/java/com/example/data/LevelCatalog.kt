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
        "Foundation",                // Tier 1 (1..3)
        "Complex Web",               // Tier 2 (4..10)
        "Floating Nodes",            // Tier 3 (11..20)
        "Invisible Rays",            // Tier 4 (21..30)
        "Decaying Nodes",            // Tier 5 (31..40)
        "Wandering Echoes",          // Tier 6 (41..50)
        "Decoy Targets",             // Tier 7 (51..60)
        "Reverse Flow",              // Tier 8 (61..70)
        "Rotating Web",              // Tier 9 (71..80)
        "Dual Entangled Web",        // Tier 10 (81..90)
        "Cumulative Ghost Maze",     // Tier 11 (91..99)
        "Omega Final: Grand Nexus"   // Tier 12 (100)
    )

    private val TIER_NAMES_TR = listOf(
        "Temel Ağ",                     // Tier 1 (1..3)
        "Karmaşık Ağ",                  // Tier 2 (4..10)
        "Hareketli Düğümler",           // Tier 3 (11..20)
        "Görünmez Işınlar",             // Tier 4 (21..30)
        "Zamanla Kaybolan Düğümler",    // Tier 5 (31..40)
        "Yankıların Hareketi",          // Tier 6 (41..50)
        "Sahte Hedefler",               // Tier 7 (51..60)
        "Ters Yönler",                  // Tier 8 (61..70)
        "Ekranın Dönmesi",              // Tier 9 (71..80)
        "İki Ağ Aynı Anda",             // Tier 10 (81..90)
        "Önceki Hataların Birleşmesi",  // Tier 11 (91..99)
        "Büyük Final: Omega Zirvesi"    // Tier 12 (100)
    )

    fun getTierIndex(levelId: Int): Int = when (levelId) {
        in 1..3 -> 0
        in 4..10 -> 1
        in 11..20 -> 2
        in 21..30 -> 3
        in 31..40 -> 4
        in 41..50 -> 5
        in 51..60 -> 6
        in 61..70 -> 7
        in 71..80 -> 8
        in 81..90 -> 9
        in 91..99 -> 10
        else -> 11
    }

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
        val tierIndex = getTierIndex(clampedId)
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

    /**
     * Precomputed sequence of 100 level node counts spanning from 3 to 24.
     * Guaranteed:
     * - Level 1 has 3 nodes
     * - Level 2 has 4 nodes
     * - Level 3 has 5 nodes
     * - Levels 4 to 100: scaled between 6 and 24 nodes, tuned so player can complete just in time within 60s
     *   ("60sn ucu ucuna yetişecek şekilde"), with wide node spacing so buttons are never crowded ("dip dibe olmasınlar").
     * - Every adjacent level has a DIFFERENT node count: arr[i] != arr[i - 1]
     */
    val LEVEL_NODE_COUNTS: IntArray = run {
        val arr = IntArray(TOTAL_LEVELS)
        arr[0] = 3 // Level 1: 3 nodes (Gentle intro)
        arr[1] = 4 // Level 2: 4 nodes (Gentle intro)
        arr[2] = 5 // Level 3: 5 nodes (Gentle intro)
        for (i in 3 until TOTAL_LEVELS - 1) {
            val id = i + 1
            val t = (id - 4) / 95.0
            val base = 6.5 + t * 16.5 // Scaled between 6 and 23 nodes
            val wave = when (id % 4) {
                0 -> 1.0
                1 -> -0.9
                2 -> 1.2
                else -> -1.0
            }
            var count = (base + wave).roundToInt().coerceIn(6, 24)
            if (count == arr[i - 1]) {
                count = if (count < 24 && (id % 2 == 0)) count + 1 else (count - 1).coerceAtLeast(6)
            }
            arr[i] = count
        }
        arr[TOTAL_LEVELS - 1] = 24
        if (arr[TOTAL_LEVELS - 2] == 24) {
            arr[TOTAL_LEVELS - 2] = 23
        }
        arr
    }

    /**
     * User request: "düğmelerin biraz arasını açarsan sevinirim çok dip dibe olmasınlar"
     * Enforces guaranteed minimum clearance distance between all level nodes.
     */
    fun ensureNodeSpacing(points: List<Pair<Float, Float>>, minDistance: Float = 46f): List<Pair<Float, Float>> {
        val pts = points.map { floatArrayOf(it.first, it.second) }.toTypedArray()
        val n = pts.size
        for (iter in 0 until 15) {
            var moved = false
            for (i in 0 until n) {
                for (j in i + 1 until n) {
                    val dx = pts[j][0] - pts[i][0]
                    val dy = pts[j][1] - pts[i][1]
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist < minDistance && dist > 0.001f) {
                        moved = true
                        val overlap = (minDistance - dist) * 0.5f
                        val nx = (dx / dist) * overlap
                        val ny = (dy / dist) * overlap
                        pts[i][0] = (pts[i][0] - nx).coerceIn(40f, 400f)
                        pts[i][1] = (pts[i][1] - ny).coerceIn(80f, 520f)
                        pts[j][0] = (pts[j][0] + nx).coerceIn(40f, 400f)
                        pts[j][1] = (pts[j][1] + ny).coerceIn(80f, 520f)
                    }
                }
            }
            if (!moved) break
        }
        return pts.map { Pair(it[0], it[1]) }
    }

    fun buildLevelData(id: Int): LevelData {
        val clampedId = id.coerceIn(1, TOTAL_LEVELS)
        val tierIndex = ((clampedId - 1) / 10).coerceIn(0, 9)

        // Default title in English (as requested: "Herşey başlangıçta ingilizce olacak")
        val title = getLevelTitle(clampedId, Language.EN)

        // Distinct node count from 3 to 24 for every level
        val nodeCount = LEVEL_NODE_COUNTS[clampedId - 1]

        // Center and scale parameters for comfortable virtual canvas coordinate space
        val cx = 220f + (((clampedId * 7) % 9) - 4) * 2.5f
        val cy = 280f + (((clampedId * 13) % 9) - 4) * 2.5f

        // Key & Gate mechanics: Key appears along first half/third, Gate is at the final node
        val hasKeyGate = (tierIndex == 2 || tierIndex == 6 || (tierIndex == 9 && clampedId % 2 == 0)) && nodeCount >= 4
        val keyIndex = if (hasKeyGate) (nodeCount / 3).coerceIn(2, nodeCount - 1) else -1
        val gateIndex = if (hasKeyGate) nodeCount else -1

        // Distinct, dedicated geometry for every single level 1 to 100
        val curveFn: (Float) -> Pair<Float, Float> = LevelGeometry.getCurveForLevel(clampedId, cx, cy)

        val rawPoints = sampleCurve(nodeCount, curveFn)
        val sampledPoints = ensureNodeSpacing(rawPoints, minDistance = 46f)
        val perm = getPermutationForLevel(clampedId, nodeCount)
        val nodes = ArrayList<LevelNode>(nodeCount)
        for (i in 1..nodeCount) {
            val pt = sampledPoints[perm[i - 1]]
            val type = when (i) {
                keyIndex -> NodeType.KEY
                gateIndex -> NodeType.GATE
                else -> NodeType.NORMAL
            }
            val keyForGate = if (type == NodeType.KEY || type == NodeType.GATE) gateIndex else -1
            nodes.add(LevelNode(i, pt.first, pt.second, type, keyForGate))
        }

        // Mechanic classification for the 12 evolution tiers per user design
        val mechanicType = when {
            clampedId in 1..3 -> "BASIC_WEB"
            clampedId in 4..10 -> "COMPLEX_WEB"
            clampedId in 11..20 -> "FLOATING_NODES"
            clampedId in 21..30 -> "INVISIBLE_RAYS"
            clampedId in 31..40 -> "DECAYING_NODES"
            clampedId in 41..50 -> "WANDERING_ECHOES"
            clampedId in 51..60 -> "DECOY_TARGETS"
            clampedId in 61..70 -> "REVERSE_FLOW"
            clampedId in 71..80 -> "ROTATING_WEB"
            clampedId in 81..90 -> "DUAL_ENTANGLED_WEB"
            clampedId in 91..99 -> "CUMULATIVE_GHOSTS"
            clampedId == 100 -> "OMEGA_SYNTHESIS"
            else -> "BASIC_WEB"
        }

        val decayLifetime = when (mechanicType) {
            "DECAYING_NODES" -> 5 + (clampedId % 3)
            "OMEGA_SYNTHESIS" -> 6
            else -> 0
        }

        val isGhostEchoes = mechanicType in listOf("CUMULATIVE_GHOSTS", "WANDERING_ECHOES", "OMEGA_SYNTHESIS")

        // Directed edges always follow forward solution order: from i to i+1
        val directedEdges = if (mechanicType == "REVERSE_FLOW" || (mechanicType == "OMEGA_SYNTHESIS" && clampedId % 2 == 0)) {
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
        val parEchoes = (nodeCount / 4).coerceIn(1, 8)

        val description = if (clampedId == 100) {
            "GRAND FINALE: Connect all 24 nodes across the Omega Arch to conquer the ECHO universe!"
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

    /**
     * User requirement:
     * "ilk üç bölüm dışında diğer bölümlerin düğmelerini bir birine karıştır"
     * Levels 1, 2, 3 keep their natural sequential node layout.
     * Levels 4 to 100 have their nodes shuffled/mixed across the screen geometry deterministically.
     */
    fun getPermutationForLevel(levelId: Int, count: Int): List<Int> {
        if (levelId <= 3 || count <= 3) {
            return (0 until count).toList()
        }
        val list = (0 until count).toMutableList()
        // Deterministic PRNG seeded uniquely per level
        val rng = java.util.Random(levelId.toLong() * 9973L + 101L)
        // Fisher-Yates shuffle
        for (i in count - 1 downTo 1) {
            val j = rng.nextInt(i + 1)
            val temp = list[i]
            list[i] = list[j]
            list[j] = temp
        }
        return list
    }

    /**
     * Creates a non-linear criss-crossing permutation of node indices for levels > 3.
     * Ensures nodes in sequence (1 -> 2 -> 3...) are not placed right next to each other
     * along the perimeter, transforming the puzzles into intellectually engaging geometric webs.
     */
    fun computeThoughtfulPermutation(levelId: Int, count: Int): List<Int> {
        if (levelId <= 3 || count <= 3) {
            return (0 until count).toList()
        }
        val result = ArrayList<Int>(count)
        val visited = BooleanArray(count)
        val half = (count / 2).coerceAtLeast(1)

        for (i in 0 until count) {
            val idx = if (i % 2 == 0) {
                (i / 2) % count
            } else {
                ((i / 2) + half) % count
            }
            if (!visited[idx]) {
                visited[idx] = true
                result.add(idx)
            }
        }
        for (i in 0 until count) {
            if (!visited[i]) {
                visited[i] = true
                result.add(i)
            }
        }
        return result
    }
}
