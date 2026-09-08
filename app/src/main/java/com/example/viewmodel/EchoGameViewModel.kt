package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdBlockDetector
import com.example.ads.StartIoManager
import com.example.audio.HarmonicAudioEngine
import com.example.data.EchoPreferences
import com.example.data.EchoRepository
import com.example.data.LevelCatalog
import com.example.data.local.LevelEntity
import com.example.game.CollisionEngine
import com.example.localization.Language
import com.example.model.ChestReward
import com.example.model.DailyLoginDay
import com.example.model.DailyQuest
import com.example.model.DirectedEdge
import com.example.model.EchoStroke
import com.example.model.EchoTheme
import com.example.model.GameStatus
import com.example.model.LevelData
import com.example.model.LevelNode
import com.example.model.Node
import com.example.model.NodeType
import kotlin.math.roundToInt
import com.example.model.Point
import com.example.model.ScreenState
import com.example.model.Segment
import com.example.model.StrokeTheme
import com.example.model.EchoBeastState
import com.example.ui.dialogs.MechanicCatalog
import com.example.ui.dialogs.LevelMechanicInfo
import com.example.model.ThemeRarity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class RewardClaimedInfo(
    val title: String,
    val subtitle: String,
    val rewardType: String,
    val tokensAdded: Int = 0,
    val breakersAdded: Int = 0,
    val diamondsAdded: Int = 0,
    val coinsAdded: Int = 0
)

data class EchoUiState(
    val screenState: ScreenState = ScreenState.INTRO,
    val currentLevelIndex: Int = 0,
    val level: LevelData = LevelCatalog.entityToLevelData(LevelCatalog.create100Levels()[0]),
    val allLevels: List<LevelEntity> = emptyList(),
    val language: Language = Language.EN,
    val nodes: List<Node> = emptyList(),
    val visitedNodeIds: List<Int> = emptyList(),
    val collectedKeyIds: Set<Int> = emptySet(),
    val currentStrokeSegments: List<Segment> = emptyList(),
    val currentPointerPos: Point? = null,
    val isDrawing: Boolean = false,
    val echoes: List<EchoStroke> = emptyList(),
    val echoCountForLevel: Int = 0,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val tokens: Int = 5,
    val echoBreakers: Int = 2,
    val diamonds: Int = 1,
    val coins: Int = 50,
    val isEchoShrinkerActive: Boolean = false,
    val totalEchoes: Int = 0,
    val completedLevels: Set<Int> = emptySet(),
    val totalStars: Int = 0,
    val isCollisionAlertActive: Boolean = false,
    val isProximityAlertActive: Boolean = false,
    val isHintActive: Boolean = false,
    val isRewardedSimulating: Boolean = false,
    val rewardSimulatingType: String = "CLEAR_ECHOES",
    val rewardCountdownSeconds: Int = 3,
    val isShopDialogVisible: Boolean = false,
    val isLevelSelectVisible: Boolean = false,
    val isSettingsDialogVisible: Boolean = false,
    val isSkinsDialogVisible: Boolean = false,
    val isDailyChallenge: Boolean = false,
    val isDailyCompletedToday: Boolean = false,
    val strokeTheme: StrokeTheme = StrokeTheme.NEON_CYAN,
    val echoTheme: EchoTheme = EchoTheme.ELECTRIC_RED,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val testAdsEnabled: Boolean = false,
    val isAdLoading: Boolean = false,
    val isAdBlockerDetected: Boolean = false,
    val isCheckingAdBlocker: Boolean = false,
    val adBlockerTitle: String = "",
    val adBlockerSubtitle: String = "",
    val adBlockerDetails: String = "",
    val pendingLevelToStart: Int? = null,
    val pendingIsDailyChallenge: Boolean = false,
    val rewardClaimedData: RewardClaimedInfo? = null,
    val toastMessage: String? = null,
    // Game Systems
    val dailyQuests: List<DailyQuest> = emptyList(),
    val unclaimedQuestsCount: Int = 0,
    val loginStreak: Int = 1,
    val isLoginRewardAvailableToday: Boolean = true,
    val loginDays: List<DailyLoginDay> = emptyList(),
    val isFreeChestAvailable: Boolean = true,
    val adChestsRemainingToday: Int = 2,
    val unlockedThemes: Set<String> = emptySet(),
    val isDailyQuestsDialogVisible: Boolean = false,
    val isDailyLoginDialogVisible: Boolean = false,
    val isChestDialogVisible: Boolean = false,
    val lastOpenedChestReward: ChestReward? = null,
    val isDoubleRewardClaimedThisLevel: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authenticatedUser: String = "",
    val rememberMe: Boolean = false,
    val isDarkTheme: Boolean = true,
    // Leaderboard & Trophy System
    val trophies: Int = 0,
    val totalPlayTimeSec: Long = 0L,
    val currentCombo: Int = 0,
    val maxCombo: Int = 0,
    val levelStartTimeMs: Long = 0L,
    val isLeaderboardDialogVisible: Boolean = false,
    val isLeaderboardRefreshing: Boolean = false,
    val isProfileDialogVisible: Boolean = false,
    val selectedPlayerProfile: com.example.model.LeaderboardPlayer? = null,
    val leaderboardPlayers: List<com.example.model.LeaderboardPlayer> = emptyList(),
    val lastTrophyRewardBreakdown: com.example.model.TrophyRewardBreakdown? = null,
    // Multi-ad, Timed Boosters & Candy Crush Effects
    val multiAdWatchCount: Int = 0,
    val doubleTrophiesExpiresAt: Long = 0L,
    val infiniteBreakersExpiresAt: Long = 0L,
    val radiusShrinkerExpiresAt: Long = 0L,
    val candyParticles: List<com.example.model.CandyParticle> = emptyList(),
    val candyCallout: com.example.model.CandyCallout? = null,
    val isSupportDialogVisible: Boolean = false,
    val shrinkerAdsWatched: Int = 0,
    val userAvatarUri: String = "",
    val userCustomTitle: String = "",
    val hintAdsWatched: Int = 0,
    val isHintPurchaseDialogVisible: Boolean = false,
    val isKvkkConsentAccepted: Boolean = false,
    // 3-Hour Reward Cooldown System (3 Hours)
    val rewardCooldownSeconds: Long = 0L, // Main reward (REWARD_DAILY)
    val diamondRewardCooldownSeconds: Long = 0L,
    val coinRewardCooldownSeconds: Long = 0L,
    val breakerRewardCooldownSeconds: Long = 0L,
    val megaChestRewardCooldownSeconds: Long = 0L,
    // 5s Preview & 60s Gameplay Timer System
    val isPreviewActive: Boolean = false,
    val previewCountdownSeconds: Int = 5,
    val showDurationStartedBanner: Boolean = false,
    val levelRemainingTimeSec: Int = 60,
    val isGameOverTimeUpDialogVisible: Boolean = false,
    // Ghost Trails of Past Errors (Geçmişin Hayalet Labirenti)
    val pastGhostStrokes: List<List<com.example.model.Point>> = emptyList(),
    // Perfect Echo & Race Against Your Ghost (Kendi Hayaletinle Yarış)
    val isGhostRaceAvailable: Boolean = false,
    val isGhostRaceActive: Boolean = true,
    val ghostReplayPoints: List<com.example.model.Point> = emptyList(),
    val ghostReplayDurationMs: Long = 0L,
    val ghostRunnerCurrentPoint: com.example.model.Point? = null,
    val ghostRacePlayerWon: Boolean = false,
    // Behavioral Intelligence (Oyuncu Davranış Analizi)
    val playerBehaviorInsight: String = "Analiz: Kusursuz Yolculuk",
    val adaptiveTendencyQuadrant: Int? = null,
    // Echo Beast Corruption System (Yankı Canavarı)
    val echoBeastState: com.example.model.EchoBeastState = com.example.model.EchoBeastState.CALM,
    // 10 Evolution Tiers Tutorial Dialog System (Her kademenin ilk bölümü tutorial)
    val isLevelMechanicIntroVisible: Boolean = false,
    val activeMechanicInfo: com.example.ui.dialogs.LevelMechanicInfo? = null,
    val activeLevelRule: com.example.model.LevelRule? = null,
    val isRuleEncyclopediaOpen: Boolean = false
)

class EchoGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = EchoPreferences(application)
    private val repo = EchoRepository(application)
    private val authRepo = com.example.data.AuthRepository(application)
    private val lootLocker = com.example.data.LootLockerManager.getInstance(application)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _uiState = MutableStateFlow(EchoUiState())
    val uiState: StateFlow<EchoUiState> = _uiState.asStateFlow()

    private var alertResetJob: Job? = null
    private var rewardJob: Job? = null
    private var toastResetJob: Job? = null
    private var ghostFadeJob: Job? = null
    private var previewJob: Job? = null
    private var levelTimerJob: Job? = null
    private var ghostRunnerJob: Job? = null

    private val nodeHitRadius: Float = 36f
    private val deadlockThreshold: Int = 8
    private var candyCalloutDismissJob: kotlinx.coroutines.Job? = null

    init {
        HarmonicAudioEngine.init(application)
        HarmonicAudioEngine.isSoundEnabled = prefs.soundEnabled

        viewModelScope.launch {
            repo.ensureLevelsPopulated()
            if (!prefs.hasFreshStartV2) {
                repo.resetAllProgress()
                prefs.resetAllProgress()
                prefs.hasFreshStartV2 = true
                loadSavedPreferences()
            }
        }

        viewModelScope.launch {
            repo.getAllLevels().collect { list ->
                _uiState.update { state ->
                    val completed = list.filter { it.isCompleted }.map { it.id - 1 }.toSet()
                    val stars = list.sumOf { it.stars }
                    state.copy(
                        allLevels = list,
                        completedLevels = completed,
                        totalStars = stars
                    )
                }
            }
        }

        loadSavedPreferences()
        checkInternetAndAdHealth()
        registerNetworkCallback()

        // Initialize LootLocker session and sync player score
        lootLocker.loginGuest(
            onSuccess = { _, _ ->
                if (prefs.trophies > 0) {
                    val user = prefs.authenticatedUsername.ifBlank { "Oyuncu" }
                    lootLocker.submitScore(user, prefs.trophies)
                }
            }
        )

        // 3-Hour Reward Cooldown Ticker
        startRewardCooldownTicker()
    }

    private fun startRewardCooldownTicker() {
        viewModelScope.launch {
            while (isActive) {
                updateRewardCooldowns()
                delay(1000L)
            }
        }
    }

    fun updateRewardCooldowns() {
        val rewardCd = prefs.getRewardCooldownRemainingSeconds("REWARD_DAILY")
        val diamondCd = prefs.getRewardCooldownRemainingSeconds("FREE_DIAMOND")
        val coinCd = prefs.getRewardCooldownRemainingSeconds("FREE_COINS")
        val breakerCd = prefs.getRewardCooldownRemainingSeconds("FREE_BREAKER")
        val megaChestCd = prefs.getRewardCooldownRemainingSeconds("WATCH_3_ADS_REWARD")

        _uiState.update {
            it.copy(
                rewardCooldownSeconds = rewardCd,
                diamondRewardCooldownSeconds = diamondCd,
                coinRewardCooldownSeconds = coinCd,
                breakerRewardCooldownSeconds = breakerCd,
                megaChestRewardCooldownSeconds = megaChestCd
            )
        }
    }

    fun isTimedRewardType(type: String): Boolean {
        return when (type) {
            "REWARD_DAILY", "FREE_BREAKER", "FREE_DIAMOND", "FREE_COINS", "WATCH_3_ADS_REWARD" -> true
            else -> false
        }
    }

    fun canClaimReward(type: String): Boolean {
        if (isTimedRewardType(type)) {
            val cd = prefs.getRewardCooldownRemainingSeconds(type)
            return cd <= 0L
        }
        return true
    }

    fun notifyRewardOnCooldown(type: String) {
        val cd = prefs.getRewardCooldownRemainingSeconds(type)
        val h = cd / 3600
        val m = (cd % 3600) / 60
        val s = cd % 60
        val timeFormatted = if (h > 0) {
            String.format(Locale.getDefault(), "%d saat %d dakika", h, m)
        } else if (m > 0) {
            String.format(Locale.getDefault(), "%d dakika %d saniye", m, s)
        } else {
            String.format(Locale.getDefault(), "%d saniye", s)
        }
        showToast("Ödülünüz 3 saatte bir yenilenir! Kalan süre: $timeFormatted")
    }

    private fun loadSavedPreferences() {
        val stroke = try { StrokeTheme.valueOf(prefs.strokeThemeName) } catch (_: Exception) { StrokeTheme.NEON_CYAN }
        val echo = try { EchoTheme.valueOf(prefs.echoThemeName) } catch (_: Exception) { EchoTheme.ELECTRIC_RED }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isDailyDone = prefs.lastDailyCompletedDate == todayStr

        _uiState.update {
            it.copy(
                currentLevelIndex = prefs.currentLevelIndex.coerceIn(0, LevelCatalog.TOTAL_LEVELS - 1),
                language = Language.fromCode(prefs.languageCode),
                tokens = prefs.tokens,
                echoBreakers = prefs.echoBreakers,
                diamonds = prefs.diamonds,
                coins = prefs.coins,
                totalEchoes = prefs.totalEchoes,
                strokeTheme = stroke,
                echoTheme = echo,
                soundEnabled = prefs.soundEnabled,
                hapticsEnabled = prefs.hapticsEnabled,
                testAdsEnabled = prefs.isTestAdsEnabled,
                isDarkTheme = prefs.isDarkTheme,
                isDailyCompletedToday = isDailyDone,
                authenticatedUser = prefs.authenticatedUsername,
                rememberMe = prefs.rememberMe,
                trophies = prefs.trophies,
                totalPlayTimeSec = prefs.totalPlayTimeSec,
                maxCombo = prefs.maxCombo,
                currentCombo = prefs.currentCombo,
                multiAdWatchCount = prefs.multiAdWatchCount,
                doubleTrophiesExpiresAt = prefs.doubleTrophiesExpiresAt,
                infiniteBreakersExpiresAt = prefs.infiniteBreakersExpiresAt,
                radiusShrinkerExpiresAt = prefs.radiusShrinkerExpiresAt,
                shrinkerAdsWatched = prefs.shrinkerAdsWatched,
                hintAdsWatched = prefs.hintAdsWatched,
                isKvkkConsentAccepted = prefs.isKvkkConsentAccepted,
                userAvatarUri = prefs.userAvatarUri,
                userCustomTitle = prefs.userCustomTitle,
                isAuthenticated = false // Opening flow requires explicit login authentication verification
            )
        }

        refreshDailySystems()
    }

    fun setLanguage(lang: Language) {
        prefs.languageCode = lang.code
        _uiState.update { it.copy(language = lang) }
    }

    fun startPlayingLevel(levelIndex: Int? = null) {
        if (!_uiState.value.isAuthenticated) {
            _uiState.update { it.copy(screenState = ScreenState.LOGIN) }
            return
        }
        if (!AdBlockDetector.hasNetworkCapability(getApplication())) {
            _uiState.update {
                it.copy(
                    isAdBlockerDetected = true,
                    adBlockerTitle = "İnternet Bağlantısı Yok",
                    adBlockerSubtitle = "Reklamlar çalıştırılamıyor",
                    adBlockerDetails = "ECHO tamamen ücretsiz bir oyundur ve reklam servisleri ile desteklenmektedir. Oyuna devam etmek için lütfen internet bağlantınızı (Wi-Fi veya Mobil Veri) açın."
                )
            }
            return
        }
        val targetIdx = (levelIndex ?: _uiState.value.currentLevelIndex).coerceIn(0, LevelCatalog.TOTAL_LEVELS - 1)
        viewModelScope.launch {
            loadAndStartLevel(targetIdx)
        }
    }

    private suspend fun loadAndStartLevel(targetIdx: Int) {
        val levelData = repo.getLevelData(targetIdx + 1) ?: LevelCatalog.entityToLevelData(LevelCatalog.create100Levels()[0])
        val rawGhosts = prefs.getGhostStrokes(levelData.levelId)
        val ghostStrokes = rawGhosts.map { pts -> pts.map { Point(it.first, it.second) } }
        val perfRun = prefs.getPerfectRun(levelData.levelId)
        val hasPerf = perfRun != null
        val perfPts = perfRun?.first?.map { Point(it.first, it.second) } ?: emptyList()
        val perfDur = perfRun?.second ?: 0L
        val isTurkish = prefs.languageCode == "tr"
        val activeRule = com.example.data.LevelRuleCatalog.getRuleForLevel(levelData.levelId, isTurkish)
        val isTierIntroLevel = com.example.data.LevelRuleCatalog.isMilestone(levelData.levelId)
        val shouldShowIntro = prefs.isAutoShowLevelRulesEnabled && (
            (isTierIntroLevel && !prefs.isMechanicIntroShown(activeRule.tier)) ||
            !prefs.isLevelRuleShown(levelData.levelId)
        )
        val introInfo = if (shouldShowIntro) activeRule.toMechanicInfo() else null
        val insight = prefs.getBehaviorInsight()
        val tendency = prefs.getAdaptiveTendencyQuadrant()

        // In cumulative ghosts tier (91-99), ensure living ghosts form a rich maze
        val finalGhosts = if (levelData.mechanicType == "CUMULATIVE_GHOSTS" && ghostStrokes.isEmpty()) {
            val synthetic = ArrayList<List<Point>>()
            if (levelData.nodes.size >= 4) {
                for (i in 0 until levelData.nodes.size - 2 step 2) {
                    val n1 = levelData.nodes[i]
                    val n2 = levelData.nodes[i + 2]
                    synthetic.add(listOf(Point(n1.x, n1.y), Point((n1.x + n2.x) / 2f + 14f, (n1.y + n2.y) / 2f - 14f), Point(n2.x, n2.y)))
                }
            }
            synthetic
        } else {
            ghostStrokes
        }

        _uiState.update {
            it.copy(
                screenState = ScreenState.PLAYING_LEVEL,
                currentLevelIndex = targetIdx,
                level = levelData,
                nodes = levelData.nodes.map { ln ->
                    Node(
                        id = ln.id,
                        x = ln.x,
                        y = ln.y,
                        connected = false,
                        type = ln.type,
                        keyForGateId = ln.keyForGateId
                    )
                },
                visitedNodeIds = emptyList(),
                collectedKeyIds = emptySet(),
                currentStrokeSegments = emptyList(),
                currentPointerPos = null,
                isDrawing = false,
                echoes = emptyList(),
                echoCountForLevel = 0,
                isEchoShrinkerActive = false,
                gameStatus = GameStatus.PLAYING,
                isHintActive = false,
                isDailyChallenge = false,
                candyParticles = emptyList(),
                candyCallout = null,
                levelStartTimeMs = System.currentTimeMillis(),
                isDoubleRewardClaimedThisLevel = false,
                pastGhostStrokes = finalGhosts,
                isGhostRaceAvailable = hasPerf,
                ghostReplayPoints = perfPts,
                ghostReplayDurationMs = perfDur,
                ghostRunnerCurrentPoint = null,
                ghostRacePlayerWon = false,
                playerBehaviorInsight = insight,
                adaptiveTendencyQuadrant = tendency,
                echoBeastState = EchoBeastState.CALM,
                isLevelMechanicIntroVisible = shouldShowIntro,
                activeMechanicInfo = introInfo,
                activeLevelRule = if (shouldShowIntro) activeRule else null,
                isRuleEncyclopediaOpen = false
            )
        }
        startLevelSequence(levelData.levelId, isRestart = false)
    }

    fun startLevelSequence(levelId: Int, isRestart: Boolean = false) {
        previewJob?.cancel()
        levelTimerJob?.cancel()

        val savedRemaining = if (isRestart) {
            prefs.clearLevelTimer(levelId)
            60
        } else {
            prefs.getLevelRemainingSeconds(levelId)
        }

        if (savedRemaining <= 0) {
            _uiState.update {
                it.copy(
                    isPreviewActive = false,
                    levelRemainingTimeSec = 0,
                    isGameOverTimeUpDialogVisible = true,
                    gameStatus = GameStatus.DEADLOCK
                )
            }
            HarmonicAudioEngine.playTimeUpHata()
            return
        }

        _uiState.update {
            it.copy(
                isPreviewActive = true,
                previewCountdownSeconds = 5,
                showDurationStartedBanner = false,
                levelRemainingTimeSec = savedRemaining,
                isGameOverTimeUpDialogVisible = false
            )
        }

        // 1. Preview begins: Stop other SFX and synchronize tick at each second (5, 4, 3, 2, 1)
        HarmonicAudioEngine.stopCurrentSfx()

        previewJob = viewModelScope.launch {
            for (sec in 5 downTo 1) {
                _uiState.update { it.copy(previewCountdownSeconds = sec) }
                // User requirement: "5second da dıt dıt diye sesler geliyorya ya onlar tam süreye denk gelecek mesela 5 sn ilk dıt 4 sn 2inci dır diye devam etsin"
                HarmonicAudioEngine.playCountdownTick(sec)
                delay(1000L)
            }
            _uiState.update {
                it.copy(
                    previewCountdownSeconds = 0,
                    isPreviewActive = false,
                    showDurationStartedBanner = true
                )
            }

            // 2. Countdown reaches 0: party.mp3 plays cleanly and "Süre Başladı" banner pops up
            HarmonicAudioEngine.stopCurrentSfx()
            HarmonicAudioEngine.playParty()

            val deadlineMs = System.currentTimeMillis() + savedRemaining * 1000L
            prefs.setLevelTimerDeadlineMs(levelId, deadlineMs)

            launch {
                delay(1800L)
                _uiState.update { it.copy(showDurationStartedBanner = false) }
            }

            // 3. Start 60s gameplay countdown
            startGameplayTimer(levelId)
        }
    }

    private fun startGameplayTimer(levelId: Int) {
        levelTimerJob?.cancel()
        levelTimerJob = viewModelScope.launch {
            var lastBeepSec = -1
            while (isActive) {
                val rem = prefs.getLevelRemainingSeconds(levelId)
                _uiState.update { it.copy(levelRemainingTimeSec = rem) }

                // Synchronized tick for last 5 seconds: exactly at 5, 4, 3, 2, 1
                if (rem in 1..5 && rem != lastBeepSec) {
                    lastBeepSec = rem
                    HarmonicAudioEngine.playCountdownTick(rem)
                }

                if (rem <= 0) {
                    if (_uiState.value.gameStatus == GameStatus.PLAYING) {
                        HarmonicAudioEngine.stopCurrentSfx()
                        HarmonicAudioEngine.playTimeUpHata()
                        _uiState.update {
                            it.copy(
                                gameStatus = GameStatus.DEADLOCK,
                                isGameOverTimeUpDialogVisible = true
                            )
                        }
                    }
                    break
                }
                delay(200L)
            }
        }
    }

    fun startDailyChallenge() {
        if (!_uiState.value.isAuthenticated) {
            _uiState.update { it.copy(screenState = ScreenState.LOGIN) }
            return
        }
        if (!AdBlockDetector.hasNetworkCapability(getApplication())) {
            _uiState.update {
                it.copy(
                    isAdBlockerDetected = true,
                    adBlockerTitle = "İnternet Bağlantısı Yok",
                    adBlockerSubtitle = "Reklamlar çalıştırılamıyor",
                    adBlockerDetails = "Günün bulmacasını oynamak için aktif bir internet bağlantısı gerekmektedir. Lütfen internetinizi açın."
                )
            }
            return
        }
        val todaySeed = (SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).hashCode() and 0x7FFFFFFF) % LevelCatalog.TOTAL_LEVELS
        viewModelScope.launch {
            loadAndStartDailyChallenge(todaySeed)
        }
    }

    private suspend fun loadAndStartDailyChallenge(todaySeed: Int) {
        val levelData = repo.getLevelData(todaySeed + 1) ?: LevelCatalog.entityToLevelData(LevelCatalog.create100Levels()[todaySeed])
        _uiState.update {
            it.copy(
                screenState = ScreenState.DAILY_CHALLENGE,
                currentLevelIndex = todaySeed,
                level = levelData.copy(title = "Günün Bulmacası (2x Jeton)"),
                nodes = levelData.nodes.map { ln ->
                    Node(id = ln.id, x = ln.x, y = ln.y, connected = false, type = ln.type, keyForGateId = ln.keyForGateId)
                },
                visitedNodeIds = emptyList(),
                collectedKeyIds = emptySet(),
                currentStrokeSegments = emptyList(),
                currentPointerPos = null,
                isDrawing = false,
                echoes = emptyList(),
                echoCountForLevel = 0,
                isEchoShrinkerActive = false,
                gameStatus = GameStatus.PLAYING,
                isHintActive = false,
                isDailyChallenge = true,
                levelStartTimeMs = System.currentTimeMillis(),
                isDoubleRewardClaimedThisLevel = false
            )
        }
        startLevelSequence(levelData.levelId, isRestart = false)
    }

    /**
     * User requested opening flow:
     * İlk intro -> Sonra kullanıcı sözleşme kabulü -> Sonra Giriş / Ana Menü
     * İntro bitmeden ve sözleşme onaylanmadan fon müziği çalmaz.
     */
    fun finishIntro() {
        HarmonicAudioEngine.setIntroActive(false)
        if (!prefs.isKvkkConsentAccepted) {
            _uiState.update { it.copy(screenState = ScreenState.LEGAL_CONSENT) }
        } else {
            HarmonicAudioEngine.startBgm()
            if (_uiState.value.isAuthenticated || (prefs.rememberMe && prefs.authenticatedUsername.isNotEmpty())) {
                _uiState.update { it.copy(screenState = ScreenState.MAIN_MENU, isAuthenticated = true) }
            } else {
                _uiState.update { it.copy(screenState = ScreenState.LOGIN) }
            }
        }
    }

    /**
     * Authenticates user session and restores their saved progress from database/server.
     */
    fun onLoginSuccess(username: String, rememberMe: Boolean) {
        viewModelScope.launch {
            HarmonicAudioEngine.startBgm()
            val user = authRepo.getUserAccount(username)
            val actualUsername = user?.username ?: username
            val userLevel = (user?.currentLevelIndex ?: 0).coerceIn(0, LevelCatalog.TOTAL_LEVELS - 1)
            val userTokens = user?.tokens ?: 10
            val userBreakers = user?.echoBreakers ?: 3
            val userDiamonds = user?.diamonds ?: 1
            val userCoins = user?.coins ?: 50
            val userEchoes = user?.totalEchoes ?: 0
            val userStars = user?.totalStars ?: 0
            val userDark = user?.isDarkTheme ?: prefs.isDarkTheme
            val userCompleted = user?.completedLevelsCsv?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?.toSet() ?: emptySet()

            prefs.setAuthenticatedUser(actualUsername, rememberMe)
            prefs.currentLevelIndex = userLevel
            prefs.tokens = userTokens
            prefs.echoBreakers = userBreakers
            prefs.diamonds = userDiamonds
            prefs.coins = userCoins
            prefs.totalEchoes = userEchoes
            prefs.trophies = user?.trophies ?: 0
            prefs.totalPlayTimeSec = user?.totalPlayTimeSec ?: 0L
            prefs.maxCombo = user?.maxCombo ?: 0
            prefs.levelStatsCsv = user?.levelStatsCsv ?: ""
            prefs.isDarkTheme = userDark
            prefs.setCompletedLevelsRaw(userCompleted.map { it.toString() }.toSet())

            val levelData = repo.getLevelData(userLevel + 1) ?: LevelCatalog.entityToLevelData(LevelCatalog.create100Levels()[userLevel.coerceIn(0, 99)])

            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    authenticatedUser = actualUsername,
                    rememberMe = rememberMe,
                    currentLevelIndex = userLevel,
                    level = levelData,
                    tokens = userTokens,
                    echoBreakers = userBreakers,
                    diamonds = userDiamonds,
                    coins = userCoins,
                    totalEchoes = userEchoes,
                    totalStars = userStars,
                    trophies = prefs.trophies,
                    totalPlayTimeSec = prefs.totalPlayTimeSec,
                    maxCombo = prefs.maxCombo,
                    currentCombo = prefs.currentCombo,
                    isDarkTheme = userDark,
                    completedLevels = userCompleted,
                    screenState = ScreenState.MAIN_MENU
                )
            }
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.isDarkTheme = isDark
        _uiState.update { it.copy(isDarkTheme = isDark) }
        val username = _uiState.value.authenticatedUser
        if (username.isNotBlank()) {
            viewModelScope.launch {
                authRepo.saveActiveUserTheme(username, isDark)
            }
        }
    }

    fun toggleDarkTheme() {
        setDarkTheme(!_uiState.value.isDarkTheme)
    }

    /**
     * Terminate user session and return to Login Screen.
     */
    fun logout() {
        val u = _uiState.value.authenticatedUser
        if (u.isNotBlank()) {
            val state = _uiState.value
            viewModelScope.launch {
                authRepo.saveActiveUserProgress(
                    username = u,
                    currentLevelIndex = state.currentLevelIndex,
                    completedLevels = prefs.getCompletedLevels(),
                    tokens = state.tokens,
                    echoBreakers = state.echoBreakers,
                    coins = state.coins,
                    diamonds = state.diamonds,
                    totalEchoes = state.totalEchoes,
                    totalStars = state.totalStars
                )
                authRepo.saveActiveUserTheme(u, state.isDarkTheme)
            }
        }
        prefs.clearAuthentication()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                authenticatedUser = "",
                screenState = ScreenState.LOGIN,
                isSettingsDialogVisible = false
            )
        }
    }

    fun returnToMainMenu() {
        previewJob?.cancel()
        levelTimerJob?.cancel()
        HarmonicAudioEngine.stopCurrentSfx()
        val isAuth = _uiState.value.isAuthenticated
        _uiState.update {
            it.copy(
                screenState = if (isAuth) ScreenState.MAIN_MENU else ScreenState.LOGIN,
                isLevelSelectVisible = false,
                isShopDialogVisible = false,
                isSettingsDialogVisible = false,
                isSkinsDialogVisible = false,
                isPreviewActive = false,
                showDurationStartedBanner = false,
                isGameOverTimeUpDialogVisible = false
            )
        }
    }

    // --- Interactive Drawing & Engine Events ---

    fun onPointerDown(point: Point) {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.PLAYING) return
        if (state.isPreviewActive) {
            showToast("Ön izleme aşamasındasınız (5sn). Süre başlayınca çizim yapabilirsiniz.")
            return
        }

        val node1 = state.nodes.firstOrNull { it.id == 1 }
        val hitNode = if (node1 != null && node1.toPoint().distanceTo(point) <= nodeHitRadius) {
            node1
        } else {
            findNodeAtPoint(point, state.nodes)
        }
        if (hitNode != null) {
            // "Karakter 1'den başlayacak" kuralı: Çizim mutlaka 1 numaralı başlangıç düğümünden başlamalıdır.
            if (hitNode.id != 1) {
                showToast("Karakter 1'den başlar! Çizime 1 numaralı noktadan başlayın. (+1 Yankı)")
                HarmonicAudioEngine.playHataSound()
                triggerCollisionFeedback()
                _uiState.update { it.copy(echoCountForLevel = it.echoCountForLevel + 1, totalEchoes = it.totalEchoes + 1) }
                prefs.incrementTotalEchoes()
                return
            }

            // Check Lock & Key if starting at gate
            if (hitNode.type == NodeType.GATE) {
                val isUnlocked = state.collectedKeyIds.contains(hitNode.keyForGateId) ||
                        state.collectedKeyIds.contains(hitNode.id) ||
                        (hitNode.keyForGateId == -1 && state.collectedKeyIds.isNotEmpty())
                if (!isUnlocked) {
                    showToast("Bu kapı kilitli! Önce anahtarı topla. (+1 Yankı)")
                    HarmonicAudioEngine.playHataSound()
                    triggerCollisionFeedback()
                    _uiState.update { it.copy(echoCountForLevel = it.echoCountForLevel + 1, totalEchoes = it.totalEchoes + 1) }
                    prefs.incrementTotalEchoes()
                    return
                }
            }

            HarmonicAudioEngine.playNodeTone(0)
            triggerHapticClick()

            val newCollectedKeys = if (hitNode.type == NodeType.KEY) {
                showToast("Anahtar toplandı! Kilitli kapı açıldı.")
                state.collectedKeyIds + hitNode.keyForGateId + hitNode.id
            } else state.collectedKeyIds

            _uiState.update {
                it.copy(
                    isDrawing = true,
                    visitedNodeIds = listOf(hitNode.id),
                    collectedKeyIds = newCollectedKeys,
                    currentStrokeSegments = emptyList(),
                    currentPointerPos = point,
                    nodes = it.nodes.map { n -> if (n.id == hitNode.id) n.copy(connected = true) else n }
                )
            }

            if (state.isGhostRaceAvailable && state.isGhostRaceActive && state.ghostReplayPoints.isNotEmpty()) {
                startGhostRunner(state.ghostReplayPoints, state.ghostReplayDurationMs)
            }
        }
    }

    fun onPointerMove(point: Point) {
        val state = _uiState.value
        if (!state.isDrawing || state.gameStatus != GameStatus.PLAYING || state.isPreviewActive) return

        val lastVisitedId = state.visitedNodeIds.lastOrNull() ?: return
        val lastNode = state.nodes.firstOrNull { it.id == lastVisitedId } ?: return

        // 1. Proximity detection to past echoes: subtle haptic feedback
        val minDist = CollisionEngine.minDistanceToEchoStrokes(point, state.echoes)
        val isNear = minDist < 18f
        if (isNear != state.isProximityAlertActive) {
            _uiState.update { it.copy(isProximityAlertActive = isNear) }
            if (isNear) triggerProximityHaptic()
        }

        // Note: Per user request:
        // "Işınlar bir birinin içinde geçebilsin yankılardanda geçebilsin. Her bir yapılacak hata yankı olarak sayılacak."
        // Lasers and strokes can freely pass through each other and past echoes without failing!

        // Expected next node determination:
        val hintOrder = state.level.hintOrder
        val expectedNextId = if (hintOrder.isNotEmpty()) {
            val curIdx = hintOrder.indexOf(lastVisitedId)
            if (curIdx != -1 && curIdx + 1 < hintOrder.size) hintOrder[curIdx + 1] else lastVisitedId + 1
        } else {
            lastVisitedId + 1
        }

        // Check if reaching a target node
        val hitNode = findTargetNodeForDrag(point, lastNode, state.nodes, state.visitedNodeIds, expectedNextId)
        if (hitNode != null && hitNode.id != lastVisitedId) {
            val attemptedSeg = Segment(lastNode.toPoint(), hitNode.toPoint(), lastVisitedId, hitNode.id)
            // Already visited node? (User request: "örneğin önceki düğüme geri dönmesi bir yankı sayılsın")
            if (state.visitedNodeIds.contains(hitNode.id)) {
                val isPrevNode = state.visitedNodeIds.size >= 2 && hitNode.id == state.visitedNodeIds[state.visitedNodeIds.size - 2]
                val msg = if (isPrevNode) {
                    "Önceki düğüme geri dönüldü! (+1 Yankı)"
                } else {
                    "Ziyaret edilen düğüme geri dönülemez! (+1 Yankı)"
                }
                showToast(msg)
                commitStrokeAsEcho(msg, attemptedSeg)
                return
            }

            // Directed edge constraint check:
            val illegalDirected = state.level.directedEdges.any { edge ->
                edge.fromId == hitNode.id && edge.toId == lastVisitedId
            }
            if (illegalDirected) {
                val msg = "Bu kenar tek yönlü! (+1 Yankı)"
                showToast(msg)
                commitStrokeAsEcho(msg, attemptedSeg)
                return
            }

            if (hitNode.id != expectedNextId) {
                val msg = "Sıradaki hedef: $expectedNextId numaralı nokta! (+1 Yankı)"
                showToast(msg)
                commitStrokeAsEcho(msg, attemptedSeg)
                return
            }

            // Lock & Key Gate check:
            if (hitNode.type == NodeType.GATE) {
                val isUnlocked = state.collectedKeyIds.contains(hitNode.keyForGateId) ||
                        state.collectedKeyIds.contains(hitNode.id) ||
                        (hitNode.keyForGateId == -1 && state.collectedKeyIds.isNotEmpty())
                if (!isUnlocked) {
                    val msg = "Kapı kilitli! Önce anahtarı topla (+1 Yankı)"
                    showToast(msg)
                    commitStrokeAsEcho(msg, attemptedSeg)
                    return
                }
            }

            // Collect key if this node is a key
            val updatedKeys = if (hitNode.type == NodeType.KEY) {
                showToast("Anahtar toplandı! Kilitli kapı açıldı.")
                state.collectedKeyIds + hitNode.keyForGateId + hitNode.id
            } else state.collectedKeyIds

            val finishedSegment = Segment(lastNode.toPoint(), hitNode.toPoint(), lastVisitedId, hitNode.id)
            val newStrokeList = state.currentStrokeSegments + finishedSegment
            val newVisited = state.visitedNodeIds + hitNode.id
            val newNodes = state.nodes.map { n -> if (n.id == hitNode.id) n.copy(connected = true) else n }

            HarmonicAudioEngine.playNodeTone(newVisited.size - 1)
            HarmonicAudioEngine.playCandyPop(newVisited.size)
            if (newVisited.size >= 4) {
                HarmonicAudioEngine.playComboCrush(newVisited.size)
            }
            triggerHapticClick()

            // Candy Crush style explosion & combo callouts!
            val (calloutText, calloutColor) = com.example.model.CandyEffectsFactory.getCalloutText(newVisited.size, state.language)
            val newParticles = com.example.model.CandyEffectsFactory.createNodeExplosion(hitNode.x, hitNode.y, count = 18)
            val now = System.currentTimeMillis()

            // Check Victory (All nodes connected)
            if (newVisited.size == state.nodes.size) {
                HarmonicAudioEngine.playVictoryCallout()
                val victoryConfetti = com.example.model.CandyEffectsFactory.createConfettiVictory(360f, 480f, count = 50)
                val victoryText = com.example.model.CandyEffectsFactory.getVictoryCalloutText(state.language)
                candyCalloutDismissJob?.cancel()
                _uiState.update {
                    it.copy(
                        candyParticles = it.candyParticles.filter { p -> now - p.createdAt < 1100L } + newParticles + victoryConfetti,
                        candyCallout = com.example.model.CandyCallout(
                            text = victoryText,
                            x = 180f,
                            y = 150f,
                            color = androidx.compose.ui.graphics.Color(0xFFFFB703),
                            alpha = 1.0f,
                            scale = 1.35f
                        )
                    )
                }
                candyCalloutDismissJob = viewModelScope.launch {
                    kotlinx.coroutines.delay(2900L)
                    _uiState.update { it.copy(candyCallout = null) }
                }
                handleVictory(newStrokeList)
                return
            }

            candyCalloutDismissJob?.cancel()
            _uiState.update {
                it.copy(
                    visitedNodeIds = newVisited,
                    collectedKeyIds = updatedKeys,
                    currentStrokeSegments = newStrokeList,
                    currentPointerPos = hitNode.toPoint(),
                    nodes = newNodes,
                    candyParticles = it.candyParticles.filter { p -> now - p.createdAt < 1100L } + newParticles,
                    candyCallout = com.example.model.CandyCallout(
                        text = calloutText,
                        x = hitNode.x,
                        y = hitNode.y - 35f,
                        color = calloutColor,
                        alpha = 1.0f,
                        scale = 1.15f
                    )
                )
            }
            candyCalloutDismissJob = viewModelScope.launch {
                kotlinx.coroutines.delay(2900L)
                _uiState.update { it.copy(candyCallout = null) }
            }
            return
        }

        _uiState.update { it.copy(currentPointerPos = point) }
    }

    fun onPointerUp() {
        ghostRunnerJob?.cancel()
        val state = _uiState.value
        if (!state.isDrawing || state.gameStatus != GameStatus.PLAYING) return

        // Only penalize with an echo barrier if the player actually drew segments and lifted mid-path.
        // If they only touched node 1 and lifted without completing a segment, reset cleanly.
        if (state.currentStrokeSegments.isNotEmpty() && state.visitedNodeIds.size < state.nodes.size) {
            triggerEarlyLiftFailure("Hamle yarım bırakıldı!")
        } else {
            _uiState.update {
                it.copy(
                    isDrawing = false,
                    currentPointerPos = null,
                    visitedNodeIds = emptyList(),
                    currentStrokeSegments = emptyList(),
                    nodes = it.nodes.map { n -> n.copy(connected = false) },
                    ghostRunnerCurrentPoint = null
                )
            }
        }
    }

    private fun handleVictory(completedSegments: List<Segment>) {
        previewJob?.cancel()
        levelTimerJob?.cancel()
        ghostRunnerJob?.cancel()
        prefs.clearLevelTimer(_uiState.value.level.levelId)

        val state = _uiState.value
        HarmonicAudioEngine.stopCurrentSfx()
        HarmonicAudioEngine.playParty()
        triggerHapticVictory()

        val echoCount = state.echoCountForLevel
        val parEchoes = state.level.parEchoes

        // User requirement:
        // "yankılar kaç yıldızla bitireceğini belirlesin bölümler 3 yıldızla değerlendirilecek"
        // 0 mistakes = 3 stars, <= 2 mistakes = 2 stars, > 2 = 1 star
        val stars = when {
            echoCount == 0 -> 3
            echoCount <= 2 -> 2
            else -> 1
        }

        // User requirement: "★★★ Kusursuz: Perfect Echo. Oyuncu 3 yıldız aldığında o bölümün kusursuz yolu kaydedilir."
        if (stars == 3) {
            val durationMs = (System.currentTimeMillis() - state.levelStartTimeMs).coerceAtLeast(1000L)
            val pts = completedSegments.flatMap { listOf(Pair(it.p1.x, it.p1.y), Pair(it.p2.x, it.p2.y)) }.distinct()
            prefs.savePerfectRun(state.level.levelId, pts, durationMs)
        }

        val wonGhostRace = state.isGhostRaceAvailable && state.ghostReplayDurationMs > 0 &&
                (System.currentTimeMillis() - state.levelStartTimeMs) < state.ghostReplayDurationMs
        if (wonGhostRace) {
            showToast("🏆 Kendi Kusursuz Hayaletini Geride Bıraktın! Yeni Rekor!")
        }

        val startTime = if (state.levelStartTimeMs > 0) state.levelStartTimeMs else (System.currentTimeMillis() - 7500L)
        val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000f).coerceIn(1.0f, 300.0f)
        val roundedDurationSec = (durationSeconds * 10).roundToInt() / 10f

        // Combo & Trophy calculations
        val newCombo = if (echoCount == 0) state.currentCombo + 1 else 0
        prefs.recordCombo(newCombo)

        val baseTrophies = 5 + (stars * 3) // 8..14 base trophies
        val zeroEchoBonus = if (echoCount == 0) 5 else 0
        val comboBonus = if (newCombo > 1) (newCombo * 2).coerceAtMost(10) else 0
        val speedBonus = if (durationSeconds <= 12f) 3 else 0
        val isDoubleBoosterActive = System.currentTimeMillis() < prefs.doubleTrophiesExpiresAt
        val rawTrophies = baseTrophies + zeroEchoBonus + comboBonus + speedBonus
        val totalLevelTrophies = if (isDoubleBoosterActive) rawTrophies * 2 else rawTrophies

        prefs.addTrophies(totalLevelTrophies)
        prefs.addPlayTime(durationSeconds.toLong())

        // Automatically submit score to LootLocker Global Leaderboard (ekoleadbordglobal)
        val currentTrophies = prefs.trophies
        val currentUsername = _uiState.value.authenticatedUser.ifBlank { "Oyuncu" }
        lootLocker.submitScore(currentUsername, currentTrophies)
        prefs.recordLevelResult(
            levelId = state.level.levelId,
            title = state.level.title,
            echoes = echoCount,
            timeTakenSec = roundedDurationSec,
            stars = stars,
            trophiesEarned = totalLevelTrophies
        )

        val trophyBreakdown = com.example.model.TrophyRewardBreakdown(
            baseTrophies = baseTrophies,
            zeroEchoBonus = zeroEchoBonus,
            comboBonus = comboBonus,
            speedBonus = speedBonus,
            totalTrophies = totalLevelTrophies,
            currentCombo = newCombo,
            timeTakenSec = roundedDurationSec
        )

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.checkAndResetDailyQuests(todayStr)
        prefs.levelsCompletedToday = prefs.levelsCompletedToday + 1
        prefs.starsEarnedToday = prefs.starsEarnedToday + stars
        if (echoCount == 0) {
            prefs.flawlessLevelsToday = prefs.flawlessLevelsToday + 1
        }

        if (state.isDailyChallenge) {
            // 2x Token reward for Daily Challenge
            prefs.addTokens(4)
            prefs.lastDailyCompletedDate = todayStr
            prefs.dailyChallengeCompletedToday = true
            showToast("Tebrikler! Günün Bulmacası Tamamlandı (+4 Jeton!)")
        } else {
            // Normal level gives base +1 token reward
            prefs.addTokens(1)
            viewModelScope.launch {
                repo.recordVictory(state.level.levelId, echoCount, parEchoes)
            }
            prefs.markLevelCompleted(state.currentLevelIndex)
        }

        val u = state.authenticatedUser
        if (u.isNotBlank()) {
            val updatedCompleted = prefs.getCompletedLevels()
            val nextLvl = (state.currentLevelIndex + 1).coerceAtMost(LevelCatalog.TOTAL_LEVELS - 1)
            viewModelScope.launch {
                authRepo.saveActiveUserProgress(
                    username = u,
                    currentLevelIndex = nextLvl,
                    completedLevels = updatedCompleted,
                    tokens = prefs.tokens,
                    echoBreakers = prefs.echoBreakers,
                    coins = prefs.coins,
                    diamonds = prefs.diamonds,
                    totalEchoes = prefs.totalEchoes,
                    totalStars = _uiState.value.totalStars + stars
                )
            }
        }

        refreshDailySystems()

        _uiState.update {
            it.copy(
                isDrawing = false,
                currentPointerPos = null,
                currentStrokeSegments = completedSegments,
                gameStatus = GameStatus.VICTORY,
                tokens = prefs.tokens,
                trophies = prefs.trophies,
                totalPlayTimeSec = prefs.totalPlayTimeSec,
                maxCombo = prefs.maxCombo,
                currentCombo = newCombo,
                lastTrophyRewardBreakdown = trophyBreakdown,
                isDailyCompletedToday = if (state.isDailyChallenge) true else it.isDailyCompletedToday,
                isDoubleRewardClaimedThisLevel = false
            )
        }
    }

    private fun triggerSelfIntersectionFailure(message: String) {
        showToast(message)
        commitStrokeAsEcho("Kendi Yoluyla Kesişti")
    }

    private fun triggerCollisionFailure(message: String) {
        showToast(message)
        commitStrokeAsEcho("Yankıya Çarpıldı")
    }

    private fun triggerEarlyLiftFailure(message: String) {
        showToast(message)
        commitStrokeAsEcho("Yarım Hamle")
    }

    private fun commitStrokeAsEcho(reason: String, attemptedSegment: Segment? = null) {
        HarmonicAudioEngine.playHataSound()
        triggerCollisionFeedback()
        ghostRunnerJob?.cancel()

        val state = _uiState.value
        val newEchoSegments = if (attemptedSegment != null) {
            state.currentStrokeSegments + attemptedSegment
        } else if (state.currentPointerPos != null && state.visitedNodeIds.isNotEmpty()) {
            val lastNode = state.nodes.firstOrNull { it.id == state.visitedNodeIds.last() }
            if (lastNode != null) {
                state.currentStrokeSegments + Segment(lastNode.toPoint(), state.currentPointerPos, lastNode.id, -1)
            } else {
                state.currentStrokeSegments.toList()
            }
        } else {
            state.currentStrokeSegments.toList()
        }
        prefs.incrementTotalEchoes()

        // Decaying Echoes logic
        val isDecaying = state.level.mechanicType in listOf("DECAYING_NODES", "OMEGA_SYNTHESIS")
        val isGhost = state.level.isGhostEchoes

        val newEchoStroke = if (newEchoSegments.isNotEmpty()) {
            EchoStroke(
                id = (state.echoes.size + 1),
                segments = newEchoSegments,
                remainingAttempts = state.level.decayLifetime,
                maxLifetime = state.level.decayLifetime,
                isGhost = isGhost
            )
        } else null

        // Save failed stroke into persistent past ghost memories & record player behavioral telemetry
        if (newEchoSegments.isNotEmpty()) {
            val pts = newEchoSegments.flatMap { listOf(Pair(it.p1.x, it.p1.y), Pair(it.p2.x, it.p2.y)) }.distinct()
            prefs.saveGhostStroke(state.level.levelId, pts)
            val lastPt = pts.lastOrNull()
            if (lastPt != null) {
                prefs.recordBehaviorError(
                    lastPt.first,
                    lastPt.second,
                    isRetreat = reason.contains("Geri") || reason.contains("Yarım"),
                    isFast = (System.currentTimeMillis() - state.levelStartTimeMs) < 4000L
                )
            }
        }

        // Decrement lifetime of past decaying echoes
        val updatedPastEchoes = state.echoes.mapNotNull { echo ->
            if (isDecaying) {
                val remaining = echo.remainingAttempts - 1
                if (remaining <= 0) null else echo.copy(remainingAttempts = remaining)
            } else echo
        }

        val finalEchoes = if (newEchoStroke != null) updatedPastEchoes + newEchoStroke else updatedPastEchoes
        val newEchoCount = state.echoCountForLevel + 1
        val isDeadlocked = newEchoCount >= deadlockThreshold

        val updatedGhosts = prefs.getGhostStrokes(state.level.levelId).map { pts -> pts.map { Point(it.first, it.second) } }
        val updatedBeast = EchoBeastState.fromEchoCount(newEchoCount)
        val updatedInsight = prefs.getBehaviorInsight()
        val updatedTendency = prefs.getAdaptiveTendencyQuadrant()

        prefs.recordCombo(0)

        _uiState.update {
            it.copy(
                isDrawing = false,
                currentPointerPos = null,
                visitedNodeIds = emptyList(),
                collectedKeyIds = emptySet(),
                currentStrokeSegments = emptyList(),
                echoes = finalEchoes,
                echoCountForLevel = newEchoCount,
                totalEchoes = prefs.totalEchoes,
                currentCombo = 0,
                isCollisionAlertActive = true,
                nodes = it.nodes.map { n -> n.copy(connected = false) },
                gameStatus = if (isDeadlocked) GameStatus.DEADLOCK else GameStatus.PLAYING,
                pastGhostStrokes = updatedGhosts,
                echoBeastState = updatedBeast,
                playerBehaviorInsight = updatedInsight,
                adaptiveTendencyQuadrant = updatedTendency,
                ghostRunnerCurrentPoint = null
            )
        }

        alertResetJob?.cancel()
        alertResetJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isCollisionAlertActive = false) }
        }
    }

    fun showMechanicGuide(levelId: Int? = null) {
        val targetId = levelId ?: _uiState.value.level.levelId
        val isTurkish = prefs.languageCode == "tr"
        val rule = com.example.data.LevelRuleCatalog.getRuleForLevel(targetId, isTurkish)
        _uiState.update {
            it.copy(
                isLevelMechanicIntroVisible = true,
                activeLevelRule = rule,
                activeMechanicInfo = rule.toMechanicInfo(),
                isRuleEncyclopediaOpen = false
            )
        }
    }

    fun openRuleEncyclopedia() {
        val currentId = _uiState.value.level.levelId
        val isTurkish = prefs.languageCode == "tr"
        val rule = com.example.data.LevelRuleCatalog.getRuleForLevel(currentId, isTurkish)
        _uiState.update {
            it.copy(
                isLevelMechanicIntroVisible = true,
                activeLevelRule = rule,
                activeMechanicInfo = rule.toMechanicInfo(),
                isRuleEncyclopediaOpen = true
            )
        }
    }

    fun dismissMechanicGuide() {
        val lvl = _uiState.value.level.levelId
        val tier = com.example.data.LevelRuleCatalog.getTierForLevel(lvl)
        prefs.markMechanicIntroShown(tier)
        prefs.markLevelRuleShown(lvl)
        _uiState.update { it.copy(isLevelMechanicIntroVisible = false, isRuleEncyclopediaOpen = false) }
    }

    fun setAutoShowLevelRules(enabled: Boolean) {
        prefs.isAutoShowLevelRulesEnabled = enabled
    }

    fun toggleGhostRace() {
        _uiState.update { it.copy(isGhostRaceActive = !it.isGhostRaceActive) }
        val statusText = if (!_uiState.value.isGhostRaceActive) "Hayaletle Yarış Kapatıldı" else "Hayaletle Yarış Aktif!"
        showToast(statusText)
    }

    private fun startGhostRunner(pts: List<Point>, durationMs: Long) {
        ghostRunnerJob?.cancel()
        ghostRunnerJob = viewModelScope.launch {
            val totalTime = durationMs.coerceIn(1500L, 60000L)
            val startTime = System.currentTimeMillis()
            while (isActive && _uiState.value.isDrawing) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / totalTime).coerceIn(0f, 1f)
                val idx = (progress * (pts.size - 1)).toInt().coerceIn(0, pts.size - 1)
                _uiState.update { it.copy(ghostRunnerCurrentPoint = pts[idx]) }
                if (progress >= 1f) break
                delay(32L)
            }
        }
    }

    // --- Power-ups & Monetization ---

    fun useEchoBreaker() {
        val state = _uiState.value
        if (state.echoes.isEmpty()) {
            showToast("Kırılacak aktif yankı bulunmuyor.")
            return
        }
        val isInfiniteActive = System.currentTimeMillis() < prefs.infiniteBreakersExpiresAt
        if (isInfiniteActive || prefs.useBreaker()) {
            HarmonicAudioEngine.playBrokenRedLine()
            triggerHapticClick()
            // Remove the most recent echo stroke
            val remainingEchoes = state.echoes.dropLast(1)
            _uiState.update {
                it.copy(
                    echoes = remainingEchoes,
                    echoBreakers = prefs.echoBreakers
                )
            }
            showToast(if (isInfiniteActive) "⏱️ Sonsuz Matkap aktif: Yankı imha edildi!" else "Matkap lazeri son yankıyı imha etti!")
        } else {
            showToast("Yetersiz Matkap Jetonu! Mağazadan temin edebilirsin.")
            setShopVisible(true)
        }
    }

    fun activateEchoShrinker(onTriggerAd: () -> Unit = {}) {
        val state = _uiState.value
        if (state.isEchoShrinkerActive) {
            showToast("🌟 Esnek Mod zaten bu bölüm için aktif!")
            return
        }
        _uiState.update {
            it.copy(
                isEchoShrinkerActive = true,
                gameStatus = GameStatus.PLAYING
            )
        }
        HarmonicAudioEngine.playVictoryCascade()
        triggerHapticClick()
        showToast("🌟 Esnek Mod Aktif! Yankı bariyerleri %50 küçültüldü (Yalnızca bu bölüm için geçerli).")
    }

    /**
     * User request: "eğer reklam izlersen 15 sn ek süre eklensin."
     * Adds +15s to level deadline and resumes timer loop cleanly.
     */
    fun addExtraTimeFromAd() {
        val levelId = _uiState.value.level.levelId
        prefs.addLevelTime(levelId, 15)
        val newRem = prefs.getLevelRemainingSeconds(levelId)
        _uiState.update {
            it.copy(
                levelRemainingTimeSec = newRem,
                isGameOverTimeUpDialogVisible = false,
                gameStatus = GameStatus.PLAYING
            )
        }
        showToast("⏱️ +15 Saniye Ek Süre Eklendi!")
        HarmonicAudioEngine.playParty()
        startGameplayTimer(levelId)
    }

    fun grantRewardedReward(type: String) {
        val rewardInfo = when (type) {
            "EXTRA_TIME", "EXTRA_TIME_15S" -> {
                addExtraTimeFromAd()
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false
                    )
                }
                RewardClaimedInfo(
                    title = "⏱️ +15 Saniye Eklendi!",
                    subtitle = "Bölüm süresine +15 saniye başarıyla eklendi.",
                    rewardType = type
                )
            }
            "CLEAR_ECHOES" -> {
                clearAllEchoes()
                prefs.addTokens(1)
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        tokens = prefs.tokens,
                        gameStatus = GameStatus.PLAYING
                    )
                }
                RewardClaimedInfo(
                    title = "Yankılar Temizlendi!",
                    subtitle = "Bütün yankı bariyerleri silindi ve +1 İpucu Jetonu kazandınız.",
                    rewardType = type,
                    tokensAdded = 1
                )
            }
            "SHRINKER" -> {
                val current = (prefs.shrinkerAdsWatched + 1).coerceAtMost(3)
                prefs.shrinkerAdsWatched = current
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        shrinkerAdsWatched = current
                    )
                }
                if (current < 3) {
                    showToast("Esnek Alan için reklam ($current/3) izlendi! ${3 - current} reklam daha izleyin.")
                    RewardClaimedInfo(
                        title = "Esnek Alan İlerlemesi ($current/3)",
                        subtitle = "Esnek Alan için ${3 - current} reklam daha izlenmeli ve 5 Elmas gereklidir.",
                        rewardType = type
                    )
                } else {
                    if (prefs.diamonds >= 5) {
                        prefs.useDiamond(5)
                        prefs.shrinkerAdsWatched = 0
                        _uiState.update {
                            it.copy(
                                isEchoShrinkerActive = true,
                                diamonds = prefs.diamonds,
                                shrinkerAdsWatched = 0,
                                gameStatus = GameStatus.PLAYING
                            )
                        }
                        showToast("🌟 Esnek Alan Aktif! 3 reklam tamamlandı ve 5 Elmas harcandı.")
                        RewardClaimedInfo(
                            title = "🌟 Esnek Alan Aktif!",
                            subtitle = "3 reklam izlendi ve 5 Elmas ile yankı bariyerleri inceltildi.",
                            rewardType = type
                        )
                    } else {
                        showToast("3 reklam tamamlandı! Şimdi aktifleştirmek için 5 Elmas zorunludur (Mevcut: ${prefs.diamonds}).")
                        RewardClaimedInfo(
                            title = "3/3 Reklam Tamamlandı!",
                            subtitle = "Esnek Alanı aktifleştirmek için 5 Elmas zorunludur (Mevcut: ${prefs.diamonds} Elmas).",
                            rewardType = type
                        )
                    }
                }
            }
            "HINT", "HINT_AD" -> {
                val nextWatched = (prefs.hintAdsWatched + 1).coerceAtMost(3)
                prefs.hintAdsWatched = nextWatched
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        hintAdsWatched = nextWatched
                    )
                }
                if (nextWatched >= 3) {
                    prefs.hintAdsWatched = 0
                    _uiState.update {
                        it.copy(
                            isHintActive = true,
                            hintAdsWatched = 0,
                            isHintPurchaseDialogVisible = false,
                            gameStatus = GameStatus.PLAYING
                        )
                    }
                    showToast("💡 İpucu Açıldı! 3 reklam başarıyla izlendi.")
                    RewardClaimedInfo(
                        title = "💡 İpucu Rehberi Aktif!",
                        subtitle = "3 reklam izlendi ve bu bölümün çözüm rotası açıldı.",
                        rewardType = type
                    )
                } else {
                    showToast("💡 İpucu için reklam ($nextWatched/3) izlendi! Kalan: ${3 - nextWatched}")
                    RewardClaimedInfo(
                        title = "İpucu Reklamı ($nextWatched/3)",
                        subtitle = "İpucu için ${3 - nextWatched} reklam daha izleyin veya 5 elmas kullanın.",
                        rewardType = type
                    )
                }
            }
            "FREE_BREAKER", "REWARD_DAILY" -> {
                prefs.recordRewardClaimed("REWARD_DAILY")
                prefs.recordRewardClaimed("FREE_BREAKER")
                prefs.addBreakers(1)
                prefs.addTokens(2)
                updateRewardCooldowns()
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        echoBreakers = prefs.echoBreakers,
                        tokens = prefs.tokens
                    )
                }
                RewardClaimedInfo(
                    title = "Ödül Başarıyla Kazanıldı!",
                    subtitle = "+1 Matkap Lazeri & +2 İpucu Jetonu hesabınıza eklendi (3 saat sonra tekrar hazır).",
                    rewardType = type,
                    tokensAdded = 2,
                    breakersAdded = 1
                )
            }
            "FREE_DIAMOND" -> {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                prefs.recordFreeDiamondAdWatched(todayStr)
                prefs.recordRewardClaimed("FREE_DIAMOND")
                prefs.addDiamonds(1)
                updateRewardCooldowns()
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        diamonds = prefs.diamonds
                    )
                }
                RewardClaimedInfo(
                    title = "Nadir Elmas Kazanıldı!",
                    subtitle = "+1 Nadir Elmas hesabınıza başarıyla eklendi (3 saat sonra tekrar hazır).",
                    rewardType = type,
                    diamondsAdded = 1
                )
            }
            "FREE_COINS" -> {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                prefs.recordFreeCoinAdWatched(todayStr)
                prefs.recordRewardClaimed("FREE_COINS")
                prefs.addTokens(3)
                prefs.addCoins(30)
                updateRewardCooldowns()
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        tokens = prefs.tokens,
                        coins = prefs.coins
                    )
                }
                RewardClaimedInfo(
                    title = "Jeton & Altın Paketi!",
                    subtitle = "+3 İpucu Jetonu ve +30 Altın hesabınıza eklendi (3 saat sonra tekrar hazır).",
                    rewardType = type,
                    tokensAdded = 3,
                    coinsAdded = 30
                )
            }
            "HINT_TOKEN" -> {
                prefs.addTokens(2)
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        tokens = prefs.tokens
                    )
                }
                RewardClaimedInfo(
                    title = "Jeton Ödülü Kazanıldı!",
                    subtitle = "+2 İpucu Jetonu hesabınıza başarıyla eklendi.",
                    rewardType = type,
                    tokensAdded = 2
                )
            }
            "WATCH_3_ADS_REWARD" -> {
                val currentCount = prefs.multiAdWatchCount + 1
                if (currentCount >= 3) {
                    prefs.multiAdWatchCount = 0
                    prefs.recordRewardClaimed("WATCH_3_ADS_REWARD")
                    updateRewardCooldowns()
                    prefs.addDiamonds(3)
                    prefs.addCoins(500)
                    prefs.addBreakers(3)
                    prefs.addTokens(5)
                    val expires = System.currentTimeMillis() + 15 * 60 * 1000L
                    prefs.doubleTrophiesExpiresAt = expires
                    _uiState.update {
                        it.copy(
                            isRewardedSimulating = false,
                            isAdLoading = false,
                            multiAdWatchCount = 0,
                            diamonds = prefs.diamonds,
                            coins = prefs.coins,
                            echoBreakers = prefs.echoBreakers,
                            tokens = prefs.tokens,
                            doubleTrophiesExpiresAt = expires
                        )
                    }
                    RewardClaimedInfo(
                        title = "🎉 3-REKLAM MEGA ÖDÜLÜ AÇILDI!",
                        subtitle = "Tebrikler! +3 💎 Elmas, +500 🪙 Altın, +3 ⚡ Matkap, +5 Jeton ve 15 Dk 2x Kupa kazandınız (3 saat sonra tekrar hazır)!",
                        rewardType = type,
                        diamondsAdded = 3,
                        coinsAdded = 500,
                        breakersAdded = 3,
                        tokensAdded = 5
                    )
                } else {
                    prefs.multiAdWatchCount = currentCount
                    _uiState.update {
                        it.copy(
                            isRewardedSimulating = false,
                            isAdLoading = false,
                            multiAdWatchCount = currentCount
                        )
                    }
                    showToast("1 Reklam İzlendi ($currentCount/3)! ${3 - currentCount} reklam sonra Büyük Mega Sandık açılacak.")
                    return
                }
            }
            "BOOSTER_DOUBLE_TROPHIES" -> {
                val expires = System.currentTimeMillis() + 15 * 60 * 1000L
                prefs.doubleTrophiesExpiresAt = expires
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        doubleTrophiesExpiresAt = expires
                    )
                }
                RewardClaimedInfo(
                    title = "⏱️ 2x Kupa Katlayıcı Aktif!",
                    subtitle = "Önümüzdeki 15 dakika boyunca her kazanılan bölümde 2 kat kupa alacaksınız!",
                    rewardType = type
                )
            }
            "BOOSTER_INFINITE_BREAKERS" -> {
                val expires = System.currentTimeMillis() + 10 * 60 * 1000L
                prefs.infiniteBreakersExpiresAt = expires
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        infiniteBreakersExpiresAt = expires
                    )
                }
                RewardClaimedInfo(
                    title = "⏱️ Sınırsız Matkap Lazeri Aktif!",
                    subtitle = "Önümüzdeki 10 dakika boyunca matkap lazeleriniz tükenmeden serbestçe kullanılabilir!",
                    rewardType = type
                )
            }
            "BOOSTER_SHIELD" -> {
                val expires = System.currentTimeMillis() + 20 * 60 * 1000L
                prefs.radiusShrinkerExpiresAt = expires
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        radiusShrinkerExpiresAt = expires,
                        isEchoShrinkerActive = true
                    )
                }
                RewardClaimedInfo(
                    title = "⏱️ Esnek Yankı Kalkanı Aktif!",
                    subtitle = "Önümüzdeki 20 dakika boyunca yankı bariyerleri %50 inceltildi.",
                    rewardType = type
                )
            }
            "OPEN_CHEST" -> {
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false
                    )
                }
                return
            }
            "DOUBLE_REWARD" -> {
                claimVictoryDoubleReward()
                RewardClaimedInfo(
                    title = "2X Zafer Ödülü!",
                    subtitle = "+2 İpucu Jetonu ve +1 Matkap Lazeri hesabınıza tanımlandı.",
                    rewardType = type,
                    tokensAdded = 2,
                    breakersAdded = 1
                )
            }
            else -> {
                prefs.addTokens(1)
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        tokens = prefs.tokens
                    )
                }
                RewardClaimedInfo(
                    title = "Ödül Alındı!",
                    subtitle = "+1 İpucu Jetonu hesabınıza tanımlandı.",
                    rewardType = type,
                    tokensAdded = 1
                )
            }
        }
        HarmonicAudioEngine.playVictoryCascade()
        val u = _uiState.value.authenticatedUser
        if (u.isNotBlank()) {
            viewModelScope.launch {
                authRepo.saveActiveUserProgress(
                    username = u,
                    currentLevelIndex = _uiState.value.currentLevelIndex,
                    completedLevels = prefs.getCompletedLevels(),
                    tokens = prefs.tokens,
                    echoBreakers = prefs.echoBreakers,
                    coins = prefs.coins,
                    diamonds = prefs.diamonds,
                    totalEchoes = prefs.totalEchoes,
                    totalStars = _uiState.value.totalStars
                )
            }
        }
        _uiState.update {
            it.copy(
                rewardClaimedData = rewardInfo,
                isShopDialogVisible = false
            )
        }
        showToast(rewardInfo.title)
    }

    fun dismissRewardClaimedDialog() {
        _uiState.update { it.copy(rewardClaimedData = null) }
    }

    private var adLoadingTimeoutJob: Job? = null

    fun setAdLoading(loading: Boolean, type: String = "HINT_TOKEN") {
        adLoadingTimeoutJob?.cancel()
        _uiState.update { it.copy(isAdLoading = loading) }
        if (loading) {
            adLoadingTimeoutJob = viewModelScope.launch {
                delay(6000)
                if (_uiState.value.isAdLoading) {
                    _uiState.update { it.copy(isAdLoading = false) }
                    showToast("Sorun oluştu! Reklam yüklenemedi veya istek zaman aşımına uğradı.")
                }
            }
        }
    }

    fun onAdUnavailable(type: String, reason: String) {
        _uiState.update {
            it.copy(
                isAdLoading = false,
                isRewardedSimulating = false
            )
        }
        // Strict policy: "sadece rewarda eğer gerçek reklam gösterilmediyse ödülü vermesin"
        showToast("Reklam yüklenemediği veya tamamlanmadığı için ödül verilemedi.")
    }

    fun shouldShowInterstitialOnNextLevel(): Boolean {
        val currentLevelId = _uiState.value.level.levelId
        return prefs.shouldShowInterstitial(currentLevelId)
    }

    fun onInterstitialAdShownOrSkipped(wasShown: Boolean) {
        val currentLevelId = _uiState.value.level.levelId
        if (wasShown) {
            prefs.onInterstitialDisplayed(currentLevelId)
        } else {
            prefs.incrementLevelsSinceInterstitial()
        }
    }

    fun checkInternetAndAdHealth(silent: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAdBlocker = true) }
            val healthResult = AdBlockDetector.checkAdHealth(getApplication())
            if (healthResult.isHealthy) {
                _uiState.update {
                    it.copy(
                        isAdBlockerDetected = false,
                        isCheckingAdBlocker = false,
                        adBlockerTitle = "",
                        adBlockerSubtitle = "",
                        adBlockerDetails = ""
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAdBlockerDetected = true,
                        isCheckingAdBlocker = false,
                        adBlockerTitle = healthResult.title,
                        adBlockerSubtitle = healthResult.subtitle,
                        adBlockerDetails = healthResult.details
                    )
                }
            }
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    viewModelScope.launch {
                        delay(600)
                        val health = AdBlockDetector.checkAdHealth(getApplication())
                        if (health.isHealthy) {
                            _uiState.update {
                                it.copy(
                                    isAdBlockerDetected = false,
                                    isCheckingAdBlocker = false,
                                    adBlockerTitle = "",
                                    adBlockerSubtitle = "",
                                    adBlockerDetails = ""
                                )
                            }
                        }
                    }
                }

                override fun onLost(network: Network) {
                    viewModelScope.launch {
                        _uiState.update {
                            it.copy(
                                isAdBlockerDetected = true,
                                adBlockerTitle = "İnternet Bağlantısı Kesildi",
                                adBlockerSubtitle = "Reklam servislerine erişilemiyor",
                                adBlockerDetails = "ECHO tamamen ücretsiz bir oyundur ve reklam servisleri ile desteklenmektedir. Oyuna devam etmek için lütfen internet bağlantınızı (Wi-Fi veya Mobil Veri) açın."
                            )
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("EchoGameViewModel", "Failed to register network callback", e)
        }
    }

    fun toggleTestAds(enabled: Boolean) {
        prefs.isTestAdsEnabled = enabled
        _uiState.update { it.copy(testAdsEnabled = enabled) }
        com.example.ads.StartIoManager.setTestModeEnabled(enabled, getApplication())
        showToast(
            if (enabled) "Test Reklam Modu Aktif"
            else "Canlı Reklam Modu Aktif (Google Play)"
        )
    }

    fun checkAdBlocker(autoStartPending: Boolean = true) {
        // Tüm reklam engelleyici uyarıları kullanıcı isteğiyle tamamen kaldırıldı
    }

    fun onBannerAdLoaded() {
        Log.d("EchoGame", "StartApp Banner loaded")
    }

    fun onBannerAdFailed(error: String) {
        Log.d("EchoGame", "StartApp Banner info: $error")
    }

    fun clearEchoesAction() {
        clearAllEchoes()
        showToast("Yankılar temizlendi!")
    }

    fun useHint(onTriggerAd: () -> Unit = {}) {
        val state = _uiState.value
        if (state.isHintActive) {
            showToast("💡 İpucu zaten bu bölüm için aktif! Numaralandırılmış rotayı takip edin.")
            return
        }
        _uiState.update {
            it.copy(
                isHintPurchaseDialogVisible = true,
                hintAdsWatched = prefs.hintAdsWatched
            )
        }
    }

    fun dismissHintPurchaseDialog() {
        _uiState.update { it.copy(isHintPurchaseDialogVisible = false) }
    }

    fun unlockHintWithDiamonds() {
        if (prefs.useDiamond(5)) {
            _uiState.update {
                it.copy(
                    isHintActive = true,
                    diamonds = prefs.diamonds,
                    isHintPurchaseDialogVisible = false,
                    gameStatus = GameStatus.PLAYING
                )
            }
            HarmonicAudioEngine.playVictoryCascade()
            triggerHapticClick()
            val u = _uiState.value.authenticatedUser
            if (u.isNotBlank()) {
                viewModelScope.launch {
                    authRepo.saveActiveUserProgress(
                        username = u,
                        currentLevelIndex = _uiState.value.currentLevelIndex,
                        completedLevels = prefs.getCompletedLevels(),
                        tokens = prefs.tokens,
                        echoBreakers = prefs.echoBreakers,
                        coins = prefs.coins,
                        diamonds = prefs.diamonds,
                        totalEchoes = prefs.totalEchoes,
                        totalStars = _uiState.value.totalStars
                    )
                }
            }
            showToast("💡 İpucu Açıldı! 5 Elmas harcandı ve rehber rota açıldı.")
        } else {
            showToast("Yetersiz Elmas! (Mevcut: ${_uiState.value.diamonds} Elmas. 5 Elmas gereklidir)")
        }
    }

    fun unlockHintWithToken() {
        if (prefs.useToken()) {
            _uiState.update {
                it.copy(
                    isHintActive = true,
                    tokens = prefs.tokens,
                    isHintPurchaseDialogVisible = false,
                    gameStatus = GameStatus.PLAYING
                )
            }
            HarmonicAudioEngine.playVictoryCascade()
            triggerHapticClick()
            showToast("💡 İpucu Açıldı! 1 İpucu Jetonu kullanıldı.")
        } else {
            showToast("Yetersiz Jeton! 3 reklam veya 5 elmas ile de açabilirsiniz.")
        }
    }

    fun acceptKvkkConsent() {
        prefs.isKvkkConsentAccepted = true
        _uiState.update {
            it.copy(
                isKvkkConsentAccepted = true,
                screenState = if (it.isAuthenticated || (prefs.rememberMe && prefs.authenticatedUsername.isNotEmpty())) {
                    ScreenState.MAIN_MENU
                } else {
                    ScreenState.LOGIN
                }
            )
        }
    }

    fun restartLevel(clearEchoes: Boolean = false) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                visitedNodeIds = emptyList(),
                collectedKeyIds = emptySet(),
                currentStrokeSegments = emptyList(),
                currentPointerPos = null,
                isDrawing = false,
                echoes = if (clearEchoes) emptyList() else it.echoes,
                echoCountForLevel = if (clearEchoes) 0 else it.echoCountForLevel,
                gameStatus = GameStatus.PLAYING,
                isGameOverTimeUpDialogVisible = false,
                levelStartTimeMs = System.currentTimeMillis(),
                candyParticles = emptyList(),
                candyCallout = null,
                nodes = it.nodes.map { n -> n.copy(connected = false) }
            )
        }
        startLevelSequence(state.level.levelId, isRestart = clearEchoes)
    }

    fun clearAllEchoes() {
        if (_uiState.value.echoes.isNotEmpty()) {
            HarmonicAudioEngine.playBrokenRedLine()
        }
        _uiState.update {
            it.copy(
                echoes = emptyList(),
                echoCountForLevel = 0,
                gameStatus = GameStatus.PLAYING
            )
        }
    }

    fun nextLevel() {
        HarmonicAudioEngine.playNextLevel()
        val nextIdx = _uiState.value.currentLevelIndex + 1
        if (nextIdx < LevelCatalog.TOTAL_LEVELS) {
            startPlayingLevel(nextIdx)
        } else {
            HarmonicAudioEngine.playWinAllLevels()
            showToast("Tebrikler! 100 bölümün tamamını fethettiniz! Büyük Zirve Tamamlandı!")
            returnToMainMenu()
        }
    }

    fun resetAllGameProgress() {
        viewModelScope.launch {
            repo.resetAllProgress()
            prefs.resetAllProgress()
            loadSavedPreferences()
            startPlayingLevel(0)
            returnToMainMenu()
            showToast("Tüm ilerleme sıfırlandı! 1. Bölümden başlıyorsunuz.")
        }
    }

    // --- Customization & Settings ---

    fun setStrokeTheme(theme: StrokeTheme) {
        prefs.strokeThemeName = theme.name
        _uiState.update { it.copy(strokeTheme = theme) }
    }

    fun setEchoTheme(theme: EchoTheme) {
        prefs.echoThemeName = theme.name
        _uiState.update { it.copy(echoTheme = theme) }
    }

    fun toggleSound(enabled: Boolean) {
        prefs.soundEnabled = enabled
        HarmonicAudioEngine.isSoundEnabled = enabled
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        prefs.hapticsEnabled = enabled
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }

    fun exchangeDiamondsForTokens(diamonds: Int, tokens: Int) {
        if (prefs.useDiamond(diamonds)) {
            prefs.addTokens(tokens)
            _uiState.update {
                it.copy(
                    diamonds = prefs.diamonds,
                    tokens = prefs.tokens
                )
            }
            saveCurrentUserProgress()
            HarmonicAudioEngine.playVictoryCascade()
            showToast("$tokens adet İpucu Jetonu takas edildi!")
        } else {
            showToast("Yetersiz Elmas! Reklam izleyerek elmas kazanabilirsiniz.")
        }
    }

    fun exchangeDiamondsForBreakers(diamonds: Int, breakers: Int) {
        if (prefs.useDiamond(diamonds)) {
            prefs.addBreakers(breakers)
            _uiState.update {
                it.copy(
                    diamonds = prefs.diamonds,
                    echoBreakers = prefs.echoBreakers
                )
            }
            saveCurrentUserProgress()
            HarmonicAudioEngine.playVictoryCascade()
            showToast("$breakers adet Matkap Lazeri takas edildi!")
        } else {
            showToast("Yetersiz Elmas! Reklam izleyerek elmas kazanabilirsiniz.")
        }
    }

    fun exchangeCoinsForBreakers(coinsCost: Int, breakers: Int) {
        if (prefs.useCoins(coinsCost)) {
            prefs.addBreakers(breakers)
            _uiState.update {
                it.copy(
                    coins = prefs.coins,
                    echoBreakers = prefs.echoBreakers
                )
            }
            saveCurrentUserProgress()
            HarmonicAudioEngine.playVictoryCascade()
            showToast("$breakers adet Matkap Lazeri alındı!")
        } else {
            showToast("Yetersiz Altın! Reklam izleyerek altın kazanabilirsiniz.")
        }
    }

    fun purchaseTokens(amount: Int) {
        prefs.addTokens(amount)
        _uiState.update { it.copy(tokens = prefs.tokens) }
        saveCurrentUserProgress()
        showToast("$amount adet İpucu Jetonu eklendi!")
    }

    fun purchaseBreakers(amount: Int) {
        prefs.addBreakers(amount)
        _uiState.update { it.copy(echoBreakers = prefs.echoBreakers) }
        saveCurrentUserProgress()
        showToast("$amount adet Matkap Lazeri eklendi!")
    }

    fun saveCurrentUserProgress() {
        val u = _uiState.value.authenticatedUser
        if (u.isNotBlank()) {
            viewModelScope.launch {
                authRepo.saveActiveUserProgress(
                    username = u,
                    currentLevelIndex = _uiState.value.currentLevelIndex,
                    completedLevels = prefs.getCompletedLevels(),
                    tokens = prefs.tokens,
                    echoBreakers = prefs.echoBreakers,
                    totalEchoes = prefs.totalEchoes,
                    totalStars = _uiState.value.totalStars
                )
            }
        }
    }

    // Modal Visibilities
    fun setLevelSelectVisible(visible: Boolean) { _uiState.update { it.copy(isLevelSelectVisible = visible) } }
    fun setShopVisible(visible: Boolean) { _uiState.update { it.copy(isShopDialogVisible = visible) } }
    fun setSettingsVisible(visible: Boolean) { _uiState.update { it.copy(isSettingsDialogVisible = visible) } }
    fun setSkinsVisible(visible: Boolean) { _uiState.update { it.copy(isSkinsDialogVisible = visible) } }
    fun setDailyQuestsVisible(visible: Boolean) {
        if (visible) refreshDailySystems()
        _uiState.update { it.copy(isDailyQuestsDialogVisible = visible) }
    }
    fun setDailyLoginVisible(visible: Boolean) {
        if (visible) refreshDailySystems()
        _uiState.update { it.copy(isDailyLoginDialogVisible = visible) }
    }
    fun setChestVisible(visible: Boolean) {
        if (visible) refreshDailySystems()
        _uiState.update { it.copy(isChestDialogVisible = visible) }
    }

    // --- Game Systems: Daily Quests, Login Calendar, Mystery Chest ---
    fun refreshDailySystems() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.checkAndResetDailyQuests(todayStr)

        // 1. Daily Quests (100% connected to real game activity)
        val q1 = DailyQuest(
            id = 1,
            title = "3 Bölüm Çöz",
            description = "Bugün 3 farklı bölümü başarıyla tamamla",
            current = prefs.levelsCompletedToday,
            target = 3,
            rewardTokens = 2,
            rewardBreakers = 0,
            isClaimed = prefs.isQuestClaimed(1)
        )
        val q2 = DailyQuest(
            id = 2,
            title = "Yıldız Avcısı",
            description = "Bugün toplam en az 6 yıldız topla",
            current = prefs.starsEarnedToday,
            target = 6,
            rewardTokens = 0,
            rewardBreakers = 1,
            isClaimed = prefs.isQuestClaimed(2)
        )
        val q3 = DailyQuest(
            id = 3,
            title = "Günün Özel Bulmacası",
            description = "Bugünün özel eko bulmacasını çöz",
            current = if (prefs.dailyChallengeCompletedToday) 1 else 0,
            target = 1,
            rewardTokens = 3,
            rewardBreakers = 0,
            isClaimed = prefs.isQuestClaimed(3)
        )
        val q4 = DailyQuest(
            id = 4,
            title = "Kusursuz Çizim (0 Yankı)",
            description = "En az 2 bölümü tek bir hata/yankı yapmadan bitir",
            current = prefs.flawlessLevelsToday,
            target = 2,
            rewardTokens = 2,
            rewardBreakers = 1,
            isClaimed = prefs.isQuestClaimed(4)
        )
        val quests = listOf(q1, q2, q3, q4)
        val unclaimedCount = quests.count { it.isCompleted && !it.isClaimed }

        // 2. Daily Login Streak (7-day calendar)
        val lastClaim = prefs.lastLoginClaimDate
        val isClaimedToday = lastClaim == todayStr
        val currentStreak = prefs.loginStreak

        val streakToUse: Int
        if (!isClaimedToday && lastClaim.isNotEmpty()) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            if (lastClaim != yesterdayStr) {
                // Streak broken due to missed day
                prefs.loginStreak = 1
                streakToUse = 1
            } else {
                streakToUse = currentStreak
            }
        } else {
            streakToUse = currentStreak
        }

        val loginDays = (1..7).map { day ->
            val isClaimed = if (isClaimedToday) day <= streakToUse else day < streakToUse
            val isToday = if (isClaimedToday) false else day == streakToUse
            val isPast = day < streakToUse
            val (tok, brk) = when (day) {
                1 -> Pair(2, 0)
                2 -> Pair(0, 1)
                3 -> Pair(3, 0)
                4 -> Pair(0, 2)
                5 -> Pair(5, 0)
                6 -> Pair(0, 3)
                else -> Pair(10, 5) // Day 7
            }
            DailyLoginDay(
                dayNumber = day,
                title = when (day) {
                    1 -> "+2 Jeton"
                    2 -> "+1 Matkap"
                    3 -> "+3 Jeton"
                    4 -> "+2 Matkap"
                    5 -> "+5 Jeton"
                    6 -> "+3 Matkap"
                    else -> "Büyük Ödül"
                },
                tokens = tok,
                breakers = brk,
                isLegendary = day == 7,
                isClaimed = isClaimed,
                isToday = isToday,
                isPast = isPast
            )
        }

        // 3. Mystery Echo Chest
        val freeChestAvail = prefs.isFreeChestAvailable(todayStr)
        val adChestsOpened = prefs.getAdChestsOpenedToday(todayStr)
        val adRemaining = (2 - adChestsOpened).coerceAtLeast(0)

        val unlocked = prefs.getUnlockedThemes()

        _uiState.update {
            it.copy(
                dailyQuests = quests,
                unclaimedQuestsCount = unclaimedCount,
                loginStreak = streakToUse,
                isLoginRewardAvailableToday = !isClaimedToday,
                loginDays = loginDays,
                isFreeChestAvailable = freeChestAvail,
                adChestsRemainingToday = adRemaining,
                unlockedThemes = unlocked
            )
        }
    }

    fun claimDailyQuest(questId: Int) {
        val quest = _uiState.value.dailyQuests.firstOrNull { it.id == questId } ?: return
        if (!quest.isCompleted || quest.isClaimed) return

        prefs.setQuestClaimed(questId, true)
        if (quest.rewardTokens > 0) prefs.addTokens(quest.rewardTokens)
        if (quest.rewardBreakers > 0) prefs.addBreakers(quest.rewardBreakers)

        HarmonicAudioEngine.playVictoryCascade()
        triggerHapticVictory()
        showToast("Görev Ödülü Alındı! (+${quest.rewardTokens} Jeton, +${quest.rewardBreakers} Matkap)")
        refreshDailySystems()
        _uiState.update {
            it.copy(
                tokens = prefs.tokens,
                echoBreakers = prefs.echoBreakers
            )
        }
    }

    fun claimDailyLoginReward() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (prefs.isLoginRewardClaimedToday(todayStr)) {
            showToast("Bugünkü giriş ödülü zaten alındı!")
            return
        }

        val streak = _uiState.value.loginStreak
        val (tok, brk) = when (streak) {
            1 -> Pair(2, 0)
            2 -> Pair(0, 1)
            3 -> Pair(3, 0)
            4 -> Pair(0, 2)
            5 -> Pair(5, 0)
            6 -> Pair(0, 3)
            else -> Pair(10, 5)
        }

        prefs.addTokens(tok)
        if (brk > 0) prefs.addBreakers(brk)
        if (streak == 7) {
            prefs.unlockTheme("GOLDEN_PULSE")
        }

        prefs.lastLoginClaimDate = todayStr
        prefs.loginStreak = if (streak >= 7) 1 else streak + 1

        HarmonicAudioEngine.playVictoryCascade()
        triggerHapticVictory()

        showToast("Gün $streak Ödülü Alındı! (+$tok Jeton${if (brk > 0) ", +$brk Matkap Lazeri" else ""})")
        refreshDailySystems()
        _uiState.update {
            it.copy(
                tokens = prefs.tokens,
                echoBreakers = prefs.echoBreakers
            )
        }
    }

    fun openEchoChest(isAd: Boolean) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (!isAd) {
            if (!prefs.isFreeChestAvailable(todayStr)) {
                showToast("Bugünkü ücretsiz sandık zaten açıldı!")
                return
            }
            prefs.lastFreeChestDate = todayStr
        } else {
            val opened = prefs.getAdChestsOpenedToday(todayStr)
            if (opened >= 2) {
                showToast("Bugünkü reklamlı sandık limiti doldu!")
                return
            }
            prefs.incrementAdChestOpened(todayStr)
        }

        val roll = (1..100).random()
        val reward: ChestReward = when {
            roll <= 60 -> {
                val t = (2..3).random()
                prefs.addTokens(t)
                ChestReward(
                    rarity = ThemeRarity.COMMON,
                    title = "Yaygın Yankı Parçacığı",
                    subtitle = "$t İpucu Jetonu kazandınız!",
                    tokens = t,
                    breakers = 0
                )
            }
            roll <= 85 -> {
                prefs.addTokens(1)
                prefs.addBreakers(1)
                ChestReward(
                    rarity = ThemeRarity.RARE,
                    title = "Nadir Lazer Kristali",
                    subtitle = "1 İpucu Jetonu & 1 Matkap Lazeri kazandınız!",
                    tokens = 1,
                    breakers = 1
                )
            }
            roll <= 97 -> {
                prefs.addTokens(5)
                prefs.addBreakers(2)
                prefs.unlockTheme("AURORA_EMERALD")
                ChestReward(
                    rarity = ThemeRarity.EPIC,
                    title = "Epik Kozmik Kasa",
                    subtitle = "5 Jeton, 2 Matkap ve 'Kutup Zümrüdü' Teması açıldı!",
                    tokens = 5,
                    breakers = 2,
                    unlockedThemeName = "Kutup Zümrüdü"
                )
            }
            else -> {
                prefs.addTokens(10)
                prefs.addBreakers(4)
                prefs.unlockTheme("GOLDEN_PULSE")
                ChestReward(
                    rarity = ThemeRarity.LEGENDARY,
                    title = "Efsanevi Omega Sandığı!",
                    subtitle = "10 Jeton, 4 Matkap ve Efsanevi 'Altın Lazer' Teması açıldı!",
                    tokens = 10,
                    breakers = 4,
                    unlockedThemeName = "Altın Lazer"
                )
            }
        }

        HarmonicAudioEngine.playVictoryCascade()
        triggerHapticVictory()

        refreshDailySystems()
        _uiState.update {
            it.copy(
                tokens = prefs.tokens,
                echoBreakers = prefs.echoBreakers,
                lastOpenedChestReward = reward
            )
        }
    }

    fun dismissChestReward() {
        _uiState.update { it.copy(lastOpenedChestReward = null) }
    }

    fun claimVictoryDoubleReward() {
        if (_uiState.value.isDoubleRewardClaimedThisLevel) return
        prefs.addTokens(2)
        prefs.addBreakers(1)
        HarmonicAudioEngine.playVictoryCascade()
        triggerHapticVictory()
        _uiState.update {
            it.copy(
                tokens = prefs.tokens,
                echoBreakers = prefs.echoBreakers,
                isDoubleRewardClaimedThisLevel = true
            )
        }
        showToast("2X Ödül Alındı! (+2 Jeton & +1 Matkap Lazeri)")
    }

    private fun findNodeAtPoint(point: Point, nodes: List<Node>): Node? {
        return nodes
            .filter { it.toPoint().distanceTo(point) <= nodeHitRadius }
            .minByOrNull { it.toPoint().distanceTo(point) }
    }

    private fun findTargetNodeForDrag(
        point: Point,
        lastNode: Node,
        nodes: List<Node>,
        visitedNodeIds: List<Int>,
        expectedNextId: Int
    ): Node? {
        val distToLast = point.distanceTo(lastNode.toPoint())

        // 1. Highest priority: expected next target node in sequence
        val expectedNode = nodes.firstOrNull { it.id == expectedNextId }
        if (expectedNode != null) {
            val distToExpected = point.distanceTo(expectedNode.toPoint())
            val segLen = lastNode.toPoint().distanceTo(expectedNode.toPoint())
            val captureRadius = (segLen * 0.48f).coerceIn(16f, 36f)
            if (distToExpected <= captureRadius && distToExpected < distToLast) {
                return expectedNode
            }
        }

        // 2. Returning specifically to previous node (User request: "örneğin önceki düğüme geri dönmesi bir yankı sayılsın")
        // User request: "bir ışın üzerine ışın binince yankı sayılmasın"
        // Crossing or drawing across existing beams is freely permitted without triggering echoes.
        if (visitedNodeIds.size >= 2) {
            val prevId = visitedNodeIds[visitedNodeIds.size - 2]
            val prevNode = nodes.firstOrNull { it.id == prevId }
            if (prevNode != null) {
                val distToPrev = point.distanceTo(prevNode.toPoint())
                // Only triggers if player explicitly retreats back onto the previous node
                if (distToPrev <= 16f && distToLast > 22f) {
                    return prevNode
                }
            }
        }

        return null
    }

    private fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
        toastResetJob?.cancel()
        toastResetJob = viewModelScope.launch {
            delay(2400)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    private fun triggerHapticClick() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    private fun triggerProximityHaptic() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    private fun triggerCollisionFeedback() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 40, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    private fun triggerHapticVictory() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40, 60, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (_: Exception) {}
    }

    // --- Leaderboard & Player Profile Systems ---

    fun openLeaderboard() {
        _uiState.update { it.copy(isLeaderboardDialogVisible = true) }
        loadLeaderboardData()
    }

    fun refreshLeaderboard() {
        loadLeaderboardData()
    }

    private fun loadLeaderboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLeaderboardRefreshing = true) }
            val username = _uiState.value.authenticatedUser.ifBlank { "Oyuncu" }

            // Submit current trophies if any
            if (prefs.trophies > 0) {
                lootLocker.submitScore(username, prefs.trophies)
            }

            // Fetch live scores from LootLocker
            val remoteResult = lootLocker.fetchLeaderboard(count = 50, currentUsername = username)
            val finalPlayers = if (remoteResult.isSuccess && !remoteResult.getOrNull().isNullOrEmpty()) {
                val remoteList = remoteResult.getOrNull()!!
                val hasMe = remoteList.any { it.isCurrentUser }
                if (!hasMe) {
                    val me = com.example.model.LeaderboardPlayer(
                        id = "current_user",
                        rank = remoteList.size + 1,
                        username = username,
                        avatarEmoji = "⚡",
                        title = if (prefs.trophies >= 1000) "🏆 Yankı Ustası" else "Ses Kaşifi",
                        trophies = prefs.trophies,
                        totalEchoes = prefs.totalEchoes,
                        totalPlayTimeSec = prefs.totalPlayTimeSec,
                        maxCombo = prefs.maxCombo,
                        completedLevelsCount = prefs.getCompletedLevels().size,
                        isCurrentUser = true,
                        levelRecords = prefs.getLevelRecords()
                    )
                    (remoteList + me).sortedByDescending { it.trophies }.mapIndexed { idx, p -> p.copy(rank = idx + 1) }
                } else {
                    remoteList
                }
            } else {
                // Fallback to local accounts and curated global competitors if offline/not configured
                authRepo.getLeaderboardPlayers()
            }

            _uiState.update {
                it.copy(
                    leaderboardPlayers = finalPlayers,
                    isLeaderboardRefreshing = false
                )
            }
        }
    }

    fun closeLeaderboard() {
        _uiState.update { it.copy(isLeaderboardDialogVisible = false) }
    }

    fun openPlayerProfile(player: com.example.model.LeaderboardPlayer) {
        _uiState.update {
            it.copy(
                selectedPlayerProfile = player,
                isProfileDialogVisible = true
            )
        }
    }

    fun closePlayerProfile() {
        _uiState.update {
            it.copy(
                isProfileDialogVisible = false,
                selectedPlayerProfile = null
            )
        }
    }

    fun openMyProfile() {
        viewModelScope.launch {
            val list = authRepo.getLeaderboardPlayers()
            val me = list.find { it.isCurrentUser } ?: com.example.model.LeaderboardPlayer(
                id = "current_user",
                rank = 1,
                username = _uiState.value.authenticatedUser.ifBlank { "Oyuncu" },
                avatarEmoji = "⚡",
                title = if (prefs.trophies >= 1000) "🏆 Yankı Ustası" else "Ses Kaşifi",
                trophies = prefs.trophies,
                totalEchoes = prefs.totalEchoes,
                totalPlayTimeSec = prefs.totalPlayTimeSec,
                maxCombo = prefs.maxCombo,
                completedLevelsCount = prefs.getCompletedLevels().size,
                isCurrentUser = true,
                levelRecords = prefs.getLevelRecords()
            )
            _uiState.update {
                it.copy(
                    selectedPlayerProfile = me,
                    isProfileDialogVisible = true
                )
            }
        }
    }

    fun setSupportVisible(visible: Boolean) {
        _uiState.update { it.copy(isSupportDialogVisible = visible) }
    }

    fun updateProfile(avatarUri: String, customTitle: String) {
        viewModelScope.launch {
            authRepo.updateUserProfile(avatarUri, customTitle)
            val updatedPlayers = authRepo.getLeaderboardPlayers()
            val myPlayer = updatedPlayers.firstOrNull { it.isCurrentUser }
            _uiState.update {
                it.copy(
                    userAvatarUri = avatarUri,
                    userCustomTitle = customTitle,
                    leaderboardPlayers = updatedPlayers,
                    selectedPlayerProfile = if (it.selectedPlayerProfile?.isCurrentUser == true) myPlayer else it.selectedPlayerProfile
                )
            }
            showToast("Profil başarıyla güncellendi!")
        }
    }
}
