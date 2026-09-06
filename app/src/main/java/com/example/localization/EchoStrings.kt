package com.example.localization

object EchoStrings {

    fun get(key: String, lang: Language): String = when (key.lowercase()) {
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

    fun level(lang: Language): String = when (lang) {
        Language.TR -> "Bölüm"
        Language.EN -> "Level"
    }

    fun play(lang: Language): String = when (lang) {
        Language.TR -> "OYNA"
        Language.EN -> "PLAY"
    }

    fun levels(lang: Language): String = when (lang) {
        Language.TR -> "Bölümler"
        Language.EN -> "Levels"
    }

    fun themes(lang: Language): String = when (lang) {
        Language.TR -> "Temalar"
        Language.EN -> "Themes"
    }

    fun shop(lang: Language): String = when (lang) {
        Language.TR -> "Mağaza"
        Language.EN -> "Shop"
    }

    fun settings(lang: Language): String = when (lang) {
        Language.TR -> "Ayarlar"
        Language.EN -> "Settings"
    }

    fun language(lang: Language): String = when (lang) {
        Language.TR -> "Dil"
        Language.EN -> "Language"
    }

    fun dailyChallenge(lang: Language): String = when (lang) {
        Language.TR -> "GÜNLÜK GÖREV"
        Language.EN -> "DAILY CHALLENGE"
    }

    fun dailyCompleted(lang: Language): String = when (lang) {
        Language.TR -> "Tamamlandı"
        Language.EN -> "Completed"
    }

    fun levelTitle(lang: Language, number: Int): String = when (lang) {
        Language.TR -> "Bölüm $number"
        Language.EN -> "Level $number"
    }

    fun hint(lang: Language): String = when (lang) {
        Language.TR -> "İpucu"
        Language.EN -> "Hint"
    }

    fun breaker(lang: Language): String = when (lang) {
        Language.TR -> "Kırıcı"
        Language.EN -> "Breaker"
    }

    fun clearEchoes(lang: Language): String = when (lang) {
        Language.TR -> "Temizle"
        Language.EN -> "Clear"
    }

    fun levelComplete(lang: Language): String = when (lang) {
        Language.TR -> "BÖLÜM TAMAMLANDI!"
        Language.EN -> "LEVEL COMPLETED!"
    }

    fun nextLevel(lang: Language): String = when (lang) {
        Language.TR -> "Sonraki Bölüm"
        Language.EN -> "Next Level"
    }

    fun retry(lang: Language): String = when (lang) {
        Language.TR -> "Tekrar Dene"
        Language.EN -> "Retry"
    }

    fun watchAd(lang: Language): String = when (lang) {
        Language.TR -> "Reklam İzle"
        Language.EN -> "Watch Video"
    }

    fun adBlockWarningTitle(lang: Language): String = when (lang) {
        Language.TR -> "Reklam Engelleyici Tespit Edildi"
        Language.EN -> "Ad Blocker Detected"
    }

    fun adBlockWarningSubtitle(lang: Language): String = when (lang) {
        Language.TR -> "Uygulama çalıştırılamıyor"
        Language.EN -> "Application cannot run"
    }

    fun adBlockWarningBody(lang: Language): String = when (lang) {
        Language.TR -> "ECHO tamamen ücretsiz bir oyundur ve sunucu/geliştirme masrafları reklam gelirleri sayesinde karşılanmaktadır.\n\nOyuna devam edebilmek için lütfen cihazınızdaki reklam engelleyiciyi (Özel DNS, AdGuard, Blokada vb.) kapatın."
        Language.EN -> "ECHO is a completely free indie game supported by advertisements.\n\nTo continue playing, please disable your ad blocker (Private DNS, AdGuard, Blokada, etc.)."
    }

    fun recheck(lang: Language): String = when (lang) {
        Language.TR -> "Tekrar Kontrol Et"
        Language.EN -> "Re-Check"
    }

    fun exitGame(lang: Language): String = when (lang) {
        Language.TR -> "Oyundan Çık"
        Language.EN -> "Exit Game"
    }

    fun sound(lang: Language): String = when (lang) {
        Language.TR -> "Ses Efektleri & Müzik"
        Language.EN -> "Sound & Music"
    }

    fun haptics(lang: Language): String = when (lang) {
        Language.TR -> "Titreşim (Haptik)"
        Language.EN -> "Haptic Feedback"
    }

    fun resetProgress(lang: Language): String = when (lang) {
        Language.TR -> "Tüm İlerlemeyi Sıfırla"
        Language.EN -> "Reset All Progress"
    }

    fun theme(lang: Language): String = when (lang) {
        Language.TR -> "Tema (Karanlık / Aydınlık)"
        Language.EN -> "Theme (Dark / White)"
    }

    fun darkTheme(lang: Language): String = when (lang) {
        Language.TR -> "Karanlık"
        Language.EN -> "Dark"
    }

    fun whiteTheme(lang: Language): String = when (lang) {
        Language.TR -> "Aydınlık"
        Language.EN -> "White"
    }
}
