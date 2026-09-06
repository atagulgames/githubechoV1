package com.example.localization

enum class Language(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String
) {
    EN("en", "English", "English", "🇬🇧"),
    TR("tr", "Türkçe", "Turkish", "🇹🇷");

    val displayName: String get() = nativeName
    val flag: String get() = flagEmoji

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
        }
    }
}

