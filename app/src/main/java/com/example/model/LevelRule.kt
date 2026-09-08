package com.example.model

import com.example.ui.dialogs.LevelMechanicInfo

enum class VisualPreviewType {
    PULSING_LIVING_WEB,
    COMPLEX_LATTICE,
    ORBITAL_SHIFTING_NODES,
    INVISIBLE_LINES_FADE,
    DECAYING_RING,
    WANDERING_ECHO_DRIFT,
    PHANTOM_DECOY,
    VECTOR_REVERSE_FLOW,
    ROTATING_CANVAS,
    DUAL_PORTAL,
    CUMULATIVE_GHOST_TRAIL,
    OMEGA_NEXUS
}

data class LevelRule(
    val levelId: Int,
    val milestoneId: Int,
    val isMajorMilestone: Boolean,
    val tier: Int,
    val tierName: String,
    val ruleTitle: String,
    val subtitle: String,
    val icon: String,
    val badgeColor: Long,
    val accentColor: Long,
    val headline: String,
    val summary: String,
    val mechanicRule: String,
    val echoHazard: String,
    val proStrategy: String,
    val visualPreviewType: VisualPreviewType,
    val ruleKey: String
) {
    fun toMechanicInfo(): LevelMechanicInfo {
        return LevelMechanicInfo(
            tier = tier,
            icon = icon,
            title = ruleTitle,
            subtitle = subtitle,
            description = "$mechanicRule\n\n⚠️ $echoHazard",
            tip = proStrategy,
            badgeColor = badgeColor
        )
    }
}
