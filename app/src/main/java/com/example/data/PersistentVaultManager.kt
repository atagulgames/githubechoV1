package com.example.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.local.UserAccountEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Ensures user accounts and gameplay progress survive app uninstall and reinstall.
 * Saves encoded JSON snapshots to multiple persistent directories on device storage.
 */
object PersistentVaultManager {
    private const val TAG = "PersistentVault"
    private const val VAULT_FILE_NAME = "echo_accounts_persistent_vault.json"

    private fun getVaultFiles(context: Context): List<File> {
        val files = mutableListOf<File>()
        try {
            // 1. App external files directory
            context.getExternalFilesDir(null)?.let { dir ->
                files.add(File(dir, VAULT_FILE_NAME))
            }
            // 2. Android media directory (standard persistent location across reinstalls on many Android versions)
            val mediaDir = File(Environment.getExternalStorageDirectory(), "Android/media/${context.packageName}")
            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }
            files.add(File(mediaDir, VAULT_FILE_NAME))

            // 3. Documents directory fallback
            val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }
            files.add(File(docsDir, "echo_game_user_vault.json"))
        } catch (e: Exception) {
            Log.w(TAG, "Error resolving external vault directories: ${e.message}")
        }
        // 4. Internal cache fallback backup
        files.add(File(context.filesDir, VAULT_FILE_NAME))
        return files
    }

    /**
     * Reads all accounts stored in the persistent external vault files.
     */
    fun loadAccountsFromVault(context: Context): List<UserAccountEntity> {
        for (file in getVaultFiles(context)) {
            try {
                if (file.exists() && file.length() > 0) {
                    val content = file.readText()
                    val accounts = parseAccountsJson(content)
                    if (accounts.isNotEmpty()) {
                        Log.d(TAG, "Restored ${accounts.size} account(s) from vault at: ${file.absolutePath}")
                        return accounts
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading vault file ${file.absolutePath}: ${e.message}")
            }
        }
        return emptyList()
    }

    /**
     * Saves or updates a user account in the persistent external vault files.
     */
    fun saveAccountToVault(context: Context, user: UserAccountEntity) {
        val existing = loadAccountsFromVault(context).toMutableList()
        val index = existing.indexOfFirst {
            it.username.equals(user.username, ignoreCase = true) ||
                    (it.email.isNotBlank() && it.email.equals(user.email, ignoreCase = true))
        }
        if (index >= 0) {
            existing[index] = user
        } else {
            existing.add(user)
        }

        val jsonString = serializeAccountsToJson(existing)
        for (file in getVaultFiles(context)) {
            try {
                file.parentFile?.mkdirs()
                file.writeText(jsonString)
                Log.d(TAG, "Backed up user '${user.username}' to persistent vault: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed writing to vault ${file.absolutePath}: ${e.message}")
            }
        }
    }

    private fun serializeAccountsToJson(accounts: List<UserAccountEntity>): String {
        val root = JSONObject()
        val array = JSONArray()
        for (u in accounts) {
            val obj = JSONObject().apply {
                put("username", u.username)
                put("email", u.email)
                put("passwordHash", u.passwordHash)
                put("currentLevelIndex", u.currentLevelIndex)
                put("completedLevelsCsv", u.completedLevelsCsv)
                put("tokens", u.tokens)
                put("echoBreakers", u.echoBreakers)
                put("coins", u.coins)
                put("diamonds", u.diamonds)
                put("totalEchoes", u.totalEchoes)
                put("totalStars", u.totalStars)
                put("isDarkTheme", u.isDarkTheme)
                put("languageCode", u.languageCode)
                put("trophies", u.trophies)
                put("totalPlayTimeSec", u.totalPlayTimeSec)
                put("maxCombo", u.maxCombo)
                put("levelStatsCsv", u.levelStatsCsv)
                put("avatarUri", u.avatarUri)
                put("customTitle", u.customTitle)
                put("createdAt", u.createdAt)
            }
            array.put(obj)
        }
        root.put("accounts", array)
        root.put("timestamp", System.currentTimeMillis())
        return root.toString()
    }

    private fun parseAccountsJson(json: String): List<UserAccountEntity> {
        val list = mutableListOf<UserAccountEntity>()
        try {
            val root = JSONObject(json)
            val array = root.optJSONArray("accounts") ?: return emptyList()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val user = UserAccountEntity(
                    username = obj.optString("username", ""),
                    email = obj.optString("email", ""),
                    passwordHash = obj.optString("passwordHash", ""),
                    currentLevelIndex = obj.optInt("currentLevelIndex", 0),
                    completedLevelsCsv = obj.optString("completedLevelsCsv", ""),
                    tokens = obj.optInt("tokens", 10),
                    echoBreakers = obj.optInt("echoBreakers", 3),
                    coins = obj.optInt("coins", 50),
                    diamonds = obj.optInt("diamonds", 1),
                    totalEchoes = obj.optInt("totalEchoes", 0),
                    totalStars = obj.optInt("totalStars", 0),
                    isDarkTheme = obj.optBoolean("isDarkTheme", true),
                    languageCode = obj.optString("languageCode", "tr"),
                    trophies = obj.optInt("trophies", 0),
                    totalPlayTimeSec = obj.optLong("totalPlayTimeSec", 0L),
                    maxCombo = obj.optInt("maxCombo", 0),
                    levelStatsCsv = obj.optString("levelStatsCsv", ""),
                    avatarUri = obj.optString("avatarUri", ""),
                    customTitle = obj.optString("customTitle", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                if (user.username.isNotBlank()) {
                    list.add(user)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing accounts JSON: ${e.message}")
        }
        return list
    }
}
