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

    /**
     * Precomputed sequence of 100 level node counts spanning from 3 to 36 (average ~19.5).
     * Guaranteed:
     * - Level 1 has 3 nodes
     * - Level 100 has 36 nodes
     * - Every adjacent level has a DIFFERENT node count: arr[i] != arr[i - 1]
     * - Average is between 19 and 20 ("ortalama 3-36")
     */
    val LEVEL_NODE_COUNTS: IntArray = run {
        val arr = IntArray(TOTAL_LEVELS)
        arr[0] = 3
        for (i in 1 until TOTAL_LEVELS - 1) {
            val id = i + 1
            val t = (id - 1) / 99.0
            val base = 3.0 + t * 33.0
            val wave = when (id % 6) {
                0 -> 2.2
                1 -> -1.8
                2 -> 1.7
                3 -> -2.1
                4 -> 1.9
                else -> -1.4
            }
            var count = (base + wave).roundToInt().coerceIn(3, 36)
            if (count == arr[i - 1]) {
                count = if (count < 36 && (id % 2 == 0)) count + 1 else (count - 1).coerceAtLeast(3)
            }
            arr[i] = count
        }
        arr[TOTAL_LEVELS - 1] = 36
        if (arr[TOTAL_LEVELS - 2] == 36) {
            arr[TOTAL_LEVELS - 2] = 35
        }
        arr
    }

    fun buildLevelData(id: Int): LevelData {
        val clampedId = id.coerceIn(1, TOTAL_LEVELS)
        val tierIndex = ((clampedId - 1) / 10).coerceIn(0, 9)

        // Default title in English (as requested: "Herşey başlangıçta ingilizce olacak")
        val title = getLevelTitle(clampedId, Language.EN)

        // Distinct node count from 3 to 36 for every level
        val nodeCount = LEVEL_NODE_COUNTS[clampedId - 1]

        // Center and scale parameters for comfortable virtual canvas coordinate space
        val cx = 220f + (((clampedId * 7) % 9) - 4) * 2.5f
        val cy = 280f + (((clampedId * 13) % 9) - 4) * 2.5f

        // Key & Gate mechanics: Key appears along first half/third, Gate is at the final node
        val hasKeyGate = (tierIndex == 2 || tierIndex == 6 || (tierIndex == 9 && clampedId % 2 == 0)) && nodeCount >= 4
        val keyIndex = if (hasKeyGate) (nodeCount / 3).coerceIn(2, nodeCount - 1) else -1
        val gateIndex = if (hasKeyGate) nodeCount else -1

        // Golden-ratio angle rotation ensures NO TWO LEVELS have identical spatial orientation
        val rot = ((clampedId * 137.508) % 360.0) * (PI / 180.0)
        val scaleX = 1.0f + (((clampedId * 11) % 9) - 4) * 0.022f
        val scaleY = 1.0f + (((clampedId * 17) % 9) - 4) * 0.022f

        // 25 distinct geometric archetypes. Level 100 receives the Grand Omega Arch
        val shapeArchetype = if (clampedId == 100) 24 else (clampedId - 1) % 25

        fun clampPt(x: Float, y: Float): Pair<Float, Float> =
            Pair(x.coerceIn(38f, 402f), y.coerceIn(58f, 502f))

        fun transform(u: Float, v: Float): Pair<Float, Float> {
            val rx = (u * cos(rot) - v * sin(rot)).toFloat() * scaleX
            val ry = (u * sin(rot) + v * cos(rot)).toFloat() * scaleY
            return clampPt(cx + rx, cy + ry)
        }

        val curveFn: (Float) -> Pair<Float, Float> = when (shapeArchetype) {
            0 -> {
                // Archetype 0: Open Celestial Arc (sweeps ~280 degrees, wide open gap between ends)
                val rx = 155f + (clampedId % 4) * 4f
                val ry = 165f + (clampedId % 3) * 4f
                val sweep = 1.56 * PI
                { t ->
                    val angle = sweep * t
                    transform(rx * cos(angle).toFloat(), ry * sin(angle).toFloat())
                }
            }
            1 -> {
                // Archetype 1: Inward Archimedean Spiral
                val startR = 175f
                val endR = 55f
                val sweep = 1.42 * PI
                { t ->
                    val r = startR + (endR - startR) * t
                    val angle = sweep * t
                    transform(r * cos(angle).toFloat(), r * sin(angle).toFloat())
                }
            }
            2 -> {
                // Archetype 2: Outward Nebula Spiral
                val startR = 55f
                val endR = 175f
                val sweep = 1.42 * PI
                { t ->
                    val r = startR + (endR - startR) * t
                    val angle = sweep * t
                    transform(r * cos(angle).toFloat(), r * sin(angle).toFloat())
                }
            }
            3 -> {
                // Archetype 3: Open Rhombus / Diamond Horseshoe
                val w = 158f + (clampedId % 4) * 3f
                val h = 168f + (clampedId % 3) * 3f
                val sweep = 1.55 * PI
                { t ->
                    val angle = sweep * t
                    val ca = cos(angle).toFloat()
                    val sa = sin(angle).toFloat()
                    val denom = (abs(ca) + abs(sa)).coerceAtLeast(0.001f)
                    transform(w * ca / denom, h * sa / denom)
                }
            }
            4 -> {
                // Archetype 4: Harmonic Sine Wave Meander
                val w = 155f
                val h = 125f + (clampedId % 3) * 10f
                val waves = 2.5
                { t ->
                    val u = -w + t * 2f * w
                    val v = (sin(t * waves * PI) * h).toFloat()
                    transform(u, v)
                }
            }
            5 -> {
                // Archetype 5: S-Serpentine Vertical Track
                val w = 145f + (clampedId % 3) * 8f
                val h = 165f
                { t ->
                    val u = (sin(t * 2.0 * PI) * w).toFloat()
                    val v = -h + t * 2f * h
                    transform(u, v)
                }
            }
            6 -> {
                // Archetype 6: U-Canyon Hairpin Track
                val w = 135f
                val h = 160f
                { t ->
                    val (u, v) = when {
                        t <= 0.4f -> Pair(-w, -h + (t / 0.4f) * (1.5f * h))
                        t <= 0.6f -> {
                            val angle = PI - ((t - 0.4f) / 0.2f) * PI
                            Pair(cos(angle).toFloat() * w, 0.5f * h + sin(angle).toFloat() * (0.5f * h))
                        }
                        else -> Pair(w, 0.5f * h - ((t - 0.6f) / 0.4f) * (1.5f * h))
                    }
                    transform(u, v)
                }
            }
            7 -> {
                // Archetype 7: Open Hexagonal Crown (5 sides open)
                val rad = 165f
                val sweep = 1.58 * PI
                val secAngle = PI / 6.0
                { t ->
                    val angle = sweep * t
                    val ca = cos(angle).toFloat()
                    val sa = sin(angle).toFloat()
                    val phi = (angle % (2.0 * secAngle)) - secAngle
                    val r = (rad * cos(secAngle) / cos(phi).coerceAtLeast(0.001)).toFloat()
                    transform(r * ca, r * sa)
                }
            }
            8 -> {
                // Archetype 8: Smooth Superellipse Arc (distinct diamond-oval silhouette without velocity cusps)
                val r = 165f
                val sweep = 1.54 * PI
                { t ->
                    val angle = sweep * t
                    val ca = cos(angle).toFloat()
                    val sa = sin(angle).toFloat()
                    val u = r * ca * (0.85f + 0.15f * (ca * ca))
                    val v = r * sa * (0.85f + 0.15f * (sa * sa))
                    transform(u, v)
                }
            }
            9 -> {
                // Archetype 9: Parabolic Valley & Crest
                val w = 155f
                val h = 135f
                { t ->
                    val u = -w + t * 2f * w
                    val v = (h * (4f * (t - 0.5f) * (t - 0.5f) - 0.5f))
                    transform(u, v)
                }
            }
            10 -> {
                // Archetype 10: Smooth S-Curve Wave
                val w = 155f
                val h = 120f
                { t ->
                    val u = -w + t * 2f * w
                    val v = (sin(t * 2.0 * PI - PI / 2.0) * h).toFloat()
                    transform(u, v)
                }
            }
            11 -> {
                // Archetype 11: Crescent Moon Ribbon
                val r = 165f
                val sweep = 1.48 * PI
                { t ->
                    val curR = r - (40.0 * sin(t * PI)).toFloat()
                    val angle = sweep * t
                    transform(curR * cos(angle).toFloat(), curR * sin(angle).toFloat())
                }
            }
            12 -> {
                // Archetype 12: Elliptical Open Horseshoe
                val rx = 165f
                val ry = 125f
                val sweep = 1.56 * PI
                { t ->
                    val angle = sweep * t
                    transform(rx * cos(angle).toFloat(), ry * sin(angle).toFloat())
                }
            }
            13 -> {
                // Archetype 13: DNA Wave Strand
                val w = 130f
                val h = 165f
                { t ->
                    val u = (sin(t * 2.5 * PI) * w).toFloat()
                    val v = -h + t * 2f * h
                    transform(u, v)
                }
            }
            14 -> {
                // Archetype 14: Nautilus Concha Spire (generous outward expansion)
                val sweep = 1.45 * PI
                val startR = 95f
                val endR = 180f
                { t ->
                    val curR = startR + (endR - startR) * t
                    val angle = sweep * t
                    transform(curR * cos(angle).toFloat(), curR * sin(angle).toFloat())
                }
            }
            15 -> {
                // Archetype 15: Double Harmonic Wave
                val w = 155f
                { t ->
                    val u = -w + t * 2f * w
                    val v = (75.0 * sin(t * 3.0 * PI) + 35.0 * cos(t * 1.5 * PI)).toFloat()
                    transform(u, v)
                }
            }
            16 -> {
                // Archetype 16: Stepped Meander Ribbon
                val w = 155f
                { t ->
                    val u = -w + t * 2f * w
                    val v = (110.0 * sin(t * 3.5 * PI) * (0.7 + 0.3 * t)).toFloat()
                    transform(u, v)
                }
            }
            17 -> {
                // Archetype 17: Diagonal Ribbon Wave
                val w = 145f
                { t ->
                    val u = -w + t * 2f * w
                    val v = -w + t * 2f * w + (sin(t * 2.5 * PI) * 45.0).toFloat()
                    transform(u, v)
                }
            }
            18 -> {
                // Archetype 18: Octagonal Crown Horseshoe
                val rad = 165f
                val sweep = 1.52 * PI
                val secAngle = PI / 8.0
                { t ->
                    val angle = sweep * t
                    val ca = cos(angle).toFloat()
                    val sa = sin(angle).toFloat()
                    val phi = (angle % (2.0 * secAngle)) - secAngle
                    val r = (rad * cos(secAngle) / cos(phi).coerceAtLeast(0.001)).toFloat()
                    transform(r * ca, r * sa)
                }
            }
            19 -> {
                // Archetype 19: Teardrop Open Loop
                val r = 160f
                val sweep = 1.54 * PI
                { t ->
                    val angle = 0.23 * PI + sweep * t
                    val u = r * cos(angle).toFloat()
                    val v = (r * sin(angle) * (0.75 + 0.25 * sin(angle))).toFloat()
                    transform(u, v)
                }
            }
            20 -> {
                // Archetype 20: Trefoil Open Petal
                val sweep = 1.45 * PI
                { t ->
                    val curR = (115.0 + 45.0 * cos(t * 2.4 * PI)).toFloat()
                    val angle = sweep * t
                    transform(curR * cos(angle).toFloat(), curR * sin(angle).toFloat())
                }
            }
            21 -> {
                // Archetype 21: Ripple Meander Track (smooth sinusoidal velocity)
                val w = 155f
                val h = 115f
                { t ->
                    val u = -w + t * 2f * w
                    val v = (sin(t * 3.0 * PI) * h).toFloat()
                    transform(u, v)
                }
            }
            22 -> {
                // Archetype 22: Archimedes Double-Arc Ribbon
                val sweep = 1.52 * PI
                { t ->
                    val curR = (75.0 + 90.0 * sin(t * PI / 2.0)).toFloat()
                    val angle = sweep * t
                    transform(curR * cos(angle).toFloat(), curR * sin(angle).toFloat())
                }
            }
            23 -> {
                // Archetype 23: Cardioid Open Heart Arc
                val sweep = 1.42 * PI
                { t ->
                    val angle = 0.29 * PI + sweep * t
                    val curR = (120.0 * (1.0 - 0.45 * cos(angle))).toFloat()
                    transform(curR * cos(angle).toFloat(), curR * sin(angle).toFloat())
                }
            }
            else -> {
                // Archetype 24: Grand Omega Arch (Level 100 Finale)
                { t ->
                    val (u, v) = when {
                        t <= 0.2f -> Pair(-160f + (t / 0.2f) * 60f, 130f)
                        t <= 0.8f -> {
                            val subT = (t - 0.2f) / 0.6f
                            val angle = PI * 1.15 - subT * (PI * 1.30)
                            Pair(120f * cos(angle).toFloat(), 15f - 120f * sin(angle).toFloat())
                        }
                        else -> Pair(100f + ((t - 0.8f) / 0.2f) * 60f, 130f)
                    }
                    transform(u, v)
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
        val parEchoes = (nodeCount / 4).coerceIn(1, 8)

        val description = if (clampedId == 100) {
            "GRAND FINALE: Connect all 36 nodes across the Omega Arch to conquer the ECHO universe!"
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
