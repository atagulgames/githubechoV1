package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.LeaderboardPlayer
import com.example.model.LevelRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * LootLockerManager handles authentication, score submissions, and global ranking queries
 * for LootLocker's Game API.
 *
 * Leaderboard Key: ekoleadbordglobal
 * Leaderboard URL: https://api.lootlocker.io/game/leaderboards/ekoleadbordglobal/submit
 */
class LootLockerManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = EchoPreferences(appContext)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var sessionToken: String? = null

    @Volatile
    private var playerId: Int = 0

    private val isLoggingIn = java.util.concurrent.atomic.AtomicBoolean(false)

    @Volatile
    private var hasInvalidGameKey: Boolean = false

    init {
        // Restore cached session token if available
        sessionToken = prefs.lootLockerSessionToken
        playerId = prefs.lootLockerPlayerId
    }

    companion object {
        private const val TAG = "LootLocker"

        const val GAME_VERSION = "1.0.0"

        // Global Leaderboard Key (ekoleadbordglobal)
        const val DEFAULT_LEADERBOARD_KEY = "ekoleadbordglobal"

        private const val BASE_URL = "https://api.lootlocker.io/game"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        @Volatile
        private var instance: LootLockerManager? = null

        fun getInstance(context: Context): LootLockerManager {
            return instance ?: synchronized(this) {
                instance ?: LootLockerManager(context).also { instance = it }
            }
        }
    }

    fun getLeaderboardKey(): String {
        val key = prefs.lootLockerLeaderboardKey
        return if (key.isNotBlank()) key else DEFAULT_LEADERBOARD_KEY
    }

    fun setLeaderboardKey(key: String) {
        prefs.lootLockerLeaderboardKey = key.trim()
    }

    fun getEffectiveGameKey(): String {
        return prefs.lootLockerGameKey.trim()
    }

    fun setGameKey(key: String) {
        prefs.lootLockerGameKey = key.trim()
        hasInvalidGameKey = false
        sessionToken = null
        prefs.lootLockerSessionToken = null
    }

    fun isConfigured(): Boolean {
        val key = getEffectiveGameKey()
        return key.isNotBlank() && key != "BURAYA_GAME_KEY" && key != DEFAULT_LEADERBOARD_KEY && !hasInvalidGameKey
    }

    fun getSessionToken(): String? = sessionToken
    fun getPlayerId(): Int = playerId

    /**
     * Logs in the player as a Guest in LootLocker.
     */
    fun loginGuest(
        onSuccess: ((playerId: Int, sessionToken: String) -> Unit)? = null,
        onFailure: ((error: String) -> Unit)? = null
    ) {
        if (!isConfigured()) {
            val msg = "LootLocker Game API Key tanımlı değil (Tablo: ${getLeaderboardKey()}). Yerel skorlar ve sıralama aktif."
            Log.i(TAG, msg)
            onFailure?.invoke(msg)
            return
        }

        val key = getEffectiveGameKey()

        // Concurrency guard: avoid launching multiple concurrent guest login calls
        if (!isLoggingIn.compareAndSet(false, true)) {
            Log.d(TAG, "Giriş işlemi zaten devam ediyor...")
            onFailure?.invoke("Giriş devam ediyor")
            return
        }

        try {
            val json = JSONObject().apply {
                put("game_key", key)
                put("game_version", GAME_VERSION)
                put("player_identifier", prefs.lootLockerPlayerIdentifier)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$BASE_URL/v2/session/guest")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    isLoggingIn.set(false)
                    val errorMsg = "Guest Login bağlantı hatası: ${e.message}"
                    Log.w(TAG, errorMsg)
                    onFailure?.invoke(errorMsg)
                }

                override fun onResponse(call: Call, response: Response) {
                    isLoggingIn.set(false)
                    response.use { resp ->
                        val result = resp.body?.string() ?: ""
                        Log.d(TAG, "Login HTTP ${resp.code}: $result")

                        if (!resp.isSuccessful) {
                            if (resp.code == 400 && result.contains("invalid game_api_key")) {
                                hasInvalidGameKey = true
                                Log.w(TAG, "LootLocker Game API Key geçersiz veya dashboard'da henüz oluşturulmamış. Yerel skorlar kullanılmaya devam edecek.")
                            } else {
                                Log.w(TAG, "LootLocker Login HTTP ${resp.code}: $result")
                            }
                            onFailure?.invoke("HTTP ${resp.code}")
                            return
                        }

                        try {
                            val respJson = JSONObject(result)
                            val token = respJson.optString("session_token", "")
                            val pId = respJson.optInt("player_id", 0)

                            if (token.isNotBlank()) {
                                sessionToken = token
                                playerId = pId
                                prefs.lootLockerSessionToken = token
                                prefs.lootLockerPlayerId = pId
                                hasInvalidGameKey = false
                                Log.i(TAG, "LootLocker giriş başarılı! Player ID: $pId")
                                onSuccess?.invoke(pId, token)
                            } else {
                                val errorMsg = "Giriş yanıtında session_token bulunamadı: $result"
                                Log.w(TAG, errorMsg)
                                onFailure?.invoke(errorMsg)
                            }
                        } catch (e: Exception) {
                            val errorMsg = "JSON ayrıştırma hatası: ${e.message}"
                            Log.w(TAG, errorMsg)
                            onFailure?.invoke(errorMsg)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            isLoggingIn.set(false)
            val errorMsg = "Login istisna: ${e.message}"
            Log.w(TAG, errorMsg)
            onFailure?.invoke(errorMsg)
        }
    }

    /**
     * Submits a player's score to the LootLocker global leaderboard.
     *
     * @param playerName The username to display on the leaderboard
     * @param score The player's score/trophies
     * @param metadata Optional metadata (e.g. avatar or combo info)
     */
    fun submitScore(
        playerName: String,
        score: Int,
        metadata: String = "",
        onComplete: ((success: Boolean, message: String) -> Unit)? = null
    ) {
        if (!isConfigured()) {
            onComplete?.invoke(false, "LootLocker Game API Key yapılandırılmamış")
            return
        }

        val currentToken = sessionToken
        if (currentToken.isNullOrBlank()) {
            Log.d(TAG, "Oturum açık değil, önce loginGuest çalıştırılıyor...")
            loginGuest(
                onSuccess = { _, _ ->
                    // Set player name as well for LootLocker profile
                    setPlayerName(playerName)
                    executeSubmitScore(playerName, score, metadata, onComplete)
                },
                onFailure = { err ->
                    Log.d(TAG, "Skor sunucuya gönderilemedi: $err")
                    onComplete?.invoke(false, err)
                }
            )
            return
        }

        // Keep player profile name in sync on LootLocker
        setPlayerName(playerName)
        executeSubmitScore(playerName, score, metadata, onComplete)
    }

    private fun executeSubmitScore(
        playerName: String,
        score: Int,
        metadata: String,
        onComplete: ((success: Boolean, message: String) -> Unit)?
    ) {
        val token = sessionToken ?: return
        try {
            val json = JSONObject().apply {
                put("member_id", playerName)
                put("score", score)
                if (metadata.isNotBlank()) {
                    put("metadata", metadata)
                }
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val url = "$BASE_URL/leaderboards/${getLeaderboardKey()}/submit"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("x-session-token", token)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val msg = "Skor gönderilemedi: ${e.message}"
                    Log.w(TAG, msg)
                    onComplete?.invoke(false, msg)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        val result = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            Log.i(TAG, "SKOR BAŞARIYLA GÖNDERİLDİ! Oyuncu: $playerName, Skor: $score")
                            Log.d(TAG, "LootLocker yanıt: $result")
                            onComplete?.invoke(true, "Başarılı")
                        } else {
                            val msg = "Skor gönderme HTTP ${resp.code}: $result"
                            Log.w(TAG, msg)
                            // If session expired, clear token to relogin next time
                            if (resp.code == 401) {
                                sessionToken = null
                                prefs.lootLockerSessionToken = null
                            }
                            onComplete?.invoke(false, msg)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            val msg = "Submit exception: ${e.message}"
            Log.w(TAG, msg)
            onComplete?.invoke(false, msg)
        }
    }

    /**
     * Sets the player's name on LootLocker.
     */
    fun setPlayerName(name: String, onComplete: ((Boolean) -> Unit)? = null) {
        val token = sessionToken ?: return
        if (name.isBlank()) return

        try {
            val json = JSONObject().apply {
                put("name", name)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$BASE_URL/player/name")
                .patch(body)
                .addHeader("x-session-token", token)
                .addHeader("LL-Version", "2021-03-01")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onComplete?.invoke(false)
                }
                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        onComplete?.invoke(resp.isSuccessful)
                    }
                }
            })
        } catch (_: Exception) {
            onComplete?.invoke(false)
        }
    }

    /**
     * Fetches the global leaderboard from LootLocker (ekoleadbordglobal).
     */
    fun getLeaderboard(
        count: Int = 50,
        currentUsername: String = "",
        onResult: (Result<List<LeaderboardPlayer>>) -> Unit
    ) {
        if (!isConfigured()) {
            onResult(Result.failure(IllegalStateException("Game API Key tanımlı değil")))
            return
        }

        val currentToken = sessionToken
        if (currentToken.isNullOrBlank()) {
            loginGuest(
                onSuccess = { _, _ ->
                    executeGetLeaderboard(count, currentUsername, onResult)
                },
                onFailure = { err ->
                    onResult(Result.failure(IOException("LootLocker oturumu açılamadı: $err")))
                }
            )
            return
        }

        executeGetLeaderboard(count, currentUsername, onResult)
    }

    private fun executeGetLeaderboard(
        count: Int,
        currentUsername: String,
        onResult: (Result<List<LeaderboardPlayer>>) -> Unit
    ) {
        val token = sessionToken ?: run {
            onResult(Result.failure(IllegalStateException("Session token bulunamadı")))
            return
        }

        val url = "$BASE_URL/leaderboards/${getLeaderboardKey()}/list?count=$count"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-session-token", token)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "Liderlik tablosu alınamadı: ${e.message}")
                onResult(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    val result = resp.body?.string() ?: ""
                    if (!resp.isSuccessful) {
                        Log.e(TAG, "Liderlik tablosu HTTP ${resp.code}: $result")
                        onResult(Result.failure(IOException("HTTP ${resp.code}: $result")))
                        return
                    }

                    try {
                        val json = JSONObject(result)
                        val itemsArray = json.optJSONArray("items")
                        val playersList = ArrayList<LeaderboardPlayer>()

                        if (itemsArray != null) {
                            for (i in 0 until itemsArray.length()) {
                                val item = itemsArray.getJSONObject(i)
                                val rank = item.optInt("rank", i + 1)
                                val score = item.optInt("score", 0)
                                val memberId = item.optString("member_id", "")
                                val playerObj = item.optJSONObject("player")
                                val pName = playerObj?.optString("name", "") ?: ""
                                val pId = playerObj?.optInt("id", 0) ?: 0
                                val metadata = item.optString("metadata", "")

                                val resolvedName = when {
                                    pName.isNotBlank() -> pName
                                    memberId.isNotBlank() -> memberId
                                    else -> "Oyuncu #$rank"
                                }

                                val isCurrent = (currentUsername.isNotBlank() && (resolvedName.equals(currentUsername, ignoreCase = true) || memberId.equals(currentUsername, ignoreCase = true)))
                                        || (playerId > 0 && pId == playerId)

                                val avatarEmoji = when (rank) {
                                    1 -> "👑"
                                    2 -> "🥈"
                                    3 -> "🥉"
                                    in 4..10 -> "⭐"
                                    else -> "🌀"
                                }

                                val customTitle = when {
                                    rank == 1 -> "🏆 Dünya Birincisi"
                                    rank in 2..3 -> "🥈 Elit Yankı Ustası"
                                    rank in 4..10 -> "⭐ Global İlk 10"
                                    score >= 1000 -> "⚡ Yankı Şampiyonu"
                                    else -> "Ses Kaşifi"
                                }

                                playersList.add(
                                    LeaderboardPlayer(
                                        id = "lootlocker_${pId}_$rank",
                                        rank = rank,
                                        username = resolvedName,
                                        avatarEmoji = avatarEmoji,
                                        title = customTitle,
                                        trophies = score,
                                        totalEchoes = 0,
                                        totalPlayTimeSec = 0L,
                                        maxCombo = 0,
                                        completedLevelsCount = (score / 30).coerceAtLeast(1),
                                        isCurrentUser = isCurrent,
                                        levelRecords = emptyList()
                                    )
                                )
                            }
                        }

                        Log.i(TAG, "LootLocker liderlik tablosu başarıyla yüklendi: ${playersList.size} oyuncu")
                        onResult(Result.success(playersList))
                    } catch (e: Exception) {
                        Log.e(TAG, "Liderlik JSON ayrıştırma hatası: ${e.message}")
                        onResult(Result.failure(e))
                    }
                }
            }
        })
    }

    /**
     * Coroutine-friendly suspend version of getLeaderboard.
     */
    suspend fun fetchLeaderboard(
        count: Int = 50,
        currentUsername: String = ""
    ): Result<List<LeaderboardPlayer>> = suspendCancellableCoroutine { continuation ->
        getLeaderboard(count, currentUsername) { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
    }

    /**
     * Coroutine-friendly suspend version of submitScore.
     */
    suspend fun submitScoreSuspend(
        playerName: String,
        score: Int,
        metadata: String = ""
    ): Boolean = suspendCancellableCoroutine { continuation ->
        submitScore(playerName, score, metadata) { success, _ ->
            if (continuation.isActive) {
                continuation.resume(success)
            }
        }
    }
}
