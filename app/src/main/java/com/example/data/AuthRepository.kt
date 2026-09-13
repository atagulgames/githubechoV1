package com.example.data

import android.content.Context
import com.example.data.local.EchoDatabase
import com.example.data.local.UserAccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = EchoDatabase.getDatabase(context)
    private val userDao = db.userAccountDao()
    private val preferences = EchoPreferences(context)

    suspend fun register(
        usernameInput: String,
        emailInput: String,
        passwordInput: String
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val username = usernameInput.trim()
        val email = emailInput.trim()

        if (username.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Kullanıcı adı en az 3 karakter olmalıdır."))
        }
        if (passwordInput.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Şifre en az 4 karakter olmalıdır."))
        }

        // Check if username is already taken in Room or in persistent vault
        val existingUserByName = userDao.getUserByUsername(username)
            ?: PersistentVaultManager.loadAccountsFromVault(appContext).firstOrNull { it.username.equals(username, ignoreCase = true) }
        if (existingUserByName != null) {
            return@withContext Result.failure(IllegalStateException("Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."))
        }

        val existingUserByEmailAsName = userDao.getUserByEmail(username)
            ?: PersistentVaultManager.loadAccountsFromVault(appContext).firstOrNull { it.email.isNotBlank() && it.email.equals(username, ignoreCase = true) }
        if (existingUserByEmailAsName != null) {
            return@withContext Result.failure(IllegalStateException("Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."))
        }

        // If email provided, check if email is already taken
        if (email.isNotBlank()) {
            val existingEmail = userDao.getUserByEmail(email)
                ?: userDao.getUserByUsername(email)
                ?: PersistentVaultManager.loadAccountsFromVault(appContext).firstOrNull { it.email.equals(email, ignoreCase = true) || it.username.equals(email, ignoreCase = true) }
            if (existingEmail != null) {
                return@withContext Result.failure(IllegalStateException("Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."))
            }
        }

        val passwordHash = hashPassword(passwordInput)
        val newUser = UserAccountEntity(
            username = username,
            email = email,
            passwordHash = passwordHash,
            currentLevelIndex = 0,
            completedLevelsCsv = "",
            tokens = 10,
            echoBreakers = 3,
            coins = 50,
            diamonds = 1,
            totalEchoes = 0,
            totalStars = 0,
            isDarkTheme = true,
            languageCode = preferences.languageCode,
            createdAt = System.currentTimeMillis()
        )

        userDao.insertOrUpdate(newUser)
        PersistentVaultManager.saveAccountToVault(appContext, newUser)

        // Set as active session
        preferences.setAuthenticatedUser(username, remember = true, passwordHash = passwordHash)
        loadUserProgressIntoPreferences(newUser)

        Result.success(newUser)
    }

    suspend fun registerOrUpdateExternalUser(
        username: String,
        email: String,
        fullName: String
    ): UserAccountEntity = withContext(Dispatchers.IO) {
        var user = userDao.getUserByEmail(email) ?: userDao.getUserByUsername(username)
        if (user == null) {
            user = UserAccountEntity(
                username = username,
                email = email,
                passwordHash = "",
                currentLevelIndex = preferences.currentLevelIndex,
                completedLevelsCsv = preferences.getCompletedLevels().joinToString(","),
                tokens = preferences.tokens,
                echoBreakers = preferences.echoBreakers,
                coins = preferences.coins,
                diamonds = preferences.diamonds,
                totalEchoes = preferences.totalEchoes,
                totalStars = preferences.getLevelRecords().sumOf { it.stars },
                isDarkTheme = true,
                languageCode = preferences.languageCode,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertOrUpdate(user)
            PersistentVaultManager.saveAccountToVault(appContext, user)
        }
        user
    }

    suspend fun login(
        usernameOrEmailInput: String,
        passwordInput: String,
        rememberMe: Boolean = true
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val query = usernameOrEmailInput.trim()
        if (query.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Lütfen kullanıcı adınızı veya e-postanızı girin."))
        }
        if (passwordInput.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Lütfen şifrenizi girin."))
        }

        var user = userDao.getUserByUsername(query) ?: userDao.getUserByEmail(query)
        // If not found in Room (e.g. app was uninstalled and reinstalled), check persistent vault
        if (user == null) {
            val vaultAccounts = PersistentVaultManager.loadAccountsFromVault(appContext)
            user = vaultAccounts.firstOrNull {
                it.username.equals(query, ignoreCase = true) || (it.email.isNotBlank() && it.email.equals(query, ignoreCase = true))
            }
            if (user != null) {
                // Restore into Room
                userDao.insertOrUpdate(user)
            }
        }

        if (user == null) {
            return@withContext Result.failure(IllegalStateException("Böyle bir hesap bulunamadı! Lütfen kayıt olun."))
        }

        val computedHash = hashPassword(passwordInput)
        if (user.passwordHash != computedHash) {
            return@withContext Result.failure(IllegalStateException("Hatalı şifre! Şifre aynı olmak zorundadır."))
        }

        // Update active session
        preferences.setAuthenticatedUser(user.username, remember = rememberMe, passwordHash = user.passwordHash)
        loadUserProgressIntoPreferences(user)

        Result.success(user)
    }

    suspend fun getUserAccount(query: String): UserAccountEntity? = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext null
        var user = userDao.getUserByUsername(q) ?: userDao.getUserByEmail(q)
        if (user == null) {
            user = PersistentVaultManager.loadAccountsFromVault(appContext).firstOrNull {
                it.username.equals(q, ignoreCase = true) || (it.email.isNotBlank() && it.email.equals(q, ignoreCase = true))
            }
            if (user != null) {
                userDao.insertOrUpdate(user)
            }
        }
        user
    }

    suspend fun saveActiveUserProgress(
        username: String = "",
        currentLevelIndex: Int,
        completedLevels: Set<Int>,
        tokens: Int,
        echoBreakers: Int = 0,
        breakers: Int = echoBreakers,
        coins: Int = preferences.coins,
        diamonds: Int = preferences.diamonds,
        totalStars: Int = 0,
        totalEchoes: Int = 0,
        trophies: Int = preferences.trophies,
        playTimeSec: Long = preferences.totalPlayTimeSec,
        maxCombo: Int = preferences.maxCombo,
        levelStatsCsv: String = preferences.levelStatsCsv
    ) = withContext(Dispatchers.IO) {
        val targetQuery = if (username.isNotBlank()) username.trim() else preferences.authenticatedUsername.trim()
        if (targetQuery.isNotBlank()) {
            val user = userDao.getUserByUsername(targetQuery) ?: userDao.getUserByEmail(targetQuery)
            val actualUsername = user?.username ?: targetQuery
            val csv = completedLevels.joinToString(",")
            val actualBreakers = if (echoBreakers != 0) echoBreakers else breakers
            userDao.updateProgress(
                username = actualUsername,
                levelIdx = currentLevelIndex,
                completedCsv = csv,
                tokens = tokens,
                breakers = actualBreakers,
                coins = coins,
                diamonds = diamonds,
                stars = totalStars,
                totalEchoes = totalEchoes,
                trophies = trophies,
                playTimeSec = playTimeSec,
                maxCombo = maxCombo,
                levelStatsCsv = levelStatsCsv
            )
            // Also backup updated state to persistent vault so uninstall/reinstall preserves latest progress
            val updatedAccount = userDao.getUserByUsername(actualUsername)
            if (updatedAccount != null) {
                PersistentVaultManager.saveAccountToVault(appContext, updatedAccount)
            }
        }
    }

    suspend fun getLeaderboardPlayers(): List<com.example.model.LeaderboardPlayer> = withContext(Dispatchers.IO) {
        val activeUsername = preferences.authenticatedUsername
        val dbUsers = userDao.getAllUsers()

        val playersList = ArrayList<com.example.model.LeaderboardPlayer>()

        // 1. Add active local user only if authenticated and has earned trophies (> 0)
        val myRecords = preferences.getLevelRecords()
        val myCompletedCount = preferences.getCompletedLevels().size
        if (activeUsername.isNotBlank() && preferences.trophies > 0) {
            val myPlayer = com.example.model.LeaderboardPlayer(
                id = "current_user",
                rank = 1,
                username = activeUsername,
                avatarEmoji = "⚡",
                avatarUri = preferences.userAvatarUri,
                title = preferences.userCustomTitle.ifBlank {
                    if (preferences.trophies >= 1000) "🏆 Yankı Ustası" else "Ses Kaşifi"
                },
                trophies = preferences.trophies,
                totalEchoes = preferences.totalEchoes,
                totalPlayTimeSec = preferences.totalPlayTimeSec,
                maxCombo = preferences.maxCombo,
                completedLevelsCount = myCompletedCount,
                isCurrentUser = true,
                levelRecords = myRecords
            )
            playersList.add(myPlayer)
        }

        // 2. Add other registered users from Room only if they have earned trophies (> 0)
        for (u in dbUsers) {
            if (u.username.equals(activeUsername, ignoreCase = true)) continue
            if (u.trophies <= 0) continue
            val completedCount = if (u.completedLevelsCsv.isNotBlank()) u.completedLevelsCsv.split(",").size else 0
            val uRecords = parseLevelStatsCsv(u.levelStatsCsv)
            playersList.add(
                com.example.model.LeaderboardPlayer(
                    id = "db_${u.username}",
                    rank = 1,
                    username = u.username,
                    avatarEmoji = "👤",
                    avatarUri = u.avatarUri,
                    title = u.customTitle.ifBlank { if (u.trophies > 1500) "Usta Çizgici" else "Kozmik Oyuncu" },
                    trophies = u.trophies,
                    totalEchoes = u.totalEchoes,
                    totalPlayTimeSec = u.totalPlayTimeSec,
                    maxCombo = u.maxCombo,
                    completedLevelsCount = completedCount,
                    isCurrentUser = false,
                    levelRecords = uRecords
                )
            )
        }

        // Sort descending by trophies, then ascending by totalEchoes
        playersList.sortWith(compareByDescending<com.example.model.LeaderboardPlayer> { it.trophies }
            .thenBy { it.totalEchoes })

        // Assign ranked indices 1, 2, 3...
        return@withContext playersList.mapIndexed { index, player ->
            player.copy(rank = index + 1)
        }
    }

    private fun parseLevelStatsCsv(raw: String): List<com.example.model.LevelRecord> {
        if (raw.isBlank()) return emptyList()
        val list = ArrayList<com.example.model.LevelRecord>()
        raw.split(";").forEach { item ->
            val parts = item.split(":")
            if (parts.size >= 4) {
                val lvlId = parts[0].toIntOrNull() ?: 1
                val echoes = parts[1].toIntOrNull() ?: 0
                val time = parts[2].toFloatOrNull() ?: 0f
                val stars = parts[3].toIntOrNull() ?: 3
                val tr = if (parts.size >= 5) parts[4].toIntOrNull() ?: 50 else 50
                list.add(
                    com.example.model.LevelRecord(
                        levelId = lvlId,
                        levelTitle = "Bölüm $lvlId",
                        echoesUsed = echoes,
                        timeTakenSec = time,
                        stars = stars,
                        trophiesEarned = tr
                    )
                )
            }
        }
        return list.sortedBy { it.levelId }
    }

    suspend fun saveActiveUserTheme(isDark: Boolean) = withContext(Dispatchers.IO) {
        val activeUsername = preferences.authenticatedUsername
        if (activeUsername.isNotBlank()) {
            val user = userDao.getUserByUsername(activeUsername) ?: userDao.getUserByEmail(activeUsername)
            val actualUsername = user?.username ?: activeUsername
            userDao.updateTheme(actualUsername, isDark)
        }
    }

    suspend fun saveActiveUserTheme(username: String, isDark: Boolean) = withContext(Dispatchers.IO) {
        val targetQuery = if (username.isNotBlank()) username.trim() else preferences.authenticatedUsername.trim()
        if (targetQuery.isNotBlank()) {
            val user = userDao.getUserByUsername(targetQuery) ?: userDao.getUserByEmail(targetQuery)
            val actualUsername = user?.username ?: targetQuery
            userDao.updateTheme(actualUsername, isDark)
        }
    }

    suspend fun saveActiveUserLanguage(langCode: String) = withContext(Dispatchers.IO) {
        val activeUsername = preferences.authenticatedUsername
        if (activeUsername.isNotBlank()) {
            userDao.updateLanguage(activeUsername, langCode)
        }
    }

    suspend fun getActiveUser(): UserAccountEntity? = withContext(Dispatchers.IO) {
        val username = preferences.authenticatedUsername
        if (username.isBlank()) return@withContext null
        userDao.getUserByUsername(username)
    }

    fun logout() {
        preferences.clearAuthentication()
        com.example.data.security.SecureTokenManager.clearToken(appContext)
    }

    private fun loadUserProgressIntoPreferences(user: UserAccountEntity) {
        preferences.currentLevelIndex = user.currentLevelIndex
        preferences.tokens = user.tokens
        preferences.echoBreakers = user.echoBreakers
        preferences.coins = user.coins
        preferences.diamonds = user.diamonds
        preferences.totalEchoes = user.totalEchoes
        preferences.trophies = user.trophies
        preferences.totalPlayTimeSec = user.totalPlayTimeSec
        preferences.maxCombo = user.maxCombo
        preferences.levelStatsCsv = user.levelStatsCsv
        preferences.isDarkTheme = user.isDarkTheme
        preferences.userAvatarUri = user.avatarUri
        preferences.userCustomTitle = user.customTitle
        if (user.languageCode.isNotBlank()) {
            preferences.languageCode = user.languageCode
        }

        val completedSet = if (user.completedLevelsCsv.isNotBlank()) {
            user.completedLevelsCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else emptySet()

        // Sync completed levels in preferences
        val stringSet = completedSet.map { it.toString() }.toSet()
        preferences.setCompletedLevelsRaw(stringSet)
    }

    suspend fun updateUserProfile(avatarUri: String, customTitle: String) = withContext(Dispatchers.IO) {
        preferences.userAvatarUri = avatarUri
        preferences.userCustomTitle = customTitle
        val activeUsername = preferences.authenticatedUsername
        if (activeUsername.isNotBlank()) {
            val user = userDao.getUserByUsername(activeUsername) ?: userDao.getUserByEmail(activeUsername)
            val actual = user?.username ?: activeUsername
            userDao.updateProfile(actual, avatarUri, customTitle)
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
