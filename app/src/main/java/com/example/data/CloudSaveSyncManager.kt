package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.LevelRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CloudSave(
    val currentLevel: Int = 1,
    val completedLevels: List<Int> = emptyList(),
    val trophies: Int = 0,
    val diamonds: Int = 1,
    val coins: Int = 50,
    val tokens: Int = 5,
    val echoBreakers: Int = 2,
    val updatedAt: String? = null
)

/**
 * CloudSaveSyncManager
 * Manages bidirectional synchronization and conflict resolution between:
 * - Local SharedPreferences / Room Database
 * - Firebase Firestore Cloud Save (/users/{userId})
 * - Firebase Firestore Global Leaderboard
 *
 * Implements strict quota optimization: local-first caching, debounced cloud writes,
 * and memory-cached leaderboards so no data is ever lost while minimizing Firestore usage.
 */
class CloudSaveSyncManager(private val context: Context) {

    private val appContext = context.applicationContext
    private val prefs = EchoPreferences(appContext)
    private val authRepo = AuthRepository(appContext)
    private val firestoreRepo = FirestoreUserRepository(appContext)

    companion object {
        private const val TAG = "CloudSaveSync"
    }

    /**
     * Executes bidirectional synchronization upon login or app launch.
     * Fetches Firestore cloud save, resolves conflicts, and applies the highest progress to both local and cloud.
     */
    suspend fun syncOnLogin(username: String = ""): Result<CloudSave> = withContext(Dispatchers.IO) {
        try {
            val userProfileResult = firestoreRepo.getUserProfile()
            val remoteUser = userProfileResult.getOrNull()

            val effectiveUsername = username.ifBlank {
                remoteUser?.username?.ifBlank { prefs.authenticatedUsername } ?: prefs.authenticatedUsername
            }

            val syncResult = firestoreRepo.syncUserProfile(
                username = effectiveUsername,
                email = prefs.authenticatedEmail,
                currentLevel = prefs.currentLevelIndex + 1,
                trophies = prefs.trophies,
                stars = prefs.getLevelRecords().sumOf { it.stars },
                tokens = prefs.tokens,
                diamonds = prefs.diamonds,
                coins = prefs.coins,
                echoBreakers = prefs.echoBreakers,
                strokeTheme = prefs.strokeThemeName,
                echoTheme = prefs.echoThemeName,
                season2Theme = prefs.selectedSeason2Theme,
                unlockedThemes = prefs.getUnlockedThemes(),
                completedLevels = prefs.getCompletedLevels(),
                levelStatsCsv = prefs.levelStatsCsv,
                maxCombo = prefs.maxCombo,
                totalEchoes = prefs.totalEchoes,
                totalPlayTimeSec = prefs.totalPlayTimeSec,
                forceImmediate = true
            )

            val mergedUser = syncResult.getOrNull()
            if (mergedUser != null) {
                applyFirestoreUserToLocal(mergedUser)

                val cloudSave = CloudSave(
                    currentLevel = mergedUser.level.toInt(),
                    completedLevels = mergedUser.completedLevels.map { it.toInt() },
                    trophies = mergedUser.trophies.toInt(),
                    diamonds = mergedUser.diamonds.toInt(),
                    coins = mergedUser.coins.toInt(),
                    tokens = mergedUser.tokens.toInt(),
                    echoBreakers = mergedUser.echoBreakers.toInt()
                )
                Result.success(cloudSave)
            } else {
                val fallbackSave = CloudSave(
                    currentLevel = prefs.currentLevelIndex + 1,
                    completedLevels = prefs.getCompletedLevels().toList(),
                    trophies = prefs.trophies,
                    diamonds = prefs.diamonds,
                    coins = prefs.coins,
                    tokens = prefs.tokens,
                    echoBreakers = prefs.echoBreakers
                )
                Result.success(fallbackSave)
            }
        } catch (e: Exception) {
            Log.e(TAG, "SyncOnLogin failed, continuing with local progress", e)
            Result.failure(e)
        }
    }

