package com.example.data

import com.example.model.LevelRule
import com.example.model.VisualPreviewType

object LevelRuleCatalog {

    val MILESTONE_LEVELS = listOf(1, 4, 11, 21, 31, 41, 51, 61, 71, 81, 91, 100, 101, 111, 121, 131, 141, 150)

    fun isMilestone(levelId: Int): Boolean = levelId in MILESTONE_LEVELS

    fun getTierForLevel(levelId: Int): Int = when (levelId.coerceIn(1, 150)) {
        in 1..3 -> 1
        in 4..10 -> 2
        in 11..20 -> 3
        in 21..30 -> 4
        in 31..40 -> 5
        in 41..50 -> 6
        in 51..60 -> 7
        in 61..70 -> 8
        in 71..80 -> 9
        in 81..90 -> 10
        in 91..99 -> 11
        100 -> 12
        in 101..110 -> 13
        in 111..120 -> 14
        in 121..130 -> 15
        in 131..140 -> 16
        in 141..149 -> 17
        else -> 18
    }

    fun getRuleForLevel(levelId: Int, isTurkish: Boolean = false): LevelRule {
        val id = levelId.coerceIn(1, 150)
        val tier = getTierForLevel(id)
        val isMajor = isMilestone(id)

        return buildEnglishRule(id, tier, isMajor)
    }

