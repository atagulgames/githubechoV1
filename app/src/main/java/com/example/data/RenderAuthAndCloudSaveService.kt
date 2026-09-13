package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AuthUser(
    val id: Int,
    val fullName: String,
    val email: String
)

data class CloudSave(
    val currentLevel: Int,
    val completedLevels: List<Int>,
    val trophies: Int,
    val coins: Int,
    val lives: Int,
    val updatedAt: String? = null
)

/**
 * Service interacting with Render PostgreSQL Backend for:
 * - Authentication (Register, Login, Me)
 * - Cloud Save Synchronization (GET /save, POST /save)
 */
class RenderAuthAndCloudSaveService private constructor() {

    companion object {
        private const val TAG = "RenderAuthService"
        const val BASE_URL = "https://githubechov1.onrender.com"
        const val AUTH_REGISTER_URL = "$BASE_URL/auth/register"
        const val AUTH_LOGIN_URL = "$BASE_URL/auth/login"
        const val AUTH_ME_URL = "$BASE_URL/auth/me"
        const val SAVE_URL = "$BASE_URL/save"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        @Volatile
        private var INSTANCE: RenderAuthAndCloudSaveService? = null

        fun getInstance(): RenderAuthAndCloudSaveService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RenderAuthAndCloudSaveService().also { INSTANCE = it }
            }
        }
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(35, TimeUnit.SECONDS) // Generous timeout for Render free tier spin-up
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Registers a new user account with Full Name, Email, and Password.
     */
    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): Result<Pair<AuthUser, String>> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("fullName", fullName.trim())
                put("email", email.trim().lowercase())
                put("password", password)
            }

            val requestBody = jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(AUTH_REGISTER_URL)
                .post(requestBody)
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseStr = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val root = JSONObject(responseStr)
                    val userObj = root.getJSONObject("user")
                    val token = root.getString("token")
                    val user = AuthUser(
                        id = userObj.getInt("id"),
                        fullName = userObj.getString("fullName"),
                        email = userObj.getString("email")
                    )
                    Result.success(Pair(user, token))
                } else {
                    val errorMsg = try {
                        val backendErr = JSONObject(responseStr).optString("error", "")
                        if (backendErr.isNotBlank() &&
                            !backendErr.contains("http", ignoreCase = true) &&
                            !backendErr.contains("sql", ignoreCase = true) &&
                            !backendErr.contains("database", ignoreCase = true)
                        ) {
                            backendErr
                        } else {
                            "Kayıt işlemi gerçekleştirilemedi. Lütfen bilgilerinizi kontrol edin."
                        }
                    } catch (_: Exception) {
                        "Kayıt işlemi gerçekleştirilemedi. Lütfen bilgilerinizi kontrol edin."
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration network error", e)
            Result.failure(Exception("Bağlantı kurulamadı. Lütfen internet bağlantınızı kontrol edin."))
        }
    }

    /**
     * Authenticates existing user with Email and Password.
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<Pair<AuthUser, String>> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }

            val requestBody = jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(AUTH_LOGIN_URL)
                .post(requestBody)
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseStr = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val root = JSONObject(responseStr)
                    val userObj = root.getJSONObject("user")
                    val token = root.getString("token")
                    val user = AuthUser(
                        id = userObj.getInt("id"),
                        fullName = userObj.getString("fullName"),
                        email = userObj.getString("email")
                    )
                    Result.success(Pair(user, token))
                } else {
                    val errorMsg = try {
                        val backendErr = JSONObject(responseStr).optString("error", "")
                        if (backendErr.isNotBlank() &&
                            !backendErr.contains("http", ignoreCase = true) &&
                            !backendErr.contains("sql", ignoreCase = true) &&
                            !backendErr.contains("database", ignoreCase = true)
                        ) {
                            backendErr
                        } else {
                            "Giriş yapılamadı. Lütfen bilgilerinizi kontrol edin."
                        }
                    } catch (_: Exception) {
                        "Giriş yapılamadı. Lütfen bilgilerinizi kontrol edin."
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login network error", e)
            Result.failure(Exception("Bağlantı kurulamadı. Lütfen internet bağlantınızı kontrol edin."))
        }
    }

    /**
     * Validates active JWT token and retrieves user account info.
     */
    suspend fun getMe(token: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(AUTH_ME_URL)
                .get()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseStr = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val root = JSONObject(responseStr)
                    val userObj = root.getJSONObject("user")
                    val user = AuthUser(
                        id = userObj.getInt("id"),
                        fullName = userObj.getString("fullName"),
                        email = userObj.getString("email")
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception("Oturum süresi dolmuş veya geçersiz."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches authenticated user's cloud save.
     */
    suspend fun getCloudSave(token: String): Result<CloudSave> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(SAVE_URL)
                .get()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseStr = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val root = JSONObject(responseStr)
                    val saveObj = root.getJSONObject("save")

                    val completedJsonArr = saveObj.optJSONArray("completedLevels") ?: JSONArray()
                    val completedList = mutableListOf<Int>()
                    for (i in 0 until completedJsonArr.length()) {
                        completedList.add(completedJsonArr.getInt(i))
                    }

                    val save = CloudSave(
                        currentLevel = saveObj.optInt("currentLevel", 1),
                        completedLevels = completedList,
                        trophies = saveObj.optInt("trophies", 0),
                        coins = saveObj.optInt("coins", 50),
                        lives = saveObj.optInt("lives", 5),
                        updatedAt = saveObj.optString("updatedAt", null)
                    )
                    Result.success(save)
                } else {
                    Result.failure(Exception("Kaydınız senkronize edilemedi."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "GetCloudSave error", e)
            Result.failure(e)
        }
    }

    /**
     * Updates authenticated user's cloud save.
     */
    suspend fun postCloudSave(token: String, save: CloudSave): Result<CloudSave> = withContext(Dispatchers.IO) {
        try {
            val completedArr = JSONArray().apply {
                save.completedLevels.forEach { put(it) }
            }
            val jsonBody = JSONObject().apply {
                put("currentLevel", save.currentLevel)
                put("completedLevels", completedArr)
                put("trophies", save.trophies)
                put("coins", save.coins)
                put("lives", save.lives)
            }

            val requestBody = jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(SAVE_URL)
                .post(requestBody)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseStr = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val root = JSONObject(responseStr)
                    val saveObj = root.optJSONObject("save") ?: jsonBody
                    val completedJsonArr = saveObj.optJSONArray("completedLevels") ?: completedArr
                    val completedList = mutableListOf<Int>()
                    for (i in 0 until completedJsonArr.length()) {
                        completedList.add(completedJsonArr.getInt(i))
                    }

                    val savedResult = CloudSave(
                        currentLevel = saveObj.optInt("currentLevel", save.currentLevel),
                        completedLevels = completedList,
                        trophies = saveObj.optInt("trophies", save.trophies),
                        coins = saveObj.optInt("coins", save.coins),
                        lives = saveObj.optInt("lives", save.lives),
                        updatedAt = saveObj.optString("updatedAt", null)
                    )
                    Result.success(savedResult)
                } else {
                    Result.failure(Exception("Kaydınız güncellenemedi."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "PostCloudSave error", e)
            Result.failure(e)
        }
    }
}
