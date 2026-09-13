package com.example.data

import android.util.Log
import com.example.model.LeaderboardPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * RenderLeaderboardService connects ECHOFLUX to the Render PostgreSQL Leaderboard backend.
 *
 * Backend URL: https://githubechov1.onrender.com
 * Endpoints:
 * - POST /leaderboard (Submits or updates player score in PostgreSQL)
 * - GET  /leaderboard (Retrieves top 100 leaderboard rankings)
 */
class RenderLeaderboardService private constructor() {

    companion object {
        private const val TAG = "RenderLeaderboard"

        // Fixed Render API Base URL
        const val BASE_URL = "https://githubechov1.onrender.com"
        const val LEADERBOARD_URL = "$BASE_URL/leaderboard"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private val AVATAR_EMOJIS = listOf(
            "⚡", "🌌", "🔮", "💠", "🌟", "✨", "🎯", "🌀", "🪐", "🚀", "🪐", "🔥"
        )

        @Volatile
        private var instance: RenderLeaderboardService? = null

        fun getInstance(): RenderLeaderboardService {
            return instance ?: synchronized(this) {
                instance ?: RenderLeaderboardService().also { instance = it }
            }
        }
    }

    // Extended timeouts to gracefully handle Render Free tier spin-up / cold starts
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(35, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Deduplication tracking to prevent repeated network calls for the same score
    @Volatile
    private var lastSubmittedUsername: String = ""

    @Volatile
    private var lastSubmittedScore: Int = -1

    @Volatile
    private var lastSubmitTimestamp: Long = 0L

    data class SubmitResult(
        val success: Boolean,
        val username: String,
        val score: Int,
        val message: String
    )

    /**
     * Submits player score to the Render PostgreSQL leaderboard.
     * Backend rules:
     * - Higher score: updates record
     * - Lower or equal score: preserves highest existing record
     *
     * Runs asynchronously on Dispatchers.IO. Never throws exceptions.
     */
    suspend fun submitScore(
        username: String,
        score: Int
    ): Result<SubmitResult> = withContext(Dispatchers.IO) {
        val sanitizedUsername = sanitizeUsername(username)
        val validScore = score.coerceAtLeast(0)

        // Prevent redundant network submission if identical username and score was submitted very recently (< 5s)
        val now = System.currentTimeMillis()
        if (sanitizedUsername == lastSubmittedUsername &&
            validScore == lastSubmittedScore &&
            (now - lastSubmitTimestamp) < 5000L
        ) {
            return@withContext Result.success(
                SubmitResult(
                    success = true,
                    username = sanitizedUsername,
                    score = validScore,
                    message = "Score already up-to-date (cached)"
                )
            )
        }

        try {
            val jsonBody = JSONObject().apply {
                put("username", sanitizedUsername)
                put("score", validScore)
            }

            val requestBody = jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(LEADERBOARD_URL)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                val statusCode = response.code

                if (response.isSuccessful) {
                    var returnedUsername = sanitizedUsername
                    var returnedScore = validScore

                    try {
                        val respObj = JSONObject(bodyString)
                        val entryObj = respObj.optJSONObject("entry")
                        if (entryObj != null) {
                            returnedUsername = entryObj.optString("username", sanitizedUsername)
                            returnedScore = entryObj.optInt("score", validScore)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse submitScore response JSON: ${e.message}")
                    }

                    lastSubmittedUsername = returnedUsername
                    lastSubmittedScore = returnedScore
                    lastSubmitTimestamp = System.currentTimeMillis()

                    Result.success(
                        SubmitResult(
                            success = true,
                            username = returnedUsername,
                            score = returnedScore,
                            message = "Score recorded successfully"
                        )
                    )
                } else {
                    Log.w(TAG, "submitScore failed with HTTP $statusCode: $bodyString")
                    Result.failure(IOException("Bağlantı kurulamadı. Lütfen internet bağlantınızı kontrol edin."))
                }
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "submitScore network unreachable (offline): ${e.message}")
            Result.failure(IOException("Çevrimdışısınız. Skorunuz yerel olarak kaydedildi."))
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "submitScore timed out waiting for server: ${e.message}")
            Result.failure(IOException("Bağlantı zaman aşımına uğradı."))
        } catch (e: Exception) {
            Log.w(TAG, "submitScore exception: ${e.message}")
            Result.failure(IOException("Skor senkronize edilemedi."))
        }
    }

