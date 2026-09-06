package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserAccountDao {

    @Query("SELECT * FROM user_accounts WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserAccountEntity)

    @Query("SELECT * FROM user_accounts")
    suspend fun getAllUsers(): List<UserAccountEntity>

    @Query("UPDATE user_accounts SET currentLevelIndex = :levelIdx, completedLevelsCsv = :completedCsv, tokens = :tokens, echoBreakers = :breakers, coins = :coins, diamonds = :diamonds, totalStars = :stars, totalEchoes = :totalEchoes WHERE LOWER(username) = LOWER(:username)")
    suspend fun updateProgress(
        username: String,
        levelIdx: Int,
        completedCsv: String,
        tokens: Int,
        breakers: Int,
        coins: Int,
        diamonds: Int,
        stars: Int,
        totalEchoes: Int
    )

    @Query("UPDATE user_accounts SET isDarkTheme = :isDark WHERE LOWER(username) = LOWER(:username)")
    suspend fun updateTheme(username: String, isDark: Boolean)

    @Query("UPDATE user_accounts SET languageCode = :lang WHERE LOWER(username) = LOWER(:username)")
    suspend fun updateLanguage(username: String, lang: String)
}
