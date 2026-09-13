package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Data class representing update info from the version check endpoint.
 */
data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersionCode: Int,
    val versionName: String,
    val updateUrl: String,
    val title: String,
    val description: String,
    val isForceUpdate: Boolean = false
)

/**
 * Robust, non-blocking Update Check Service.
 *
 * Checks https://echoflux-kbh9.onrender.com/version.json
 * (with fallback to https://githubechov1.onrender.com/version.json).
 *
 * Requirements:
 * - Runs entirely on Dispatchers.IO with strict timeout
 * - Never blocks the Main thread or game loop
 * - Tolerates network failure gracefully (silent fallback, no crashes, no technical error text)
 * - Compares latest_version with local BuildConfig.VERSION_CODE
 */
class AppUpdateManager private constructor() {

    companion object {
        private const val TAG = "AppUpdateManager"
        private const val PRIMARY_URL = "https://echoflux-kbh9.onrender.com/version.json"
        private const val FALLBACK_URL = "https://githubechov1.onrender.com/version.json"
        private const val DEFAULT_UPDATE_URL = "https://echoflux.apk.com"

        @Volatile
        private var INSTANCE: AppUpdateManager? = null

        fun getInstance(): AppUpdateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppUpdateManager().also { INSTANCE = it }
            }
        }
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Checks if a new app version is available.
     * Guaranteed to never throw an uncaught exception.
     */
    suspend fun checkForUpdate(currentVersionCode: Int = BuildConfig.VERSION_CODE): UpdateInfo? = withContext(Dispatchers.IO) {
        // Attempt 1: Primary URL
        val primaryResult = queryVersionEndpoint(PRIMARY_URL, currentVersionCode)
        if (primaryResult != null) {
            return@withContext primaryResult
        }

        // Attempt 2: Fallback URL
        return@withContext queryVersionEndpoint(FALLBACK_URL, currentVersionCode)
    }

    private fun queryVersionEndpoint(url: String, currentVersionCode: Int): UpdateInfo? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "ECHOFLUX-Android/${BuildConfig.VERSION_NAME}")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }

                val bodyString = response.body?.string() ?: return null
                parseUpdateJson(bodyString, currentVersionCode)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Update check failed gracefully: ${e.message}")
            null
        }
    }

    private fun parseUpdateJson(jsonStr: String, currentVersionCode: Int): UpdateInfo? {
        return try {
            val json = JSONObject(jsonStr)
            val latestVersion = json.optInt("latest_version", json.optInt("version", currentVersionCode))
            val versionName = json.optString("version_name", json.optString("versionName", "1.3"))
            val updateUrl = json.optString("update_url", json.optString("updateUrl", DEFAULT_UPDATE_URL))
            val title = json.optString("title", "Yeni Güncelleme")
            val description = json.optString("description", "Yeni bir ECHOFLUX güncellemesi mevcut.")
            val forceUpdate = json.optBoolean("force_update", json.optBoolean("forceUpdate", false))

            val isAvailable = latestVersion > currentVersionCode

            UpdateInfo(
                isUpdateAvailable = isAvailable,
                latestVersionCode = latestVersion,
                versionName = versionName,
                updateUrl = if (updateUrl.isNotBlank()) updateUrl else DEFAULT_UPDATE_URL,
                title = if (title.isNotBlank()) title else "Yeni Güncelleme",
                description = if (description.isNotBlank()) description else "Yeni bir ECHOFLUX güncellemesi mevcut.",
                isForceUpdate = forceUpdate
            )
        } catch (e: Exception) {
            Log.d(TAG, "Failed to parse update json: ${e.message}")
            null
        }
    }
}