    private fun buildEnglishRule(id: Int, tier: Int, isMajor: Boolean): LevelRule {
        return when (tier) {
            1 -> { // Levels 1..3
                val (title, sub, rule, hazard, tip, icon) = when (id) {
                    1 -> Tuple6(
                        "Continuous Flow",
                        "Milestone: Level 1 • Genesis",
                        "The player always starts strictly from Node 1. Connect all nodes with a single uninterrupted gesture without lifting your finger.",
                        "Lifting your finger, starting anywhere except Node 1, or backtracking to an earlier node triggers an Echo (+1).",
                        "Use the 5-second preview to scan your path before drawing. A 0-error run unlocks the Perfect Ghost run!",
                        "🎯"
                    )
                    2 -> Tuple6(
                        "Echo Footprint Hazard",
                        "Harmonic Foundation • Level 2",
                        "Mistakes do not vanish; they solidify into persistent red 'Echo Rays'. These red hazard lines become physical obstacles on subsequent attempts.",
                        "Colliding with your past red echo rays triggers fresh echoes and risks locking the board.",
                        "Strokes may pass through existing lines, but avoid retreating back onto red echo obstacles.",
                        "👣"
                    )
                    else -> Tuple6(
                        "Living Web Elasticity",
                        "Harmonic Foundation • Level 3",
                        "The web is alive! Connective rays stretch, breathe, and pull elastically toward your active touch cursor.",
                        "Jerky or abrupt finger movements can overshoot node hitboxes and register unintended retreats.",
                        "Glide your finger smoothly. Let the harmonic rhythm guide your stroke into each node.",
                        "🕸️"
                    )
                }
                LevelRule(
                    levelId = id,
                    milestoneId = 1,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Foundation: Living Web",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = icon,
                    badgeColor = 0xFF0284C7,
                    accentColor = 0xFF38BDF8,
                    headline = "Core Rule: Single Continuous Stroke",
                    summary = "Connect all nodes in one continuous line; avoid creating red echoes.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.PULSING_LIVING_WEB,
                    ruleKey = "CONTINUOUS_FLOW"
                )
            }
            2 -> { // Levels 4..10
                val title = if (id == 4) "Complex Web Lattice" else if (id == 7) "Elastic Bridge" else "Resonance Matrix #$id"
                val sub = "Milestone: Level 4 • Tier II (4 - 10)"
                val rule = "The web expands! Interconnected strands multiply, and previous rays vibrate with harmonic tension as your path grows."
                val hazard = "Branching density can trap you; entering a dead-end requires backtracking which incurs +1 Echo."
                val tip = "Trace internal bridge hubs early so the outer perimeter remains open for a clean exit."
                LevelRule(
                    levelId = id,
                    milestoneId = 4,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Complex Web",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🕸️",
                    badgeColor = 0xFF0EA5E9,
                    accentColor = 0xFF7DD3FC,
                    headline = "Rule: Dense Interconnections & Harmonic Tension",
                    summary = "Navigate dense thread lattices and plan your route ahead of time.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.COMPLEX_LATTICE,
                    ruleKey = "COMPLEX_WEB"
                )
            }
            3 -> { // Levels 11..20
                val title = if (id == 11) "Shifting Nodes" else if (id == 15) "Orbital Drift Velocity" else "Cosmic Current #$id"
                val sub = "Milestone: Level 11 • Tier III (11 - 20)"
                val rule = "Nodes are no longer stationary! They drift smoothly along orbital paths driven by cosmic currents. Rote memorization will fail; you must synchronize with their motion."
                val hazard = "Leading the finger into empty space where a node used to be breaks your path (+1 Echo)."
                val tip = "Watch the sinusoidal swing of each node; connect when they slow down at their orbital apexes."
                LevelRule(
                    levelId = id,
                    milestoneId = 11,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Shifting Nodes",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "⚡",
                    badgeColor = 0xFF8B5CF6,
                    accentColor = 0xFFA78BFA,
                    headline = "Rule: Dynamic Orbital Drift & Shifting Nodes",
                    summary = "Nodes drift continuously in space; match your timing to their orbit.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.ORBITAL_SHIFTING_NODES,
                    ruleKey = "SHIFTING_NODES"
                )
            }
            4 -> { // Levels 21..30
                val title = if (id == 21) "Invisible Lines" else if (id == 25) "Pulsar Stealth Beams" else "Stealth Photon #$id"
                val sub = "Milestone: Level 21 • Tier IV (21 - 30)"
                val rule = "Connected lines dissolve into invisibility seconds after being drawn! The beams remain physically active, but you must navigate using spatial memory."
                val hazard = "Colliding with an invisible earlier path segment or re-entering completed nodes incurs +1 Echo."
                val tip = "Maintain a vivid mental blueprint of the geometry you've already completed."
                LevelRule(
                    levelId = id,
                    milestoneId = 21,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Invisible Lines",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🌌",
                    badgeColor = 0xFFD97706,
                    accentColor = 0xFFFBBF24,
                    headline = "Rule: Stealth Photons & Spatial Memory",
                    summary = "Lines vanish from sight after connection; rely on your mental map.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.INVISIBLE_LINES_FADE,
                    ruleKey = "INVISIBLE_LINES"
                )
            }
            5 -> { // Levels 31..40
                val title = if (id == 31) "Decaying Nodes" else if (id == 35) "Temporal Dissolution Rush" else "Decay Ring #$id"
                val sub = "Milestone: Level 31 • Tier V (31 - 40)"
                val rule = "Selected nodes lose cosmic charge over time; their outer aura rings dissolve in a countdown. Visit decaying nodes before their energy fully evaporates!"
                val hazard = "If a decaying node collapses before your stroke touches it, the circuit breaks (+1 Echo)."
                val tip = "Prioritize nodes flashing red/orange on low energy; stabilize them during the first 15 seconds."
                LevelRule(
                    levelId = id,
                    milestoneId = 31,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Decaying Nodes",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "⏳",
                    badgeColor = 0xFFEC4899,
                    accentColor = 0xFFF472B6,
                    headline = "Rule: Temporal Dissolution & Rush Recovery",
                    summary = "Energy rings dissolve over time; visit endangered nodes early.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.DECAYING_RING,
                    ruleKey = "DECAYING_NODES"
                )
            }
            6 -> { // Levels 41..50
                val title = if (id == 41) "Wandering Echoes" else if (id == 45) "Drifting Past & Beast Growl" else "Floating Echo #$id"
                val sub = "Milestone: Level 41 • Tier VI (41 - 50)"
                val rule = "Red echo hazard walls do not stay in place! They drift slowly through space. Mistakes agitate the slumbering Echo Beast into heightened aggression."
                val hazard = "A drifting red echo beam that sweeps into your active finger stroke instantly triggers a collision (+1 Echo)."
                val tip = "Track the drift trajectory of floating echoes; slide through open channels when gaps widen."
                LevelRule(
                    levelId = id,
                    milestoneId = 41,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Wandering Echoes",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "👻",
                    badgeColor = 0xFF6366F1,
                    accentColor = 0xFF818CF8,
                    headline = "Rule: Floating Hazards & The Awakening Beast",
                    summary = "Red echo barriers drift across the canvas; avoid crossing paths.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.WANDERING_ECHO_DRIFT,
                    ruleKey = "WANDERING_ECHOES"
                )
            }
            7 -> { // Levels 51..60
                val title = if (id == 51) "Decoy Targets" else if (id == 55) "Phantom Mirage Minefield" else "Illusion Node #$id"
                val sub = "Milestone: Level 51 • Tier VII (51 - 60)"
                val rule = "Phantom decoy nodes populate the web! Real nodes pulse with steady harmonic resonance, while decoys exhibit stuttered, broken rings."
                val hazard = "Touching any phantom decoy node registers an immediate collision penalty (+1 Echo)."
                val tip = "True targets produce melodic chimes; ignore jittery decoy illusions."
                LevelRule(
                    levelId = id,
                    milestoneId = 51,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Decoy Targets",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🔮",
                    badgeColor = 0xFF14B8A6,
                    accentColor = 0xFF2DD4BF,
                    headline = "Rule: Decoy Illusions & Pattern Discrimination",
                    summary = "Distinguish genuine resonant nodes from phantom decoys.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.PHANTOM_DECOY,
                    ruleKey = "DECOY_TARGETS"
                )
            }
            8 -> { // Levels 61..70
                val title = if (id == 61) "Reverse Flow (One-Way Vectors)" else if (id == 65) "Vector Flow Highway" else "Counter Current #$id"
                val sub = "Milestone: Level 61 • Tier VIII (61 - 70)"
                val rule = "Directional vector arrows enforce strict one-way flow! You can only traverse these paths in the exact direction the arrow points."
                val hazard = "Drawing against the vector arrow direction blocks current and triggers +1 Echo."
                val tip = "Study one-way vectors before drawing to avoid entering a one-way street with no escape."
                LevelRule(
                    levelId = id,
                    milestoneId = 61,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Reverse Flow",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🔄",
                    badgeColor = 0xFF2563EB,
                    accentColor = 0xFF60A5FA,
                    headline = "Rule: One-Way Vector Conduits",
                    summary = "Travel strictly in the direction of vector arrows.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.VECTOR_REVERSE_FLOW,
                    ruleKey = "REVERSE_FLOW"
                )
            }
            9 -> { // Levels 71..80
                val title = if (id == 71) "Rotating Node Web" else if (id == 75) "Angular Node Vortex" else "Spinning Nodes #$id"
                val sub = "Milestone: Level 71 • Tier IX (71 - 80)"
                val rule = "The cluster of nodes and connective web rotates smoothly around the center while the screen remains fixed! Trace through the rotating nodes in sequence."
                val hazard = "Failing to compensate for node rotation causes finger stroke to deviate, risking missed nodes (+1 Echo)."
                val tip = "The background remains stationary while the nodes spin smoothly; track and connect the next node in order."
                LevelRule(
                    levelId = id,
                    milestoneId = 71,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Rotating Nodes",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🌀",
                    badgeColor = 0xFFF59E0B,
                    accentColor = 0xFFFBBF24,
                    headline = "Rule: Rotating Node Constellation",
                    summary = "Connect nodes as the constellation revolves around the center.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.ROTATING_CANVAS,
                    ruleKey = "ROTATING_WEB"
                )
            }
            10 -> { // Levels 81..90
                val title = if (id == 81) "Dual Entangled Web" else if (id == 85) "Interdimensional Key & Gate" else "Quantum Portal #$id"
                val sub = "Milestone: Level 81 • Tier X (81 - 90)"
                val rule = "Two intertwined dimensional planes (Alpha & Beta) co-exist! The golden Key in Dimension Alpha must be collected to unlock the Gate in Dimension Beta."
                val hazard = "Touching the locked Gate node before obtaining the matching Key breaks your path (+1 Echo)."
                val tip = "Use cross-dimensional bridge nodes to reach the Key first, saving the Gate for your final stretch."
                LevelRule(
                    levelId = id,
                    milestoneId = 81,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Dual Entangled Web",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "✨",
                    badgeColor = 0xFF8B5CF6,
                    accentColor = 0xFFC084FC,
                    headline = "Rule: Dual Dimensions & Quantum Keys",
                    summary = "Collect the Dimension Alpha Key to unlock the Dimension Beta Gate.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.DUAL_PORTAL,
                    ruleKey = "DUAL_ENTANGLED"
                )
            }
            11 -> { // Levels 91..99
                val title = if (id == 91) "Cumulative Ghost Maze" else if (id == 95) "Living Spirit Labyrinth" else "Spectral Memory #$id"
                val sub = "Milestone: Level 91 • Tier XI (91 - 99)"
                val rule = "All historical failed attempts from your past persist as translucent living ghost trails with floating spirit wisps, creating an evolving labyrinth!"
                val hazard = "Colliding with your past ghost labyrinth adds weight to your echoes (+1 Echo)."
                val tip = "Observe the clear corridors between your past ghost lines; let your past mistakes guide your winning path."
                LevelRule(
                    levelId = id,
                    milestoneId = 91,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Cumulative Ghost Maze",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🧬",
                    badgeColor = 0xFFA855F7,
                    accentColor = 0xFFE879F9,
                    headline = "Rule: Cumulative Ghost Maze & Past Mistakes",
                    summary = "Past failed attempts form an active ethereal maze to overcome.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.CUMULATIVE_GHOST_TRAIL,
                    ruleKey = "CUMULATIVE_GHOSTS"
                )
            }
            12 -> { // Level 100
                LevelRule(
                    levelId = 100,
                    milestoneId = 100,
                    isMajorMilestone = true,
                    tier = 12,
                    tierName = "Grand Finale: Omega Synthesis",
                    ruleTitle = "Grand Finale: All Rules United",
                    subtitle = "Apex Milestone: Level 100 • The Nexus of Echo",
                    icon = "👑",
                    badgeColor = 0xFFEF4444,
                    accentColor = 0xFFF87171,
                    headline = "THE ULTIMATE TRIAL: All 100 Level Rules in a Single Cosmic Breath!",
                    summary = "Shifting nodes, invisible rays, rotating canvas, gates, and the awakened Echo Beast!",
                    mechanicRule = "The monumental climax of the ECHO universe! Shifting nodes drift along orbits, connected lines vanish into stealth, the entire canvas rotates, cumulative ghosts weave an ethereal labyrinth, and the awakened Echo Beast mimics your path in DOMINION mode!",
                    echoHazard = "Any retreat, collision, or directional slip provokes the awakened Beast (+1 Echo).",
                    proStrategy = "Keep calm and steady. Complete all 24 nodes along the Omega Arch within 60 seconds to claim the Legendary Champion title!",
                    visualPreviewType = VisualPreviewType.OMEGA_NEXUS,
                    ruleKey = "OMEGA_SYNTHESIS"
                )
            }
            13 -> { // Levels 101..110: Hyper-Flux Matrix
                LevelRule(
                    levelId = id,
                    milestoneId = 101,
                    isMajorMilestone = isMajor,
                    tier = 13,
                    tierName = "Hyper-Flux Matrix",
                    ruleTitle = "Hyper-Speed Reflections & Rotating Field",
                    subtitle = "Level $id • Hyper-Flux Domain",
                    icon = "⚡",
                    badgeColor = 0xFF8B5CF6,
                    accentColor = 0xFFA78BFA,
                    headline = "Light-Speed Reflections and Dynamic Rotation!",
                    summary = "Echoes reflect twice as fast as the cosmic grid rotates continuously.",
                    mechanicRule = "Connecting nodes requires rapid reactions. Pre-plan your route before making your move.",
                    echoHazard = "Pausing during drawing risks collision with rapidly revolving echoes.",
                    proStrategy = "Work from the core outward and synchronize with the rotation cadence.",
                    visualPreviewType = VisualPreviewType.ROTATING_CANVAS,
                    ruleKey = "HYPER_FLUX"
                )
            }
            14 -> { // Levels 111..120: Singularity Void
                LevelRule(
                    levelId = id,
                    milestoneId = 111,
                    isMajorMilestone = isMajor,
                    tier = 14,
                    tierName = "Singularity Void",
                    ruleTitle = "Gravitational Pull & Wandering Singularities",
                    subtitle = "Level $id • Event Horizon",
                    icon = "🕳️",
                    badgeColor = 0xFF4F46E5,
                    accentColor = 0xFF818CF8,
                    headline = "Singularity Void and Wandering Ghosts!",
                    summary = "Gravitational vortex pulls echoes while phantom decoy nodes confuse your path.",
                    mechanicRule = "Filter out deceptive decoy nodes and strictly link valid sequential targets.",
                    echoHazard = "Contacting a decoy node immediately spawns an active crimson echo.",
                    proStrategy = "Carefully note node numbers during preview and maintain steady speed.",
                    visualPreviewType = VisualPreviewType.WANDERING_ECHO_DRIFT,
                    ruleKey = "SINGULARITY_VOID"
                )
            }
            15 -> { // Levels 121..130: Quantum Entanglement
                LevelRule(
                    levelId = id,
                    milestoneId = 121,
                    isMajorMilestone = isMajor,
                    tier = 15,
                    tierName = "Quantum Entanglement",
                    ruleTitle = "Multi-Keys & One-Way Quantum Bridges",
                    subtitle = "Level $id • Entangled Gateways",
                    icon = "🌌",
                    badgeColor = 0xFF06B6D4,
                    accentColor = 0xFF22D3EE,
                    headline = "Quantum Keys and Directed Passages!",
                    summary = "Multiple color-coded locked gates and strictly unidirectional arrows.",
                    mechanicRule = "Collect keys in proper sequence before attempting to cross directional quantum gates.",
                    echoHazard = "Attempting to traverse against one-way arrows blocks your stroke and causes failure.",
                    proStrategy = "Trace gate sequences mentally during preview to ensure no deadlocks.",
                    visualPreviewType = VisualPreviewType.DUAL_PORTAL,
                    ruleKey = "QUANTUM_ENTANGLEMENT"
                )
            }
            16 -> { // Levels 131..140: Chrono-Stasis Fracture
                LevelRule(
                    levelId = id,
                    milestoneId = 131,
                    isMajorMilestone = isMajor,
                    tier = 16,
                    tierName = "Chrono-Stasis Fracture",
                    ruleTitle = "Ultra-Fast Decaying Nodes & Chronos Pressure",
                    subtitle = "Level $id • Temporal Collapse",
                    icon = "⏳",
                    badgeColor = 0xFFF59E0B,
                    accentColor = 0xFFFBBF24,
                    headline = "Nodes Vanish in 2 Seconds!",
                    summary = "Touched nodes decay and vanish rapidly. Constant momentum is mandatory!",
                    mechanicRule = "Each touched node disappears within 2 seconds. Connect all targets in one continuous rush.",
                    echoHazard = "Letting a node vanish before connection breaks the circuit.",
                    proStrategy = "Move decisively without hesitation. Practice the stroke geometry in advance.",
                    visualPreviewType = VisualPreviewType.DECAYING_RING,
                    ruleKey = "CHRONO_STASIS"
                )
            }
            17 -> { // Levels 141..149: Celestial Apex
                LevelRule(
                    levelId = id,
                    milestoneId = 141,
                    isMajorMilestone = isMajor,
                    tier = 17,
                    tierName = "Celestial Apex",
                    ruleTitle = "Titan Constellation Challenge",
                    subtitle = "Level $id • Universal Zenith",
                    icon = "⭐",
                    badgeColor = 0xFFEC4899,
                    accentColor = 0xFFF472B6,
                    headline = "Grand 38-Node Constellation!",
                    summary = "Time, directional flow, phantom ghosts, and rotation coalesce into the ultimate test.",
                    mechanicRule = "Master-class puzzle field. Exercise all acquired reflexes and strategic intuition.",
                    echoHazard = "Any geometric collision creates an instant echo penalty.",
                    proStrategy = "Chunk the node order into sets of 3 during the 5-second preview.",
                    visualPreviewType = VisualPreviewType.OMEGA_NEXUS,
                    ruleKey = "CELESTIAL_APEX"
                )
            }
            else -> { // Level 150: Cosmic Infinity: Ultimate Nexus
                LevelRule(
                    levelId = 150,
                    milestoneId = 150,
                    isMajorMilestone = true,
                    tier = 18,
                    tierName = "Cosmic Infinity: Ultimate Nexus",
                    ruleTitle = "ABSOLUTE COSMIC INFINITY",
                    subtitle = "Apex Milestone: Level 150 • The Sovereign Crown",
                    icon = "👑",
                    badgeColor = 0xFFFFD700,
                    accentColor = 0xFFFFF176,
                    headline = "THE ABSOLUTE PINNACLE OF ECHO FLUX: 40-Node Universal Mandala!",
                    summary = "All harmonic laws of the ECHO cosmos converge in a breathtaking 40-node masterpiece!",
                    mechanicRule = "The true crowning finale of the 150-level journey! Connect all 40 harmonic nodes in a single immaculate breath to become the Eternal Champion!",
                    echoHazard = "Zero margin for error. A single contact or backtracking ends the run.",
                    proStrategy = "Summon everything you've learned. Breathe calmly and let the constellation guide your hand!",
                    visualPreviewType = VisualPreviewType.OMEGA_NEXUS,
                    ruleKey = "COSMIC_INFINITY"
                )
            }
        }
    }

    fun getAllMilestones(isTurkish: Boolean = false): List<LevelRule> {
        return MILESTONE_LEVELS.map { getRuleForLevel(it, isTurkish) }
    }

    fun getAllRules(isTurkish: Boolean = false): List<LevelRule> {
        return (1..LevelCatalog.TOTAL_LEVELS).map { getRuleForLevel(it, isTurkish) }
    }

    fun getAll100Rules(isTurkish: Boolean = false): List<LevelRule> = getAllRules(isTurkish)

    private data class Tuple6<A, B, C, D, E, F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F
    )
}
