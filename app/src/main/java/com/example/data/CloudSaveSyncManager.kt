package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.security.SecureTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CloudSaveSyncManager
 * Manages bidirectional synchronization and conflict resolution between:
 * - Local SharedPreferences / Room Database
 * - Render PostgreSQL Cloud Save (GET /save, POST /save)
 * - Render Global Leaderboard (POST /leaderboard)
 */
class CloudSaveSyncManager(private val context: Context) {

    private val appContext = context.applicationContext
    private val prefs = EchoPreferences(appContext)
    private val authRepo = AuthRepository(appContext)
    private val cloudService = RenderAuthAndCloudSaveService.getInstance()
    private val leaderboardService = RenderLeaderboardService.getInstance()

    companion object {
        private const val TAG = "CloudSaveSync"
    }

    /**
     * Executes bidirectional synchronization upon login or app launch.
     * Fetches cloud save, resolves conflicts, and applies the highest progress to both local and cloud.
     */
    suspend fun syncOnLogin(token: String): Result<CloudSave> = withContext(Dispatchers.IO) {
        try {
            val cloudResult = cloudService.getCloudSave(token)
            val cloudSave = cloudResult.getOrNull()

            val localCurrentLevel = prefs.currentLevelIndex + 1
            val localCompleted = prefs.getCompletedLevels()
            val localTrophies = prefs.trophies
            val localCoins = prefs.coins
            val localLives = prefs.tokens.coerceIn(1, 5)

            val merged = if (cloudSave != null) {
                resolveConflict(
                    localLevel = localCurrentLevel,
                    localCompleted = localCompleted,
                    localTrophies = localTrophies,
                    localCoins = localCoins,
                    localLives = localLives,
                    cloud = cloudSave
                )
            } else {
                CloudSave(
                    currentLevel = localCurrentLevel,
                    completedLevels = localCompleted.toList(),
                    trophies = localTrophies,
                    coins = localCoins,
                    lives = localLives
                )
            }

            // Apply merged state to local storage
            applyToLocal(merged)

            // Push merged state back to cloud to guarantee synchronization
            try {
                cloudService.postCloudSave(token, merged)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push merged save to cloud, will retry later", e)
            }

            // Sync with Render Global Leaderboard
            val username = prefs.authenticatedUsername
            if (username.isNotBlank() && merged.trophies > 0) {
                try {
                    leaderboardService.submitScore(username, merged.trophies)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to submit score to leaderboard", e)
                }
            }

            Result.success(merged)
        } catch (e: Exception) {
            Log.e(TAG, "SyncOnLogin failed", e)
            Result.failure(e)
        }
    }

    /**
     * Pushes current local progress to cloud if authenticated with JWT.
     */
    suspend fun pushLocalToCloud(): Result<CloudSave>? = withContext(Dispatchers.IO) {
        val token = SecureTokenManager.getToken(appContext) ?: return@withContext null

        try {
            val localSave = CloudSave(
                currentLevel = prefs.currentLevelIndex + 1,
                completedLevels = prefs.getCompletedLevels().toList(),
                trophies = prefs.trophies,
                coins = prefs.coins,
                lives = prefs.tokens.coerceIn(1, 5)
            )

            val result = cloudService.postCloudSave(token, localSave)

            val username = prefs.authenticatedUsername
            if (username.isNotBlank() && prefs.trophies > 0) {
                leaderboardService.submitScore(username, prefs.trophies)
            }

            result
        } catch (e: Exception) {
            Log.w(TAG, "pushLocalToCloud encountered error (safe fallback to local)", e)
            null
        }
    }

    /**
     * Resolves conflict between local save and cloud save.
     * Invariants:
     * - Never regresses level (keeps highest currentLevel).
     * - Never regresses trophies (keeps highest trophies).
     * - Keeps union of completed levels so no beaten level is ever lost.
     * - Keeps highest accumulated coins.
     */
    fun resolveConflict(
        localLevel: Int,
        localCompleted: Set<Int>,
        localTrophies: Int,
        localCoins: Int,
        localLives: Int,
        cloud: CloudSave
    ): CloudSave {
        val mergedLevel = maxOf(localLevel, cloud.currentLevel)
        val mergedTrophies = maxOf(localTrophies, cloud.trophies)
        val mergedCoins = maxOf(localCoins, cloud.coins)
        val mergedLives = maxOf(localLives, cloud.lives).coerceIn(1, 5)
        val mergedCompleted = (localCompleted + cloud.completedLevels.toSet()).sorted()

        return CloudSave(
            currentLevel = mergedLevel,
            completedLevels = mergedCompleted,
            trophies = mergedTrophies,
            coins = mergedCoins,
            lives = mergedLives
        )
    }

    private suspend fun applyToLocal(save: CloudSave) {
        prefs.currentLevelIndex = (save.currentLevel - 1).coerceAtLeast(0)
        prefs.setCompletedLevelsRaw(save.completedLevels.map { it.toString() }.toSet())
        prefs.trophies = save.trophies
        prefs.coins = save.coins

        val username = prefs.authenticatedUsername
        if (username.isNotBlank()) {
            authRepo.saveActiveUserProgress(
                username = username,
                currentLevelIndex = prefs.currentLevelIndex,
                completedLevels = save.completedLevels.toSet(),
                tokens = save.lives,
                coins = save.coins,
                trophies = save.trophies
            )
        }
    }
}