    /**
     * Fetches top 100 players from Render PostgreSQL leaderboard.
     * Maps the response to LeaderboardPlayer models.
     *
     * Runs asynchronously on Dispatchers.IO. Never throws exceptions.
     */
    suspend fun fetchLeaderboard(
        currentUsername: String = ""
    ): Result<List<LeaderboardPlayer>> = withContext(Dispatchers.IO) {
        val sanitizedCurrentUser = sanitizeUsername(currentUsername)

        try {
            val request = Request.Builder()
                .url(LEADERBOARD_URL)
                .get()
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                val statusCode = response.code

                if (response.isSuccessful) {
                    val resultList = mutableListOf<LeaderboardPlayer>()
                    val jsonResponse = JSONObject(bodyString)
                    val leaderboardArray = jsonResponse.optJSONArray("leaderboard") ?: JSONArray()

                    for (i in 0 until leaderboardArray.length()) {
                        val item = leaderboardArray.optJSONObject(i) ?: continue
                        val itemUsername = item.optString("username", "Oyuncu").trim()
                        val itemScore = item.optInt("score", 0)
                        val rank = i + 1

                        val isMe = sanitizedCurrentUser.isNotEmpty() &&
                                itemUsername.equals(sanitizedCurrentUser, ignoreCase = true)

                        val emoji = when (rank) {
                            1 -> "👑"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> if (isMe) "⚡" else getEmojiForIndex(i)
                        }

                        val title = when {
                            itemScore >= 2500 -> "🌟 Galaktik Efsane"
                            itemScore >= 1500 -> "🏆 Yankı Ustası"
                            itemScore >= 800 -> "⚡ Frekans Ustası"
                            itemScore >= 300 -> "🔷 Ses Kaşifi"
                            else -> "🌱 Çaylak"
                        }

                        resultList.add(
                            LeaderboardPlayer(
                                id = "render_${itemUsername}_$rank",
                                rank = rank,
                                username = itemUsername,
                                avatarEmoji = emoji,
                                title = title,
                                trophies = itemScore,
                                totalEchoes = 0,
                                totalPlayTimeSec = 0,
                                maxCombo = 0,
                                completedLevelsCount = 0,
                                isCurrentUser = isMe
                            )
                        )
                    }

                    Result.success(resultList)
                } else {
                    Log.w(TAG, "fetchLeaderboard failed with HTTP $statusCode: $bodyString")
                    Result.failure(IOException("Şu anda çevrimiçi sıralamaya ulaşılamıyor. Lütfen biraz sonra tekrar deneyin."))
                }
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "fetchLeaderboard network unreachable (offline): ${e.message}")
            Result.failure(IOException("Çevrimdışısınız. Lütfen internet bağlantınızı kontrol edin."))
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "fetchLeaderboard timed out: ${e.message}")
            Result.failure(IOException("Bağlantı zaman aşımına uğradı."))
        } catch (e: Exception) {
            Log.w(TAG, "fetchLeaderboard exception: ${e.message}")
            Result.failure(IOException("Şu anda çevrimiçi sıralamaya ulaşılamıyor."))
        }
    }

    private fun sanitizeUsername(username: String): String {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) return "Oyuncu"
        // Strip out control chars or html tags
        val cleaned = trimmed.replace(Regex("<[^>]*>|[<>]"), "").trim()
        return if (cleaned.isEmpty()) "Oyuncu" else cleaned.take(20)
    }

    private fun getEmojiForIndex(index: Int): String {
        return AVATAR_EMOJIS[index % AVATAR_EMOJIS.size]
    }
}
