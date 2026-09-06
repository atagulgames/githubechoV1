package com.example.data

import android.content.Context
import com.example.data.local.EchoDatabase
import com.example.data.local.UserAccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthRepository(context: Context) {

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

        // Check if username is already taken
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser != null) {
            return@withContext Result.failure(IllegalStateException("Bu kullanıcı adı zaten kayıtlı! Lütfen başka bir ad seçin veya giriş yapın."))
        }

        // If email provided, check if email is already taken
        if (email.isNotBlank()) {
            val existingEmail = userDao.getUserByEmail(email)
            if (existingEmail != null) {
                return@withContext Result.failure(IllegalStateException("Bu e-posta adresiyle zaten bir hesap var!"))
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
            totalEchoes = 0,
            totalStars = 0,
            isDarkTheme = true,
            languageCode = preferences.languageCode,
            createdAt = System.currentTimeMillis()
        )

        userDao.insertOrUpdate(newUser)

        // Set as active session
        preferences.setAuthenticatedUser(username, remember = true, passwordHash = passwordHash)
        loadUserProgressIntoPreferences(newUser)

        Result.success(newUser)
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

        val user = userDao.getUserByUsername(query) ?: userDao.getUserByEmail(query)
        if (user == null) {
            return@withContext Result.failure(IllegalStateException("Bu kullanıcı adı veya e-posta ile kayıtlı hesap bulunamadı! Lütfen kayıt olun."))
        }

        val computedHash = hashPassword(passwordInput)
        if (user.passwordHash != computedHash) {
            return@withContext Result.failure(IllegalStateException("Hatalı şifre girdiniz! Lütfen tekrar deneyin."))
        }

        // Update active session
        preferences.setAuthenticatedUser(user.username, remember = rememberMe, passwordHash = user.passwordHash)
        loadUserProgressIntoPreferences(user)

        Result.success(user)
    }

    suspend fun getUserAccount(username: String): UserAccountEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username)
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
        totalEchoes: Int = 0
    ) = withContext(Dispatchers.IO) {
        val targetUsername = if (username.isNotBlank()) username else preferences.authenticatedUsername
        if (targetUsername.isNotBlank()) {
            val csv = completedLevels.joinToString(",")
            val actualBreakers = if (echoBreakers != 0) echoBreakers else breakers
            userDao.updateProgress(
                username = targetUsername,
                levelIdx = currentLevelIndex,
                completedCsv = csv,
                tokens = tokens,
                breakers = actualBreakers,
                coins = coins,
                diamonds = diamonds,
                stars = totalStars,
                totalEchoes = totalEchoes
            )
        }
    }

    suspend fun saveActiveUserTheme(isDark: Boolean) = withContext(Dispatchers.IO) {
        val activeUsername = preferences.authenticatedUsername
        if (activeUsername.isNotBlank()) {
            userDao.updateTheme(activeUsername, isDark)
        }
    }

    suspend fun saveActiveUserTheme(username: String, isDark: Boolean) = withContext(Dispatchers.IO) {
        val targetUsername = if (username.isNotBlank()) username else preferences.authenticatedUsername
        if (targetUsername.isNotBlank()) {
            userDao.updateTheme(targetUsername, isDark)
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
    }

    private fun loadUserProgressIntoPreferences(user: UserAccountEntity) {
        preferences.currentLevelIndex = user.currentLevelIndex
        preferences.tokens = user.tokens
        preferences.echoBreakers = user.echoBreakers
        preferences.coins = user.coins
        preferences.diamonds = user.diamonds
        preferences.totalEchoes = user.totalEchoes
        preferences.isDarkTheme = user.isDarkTheme
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

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
