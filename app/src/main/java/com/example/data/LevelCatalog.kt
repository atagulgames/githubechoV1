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
        val centerY = 220f

        // Key & Gate mechanics: Key appears early (index 2..nodeCount-2), Gate is at index nodeCount
        val hasKeyGate = (tierIndex == 2 || tierIndex == 6 || (tierIndex == 9 && clampedId % 2 == 0)) && nodeCount >= 4
        val keyIndex = if (hasKeyGate) (2 + ((clampedId % (nodeCount - 3)).coerceAtLeast(0))).coerceIn(2, nodeCount - 1) else -1
        val gateIndex = if (hasKeyGate) nodeCount else -1

        val nodes = ArrayList<LevelNode>(nodeCount)

        if (nodeCount <= 10) {
            // Tier 1: Harmonic cosmic arcs (3 to 10 nodes)
            val rx = when (clampedId % 4) {
                0 -> 96f
                1 -> 104f
                2 -> 90f
                else -> 98f
            }
            val ry = when (clampedId % 4) {
                0 -> 96f
                1 -> 88f
                2 -> 106f
                else -> 94f
            }
            val baseAngle = ((clampedId * 37) % 360) * (PI / 180.0)
            val isCounterClockwise = clampedId % 3 == 0
            val sweepSpan = (1.52 + ((clampedId % 5) * 0.03)) * PI
            val harmonicFreq = (clampedId % 3) + 1
            val harmonicAmp = if (clampedId % 3 == 1) 0.05f else if (clampedId % 3 == 2) -0.04f else 0.0f

            for (i in 1..nodeCount) {
                val progress = (i - 1).toFloat() / (nodeCount - 1).coerceAtLeast(1)
                val dir = if (isCounterClockwise) -1.0 else 1.0
                val angle = baseAngle + (dir * sweepSpan * progress)
                val ripple = 1.0f + (harmonicAmp * sin(harmonicFreq * progress * PI.toFloat()))
                val px = centerX + (rx * ripple * cos(angle).toFloat())
                val py = centerY + (ry * ripple * sin(angle).toFloat())

                val type = when (i) {
                    keyIndex -> NodeType.KEY
                    gateIndex -> NodeType.GATE
                    else -> NodeType.NORMAL
                }
                val keyForGateId = if (type == NodeType.KEY) gateIndex else -1
                nodes.add(LevelNode(i, px, py, type, keyForGateId))
            }
        } else {
            // Tier 2-4: Clean serpentine constellation tracks (11 to 36 nodes)
            val numRows = when {
                nodeCount <= 18 -> 2
                nodeCount <= 27 -> 3
                else -> 4
            }
            val nodesPerRow = (nodeCount + numRows - 1) / numRows
            val yStart = when (numRows) {
                2 -> 155f
                3 -> 115f
                else -> 90f
            }
            val ySpacing = when (numRows) {
                2 -> 130f
                3 -> 100f
                else -> 78f
            }
            val xMin = 65f + (clampedId % 3) * 3f
            val xMax = 295f - (clampedId % 4) * 3f

            for (i in 1..nodeCount) {
                val index0 = i - 1
                val rowIndex = (index0 / nodesPerRow).coerceAtMost(numRows - 1)
                val inRowIndex = index0 % nodesPerRow
                val rowTotal = if (rowIndex == numRows - 1) (nodeCount - rowIndex * nodesPerRow) else nodesPerRow
                val rowProgress = if (rowTotal > 1) inRowIndex.toFloat() / (rowTotal - 1) else 0.5f

                val isLeftToRight = rowIndex % 2 == 0
                val effectiveProgress = if (isLeftToRight) rowProgress else (1f - rowProgress)
                val px = xMin + (xMax - xMin) * effectiveProgress
                val py = yStart + (rowIndex * ySpacing)

                val type = when (i) {
                    keyIndex -> NodeType.KEY
                    gateIndex -> NodeType.GATE
                    else -> NodeType.NORMAL
                }
                val keyForGateId = if (type == NodeType.KEY) gateIndex else -1
                nodes.add(LevelNode(i, px, py, type, keyForGateId))
            }
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
}
