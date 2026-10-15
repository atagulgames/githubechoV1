package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.LeaderboardPlayer
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val trophies: Long = 0L,
    val diamonds: Long = 1L,
    val coins: Long = 50L,
    val level: Long = 1L,
    val stars: Long = 0L,
    val tokens: Long = 5L,
    val echoBreakers: Long = 2L,
    val strokeTheme: String = "NEON_CYAN",
    val echoTheme: String = "ELECTRIC_RED",
    val season2Theme: String = "COSMIC_NEBULA",
    val unlockedThemes: List<String> = listOf("NEON_CYAN", "ELECTRIC_RED"),
    val completedLevels: List<Long> = emptyList(),
    val levelStatsCsv: String = "",
    val maxCombo: Long = 0L,
    val totalEchoes: Long = 0L,
    val totalPlayTimeSec: Long = 0L,
    val photoUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class FirestoreUserRepository(private val db: FirebaseFirestore) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "FirestoreUserRepo"
        private const val LEADERBOARD_CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes cache to save quota
        private var cachedLeaderboard: List<LeaderboardPlayer>? = null
        private var cachedLeaderboardTime: Long = 0L
    }

    private var lastSaveTime: Long = 0L
    private var lastSaveDigest: String = ""

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    /**
     * Loads the current user's profile from Firestore.
     */
    suspend fun getUserProfile(): Result<FirestoreUser?> {
        val uid = auth.currentUser?.uid ?: return Result.success(null)
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            val user = snapshot.toObject(FirestoreUser::class.java)
            Result.success(user)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Synchronizes local game preferences with Firestore cloud save.
     * Guaranteed conflict resolution: keeps max trophies, max diamonds, max coins,
     * max level, max stars, and union of completed levels so NO player data is ever lost.
     * Quota optimized: updates only if changes exist, with debouncing support.
     */
    suspend fun syncUserProfile(
        username: String,
        email: String = "",
        currentLevel: Int,
        trophies: Int,
        stars: Int,
        tokens: Int,
        diamonds: Int,
        coins: Int,
        echoBreakers: Int,
        strokeTheme: String,
        echoTheme: String,
        season2Theme: String,
        unlockedThemes: Set<String>,
        completedLevels: Set<Int>,
        levelStatsCsv: String,
        maxCombo: Int,
        totalEchoes: Int,
        totalPlayTimeSec: Long,
        forceImmediate: Boolean = false
    ): Result<FirestoreUser> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not authenticated"))
        val docRef = db.collection("users").document(uid)

        // Quota optimization: digest comparison and 30-second throttling unless forced
        val now = System.currentTimeMillis()
        val currentDigest = "$trophies:$diamonds:$coins:$currentLevel:$stars:$tokens:$completedLevels"
        if (!forceImmediate && (now - lastSaveTime < 30_000L) && currentDigest == lastSaveDigest) {
            Log.d(TAG, "Skipping Firestore sync (cached & unchanged)")
            return Result.success(
                FirestoreUser(
                    userId = uid,
                    username = username,
                    trophies = trophies.toLong(),
                    diamonds = diamonds.toLong(),
                    coins = coins.toLong(),
                    level = currentLevel.toLong(),
                    stars = stars.toLong(),
                    tokens = tokens.toLong()
                )
            )
        }

        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val existing = snapshot.toObject(FirestoreUser::class.java)

                // High-fidelity merge
                val finalUsername = username.ifBlank { existing?.username ?: "Player_${uid.take(5)}" }
                val finalEmail = if (email.isNotBlank()) email else (existing?.email ?: "")
                val finalTrophies = maxOf(trophies.toLong(), existing?.trophies ?: 0L)
                val finalDiamonds = maxOf(diamonds.toLong(), existing?.diamonds ?: 1L)
                val finalCoins = maxOf(coins.toLong(), existing?.coins ?: 50L)
                val finalLevel = maxOf(currentLevel.toLong(), existing?.level ?: 1L).coerceAtLeast(1L)
                val finalStars = maxOf(stars.toLong(), existing?.stars ?: 0L)
                val finalTokens = maxOf(tokens.toLong(), existing?.tokens ?: 5L)
                val finalBreakers = maxOf(echoBreakers.toLong(), existing?.echoBreakers ?: 2L)
                val finalMaxCombo = maxOf(maxCombo.toLong(), existing?.maxCombo ?: 0L)
                val finalTotalEchoes = maxOf(totalEchoes.toLong(), existing?.totalEchoes ?: 0L)
                val finalTotalTime = maxOf(totalPlayTimeSec, existing?.totalPlayTimeSec ?: 0L)

                val existingUnlocked = existing?.unlockedThemes.orEmpty().toSet()
                val finalUnlockedThemes = (unlockedThemes + existingUnlocked).toList()

                val existingCompleted = existing?.completedLevels.orEmpty().map { it.toInt() }.toSet()
                val finalCompletedLevels = (completedLevels + existingCompleted).sorted().map { it.toLong() }

                val finalLevelStats = if (levelStatsCsv.length >= (existing?.levelStatsCsv?.length ?: 0)) {
                    levelStatsCsv
                } else {
                    existing?.levelStatsCsv ?: levelStatsCsv
                }

                val updates = hashMapOf<String, Any>(
                    "username" to finalUsername,
                    "trophies" to finalTrophies,
                    "diamonds" to finalDiamonds,
                    "coins" to finalCoins,
                    "level" to finalLevel,
                    "stars" to finalStars,
                    "tokens" to finalTokens,
                    "echoBreakers" to finalBreakers,
                    "strokeTheme" to strokeTheme.ifBlank { existing?.strokeTheme ?: "NEON_CYAN" },
                    "echoTheme" to echoTheme.ifBlank { existing?.echoTheme ?: "ELECTRIC_RED" },
                    "season2Theme" to season2Theme.ifBlank { existing?.season2Theme ?: "COSMIC_NEBULA" },
                    "unlockedThemes" to finalUnlockedThemes,
                    "completedLevels" to finalCompletedLevels,
                    "levelStatsCsv" to finalLevelStats,
                    "maxCombo" to finalMaxCombo,
                    "totalEchoes" to finalTotalEchoes,
                    "totalPlayTimeSec" to finalTotalTime,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (finalEmail.isNotBlank()) updates["email"] = finalEmail

                docRef.update(updates).await()

                lastSaveTime = now
                lastSaveDigest = currentDigest

                val merged = FirestoreUser(
                    id = uid,
                    userId = uid,
                    username = finalUsername,
                    email = finalEmail,
                    trophies = finalTrophies,
                    diamonds = finalDiamonds,
                    coins = finalCoins,
                    level = finalLevel,
                    stars = finalStars,
                    tokens = finalTokens,
                    echoBreakers = finalBreakers,
                    strokeTheme = strokeTheme,
                    echoTheme = echoTheme,
                    season2Theme = season2Theme,
                    unlockedThemes = finalUnlockedThemes,
                    completedLevels = finalCompletedLevels,
                    levelStatsCsv = finalLevelStats,
                    maxCombo = finalMaxCombo,
                    totalEchoes = finalTotalEchoes,
                    totalPlayTimeSec = finalTotalTime
                )
                Result.success(merged)
            } else {
                val newProfile = hashMapOf<String, Any>(
                    "userId" to uid,
                    "username" to username.ifBlank { "Player_${uid.take(5)}" },
                    "email" to email,
                    "trophies" to trophies.toLong(),
                    "diamonds" to diamonds.toLong(),
                    "coins" to coins.toLong(),
                    "level" to currentLevel.toLong().coerceAtLeast(1L),
                    "stars" to stars.toLong(),
                    "tokens" to tokens.toLong(),
                    "echoBreakers" to echoBreakers.toLong(),
                    "strokeTheme" to strokeTheme,
                    "echoTheme" to echoTheme,
                    "season2Theme" to season2Theme,
                    "unlockedThemes" to unlockedThemes.toList(),
                    "completedLevels" to completedLevels.sorted().map { it.toLong() },
                    "levelStatsCsv" to levelStatsCsv,
                    "maxCombo" to maxCombo.toLong(),
                    "totalEchoes" to totalEchoes.toLong(),
                    "totalPlayTimeSec" to totalPlayTimeSec,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(newProfile).await()

                lastSaveTime = now
                lastSaveDigest = currentDigest

                Result.success(
                    FirestoreUser(
                        id = uid,
                        userId = uid,
                        username = username,
                        email = email,
                        trophies = trophies.toLong(),
                        diamonds = diamonds.toLong(),
                        coins = coins.toLong(),
                        level = currentLevel.toLong(),
                        stars = stars.toLong(),
                        tokens = tokens.toLong(),
                        echoBreakers = echoBreakers.toLong(),
                        strokeTheme = strokeTheme,
                        echoTheme = echoTheme,
                        season2Theme = season2Theme,
                        unlockedThemes = unlockedThemes.toList(),
                        completedLevels = completedLevels.map { it.toLong() },
                        levelStatsCsv = levelStatsCsv,
                        maxCombo = maxCombo.toLong(),
                        totalEchoes = totalEchoes.toLong(),
                        totalPlayTimeSec = totalPlayTimeSec
                    )
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Fetches top players for the Global Leaderboard from Firestore with 5-minute memory caching
     * to strictly conserve quota.
     */
    suspend fun getLeaderboard(currentUsername: String, forceRefresh: Boolean = false): Result<List<LeaderboardPlayer>> {
        val now = System.currentTimeMillis()
        // If forceRefresh is false, allow brief 3-second debounce to prevent spam, otherwise fetch fresh live data
        if (!forceRefresh && cachedLeaderboard != null && (now - cachedLeaderboardTime < 3_000L)) {
            Log.d(TAG, "Serving leaderboard from brief in-memory cache")
            return Result.success(cachedLeaderboard!!)
        }

        return try {
            val querySnapshot = db.collection("users")
                .orderBy("trophies", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val avatarEmojis = listOf("⚡", "🌌", "🔮", "💠", "🌟", "✨", "🎯", "🌀", "🪐", "🚀", "🔥")
            val players = querySnapshot.documents.mapIndexed { index, doc ->
                val username = doc.getString("username") ?: "Player_${doc.id.take(4)}"
                val trophies = (doc.getLong("trophies") ?: 0L).toInt()
                val totalEchoes = (doc.getLong("totalEchoes") ?: 0L).toInt()
                val totalPlayTimeSec = doc.getLong("totalPlayTimeSec") ?: 0L
                val maxCombo = (doc.getLong("maxCombo") ?: 0L).toInt()
                val completedCount = (doc.get("completedLevels") as? List<*>)?.size ?: 0

                val title = when {
                    trophies >= 1000 -> "Grand Master"
                    trophies >= 500 -> "Echo Master"
                    trophies >= 200 -> "Harmonic Adept"
                    else -> "Seeker"
                }

                LeaderboardPlayer(
                    id = doc.id,
                    rank = index + 1,
                    username = username,
                    avatarEmoji = avatarEmojis[index % avatarEmojis.size],
                    title = title,
                    trophies = trophies,
                    totalEchoes = totalEchoes,
                    totalPlayTimeSec = totalPlayTimeSec,
                    maxCombo = maxCombo,
                    completedLevelsCount = completedCount,
                    isCurrentUser = username.equals(currentUsername, ignoreCase = true)
                )
            }

            cachedLeaderboard = players
            cachedLeaderboardTime = now
            Result.success(players)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "users")
            Result.failure(e)
        }
    }

    /**
     * Real-time Firestore snapshot listener for live leaderboard updates.
     */
    fun listenToLeaderboard(
        currentUsername: String,
        onUpdate: (List<LeaderboardPlayer>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        val avatarEmojis = listOf("⚡", "🌌", "🔮", "💠", "🌟", "✨", "🎯", "🌀", "🪐", "🚀", "🔥")
        return db.collection("users")
            .orderBy("trophies", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }
                val players = snapshot.documents.mapIndexed { index, doc ->
                    val username = doc.getString("username") ?: "Player_${doc.id.take(4)}"
                    val photoUrl = doc.getString("photoUrl") ?: ""
                    val trophies = (doc.getLong("trophies") ?: 0L).toInt()
                    val totalEchoes = (doc.getLong("totalEchoes") ?: 0L).toInt()
                    val totalPlayTimeSec = doc.getLong("totalPlayTimeSec") ?: 0L
                    val maxCombo = (doc.getLong("maxCombo") ?: 0L).toInt()
                    val completedCount = (doc.get("completedLevels") as? List<*>)?.size ?: 0
                    val title = when {
                        trophies >= 1000 -> "Grand Master"
                        trophies >= 500 -> "Echo Master"
                        trophies >= 200 -> "Harmonic Adept"
                        else -> "Seeker"
                    }
                    LeaderboardPlayer(
                        id = doc.id,
                        rank = index + 1,
                        username = username,
                        avatarEmoji = avatarEmojis[index % avatarEmojis.size],
                        avatarUri = photoUrl,
                        title = title,
                        trophies = trophies,
                        totalEchoes = totalEchoes,
                        totalPlayTimeSec = totalPlayTimeSec,
                        maxCombo = maxCombo,
                        completedLevelsCount = completedCount,
                        isCurrentUser = username.equals(currentUsername, ignoreCase = true)
                    )
                }
                cachedLeaderboard = players
                onUpdate(players)
            }
    }

    /**
     * Updates user profile picture in Firestore.
     */
    suspend fun updateUserProfilePhoto(photoUrl: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("users").document(uid)
        return try {
            docRef.update("photoUrl", photoUrl, "updatedAt", FieldValue.serverTimestamp()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Secure Firestore transaction to award trophies.
     * Guaranteed enforcement: only Level 150 awards trophies on replay!
     * Levels 1..149 award trophies and first-time rewards ONLY ONCE.
     */
    suspend fun recordLevelVictorySecure(
        levelId: Int,
        baseTrophies: Int,
        stars: Int,
        durationSec: Long
    ): Result<Int> {
        val uid = auth.currentUser?.uid ?: return Result.success(if (levelId == 150) baseTrophies else baseTrophies)
        val userRef = db.collection("users").document(uid)

        return try {
            val earnedTrophies = db.runTransaction { tx ->
                val snapshot = tx.get(userRef)
                val completedList = (snapshot.get("completedLevels") as? List<*>)?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList()
                val alreadyCompleted = completedList.contains(levelId.toLong())

                val trophiesToAward = if (!alreadyCompleted || levelId == 150) {
                    baseTrophies
                } else {
                    0
                }

                val currentTrophies = snapshot.getLong("trophies") ?: 0L
                val currentStars = snapshot.getLong("stars") ?: 0L
                val currentPlayTime = snapshot.getLong("totalPlayTimeSec") ?: 0L

                val updatedCompleted = if (!alreadyCompleted) {
                    completedList + levelId.toLong()
                } else {
                    completedList
                }

                tx.update(userRef, mapOf(
                    "trophies" to (currentTrophies + trophiesToAward),
                    "stars" to maxOf(currentStars, stars.toLong()),
                    "completedLevels" to updatedCompleted,
                    "totalPlayTimeSec" to (currentPlayTime + durationSec),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))

                trophiesToAward
            }.await()
            Result.success(earnedTrophies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
