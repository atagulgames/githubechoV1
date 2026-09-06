package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val username: String,
    val email: String = "",
    val passwordHash: String = "",
    val currentLevelIndex: Int = 0,
    val completedLevelsCsv: String = "",
    val tokens: Int = 10,
    val echoBreakers: Int = 3,
    val coins: Int = 50,
    val diamonds: Int = 1,
    val totalEchoes: Int = 0,
    val totalStars: Int = 0,
    val isDarkTheme: Boolean = true,
    val languageCode: String = "tr",
    val lastDailyCompletedDate: String = "",
    val loginStreak: Int = 1,
    val lastLoginDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
