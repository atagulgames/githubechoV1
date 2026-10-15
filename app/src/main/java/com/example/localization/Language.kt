package com.example.localization

enum class Language(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String
) {
    EN("en", "English", "English", "🇬🇧"),
    TR("tr", "English", "English", "🇬🇧");

    val displayName: String get() = "English"
    val flag: String get() = "🇬🇧"

    companion object {
        fun fromCode(code: String): Language {
            return EN
        }
    }
}
