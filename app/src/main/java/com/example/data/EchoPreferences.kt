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
        private const val KEY_SOUND = "echo_sound_enabled"
        private const val KEY_HAPTICS = "echo_haptics_enabled"
        private const val KEY_STROKE_THEME = "echo_stroke_theme"
        private const val KEY_ECHO_THEME = "echo_theme_choice"
        private const val KEY_LAST_DAILY = "echo_last_daily_date"
        private const val KEY_TEST_ADS = "echo_test_ads_enabled"
        private const val KEY_LANGUAGE = "echo_language_code"
        private const val KEY_DARK_THEME = "echo_is_dark_theme"

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

        // Currency & Economy
        private const val KEY_COINS = "echo_coins"
        private const val KEY_DIAMONDS = "echo_diamonds"
        private const val KEY_THEME_AD_WATCH_PREFIX = "echo_theme_ad_progress_"
        private const val KEY_FREE_DIAMOND_ADS_DATE = "echo_free_diamond_ads_date"
        private const val KEY_FREE_DIAMOND_ADS_COUNT = "echo_free_diamond_ads_count"
        private const val KEY_LAST_DIAMOND_AD_TIME = "echo_last_diamond_ad_time"
        private const val KEY_FREE_COIN_ADS_DATE = "echo_free_coin_ads_date"
        private const val KEY_FREE_COIN_ADS_COUNT = "echo_free_coin_ads_count"
        private const val KEY_LAST_COIN_AD_TIME = "echo_last_coin_ad_time"

        // Unlocked themes
        private const val KEY_UNLOCKED_THEMES = "echo_unlocked_themes"

        // Authentication
        private const val KEY_IS_AUTHENTICATED = "echo_is_authenticated"
        private const val KEY_AUTH_USERNAME = "echo_auth_username"
        private const val KEY_REMEMBER_ME = "echo_remember_me"
        private const val KEY_AUTH_PASSWORD_HASH = "echo_auth_password_hash"

        // Leaderboard, Trophies & Gameplay Records
        private const val KEY_TROPHIES = "echo_trophies"
        private const val KEY_TOTAL_PLAY_TIME = "echo_total_play_time_sec"
        private const val KEY_MAX_COMBO = "echo_max_combo"
        private const val KEY_CURRENT_COMBO = "echo_current_combo"
        private const val KEY_LEVEL_STATS_CSV = "echo_level_stats_csv"

        // Multi-ad rewards & Timed Boosters & Ad Pacing
        private const val KEY_MULTI_AD_WATCH_COUNT = "echo_multi_ad_watch_count"
        private const val KEY_DOUBLE_TROPHIES_EXPIRES = "echo_double_trophies_expires"
        private const val KEY_INFINITE_BREAKERS_EXPIRES = "echo_infinite_breakers_expires"
        private const val KEY_RADIUS_SHRINKER_EXPIRES = "echo_radius_shrinker_expires"
        private const val KEY_LEVELS_SINCE_LAST_AD = "echo_levels_since_last_ad"
        private const val KEY_NEXT_INTERSTITIAL_THRESHOLD = "echo_next_interstitial_threshold"
        private const val KEY_SHRINKER_ADS_WATCHED = "echo_shrinker_ads_watched"
        private const val KEY_USER_AVATAR_URI = "echo_user_avatar_uri"
        private const val KEY_USER_CUSTOM_TITLE = "echo_user_custom_title"
        private const val KEY_KVKK_CONSENT_ACCEPTED = "echo_user_agreement_v2_accepted"
        private const val KEY_HINT_ADS_WATCHED = "echo_hint_ads_watched"
        private const val KEY_SHOWN_MECHANIC_TIERS = "echo_shown_mechanic_tiers"
        private const val KEY_SHOWN_LEVEL_RULES = "echo_shown_level_rules"
        private const val KEY_AUTO_SHOW_LEVEL_RULES = "echo_auto_show_level_rules"

        // LootLocker Global Leaderboard
        private const val KEY_LOOTLOCKER_SESSION_TOKEN = "echo_lootlocker_session_token"
        private const val KEY_LOOTLOCKER_PLAYER_ID = "echo_lootlocker_player_id"
        private const val KEY_LOOTLOCKER_PLAYER_IDENTIFIER = "echo_lootlocker_player_identifier"
        private const val KEY_LOOTLOCKER_GAME_KEY = "echo_lootlocker_game_key"
        private const val KEY_LOOTLOCKER_LEADERBOARD_KEY = "echo_lootlocker_leaderboard_key"

        // 3-Hour Reward Cooldown System (3 Hours = 3 * 3600 * 1000L = 10,800,000 ms)
        private const val KEY_REWARD_LAST_TIME_PREFIX = "echo_reward_last_claim_time_"
        const val REWARD_COOLDOWN_MILLIS = 3 * 60 * 60 * 1000L // 3 Hours (10800000 ms)
    }

    var shownMechanicTiersCsv: String
        get() = prefs.getString(KEY_SHOWN_MECHANIC_TIERS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SHOWN_MECHANIC_TIERS, value).apply()

    fun isMechanicIntroShown(tier: Int): Boolean {
        val tiers = shownMechanicTiersCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        return tiers.contains(tier)
    }

    fun markMechanicIntroShown(tier: Int) {
        val tiers = shownMechanicTiersCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toMutableSet()
        tiers.add(tier)
        shownMechanicTiersCsv = tiers.joinToString(",")
    }

    var shownLevelRulesCsv: String
        get() = prefs.getString(KEY_SHOWN_LEVEL_RULES, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SHOWN_LEVEL_RULES, value).apply()

    var isAutoShowLevelRulesEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SHOW_LEVEL_RULES, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SHOW_LEVEL_RULES, value).apply()

    fun isLevelRuleShown(levelId: Int): Boolean {
        val rules = shownLevelRulesCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        return rules.contains(levelId)
    }

    fun markLevelRuleShown(levelId: Int) {
        val rules = shownLevelRulesCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toMutableSet()
        rules.add(levelId)
        shownLevelRulesCsv = rules.joinToString(",")
    }

    var isKvkkConsentAccepted: Boolean
        get() = prefs.getBoolean(KEY_KVKK_CONSENT_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_KVKK_CONSENT_ACCEPTED, value).apply()

    var hintAdsWatched: Int
        get() = prefs.getInt(KEY_HINT_ADS_WATCHED, 0)
        set(value) = prefs.edit().putInt(KEY_HINT_ADS_WATCHED, value.coerceIn(0, 3)).apply()

    var shrinkerAdsWatched: Int
        get() = prefs.getInt(KEY_SHRINKER_ADS_WATCHED, 0)
        set(value) = prefs.edit().putInt(KEY_SHRINKER_ADS_WATCHED, value.coerceAtLeast(0)).apply()

    var userAvatarUri: String
        get() = prefs.getString(KEY_USER_AVATAR_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_AVATAR_URI, value).apply()

    var userCustomTitle: String
        get() = prefs.getString(KEY_USER_CUSTOM_TITLE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_CUSTOM_TITLE, value).apply()

    var multiAdWatchCount: Int
        get() = prefs.getInt(KEY_MULTI_AD_WATCH_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_MULTI_AD_WATCH_COUNT, value.coerceIn(0, 3)).apply()

    var doubleTrophiesExpiresAt: Long
        get() = prefs.getLong(KEY_DOUBLE_TROPHIES_EXPIRES, 0L)
        set(value) = prefs.edit().putLong(KEY_DOUBLE_TROPHIES_EXPIRES, value).apply()

    var infiniteBreakersExpiresAt: Long
        get() = prefs.getLong(KEY_INFINITE_BREAKERS_EXPIRES, 0L)
        set(value) = prefs.edit().putLong(KEY_INFINITE_BREAKERS_EXPIRES, value).apply()

    var radiusShrinkerExpiresAt: Long
        get() = prefs.getLong(KEY_RADIUS_SHRINKER_EXPIRES, 0L)
        set(value) = prefs.edit().putLong(KEY_RADIUS_SHRINKER_EXPIRES, value).apply()

    var levelsSinceLastInterstitial: Int
        get() = prefs.getInt(KEY_LEVELS_SINCE_LAST_AD, 0)
        set(value) = prefs.edit().putInt(KEY_LEVELS_SINCE_LAST_AD, value).apply()

    var nextInterstitialThreshold: Int
        get() = prefs.getInt(KEY_NEXT_INTERSTITIAL_THRESHOLD, 2)
        set(value) = prefs.edit().putInt(KEY_NEXT_INTERSTITIAL_THRESHOLD, value).apply()

    var trophies: Int
        get() = prefs.getInt(KEY_TROPHIES, 0)
        set(value) = prefs.edit().putInt(KEY_TROPHIES, value.coerceAtLeast(0)).apply()

    var totalPlayTimeSec: Long
        get() = prefs.getLong(KEY_TOTAL_PLAY_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_PLAY_TIME, value.coerceAtLeast(0L)).apply()

    var maxCombo: Int
        get() = prefs.getInt(KEY_MAX_COMBO, 0)
        set(value) = prefs.edit().putInt(KEY_MAX_COMBO, value.coerceAtLeast(0)).apply()

    var currentCombo: Int
        get() = prefs.getInt(KEY_CURRENT_COMBO, 0)
        set(value) = prefs.edit().putInt(KEY_CURRENT_COMBO, value.coerceAtLeast(0)).apply()

    var levelStatsCsv: String
        get() = prefs.getString(KEY_LEVEL_STATS_CSV, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LEVEL_STATS_CSV, value).apply()

    fun addPlayTime(seconds: Long) {
        if (seconds > 0) {
            totalPlayTimeSec += seconds
        }
    }

    fun addTrophies(amount: Int) {
        if (amount > 0) {
            trophies += amount
        }
    }

    fun recordCombo(combo: Int) {
        currentCombo = combo
        if (combo > maxCombo) {
            maxCombo = combo
        }
    }

    fun getLevelRecords(): List<com.example.model.LevelRecord> {
        val raw = levelStatsCsv
        if (raw.isBlank()) return emptyList()
        val results = ArrayList<com.example.model.LevelRecord>()
        raw.split(";").forEach { item ->
            val parts = item.split(":")
            if (parts.size >= 4) {
                val lvlId = parts[0].toIntOrNull() ?: 1
                val echoes = parts[1].toIntOrNull() ?: 0
                val time = parts[2].toFloatOrNull() ?: 0f
                val stars = parts[3].toIntOrNull() ?: 3
                val trEarned = if (parts.size >= 5) parts[4].toIntOrNull() ?: 50 else 50
                val title = "Bölüm $lvlId"
                results.add(
                    com.example.model.LevelRecord(
                        levelId = lvlId,
                        levelTitle = title,
                        echoesUsed = echoes,
                        timeTakenSec = time,
                        stars = stars,
                        trophiesEarned = trEarned
                    )
                )
            }
        }
        return results.sortedBy { it.levelId }
    }

    fun recordLevelResult(levelId: Int, title: String, echoes: Int, timeTakenSec: Float, stars: Int, trophiesEarned: Int) {
        val currentRecords = getLevelRecords().toMutableList()
        val existingIndex = currentRecords.indexOfFirst { it.levelId == levelId }
        val newRecord = com.example.model.LevelRecord(
            levelId = levelId,
            levelTitle = title,
            echoesUsed = echoes,
            timeTakenSec = timeTakenSec,
            stars = stars,
            trophiesEarned = trophiesEarned
        )
        if (existingIndex >= 0) {
            // Keep best or replace if better
            val old = currentRecords[existingIndex]
            if (echoes <= old.echoesUsed) {
                currentRecords[existingIndex] = newRecord
            }
        } else {
            currentRecords.add(newRecord)
        }
        // Serialize back
        val csv = currentRecords.joinToString(";") {
            "${it.levelId}:${it.echoesUsed}:${(it.timeTakenSec * 10).toInt() / 10f}:${it.stars}:${it.trophiesEarned}"
        }
        levelStatsCsv = csv
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

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 50) // Modest starting coin balance
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    var diamonds: Int
        get() = prefs.getInt(KEY_DIAMONDS, 1) // Diamonds are very rare and hard to obtain
        set(value) = prefs.edit().putInt(KEY_DIAMONDS, value.coerceAtLeast(0)).apply()

    fun getThemeAdProgress(themeName: String): Int {
        return prefs.getInt(KEY_THEME_AD_WATCH_PREFIX + themeName, 0)
    }

    fun incrementThemeAdProgress(themeName: String): Int {
        val current = getThemeAdProgress(themeName) + 1
        prefs.edit().putInt(KEY_THEME_AD_WATCH_PREFIX + themeName, current).apply()
        return current
    }

    fun getFreeDiamondAdsRemaining(todayDate: String): Int {
        val savedDate = prefs.getString(KEY_FREE_DIAMOND_ADS_DATE, "")
        return if (savedDate == todayDate) {
            val watched = prefs.getInt(KEY_FREE_DIAMOND_ADS_COUNT, 0)
            (3 - watched).coerceAtLeast(0) // 3 diamond ads per day limit
        } else {
            3
        }
    }

    fun recordFreeDiamondAdWatched(todayDate: String) {
        val savedDate = prefs.getString(KEY_FREE_DIAMOND_ADS_DATE, "")
        val currentCount = if (savedDate == todayDate) prefs.getInt(KEY_FREE_DIAMOND_ADS_COUNT, 0) else 0
        prefs.edit()
            .putString(KEY_FREE_DIAMOND_ADS_DATE, todayDate)
            .putInt(KEY_FREE_DIAMOND_ADS_COUNT, currentCount + 1)
            .putLong(KEY_LAST_DIAMOND_AD_TIME, System.currentTimeMillis())
            .apply()
        recordRewardClaimed("FREE_DIAMOND")
    }

    fun getRewardCooldownRemainingSeconds(rewardType: String): Long {
        val lastTime = prefs.getLong("${KEY_REWARD_LAST_TIME_PREFIX}$rewardType", 0L)
        if (lastTime <= 0L) return 0L
        val elapsedMs = System.currentTimeMillis() - lastTime
        val remainingMs = REWARD_COOLDOWN_MILLIS - elapsedMs
        return (remainingMs / 1000L).coerceAtLeast(0L)
    }

    fun isRewardAvailable(rewardType: String): Boolean {
        return getRewardCooldownRemainingSeconds(rewardType) <= 0L
    }

    fun recordRewardClaimed(rewardType: String) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putLong("${KEY_REWARD_LAST_TIME_PREFIX}$rewardType", now)
            .putLong("${KEY_REWARD_LAST_TIME_PREFIX}GLOBAL", now)
            .apply()
    }

    fun getDiamondAdCooldownSeconds(): Long {
        return getRewardCooldownRemainingSeconds("FREE_DIAMOND")
    }

    fun getFreeCoinAdsRemaining(todayDate: String): Int {
        val savedDate = prefs.getString(KEY_FREE_COIN_ADS_DATE, "")
        return if (savedDate == todayDate) {
            val watched = prefs.getInt(KEY_FREE_COIN_ADS_COUNT, 0)
            (5 - watched).coerceAtLeast(0) // 5 coin ads per day limit
        } else {
            5
        }
    }

    fun recordFreeCoinAdWatched(todayDate: String) {
        val savedDate = prefs.getString(KEY_FREE_COIN_ADS_DATE, "")
        val currentCount = if (savedDate == todayDate) prefs.getInt(KEY_FREE_COIN_ADS_COUNT, 0) else 0
        prefs.edit()
            .putString(KEY_FREE_COIN_ADS_DATE, todayDate)
            .putInt(KEY_FREE_COIN_ADS_COUNT, currentCount + 1)
            .putLong(KEY_LAST_COIN_AD_TIME, System.currentTimeMillis())
            .apply()
        recordRewardClaimed("FREE_COINS")
    }

    fun getCoinAdCooldownSeconds(): Long {
        return getRewardCooldownRemainingSeconds("FREE_COINS")
    }

    var totalEchoes: Int
        get() = prefs.getInt(KEY_TOTAL_ECHOES, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_ECHOES, value).apply()

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

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    fun getCompletedLevels(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setCompletedLevelsRaw(stringSet: Set<String>) {
        prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, stringSet).apply()
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

    fun addDiamonds(count: Int) {
        diamonds += count
    }

    fun useDiamond(count: Int = 1): Boolean {
        return if (diamonds >= count) {
            diamonds -= count
            true
        } else false
    }

    fun addCoins(count: Int) {
        coins += count
    }

    fun useCoins(count: Int): Boolean {
        return if (coins >= count) {
            coins -= count
            true
        } else false
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

    fun shouldShowInterstitial(levelId: Int): Boolean {
        if (levelId <= 5) return false
        val threshold = if (levelId > 50) 2 else nextInterstitialThreshold
        return levelsSinceLastInterstitial >= threshold
    }

    fun onInterstitialDisplayed(levelId: Int) {
        levelsSinceLastInterstitial = 0
        if (levelId in 6..50) {
            // Alternate between 2 and 3 levels: 2 -> 3 -> 2 -> 3
            nextInterstitialThreshold = if (nextInterstitialThreshold == 2) 3 else 2
        } else if (levelId > 50) {
            nextInterstitialThreshold = 2
        }
    }

    fun incrementLevelsSinceInterstitial() {
        levelsSinceLastInterstitial += 1
    }

    fun getLevelTimerDeadlineMs(levelId: Int): Long {
        return prefs.getLong("echo_level_deadline_$levelId", 0L)
    }

    fun setLevelTimerDeadlineMs(levelId: Int, deadlineMs: Long) {
        prefs.edit().putLong("echo_level_deadline_$levelId", deadlineMs).apply()
    }

    fun clearLevelTimer(levelId: Int) {
        prefs.edit().remove("echo_level_deadline_$levelId").apply()
    }

    fun getLevelRemainingSeconds(levelId: Int): Int {
        val deadline = getLevelTimerDeadlineMs(levelId)
        if (deadline <= 0L) return 60
        val remaining = ((deadline - System.currentTimeMillis()) / 1000L).toInt()
        return remaining.coerceAtLeast(0)
    }

    /**
     * User request: "eğer reklam izlersen 15 sn ek süre eklensin."
     * Adds extra seconds to current level deadline.
     */
    fun addLevelTime(levelId: Int, additionalSeconds: Int = 15) {
        val currentDeadline = getLevelTimerDeadlineMs(levelId)
        val now = System.currentTimeMillis()
        val baseMs = if (currentDeadline > now) currentDeadline else now
        setLevelTimerDeadlineMs(levelId, baseMs + additionalSeconds * 1000L)
    }

    // --- LootLocker Session & Player Credentials ---
    var lootLockerSessionToken: String?
        get() = prefs.getString(KEY_LOOTLOCKER_SESSION_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_LOOTLOCKER_SESSION_TOKEN, value).apply()

    var lootLockerPlayerId: Int
        get() = prefs.getInt(KEY_LOOTLOCKER_PLAYER_ID, 0)
        set(value) = prefs.edit().putInt(KEY_LOOTLOCKER_PLAYER_ID, value).apply()

    var lootLockerPlayerIdentifier: String
        get() {
            var id = prefs.getString(KEY_LOOTLOCKER_PLAYER_IDENTIFIER, null)
            if (id.isNullOrBlank()) {
                id = java.util.UUID.randomUUID().toString()
                prefs.edit().putString(KEY_LOOTLOCKER_PLAYER_IDENTIFIER, id).apply()
            }
            return id
        }
        set(value) = prefs.edit().putString(KEY_LOOTLOCKER_PLAYER_IDENTIFIER, value).apply()

    var lootLockerGameKey: String
        get() {
            val key = prefs.getString(KEY_LOOTLOCKER_GAME_KEY, "") ?: ""
            return if (key == "BURAYA_GAME_KEY" || key == "ekoleadbordglobal") "" else key
        }
        set(value) = prefs.edit().putString(KEY_LOOTLOCKER_GAME_KEY, value.trim()).apply()

    var lootLockerLeaderboardKey: String
        get() = prefs.getString(KEY_LOOTLOCKER_LEADERBOARD_KEY, "ekoleadbordglobal") ?: "ekoleadbordglobal"
        set(value) = prefs.edit().putString(KEY_LOOTLOCKER_LEADERBOARD_KEY, value.trim()).apply()

    // --- Ghost Error Trails (Geçmişin Hayaletleri) ---
    fun saveGhostStroke(levelId: Int, points: List<Pair<Float, Float>>) {
        if (points.isEmpty()) return
        val currentRaw = prefs.getString("echo_ghost_strokes_$levelId", "") ?: ""
        val newStrokeStr = points.joinToString(";") { "${it.first.toInt()},${it.second.toInt()}" }
        // Keep up to 6 historical ghost strokes so screen turns into a rich living labyrinth of memories
        val list = currentRaw.split("|").filter { it.isNotBlank() }.toMutableList()
        if (list.size >= 6) list.removeAt(0)
        list.add(newStrokeStr)
        prefs.edit().putString("echo_ghost_strokes_$levelId", list.joinToString("|")).apply()
    }

    fun getGhostStrokes(levelId: Int): List<List<Pair<Float, Float>>> {
        val raw = prefs.getString("echo_ghost_strokes_$levelId", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("|").filter { it.isNotBlank() }.map { strokeStr ->
            strokeStr.split(";").mapNotNull { ptStr ->
                val parts = ptStr.split(",")
                if (parts.size == 2) {
                    val x = parts[0].toFloatOrNull()
                    val y = parts[1].toFloatOrNull()
                    if (x != null && y != null) Pair(x, y) else null
                } else null
            }
        }
    }

    fun clearGhostStrokes(levelId: Int) {
        prefs.edit().remove("echo_ghost_strokes_$levelId").apply()
    }

    // --- Perfect Echo (Kusursuzluk & Kendi Hayaletinle Yarış) ---
    fun savePerfectRun(levelId: Int, points: List<Pair<Float, Float>>, durationMs: Long) {
        if (points.isEmpty()) return
        val pathStr = points.joinToString(";") { "${it.first.toInt()},${it.second.toInt()}" }
        prefs.edit()
            .putString("echo_perfect_run_path_$levelId", pathStr)
            .putLong("echo_perfect_run_dur_$levelId", durationMs)
            .apply()
    }

    fun getPerfectRun(levelId: Int): Pair<List<Pair<Float, Float>>, Long>? {
        val pathStr = prefs.getString("echo_perfect_run_path_$levelId", null) ?: return null
        val dur = prefs.getLong("echo_perfect_run_dur_$levelId", 0L)
        val pts = pathStr.split(";").mapNotNull { ptStr ->
            val parts = ptStr.split(",")
            if (parts.size == 2) {
                val x = parts[0].toFloatOrNull()
                val y = parts[1].toFloatOrNull()
                if (x != null && y != null) Pair(x, y) else null
            } else null
        }
        if (pts.isEmpty()) return null
        return Pair(pts, dur)
    }

    fun hasPerfectRun(levelId: Int): Boolean {
        return prefs.contains("echo_perfect_run_path_$levelId")
    }

    // --- Behavioral Telemetry (Oyuncu Davranış Analizi) ---
    fun recordBehaviorError(x: Float, y: Float, isRetreat: Boolean, isFast: Boolean) {
        val quadKey = if (y < 300f) {
            if (x < 220f) "quad_top_left" else "quad_top_right"
        } else {
            if (x < 220f) "quad_bottom_left" else "quad_bottom_right"
        }
        val currentCount = prefs.getInt("echo_behavior_$quadKey", 0)
        prefs.edit().putInt("echo_behavior_$quadKey", currentCount + 1).apply()

        if (isRetreat) {
            val retreats = prefs.getInt("echo_behavior_retreats", 0)
            prefs.edit().putInt("echo_behavior_retreats", retreats + 1).apply()
        }
        if (isFast) {
            val fastMoves = prefs.getInt("echo_behavior_fast_moves", 0)
            prefs.edit().putInt("echo_behavior_fast_moves", fastMoves + 1).apply()
        }
    }

    fun getBehaviorInsight(): String {
        val tl = prefs.getInt("echo_behavior_quad_top_left", 0)
        val tr = prefs.getInt("echo_behavior_quad_top_right", 0)
        val bl = prefs.getInt("echo_behavior_quad_bottom_left", 0)
        val br = prefs.getInt("echo_behavior_quad_bottom_right", 0)
        val retreats = prefs.getInt("echo_behavior_retreats", 0)
        val fastMoves = prefs.getInt("echo_behavior_fast_moves", 0)

        val totalErrors = tl + tr + bl + br
        return when {
            totalErrors == 0 -> "Analiz: Kusursuz Yolculuk"
            tr > tl && tr > bl && tr > br -> "Analiz: Sağ Üstte Tereddüt Tespit Edildi"
            tl > tr && tl > bl && tl > br -> "Analiz: Sol Üst Dönüşlerde Hata Eğilimi"
            bl > tr && bl > tl && bl > br -> "Analiz: Sol Alt Köşede Kararsızlık"
            br > tr && br > tl && br > bl -> "Analiz: Sağ Alt Bölgede Odaklanma"
            retreats >= 3 -> "Analiz: Geri Çekilme & İkilem Eğilimi"
            fastMoves >= 4 -> "Analiz: Yüksek Hızlı Refleks Oyuncusu"
            else -> "Analiz: Zihinsel Rota Hesaplaması Aktif"
        }
    }

    fun getAdaptiveTendencyQuadrant(): Int? {
        val tl = prefs.getInt("echo_behavior_quad_top_left", 0)
        val tr = prefs.getInt("echo_behavior_quad_top_right", 0)
        val bl = prefs.getInt("echo_behavior_quad_bottom_left", 0)
        val br = prefs.getInt("echo_behavior_quad_bottom_right", 0)
        return when {
            tr > tl && tr > bl && tr > br && tr >= 2 -> 1 // top-right
            tl > tr && tl > bl && tl > br && tl >= 2 -> 2 // top-left
            bl > tr && bl > tl && bl > br && bl >= 2 -> 3 // bottom-left
            br > tr && br > tl && br > bl && br >= 2 -> 4 // bottom-right
            else -> null
        }
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
            .remove(KEY_KVKK_CONSENT_ACCEPTED)
            .apply()
    }
}
