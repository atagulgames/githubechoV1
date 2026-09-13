package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Patterns that drive dynamic ambient canvas effects for Season 2.
 */
enum class Season2PatternType {
    COSMIC_CONSTELLATION,
    AURORA_WAVES,
    SOLAR_RINGS,
    QUANTUM_GRID
}

/**
 * Season 2 specialized visual themes with custom color palettes and background patterns.
 * Activated immediately once the Season 2 announcement screen finishes.
 */
enum class Season2VisualTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val iconEmoji: String,
    val patternType: Season2PatternType,
    // Dark Theme Palette
    val darkBackgroundTop: Color,
    val darkBackgroundBottom: Color,
    val darkCardBackground: Color,
    val darkBorderColor: Color,
    val darkDotColor: Color,
    // Light Theme Palette
    val lightBackgroundTop: Color,
    val lightBackgroundBottom: Color,
    val lightCardBackground: Color,
    val lightBorderColor: Color,
    val lightDotColor: Color,
    // Accents & Atmosphere
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val tertiaryAccent: Color,
    val glowColor: Color,
    val badgeGradient: List<Color>
) {
    COSMIC_NEBULA(
        id = "COSMIC_NEBULA",
        displayName = "Kozmik Nebula",
        subtitle = "Yıldız tozu, takımyıldız hatları ve mor ışık",
        iconEmoji = "🌌",
        patternType = Season2PatternType.COSMIC_CONSTELLATION,
        darkBackgroundTop = Color(0xFF070514),
        darkBackgroundBottom = Color(0xFF140D2D),
        darkCardBackground = Color(0xFF161033),
        darkBorderColor = Color(0xFF6D28D9),
        darkDotColor = Color(0x33A78BFA),
        lightBackgroundTop = Color(0xFFFAF5FF),
        lightBackgroundBottom = Color(0xFFEDE9FE),
        lightCardBackground = Color(0xFFFFFFFF),
        lightBorderColor = Color(0xFFDDD6FE),
        lightDotColor = Color(0x338B5CF6),
        primaryAccent = Color(0xFF8B5CF6),
        secondaryAccent = Color(0xFFD946EF),
        tertiaryAccent = Color(0xFF06B6D4),
        glowColor = Color(0x668B5CF6),
        badgeGradient = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4))
    ),

    NEON_AURORA(
        id = "NEON_AURORA",
        displayName = "Neon Kutup Işığı",
        subtitle = "Kutup zümrüdü, sibernetik dalgalar ve akış",
        iconEmoji = "✨",
        patternType = Season2PatternType.AURORA_WAVES,
        darkBackgroundTop = Color(0xFF031412),
        darkBackgroundBottom = Color(0xFF072721),
        darkCardBackground = Color(0xFF0C241E),
        darkBorderColor = Color(0xFF059669),
        darkDotColor = Color(0x3334D399),
        lightBackgroundTop = Color(0xFFF0FDF4),
        lightBackgroundBottom = Color(0xFFDCFCE7),
        lightCardBackground = Color(0xFFFFFFFF),
        lightBorderColor = Color(0xFFA7F3D0),
        lightDotColor = Color(0x3310B981),
        primaryAccent = Color(0xFF10B981),
        secondaryAccent = Color(0xFF00E5FF),
        tertiaryAccent = Color(0xFF34D399),
        glowColor = Color(0x6610B981),
        badgeGradient = listOf(Color(0xFF10B981), Color(0xFF00E5FF), Color(0xFFA3E635))
    ),

    SOLAR_FLARE(
        id = "SOLAR_FLARE",
        displayName = "Güneş Parlaması",
        subtitle = "Magma ışıltısı, güneş halkaları ve kor ateşi",
        iconEmoji = "☀️",
        patternType = Season2PatternType.SOLAR_RINGS,
        darkBackgroundTop = Color(0xFF140601),
        darkBackgroundBottom = Color(0xFF2B0C03),
        darkCardBackground = Color(0xFF260D05),
        darkBorderColor = Color(0xFFC2410C),
        darkDotColor = Color(0x33F97316),
        lightBackgroundTop = Color(0xFFFFF7ED),
        lightBackgroundBottom = Color(0xFFFFEDD5),
        lightCardBackground = Color(0xFFFFFFFF),
        lightBorderColor = Color(0xFFFED7AA),
        lightDotColor = Color(0x33EA580C),
        primaryAccent = Color(0xFFEA580C),
        secondaryAccent = Color(0xFFF59E0B),
        tertiaryAccent = Color(0xFFF43F5E),
        glowColor = Color(0x66EA580C),
        badgeGradient = listOf(Color(0xFFEA580C), Color(0xFFF59E0B), Color(0xFFFDE047))
    ),

    QUANTUM_CYBER(
        id = "QUANTUM_CYBER",
        displayName = "Kuantum Siber",
        subtitle = "Kuantum veri ızgarası, elmas düğümler ve neon mavi",
        iconEmoji = "🔮",
        patternType = Season2PatternType.QUANTUM_GRID,
        darkBackgroundTop = Color(0xFF050B1B),
        darkBackgroundBottom = Color(0xFF0B193B),
        darkCardBackground = Color(0xFF0F2047),
        darkBorderColor = Color(0xFF2563EB),
        darkDotColor = Color(0x3360A5FA),
        lightBackgroundTop = Color(0xFFF0F9FF),
        lightBackgroundBottom = Color(0xFFE0F2FE),
        lightCardBackground = Color(0xFFFFFFFF),
        lightBorderColor = Color(0xFFBAE6FD),
        lightDotColor = Color(0x333B82F6),
        primaryAccent = Color(0xFF3B82F6),
        secondaryAccent = Color(0xFF8B5CF6),
        tertiaryAccent = Color(0xFF06B6D4),
        glowColor = Color(0x663B82F6),
        badgeGradient = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))
    );

    fun getBackgroundTop(isDark: Boolean): Color = if (isDark) darkBackgroundTop else lightBackgroundTop
    fun getBackgroundBottom(isDark: Boolean): Color = if (isDark) darkBackgroundBottom else lightBackgroundBottom
    fun getCardBackground(isDark: Boolean): Color = if (isDark) darkCardBackground else lightCardBackground
    fun getBorderColor(isDark: Boolean): Color = if (isDark) darkBorderColor else lightBorderColor
    fun getDotColor(isDark: Boolean): Color = if (isDark) darkDotColor else lightDotColor

    companion object {
        fun fromId(id: String?): Season2VisualTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: COSMIC_NEBULA
        }
    }
}
