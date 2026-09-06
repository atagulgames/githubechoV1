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

        // Check if username is already taken (as username or email)
        val existingUserByName = userDao.getUserByUsername(username)
        if (existingUserByName != null) {
            return@withContext Result.failure(IllegalStateException("Zaten bu kullanıcı adı kullanılıyor!"))
        }

        val existingUserByEmailAsName = userDao.getUserByEmail(username)
        if (existingUserByEmailAsName != null) {
            return@withContext Result.failure(IllegalStateException("Zaten böyle bir hesap var veya zaten bu kullanıcı adı kullanılıyor!"))
        }

        // If email provided, check if email is already taken
        if (email.isNotBlank()) {
            val existingEmail = userDao.getUserByEmail(email) ?: userDao.getUserByUsername(email)
            if (existingEmail != null) {
                return@withContext Result.failure(IllegalStateException("Zaten böyle bir hesap var!"))
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
            return@withContext Result.failure(IllegalStateException("Böyle bir hesap bulunamadı! Lütfen kayıt olun."))
        }

        val computedHash = hashPassword(passwordInput)
        if (user.passwordHash != computedHash) {
            return@withContext Result.failure(IllegalStateException("Hatalı şifre! Şifreniz doğruysa kaldığınız yerden devam edebilirsiniz."))
        }

        // Update active session
        preferences.setAuthenticatedUser(user.username, remember = rememberMe, passwordHash = user.passwordHash)
        loadUserProgressIntoPreferences(user)

        Result.success(user)
    }

    suspend fun getUserAccount(query: String): UserAccountEntity? = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext null
        userDao.getUserByUsername(q) ?: userDao.getUserByEmail(q)
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
        }
    }

    suspend fun getLeaderboardPlayers(): List<com.example.model.LeaderboardPlayer> = withContext(Dispatchers.IO) {
        val activeUsername = preferences.authenticatedUsername.ifBlank { "Oyuncu" }
        val dbUsers = userDao.getAllUsers()

        val playersList = ArrayList<com.example.model.LeaderboardPlayer>()

        // 1. Add active local user
        val myRecords = preferences.getLevelRecords()
        val myCompletedCount = preferences.getCompletedLevels().size
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

        // 2. Add other registered users from Room
        for (u in dbUsers) {
            if (u.username.equals(activeUsername, ignoreCase = true)) continue
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

        // 3. Global rival champions to ensure rich global competition
        val globalRivals = listOf(
            GlobalRivalTemplate("👑 Can_Echo", "🇹🇷", "Küresel Büyükusta", 3420, 10, 18200L, 25, 99),
            GlobalRivalTemplate("⚡ AlexVortex", "🇺🇸", "Kusursuz Gölge", 3180, 8, 15400L, 22, 94),
            GlobalRivalTemplate("🚀 Lucas_M", "🇩🇪", "Yıldızlararası Rehber", 2940, 14, 13650L, 20, 89),
            GlobalRivalTemplate("🎯 Kenji_Lines", "🇯🇵", "Hızlı Hat Ustası", 2710, 12, 11980L, 19, 85),
            GlobalRivalTemplate("🧘 Oliver_99", "🇬🇧", "Zen Çizgici", 2520, 7, 10500L, 18, 79),
            GlobalRivalTemplate("✨ Minho_Star", "🇰🇷", "Fotonik Virtüöz", 2350, 19, 9400L, 16, 74),
            GlobalRivalTemplate("🎨 Camille_Art", "🇫🇷", "Geometri Kraliçesi", 2180, 22, 8600L, 15, 69),
            GlobalRivalTemplate("⚽ Gabriel_R", "🇧🇷", "Samba Refleks", 2010, 18, 7900L, 14, 64),
            GlobalRivalTemplate("🔥 Mehmet_Pro", "🇹🇷", "Anadolu Fatihi", 1880, 26, 7150L, 13, 59),
            GlobalRivalTemplate("🛡️ Mateo_Arg", "🇦🇷", "Çelik Muhafız", 1720, 31, 6450L, 12, 54),
            GlobalRivalTemplate("🌟 Sofia_Milano", "🇮🇹", "Zarif Çizgici", 1580, 24, 5800L, 11, 49),
            GlobalRivalTemplate("🏔️ Elena_Nordic", "🇳🇴", "Buz Kaşifi", 1430, 29, 5100L, 10, 44),
            GlobalRivalTemplate("🌀 Zeynep_Echo", "🇹🇷", "Boyut Bükücü", 1290, 33, 4450L, 9, 39),
            GlobalRivalTemplate("🦅 Liam_Canada", "🇨🇦", "Kutup Kartalı", 1120, 38, 3800L, 8, 34),
            GlobalRivalTemplate("💫 Arda_Yıldız", "🇹🇷", "Genç Yetenek", 950, 42, 3100L, 7, 28)
        )

        for (rival in globalRivals) {
            // Generate realistic level-by-level performance history for this global player
            val records = ArrayList<com.example.model.LevelRecord>()
            for (lvl in 1..rival.levelsCleared) {
                val lvlTitle = LevelCatalog.buildLevelData(lvl).title
                val echoes = if (lvl % 4 == 0) 1 else 0 // mostly 0-echo clears for top players
                val timeSec = 5.2f + ((lvl * 7) % 9) + ((lvl % 5) * 0.3f)
                val tr = 30 + 30 + (if (echoes == 0) 50 else 0) + 10
                records.add(
                    com.example.model.LevelRecord(
                        levelId = lvl,
                        levelTitle = lvlTitle,
                        echoesUsed = echoes,
                        timeTakenSec = timeSec,
                        stars = 3,
                        trophiesEarned = tr
                    )
                )
            }

            playersList.add(
                com.example.model.LeaderboardPlayer(
                    id = "global_${rival.name}",
                    rank = 1,
                    username = rival.name,
                    avatarEmoji = rival.avatar,
                    title = rival.title,
                    trophies = rival.trophies,
                    totalEchoes = rival.echoes,
                    totalPlayTimeSec = rival.playTimeSec,
                    maxCombo = rival.maxCombo,
                    completedLevelsCount = rival.levelsCleared,
                    isCurrentUser = false,
                    levelRecords = records
                )
            )
        }

        // Sort descending by trophies, then ascending by totalEchoes
        playersList.sortWith(compareByDescending<com.example.model.LeaderboardPlayer> { it.trophies }
            .thenBy { it.totalEchoes })

        // Assign ranked indices 1, 2, 3...
        playersList.mapIndexed { index, player ->
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

    private data class GlobalRivalTemplate(
        val name: String,
        val avatar: String,
        val title: String,
        val trophies: Int,
        val echoes: Int,
        val playTimeSec: Long,
        val maxCombo: Int,
        val levelsCleared: Int
    )

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
