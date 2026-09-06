package com.example.data

import android.content.Context
import android.content.SharedPreferences

class EchoPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("echo_game_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_LEVEL = "echo_current_level"
        private const val KEY_TOKENS = "echo_tokens"
        private const val KEY_ECHO_BREAKERS = "echo_breakers"
        private const val KEY_TOTAL_ECHOES = "echo_stats_total_echoes"
        private const val KEY_COMPLETED_LEVELS = "echo_completed_levels"
        private const val KEY_AD_FREE = "echo_is_ad_free"
        private const val KEY_SOUND = "echo_sound_enabled"
        private const val KEY_HAPTICS = "echo_haptics_enabled"
        private const val KEY_STROKE_THEME = "echo_stroke_theme"
        private const val KEY_ECHO_THEME = "echo_theme_choice"
        private const val KEY_LAST_DAILY = "echo_last_daily_date"
        private const val KEY_TEST_ADS = "echo_test_ads_enabled"
        private const val KEY_LANGUAGE = "echo_language_code"

        // Daily Quests
        private const val KEY_QUESTS_DATE = "echo_quests_date"
        private const val KEY_QUEST_LEVELS_TODAY = "echo_quest_levels_today"
        private const val KEY_QUEST_DAILY_CHALLENGE = "echo_quest_daily_challenge"
        private const val KEY_QUEST_FLAWLESS_TODAY = "echo_quest_flawless_today"
        private const val KEY_QUEST_STARS_TODAY = "echo_quest_stars_today"
        private const val KEY_QUEST_CLAIMED_PREFIX = "echo_quest_claimed_"

        // Daily Login Streak (7-day calendar)
        private const val KEY_LOGIN_STREAK = "echo_login_streak"
        private const val KEY_LAST_LOGIN_CLAIM_DATE = "echo_last_login_claim_date"

        // Mystery Chest
        private const val KEY_LAST_FREE_CHEST_DATE = "echo_last_free_chest_date"
        private const val KEY_AD_CHESTS_TODAY = "echo_ad_chests_today"
        private const val KEY_CHESTS_DATE = "echo_chests_date"

        // Unlocked themes
        private const val KEY_UNLOCKED_THEMES = "echo_unlocked_themes"

        // Authentication
        private const val KEY_IS_AUTHENTICATED = "echo_is_authenticated"
        private const val KEY_AUTH_USERNAME = "echo_auth_username"
        private const val KEY_REMEMBER_ME = "echo_remember_me"
        private const val KEY_AUTH_PASSWORD_HASH = "echo_auth_password_hash"
    }

    var isAuthenticated: Boolean
        get() = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_AUTHENTICATED, value).apply()

    var authenticatedUsername: String
        get() = prefs.getString(KEY_AUTH_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_USERNAME, value).apply()

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()

    var storedPasswordHash: String
        get() = prefs.getString(KEY_AUTH_PASSWORD_HASH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_PASSWORD_HASH, value).apply()

    fun setAuthenticatedUser(username: String, remember: Boolean, passwordHash: String = "") {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, true)
            .putString(KEY_AUTH_USERNAME, username)
            .putBoolean(KEY_REMEMBER_ME, remember)
            .putString(KEY_AUTH_PASSWORD_HASH, passwordHash)
            .apply()
    }

    fun clearAuthentication() {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, false)
            .apply()
        if (!rememberMe) {
            prefs.edit().putString(KEY_AUTH_USERNAME, "").apply()
        }
    }

    var languageCode: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var currentLevelIndex: Int
        get() = prefs.getInt(KEY_CURRENT_LEVEL, 0)
        set(value) = prefs.edit().putInt(KEY_CURRENT_LEVEL, value).apply()

    var tokens: Int
        get() = prefs.getInt(KEY_TOKENS, 5) // 5 hint tokens
        set(value) = prefs.edit().putInt(KEY_TOKENS, value.coerceAtLeast(0)).apply()

    var echoBreakers: Int
        get() = prefs.getInt(KEY_ECHO_BREAKERS, 2) // Start with 2 free Echo Breakers
        set(value) = prefs.edit().putInt(KEY_ECHO_BREAKERS, value.coerceAtLeast(0)).apply()

    var totalEchoes: Int
        get() = prefs.getInt(KEY_TOTAL_ECHOES, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_ECHOES, value).apply()

    var isAdFree: Boolean
        get() = prefs.getBoolean(KEY_AD_FREE, false)
        set(value) = prefs.edit().putBoolean(KEY_AD_FREE, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var strokeThemeName: String
        get() = prefs.getString(KEY_STROKE_THEME, "NEON_CYAN") ?: "NEON_CYAN"
        set(value) = prefs.edit().putString(KEY_STROKE_THEME, value).apply()

    var echoThemeName: String
        get() = prefs.getString(KEY_ECHO_THEME, "ELECTRIC_RED") ?: "ELECTRIC_RED"
        set(value) = prefs.edit().putString(KEY_ECHO_THEME, value).apply()

    var lastDailyCompletedDate: String
        get() = prefs.getString(KEY_LAST_DAILY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_DAILY, value).apply()

    var isTestAdsEnabled: Boolean
        get() = prefs.getBoolean(KEY_TEST_ADS, false)
        set(value) = prefs.edit().putBoolean(KEY_TEST_ADS, value).apply()

    fun getCompletedLevels(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun markLevelCompleted(levelIndex: Int) {
        val currentSet = getCompletedLevels().toMutableSet()
        currentSet.add(levelIndex)
        prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, currentSet.map { it.toString() }.toSet()).apply()
        if (levelIndex >= currentLevelIndex) {
            currentLevelIndex = levelIndex + 1
        }
    }

    fun incrementTotalEchoes() {
        totalEchoes += 1
    }

    fun addTokens(count: Int) {
        tokens += count
    }

    fun addBreakers(count: Int) {
        echoBreakers += count
    }

    fun useToken(): Boolean {
        return if (tokens > 0) {
            tokens -= 1
            true
        } else false
    }

    fun useBreaker(): Boolean {
        return if (echoBreakers > 0) {
            echoBreakers -= 1
            true
        } else false
    }

    // --- Daily Quests ---
    fun checkAndResetDailyQuests(todayDate: String) {
        val savedDate = prefs.getString(KEY_QUESTS_DATE, "")
        if (savedDate != todayDate) {
            prefs.edit()
                .putString(KEY_QUESTS_DATE, todayDate)
                .putInt(KEY_QUEST_LEVELS_TODAY, 0)
                .putBoolean(KEY_QUEST_DAILY_CHALLENGE, false)
                .putInt(KEY_QUEST_FLAWLESS_TODAY, 0)
                .putInt(KEY_QUEST_STARS_TODAY, 0)
                .putBoolean("${KEY_QUEST_CLAIMED_PREFIX}1", false)
                .putBoolean("${KEY_QUEST_CLAIMED_PREFIX}2", false)
                .putBoolean("${KEY_QUEST_CLAIMED_PREFIX}3", false)
                .putBoolean("${KEY_QUEST_CLAIMED_PREFIX}4", false)
                .apply()
        }
    }

    var levelsCompletedToday: Int
        get() = prefs.getInt(KEY_QUEST_LEVELS_TODAY, 0)
        set(value) = prefs.edit().putInt(KEY_QUEST_LEVELS_TODAY, value).apply()

    var dailyChallengeCompletedToday: Boolean
        get() = prefs.getBoolean(KEY_QUEST_DAILY_CHALLENGE, false)
        set(value) = prefs.edit().putBoolean(KEY_QUEST_DAILY_CHALLENGE, value).apply()

    var flawlessLevelsToday: Int
        get() = prefs.getInt(KEY_QUEST_FLAWLESS_TODAY, 0)
        set(value) = prefs.edit().putInt(KEY_QUEST_FLAWLESS_TODAY, value).apply()

    var starsEarnedToday: Int
        get() = prefs.getInt(KEY_QUEST_STARS_TODAY, 0)
        set(value) = prefs.edit().putInt(KEY_QUEST_STARS_TODAY, value).apply()

    fun isQuestClaimed(questId: Int): Boolean {
        return prefs.getBoolean("${KEY_QUEST_CLAIMED_PREFIX}$questId", false)
    }

    fun setQuestClaimed(questId: Int, claimed: Boolean = true) {
        prefs.edit().putBoolean("${KEY_QUEST_CLAIMED_PREFIX}$questId", claimed).apply()
    }

    // --- Daily Login Streak (7-day calendar) ---
    var loginStreak: Int
        get() = prefs.getInt(KEY_LOGIN_STREAK, 1).coerceIn(1, 7)
        set(value) = prefs.edit().putInt(KEY_LOGIN_STREAK, value.coerceIn(1, 7)).apply()

    var lastLoginClaimDate: String
        get() = prefs.getString(KEY_LAST_LOGIN_CLAIM_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_LOGIN_CLAIM_DATE, value).apply()

    fun isLoginRewardClaimedToday(todayDate: String): Boolean {
        return lastLoginClaimDate == todayDate
    }

    // --- Mystery Echo Chest ---
    var lastFreeChestDate: String
        get() = prefs.getString(KEY_LAST_FREE_CHEST_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_FREE_CHEST_DATE, value).apply()

    fun isFreeChestAvailable(todayDate: String): Boolean {
        return lastFreeChestDate != todayDate
    }

    fun getAdChestsOpenedToday(todayDate: String): Int {
        val savedDate = prefs.getString(KEY_CHESTS_DATE, "")
        return if (savedDate == todayDate) {
            prefs.getInt(KEY_AD_CHESTS_TODAY, 0)
        } else {
            0
        }
    }

    fun incrementAdChestOpened(todayDate: String) {
        val savedDate = prefs.getString(KEY_CHESTS_DATE, "")
        if (savedDate == todayDate) {
            val count = prefs.getInt(KEY_AD_CHESTS_TODAY, 0)
            prefs.edit().putInt(KEY_AD_CHESTS_TODAY, count + 1).apply()
        } else {
            prefs.edit()
                .putString(KEY_CHESTS_DATE, todayDate)
                .putInt(KEY_AD_CHESTS_TODAY, 1)
                .apply()
        }
    }

    // --- Unlocked Themes ---
    fun getUnlockedThemes(): Set<String> {
        val set = prefs.getStringSet(KEY_UNLOCKED_THEMES, null)
        return set ?: setOf(
            "NEON_CYAN", "SOLAR_FLAME", "ELECTRIC_RED", "SHATTERED_ICE"
        )
    }

    fun unlockTheme(themeName: String) {
        val current = getUnlockedThemes().toMutableSet()
        current.add(themeName)
        prefs.edit().putStringSet(KEY_UNLOCKED_THEMES, current).apply()
    }

    fun isThemeUnlocked(themeName: String, requiredStars: Int, totalStars: Int): Boolean {
        if (requiredStars == 0 || totalStars >= requiredStars) return true
        return getUnlockedThemes().contains(themeName)
    }

    var hasFreshStartV2: Boolean
        get() = prefs.getBoolean("echo_v2_fresh_start_done", false)
        set(value) = prefs.edit().putBoolean("echo_v2_fresh_start_done", value).apply()

    fun resetAllProgress() {
        prefs.edit()
            .putInt(KEY_CURRENT_LEVEL, 0)
            .putInt(KEY_TOTAL_ECHOES, 0)
            .putInt(KEY_TOKENS, 5)
            .putInt(KEY_ECHO_BREAKERS, 2)
            .remove(KEY_COMPLETED_LEVELS)
            .remove(KEY_LAST_DAILY)
            .remove(KEY_QUESTS_DATE)
            .remove(KEY_LOGIN_STREAK)
            .remove(KEY_LAST_LOGIN_CLAIM_DATE)
            .remove(KEY_LAST_FREE_CHEST_DATE)
            .remove(KEY_UNLOCKED_THEMES)
            .apply()
    }
}
