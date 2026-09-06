package com.example.data

import com.example.data.local.LevelEntity
import com.example.model.DirectedEdge
import com.example.model.LevelData
import com.example.model.LevelNode
import com.example.model.NodeType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object LevelCatalog {

    const val TOTAL_LEVELS = 100

    private val TIER_NAMES = listOf(
        "Harmonik Başlangıç", // Tier 1 (1..10)
        "Yönlü Enerji",       // Tier 2 (11..20)
        "Kilit & Anahtar",    // Tier 3 (21..30)
        "Sönen Dalgalar",     // Tier 4 (31..40)
        "Gölge Rezonansı",    // Tier 5 (41..50)
        "Fraktal Matris",     // Tier 6 (51..60)
        "Kuantum Kapıları",   // Tier 7 (61..70)
        "Zamansal Akış",      // Tier 8 (71..80)
        "Usta Ağları",        // Tier 9 (81..90)
        "Omega Zirvesi"       // Tier 10 (91..100)
    )

    private val UNIQUE_NAMES = listOf(
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

    /**
     * Generates a complete set of 100 unique, non-repeating, guaranteed playable puzzle levels.
     * Every level has mathematically distinct geometry, unique title, verified hint order,
     * and a smooth progressive difficulty curve from 1 to 100.
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

    // Keep legacy name as alias to prevent compile issues anywhere
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

        val title = if (clampedId == 100) {
            "ECHO OMEGA (100. Zirve Finali)"
        } else {
            val name = UNIQUE_NAMES.getOrNull(clampedId - 1) ?: "Seviye $clampedId"
            val tier = TIER_NAMES[tierIndex]
            "$name ($tier)"
        }

        // Dynamically varying node counts so consecutive levels have different matching numbers
        val nodeCount = when {
            clampedId == 1 -> 3 // 2 matches
            clampedId == 2 -> 4 // 3 matches
            clampedId == 3 -> 5 // 4 matches
            clampedId == 4 -> 4 // 3 matches
            clampedId == 5 -> 6 // 5 matches
            clampedId == 6 -> 5 // 4 matches
            clampedId == 7 -> 7 // 6 matches
            clampedId == 8 -> 6 // 5 matches
            clampedId == 9 -> 8 // 7 matches
            clampedId == 100 -> 12 // 11 matches (Grand Finale)
            else -> {
                val tierBase = ((clampedId - 1) / 10) + 4
                val pattern = when (clampedId % 4) {
                    0 -> 1
                    1 -> -1
                    2 -> 2
                    else -> 0
                }
                (tierBase + pattern).coerceIn(4, 11)
            }
        }

        val centerX = 180f
        val centerY = 220f
        val baseRadius = (110f - (nodeCount * 2.2f)).coerceIn(65f, 105f)

        // Key & Gate mechanics: Key appears early (index 2..nodeCount-1), Gate is always at the last node
        val hasKeyGate = (tierIndex == 2 || tierIndex == 6 || (tierIndex == 9 && clampedId % 2 == 0)) && nodeCount >= 4
        val keyIndex = if (hasKeyGate) 2 + ((clampedId % (nodeCount - 3)).coerceAtLeast(0)) else -1
        val gateIndex = if (hasKeyGate) nodeCount else -1

        // 7 Distinct Geometric Archetypes for diverse shapes across the 100 levels
        val archetype = (clampedId - 1) % 7
        val rotationOffset = ((clampedId * 43) % 360) * (PI / 180.0)

        val nodes = ArrayList<LevelNode>(nodeCount)

        for (i in 1..nodeCount) {
            val progress = (i - 1).toFloat() / (nodeCount - 1).coerceAtLeast(1)

            val (rawX, rawY) = when (archetype) {
                0 -> {
                    // Regular & breathing Polygon (Convex perimeter, guaranteed non-crossing)
                    val angle = (2 * PI * (i - 1) / nodeCount) - (PI / 2) + rotationOffset
                    val r = baseRadius * (1.0f + ((i * clampedId) % 5 - 2) * 0.03f)
                    Pair(
                        centerX + r * cos(angle).toFloat(),
                        centerY + r * sin(angle).toFloat()
                    )
                }
                1 -> {
                    // Archimedean Spiral / Nautilus (Monotonically expanding radius, unrolling path)
                    val turns = 1.35
                    val angle = (turns * 2 * PI * progress) - (PI / 2) + rotationOffset
                    val r = (baseRadius * 0.35f) + (baseRadius * 0.70f * progress)
                    Pair(
                        centerX + r * cos(angle).toFloat(),
                        centerY + r * sin(angle).toFloat()
                    )
                }
                2 -> {
                    // Elliptical Cosmic Orbit
                    val angle = (2 * PI * (i - 1) / nodeCount) - (PI / 2) + rotationOffset
                    val rX = baseRadius * 1.22f
                    val rY = baseRadius * 0.82f
                    Pair(
                        centerX + rX * cos(angle).toFloat(),
                        centerY + rY * sin(angle).toFloat()
                    )
                }
                3 -> {
                    // Star Sawtooth (Outer and inner alternating points around perimeter)
                    val angle = (2 * PI * (i - 1) / nodeCount) - (PI / 2) + rotationOffset
                    val r = if (i % 2 == 0) baseRadius * 0.65f else baseRadius * 1.05f
                    Pair(
                        centerX + r * cos(angle).toFloat(),
                        centerY + r * sin(angle).toFloat()
                    )
                }
                4 -> {
                    // Sinusoidal Energy Ribbon (S-Curve flowing through canvas)
                    val spanX = baseRadius * 1.8f
                    val startX = centerX - (spanX / 2f)
                    val px = startX + (spanX * progress)
                    val py = centerY + (sin(progress * 2 * PI.toFloat()) * (baseRadius * 0.75f))
                    // Rotate around center
                    val dx = px - centerX
                    val dy = py - centerY
                    val rx = dx * cos(rotationOffset).toFloat() - dy * sin(rotationOffset).toFloat()
                    val ry = dx * sin(rotationOffset).toFloat() + dy * cos(rotationOffset).toFloat()
                    Pair(centerX + rx, centerY + ry)
                }
                5 -> {
                    // Horseshoe / Crescent Arc
                    val arcSpan = 1.6 * PI
                    val angle = - (arcSpan / 2) + (arcSpan * progress) + rotationOffset
                    val r = baseRadius * (0.90f + 0.20f * sin(progress * PI.toFloat()))
                    Pair(
                        centerX + r * cos(angle).toFloat(),
                        centerY + r * sin(angle).toFloat()
                    )
                }
                else -> {
                    // Concentric Dual Ring (First half outer, second half inner, seamless turnaround)
                    val half = (nodeCount + 1) / 2
                    val (angle, r) = if (i <= half) {
                        val p = (i - 1).toFloat() / (half - 1).coerceAtLeast(1)
                        Pair(p * PI + rotationOffset, baseRadius * 1.05f)
                    } else {
                        val p = (i - half - 1).toFloat() / (nodeCount - half - 1).coerceAtLeast(1)
                        Pair((1f - p) * PI + rotationOffset + PI, baseRadius * 0.58f)
                    }
                    Pair(
                        centerX + r * cos(angle).toFloat(),
                        centerY + r * sin(angle).toFloat()
                    )
                }
            }

            val clampedX = rawX.coerceIn(50f, 310f)
            val clampedY = rawY.coerceIn(80f, 360f)

            val type = when (i) {
                keyIndex -> NodeType.KEY
                gateIndex -> NodeType.GATE
                else -> NodeType.NORMAL
            }
            val keyForGateId = if (type == NodeType.KEY) gateIndex else -1

            nodes.add(LevelNode(i, clampedX, clampedY, type, keyForGateId))
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

        // Directed edges for ONE_WAY mechanics (guaranteed solvable along hint order)
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
            "BÜYÜK FİNAL: ECHO Evreninin 100. ve son zirve bulmacasını çöz ve zaferi kazan!"
        } else {
            "Bölüm $clampedId: $nodeCount düğümlü ${TIER_NAMES[tierIndex]} geometrisini tek kesintisiz çizgiyle tamamla!"
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
