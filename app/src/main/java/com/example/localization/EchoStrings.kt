package com.example.localization

object EchoStrings {

    fun get(key: String, lang: Language = Language.EN): String = when (key.lowercase()) {
        "play" -> play(lang)
        "levels" -> levels(lang)
        "level" -> level(lang)
        "themes" -> themes(lang)
        "shop" -> shop(lang)
        "settings" -> settings(lang)
        "language" -> language(lang)
        "sound" -> sound(lang)
        "haptics" -> haptics(lang)
        "daily_challenge" -> dailyChallenge(lang)
        "hint" -> hint(lang)
        "breaker" -> breaker(lang)
        "clear" -> clearEchoes(lang)
        "theme" -> theme(lang)
        "dark_theme" -> darkTheme(lang)
        "white_theme" -> whiteTheme(lang)
        else -> key
    }

    fun level(lang: Language = Language.EN): String = "Level"
    fun play(lang: Language = Language.EN): String = "PLAY"
    fun levels(lang: Language = Language.EN): String = "Levels"
    fun themes(lang: Language = Language.EN): String = "Themes"
    fun shop(lang: Language = Language.EN): String = "Shop"
    fun settings(lang: Language = Language.EN): String = "Settings"
    fun language(lang: Language = Language.EN): String = "Language"
    fun dailyChallenge(lang: Language = Language.EN): String = "DAILY CHALLENGE"
    fun dailyCompleted(lang: Language = Language.EN): String = "Completed"
    fun levelTitle(lang: Language = Language.EN, number: Int): String = "Level $number"
    fun hint(lang: Language = Language.EN): String = "Hint"
    fun breaker(lang: Language = Language.EN): String = "Breaker"
    fun clearEchoes(lang: Language = Language.EN): String = "Clear"
    fun levelComplete(lang: Language = Language.EN): String = "LEVEL COMPLETED!"
    fun nextLevel(lang: Language = Language.EN): String = "Next Level"
    fun retry(lang: Language = Language.EN): String = "Retry"
    fun watchAd(lang: Language = Language.EN): String = "Watch Video"
    fun adBlockWarningTitle(lang: Language = Language.EN): String = "Ad Blocker Detected"
    fun adBlockWarningSubtitle(lang: Language = Language.EN): String = "Application cannot run"
    fun adBlockWarningBody(lang: Language = Language.EN): String = "ECHO is a completely free indie game supported by advertisements.\n\nTo continue playing, please disable your ad blocker (Private DNS, AdGuard, Blokada, etc.)."
    fun recheck(lang: Language = Language.EN): String = "Re-Check"
    fun exitGame(lang: Language = Language.EN): String = "Exit Game"
    fun sound(lang: Language = Language.EN): String = "Sound & Music"
    fun haptics(lang: Language = Language.EN): String = "Haptic Feedback"
    fun resetProgress(lang: Language = Language.EN): String = "Reset All Progress"
    fun theme(lang: Language = Language.EN): String = "Theme (Dark / White)"
    fun darkTheme(lang: Language = Language.EN): String = "Dark"
    fun whiteTheme(lang: Language = Language.EN): String = "White"

    // Fantastic Boosters
    fun arcaneBlast(lang: Language = Language.EN): String = "Arcane Blast"
    fun timeFreeze(lang: Language = Language.EN): String = "Time Freeze"
    fun numberShift(lang: Language = Language.EN): String = "Number Shift"
    fun luckyCrystal(lang: Language = Language.EN): String = "Lucky Crystal"

    // Account & Guest
    fun playAsGuest(lang: Language = Language.EN): String = "PLAY AS GUEST"
    fun signOut(lang: Language = Language.EN): String = "Sign Out"
    fun guestAccount(lang: Language = Language.EN): String = "Guest Player"
    fun loginRequiredFeature(lang: Language = Language.EN): String = "Please sign in or register to access Leaderboard and Quests."
    fun startNode(lang: Language = Language.EN): String = "START"
    fun close(lang: Language = Language.EN): String = "Close"
    fun back(lang: Language = Language.EN): String = "Back"
    fun magicCombo(lang: Language = Language.EN): String = "Magic Combo!"
    fun levelAlreadyCompleted(lang: Language = Language.EN): String = "Level already completed! Trophies are awarded only once, except on Level 150."
    fun bossPuzzle(lang: Language = Language.EN): String = "Fantastic Boss Puzzle"
}