    /**
     * Pushes current local progress to Firestore.
     * Uses intelligent throttling/debouncing to conserve Firebase quota.
     */
    suspend fun pushLocalToCloud(forceImmediate: Boolean = false): Result<CloudSave>? = withContext(Dispatchers.IO) {
        val username = prefs.authenticatedUsername
        if (username.isBlank() && !prefs.isAuthenticated) return@withContext null

        try {
            val result = firestoreRepo.syncUserProfile(
                username = username,
                email = prefs.authenticatedEmail,
                currentLevel = prefs.currentLevelIndex + 1,
                trophies = prefs.trophies,
                stars = prefs.getLevelRecords().sumOf { it.stars },
                tokens = prefs.tokens,
                diamonds = prefs.diamonds,
                coins = prefs.coins,
                echoBreakers = prefs.echoBreakers,
                strokeTheme = prefs.strokeThemeName,
                echoTheme = prefs.echoThemeName,
                season2Theme = prefs.selectedSeason2Theme,
                unlockedThemes = prefs.getUnlockedThemes(),
                completedLevels = prefs.getCompletedLevels(),
                levelStatsCsv = prefs.levelStatsCsv,
                maxCombo = prefs.maxCombo,
                totalEchoes = prefs.totalEchoes,
                totalPlayTimeSec = prefs.totalPlayTimeSec,
                forceImmediate = forceImmediate
            )

            val user = result.getOrNull()
            if (user != null) {
                // Keep local values synchronized with merged results
                applyFirestoreUserToLocal(user)
                Result.success(
                    CloudSave(
                        currentLevel = user.level.toInt(),
                        completedLevels = user.completedLevels.map { it.toInt() },
                        trophies = user.trophies.toInt(),
                        diamonds = user.diamonds.toInt(),
                        coins = user.coins.toInt(),
                        tokens = user.tokens.toInt(),
                        echoBreakers = user.echoBreakers.toInt()
                    )
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "pushLocalToCloud encountered error (safe fallback to local)", e)
            null
        }
    }

    private suspend fun applyFirestoreUserToLocal(user: FirestoreUser) {
        val newLevelIndex = (user.level.toInt() - 1).coerceAtLeast(0)
        if (newLevelIndex > prefs.currentLevelIndex) {
            prefs.currentLevelIndex = newLevelIndex
        }

        val remoteCompleted = user.completedLevels.map { it.toInt() }.toSet()
        val localCompleted = prefs.getCompletedLevels()
        val combinedCompleted = (localCompleted + remoteCompleted).map { it.toString() }.toSet()
        prefs.setCompletedLevelsRaw(combinedCompleted)

        if (user.trophies.toInt() > prefs.trophies) {
            prefs.trophies = user.trophies.toInt()
        }
        if (user.diamonds.toInt() > prefs.diamonds) {
            prefs.diamonds = user.diamonds.toInt()
        }
        if (user.coins.toInt() > prefs.coins) {
            prefs.coins = user.coins.toInt()
        }
        if (user.tokens.toInt() > prefs.tokens) {
            prefs.tokens = user.tokens.toInt()
        }
        if (user.echoBreakers.toInt() > prefs.echoBreakers) {
            prefs.echoBreakers = user.echoBreakers.toInt()
        }
        if (user.maxCombo.toInt() > prefs.maxCombo) {
            prefs.maxCombo = user.maxCombo.toInt()
        }
        if (user.totalEchoes.toInt() > prefs.totalEchoes) {
            prefs.totalEchoes = user.totalEchoes.toInt()
        }
        if (user.totalPlayTimeSec > prefs.totalPlayTimeSec) {
            prefs.totalPlayTimeSec = user.totalPlayTimeSec
        }

        if (user.strokeTheme.isNotBlank()) prefs.strokeThemeName = user.strokeTheme
        if (user.echoTheme.isNotBlank()) prefs.echoThemeName = user.echoTheme
        if (user.season2Theme.isNotBlank()) prefs.selectedSeason2Theme = user.season2Theme

        user.unlockedThemes.forEach { themeId ->
            if (themeId.isNotBlank()) prefs.unlockTheme(themeId)
        }

        if (user.levelStatsCsv.isNotBlank() && user.levelStatsCsv.length > prefs.levelStatsCsv.length) {
            prefs.levelStatsCsv = user.levelStatsCsv
        }

        val username = prefs.authenticatedUsername
        if (username.isNotBlank()) {
            authRepo.saveActiveUserProgress(
                username = username,
                currentLevelIndex = prefs.currentLevelIndex,
                completedLevels = prefs.getCompletedLevels(),
                tokens = prefs.tokens,
                coins = prefs.coins,
                trophies = prefs.trophies
            )
        }
    }
}
