package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.hypot

data class Point(val x: Float, val y: Float) {
    fun distanceTo(other: Point): Float = hypot(x - other.x, y - other.y)
}

enum class NodeType {
    NORMAL,
    KEY,
    GATE
}

data class Node(
    val id: Int,
    val x: Float,
    val y: Float,
    val connected: Boolean = false,
    val type: NodeType = NodeType.NORMAL,
    val keyForGateId: Int = -1,
    val isKeyCollected: Boolean = false,
    val isGateUnlocked: Boolean = false
) {
    fun toPoint(): Point = Point(x, y)
}

data class Segment(
    val p1: Point,
    val p2: Point,
    val fromNodeId: Int = -1,
    val toNodeId: Int = -1
) {
    val length: Float get() = p1.distanceTo(p2)
}

data class DirectedEdge(
    val fromId: Int,
    val toId: Int
)

data class EchoStroke(
    val id: Int,
    val segments: List<Segment>,
    val remainingAttempts: Int = 3,
    val maxLifetime: Int = 3,
    val isGhost: Boolean = false
)

data class LevelNode(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: NodeType = NodeType.NORMAL,
    val keyForGateId: Int = -1
)

data class LevelData(
    val levelId: Int,
    val title: String,
    val gridSize: Int,
    val nodes: List<LevelNode>,
    val directedEdges: List<DirectedEdge> = emptyList(),
    val parEchoes: Int = 0,
    val hintOrder: List<Int> = emptyList(),
    val mechanicType: String = "STANDARD", // STANDARD, DECAYING, LOCK_KEY, ONE_WAY, GHOST, MAZE
    val decayLifetime: Int = 3,
    val isGhostEchoes: Boolean = false,
    val description: String = ""
)

enum class GameStatus {
    PLAYING,
    VICTORY,
    DEADLOCK
}

enum class ScreenState {
    INTRO,
    LOGIN,
    MAIN_MENU,
    PLAYING_LEVEL,
    DAILY_CHALLENGE
}

data class EchoStats(
    val totalEchoesCreated: Int = 0,
    val completedLevelsCount: Int = 0,
    val totalPlayTimeSec: Long = 0L
)

enum class ThemeRarity(
    val title: String,
    val color: Color,
    val backgroundColor: Color,
    val requiredAds: Int = 1,
    val coinCost: Int = 40,
    val diamondCost: Int = 0
) {
    COMMON("Yaygın", Color(0xFF64748B), Color(0xFFF1F5F9), requiredAds = 1, coinCost = 40, diamondCost = 0),
    RARE("Nadir", Color(0xFF0284C7), Color(0xFFE0F2FE), requiredAds = 3, coinCost = 120, diamondCost = 0),
    EPIC("Epik", Color(0xFF9333EA), Color(0xFFF3E8FF), requiredAds = 6, coinCost = 300, diamondCost = 1),
    LEGENDARY("Çok Nadir (Efsanevi)", Color(0xFFD97706), Color(0xFFFEF3C7), requiredAds = 10, coinCost = 600, diamondCost = 4)
}

enum class StrokeTheme(
    val displayName: String,
    val primaryColor: Color,
    val glowColor: Color,
    val rarity: ThemeRarity = ThemeRarity.COMMON,
    val requiredStars: Int = 0
) {
    NEON_CYAN("Okyanus Mavisi", Color(0xFF0284C7), Color(0x440284C7), ThemeRarity.COMMON, 0),
    SOLAR_FLAME("Güneş Turuncusu", Color(0xFFEA580C), Color(0x44EA580C), ThemeRarity.COMMON, 0),
    CYBER_MAGENTA("Fuşya Enerji", Color(0xFFD946EF), Color(0x44D946EF), ThemeRarity.RARE, 30),
    ZEN_INK("Gece Kobaltı", Color(0xFF312E81), Color(0x44312E81), ThemeRarity.RARE, 60),
    AURORA_EMERALD("Kutup Zümrüdü", Color(0xFF10B981), Color(0x4410B981), ThemeRarity.EPIC, 120),
    GOLDEN_PULSE("Altın Lazer", Color(0xFFF59E0B), Color(0x44F59E0B), ThemeRarity.LEGENDARY, 200)
}

enum class EchoTheme(
    val displayName: String,
    val echoColor: Color,
    val glowColor: Color,
    val rarity: ThemeRarity = ThemeRarity.COMMON,
    val requiredStars: Int = 0
) {
    ELECTRIC_RED("Kızıl Lazer", Color(0xFFDC2626), Color(0x44DC2626), ThemeRarity.COMMON, 0),
    SHATTERED_ICE("Buz Mavisi", Color(0xFF0284C7), Color(0x440284C7), ThemeRarity.COMMON, 0),
    VOID_DARK("Mor Sis", Color(0xFF7C3AED), Color(0x447C3AED), ThemeRarity.RARE, 45),
    CRIMSON_NEBULA("Karanlık Nebula", Color(0xFFBE123C), Color(0x44BE123C), ThemeRarity.EPIC, 100),
    SOLAR_PLASMA("Güneş Plazması", Color(0xFFF97316), Color(0x44F97316), ThemeRarity.LEGENDARY, 180)
}

data class DailyQuest(
    val id: Int,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val rewardTokens: Int,
    val rewardBreakers: Int,
    val isClaimed: Boolean,
    val rewardCoins: Int = 15,
    val rewardDiamonds: Int = 0
) {
    val isCompleted: Boolean get() = current >= target
}

data class DailyLoginDay(
    val dayNumber: Int,
    val title: String,
    val tokens: Int,
    val breakers: Int,
    val coins: Int = 20,
    val diamonds: Int = 0,
    val isLegendary: Boolean = false,
    val isClaimed: Boolean = false,
    val isToday: Boolean = false,
    val isPast: Boolean = false
)

data class ChestReward(
    val rarity: ThemeRarity,
    val title: String,
    val subtitle: String,
    val tokens: Int,
    val breakers: Int,
    val coins: Int = 25,
    val diamonds: Int = 0,
    val unlockedThemeName: String? = null
)

data class LevelRecord(
    val levelId: Int,
    val levelTitle: String,
    val echoesUsed: Int,
    val timeTakenSec: Float,
    val stars: Int,
    val trophiesEarned: Int
)

data class TrophyRewardBreakdown(
    val baseTrophies: Int,
    val zeroEchoBonus: Int,
    val comboBonus: Int,
    val speedBonus: Int,
    val totalTrophies: Int,
    val currentCombo: Int,
    val timeTakenSec: Float
)

data class LeaderboardPlayer(
    val id: String,
    val rank: Int,
    val username: String,
    val avatarEmoji: String,
    val avatarUri: String = "",
    val title: String,
    val trophies: Int,
    val totalEchoes: Int,
    val totalPlayTimeSec: Long,
    val maxCombo: Int,
    val completedLevelsCount: Int,
    val isCurrentUser: Boolean = false,
    val levelRecords: List<LevelRecord> = emptyList()
)

