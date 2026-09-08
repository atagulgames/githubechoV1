package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dialogs.LevelMechanicIntroDialog
import com.example.ui.dialogs.LevelRuleOnboardingOverlay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameStatus
import com.example.model.ScreenState
import com.example.ui.components.EchoBottomHUD
import com.example.ui.components.EchoCanvas
import com.example.ui.components.EchoToastBanner
import com.example.ui.components.EchoTopHUD
import com.example.ui.dialogs.AdLoadingDialog
import com.example.ui.dialogs.ChestDialog
import com.example.ui.dialogs.DailyLoginDialog
import com.example.ui.dialogs.DailyQuestsDialog
import com.example.ui.dialogs.DeadlockDialog
import com.example.ui.dialogs.LeaderboardDialog
import com.example.ui.dialogs.LevelSelectDialog
import com.example.ui.dialogs.PlayerProfileDialog
import com.example.ui.dialogs.RewardClaimedDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.ShopDialog
import com.example.ui.dialogs.SkinsDialog
import com.example.ui.dialogs.SupportDialog
import com.example.ui.dialogs.VictoryDialog
import com.example.ui.auth.LoginScreen
import com.example.ui.intro.IntroVideoScreen
import com.example.ui.menu.EchoMainMenu
import com.example.viewmodel.EchoGameViewModel

@Composable
fun EchoGameScreen(
    viewModel: EchoGameViewModel = viewModel(),
    onShowRewardedAd: (String) -> Unit = {},
    onShowInterstitialAd: (() -> Unit) -> Unit = { it() },
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle system back button to return to Main Menu from game (disabled during Intro, Legal Consent and Login)
    BackHandler(
        enabled = state.screenState != ScreenState.MAIN_MENU &&
                state.screenState != ScreenState.INTRO &&
                state.screenState != ScreenState.LEGAL_CONSENT &&
                state.screenState != ScreenState.LOGIN
    ) {
        viewModel.returnToMainMenu()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("echo_game_scaffold"),
        containerColor = if (state.isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.screenState) {
                ScreenState.INTRO -> {
                    // Direct intro.mp4 video playback as specified by user
                    IntroVideoScreen(
                        onIntroFinished = { viewModel.finishIntro() }
                    )
                }

                ScreenState.LEGAL_CONSENT -> {
                    // First intro -> then user agreement acceptance
                    val activity = context as? Activity
                    com.example.ui.dialogs.LegalConsentScreen(
                        onAccept = {
                            viewModel.acceptKvkkConsent()
                        },
                        onDecline = {
                            activity?.finishAffinity()
                        }
                    )
                }

                ScreenState.LOGIN -> {
                    // Mandatory authentication gatekeeper: Main Menu strictly inaccessible until verified
                    LoginScreen(
                        initialUsername = state.authenticatedUser,
                        initialRememberMe = state.rememberMe,
                        onLoginSuccess = { username, rememberMe ->
                            viewModel.onLoginSuccess(username, rememberMe)
                        }
                    )
                }

                ScreenState.MAIN_MENU -> {
                    EchoMainMenu(
                        state = state,
                        onPlay = { viewModel.startPlayingLevel() },
                        onDailyChallenge = { viewModel.startDailyChallenge() },
                        onOpenLevelSelect = { viewModel.setLevelSelectVisible(true) },
                        onOpenShop = { viewModel.setShopVisible(true) },
                        onOpenSkins = { viewModel.setSkinsVisible(true) },
                        onOpenSettings = { viewModel.setSettingsVisible(true) },
                        onOpenSupport = { viewModel.setSupportVisible(true) },
                        onOpenLeaderboard = { viewModel.openLeaderboard() },
                        onOpenMyProfile = { viewModel.openMyProfile() },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onOpenDailyQuests = { viewModel.setDailyQuestsVisible(true) },
                        onOpenDailyLogin = { viewModel.setDailyLoginVisible(true) },
                        onOpenChest = { viewModel.setChestVisible(true) },
                        onWatchRewardedAd = {
                            if (viewModel.canClaimReward("REWARD_DAILY")) {
                                onShowRewardedAd("REWARD_DAILY")
                            } else {
                                viewModel.notifyRewardOnCooldown("REWARD_DAILY")
                            }
                        },
                        onBannerAdLoaded = { viewModel.onBannerAdLoaded() },
                        onBannerAdFailed = { err -> viewModel.onBannerAdFailed(err) }
                    )
                }

                ScreenState.PLAYING_LEVEL, ScreenState.DAILY_CHALLENGE -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top HUD
                        EchoTopHUD(
                            state = state,
                            onBackToMenu = { viewModel.returnToMainMenu() },
                            onOpenLevelSelect = { viewModel.setLevelSelectVisible(true) },
                            onOpenShop = { viewModel.setShopVisible(true) },
                            onAddTimeWithAd = { onShowRewardedAd("EXTRA_TIME_15S") },
                            onOpenMechanicGuide = { viewModel.showMechanicGuide() },
                            onToggleGhostRace = { viewModel.toggleGhostRace() }
                        )

                        // Interactive Canvas with mathematical collision engine
                        EchoCanvas(
                            state = state,
                            onPointerDown = viewModel::onPointerDown,
                            onPointerMove = viewModel::onPointerMove,
                            onPointerUp = viewModel::onPointerUp,
                            modifier = Modifier.weight(1f)
                        )

                        // Bottom Action Bar: Reset, Clear Echoes, Power-ups, Hint
                        EchoBottomHUD(
                            state = state,
                            onReset = { viewModel.restartLevel(clearEchoes = false) },
                            onClearEchoes = { viewModel.clearAllEchoes() },
                            onUseBreaker = { viewModel.useEchoBreaker() },
                            onActivateShrinker = {
                                viewModel.activateEchoShrinker()
                            },
                            onHint = {
                                viewModel.useHint()
                            }
                        )

                        // Bottom docked Banner Ad - always visible
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (state.isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.ads.StartAppBannerView()
                        }
                    }
                }
            }

            // Toast feedback banner (top-center)
            EchoToastBanner(
                message = state.toastMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            )

            // 1. Victory Dialog
            if (state.gameStatus == GameStatus.VICTORY) {
                var isNextLevelTransitioning by remember(state.level.levelId) { mutableStateOf(false) }

                VictoryDialog(
                    levelId = state.level.levelId,
                    echoCount = state.echoCountForLevel,
                    parEchoes = state.level.parEchoes,
                    trophyBreakdown = state.lastTrophyRewardBreakdown,
                    isDoubleClaimed = state.isDoubleRewardClaimedThisLevel,
                    onClaimDoubleReward = { onShowRewardedAd("DOUBLE_REWARD") },
                    onNextLevel = {
                        if (!isNextLevelTransitioning) {
                            isNextLevelTransitioning = true
                            if (viewModel.shouldShowInterstitialOnNextLevel()) {
                                onShowInterstitialAd {
                                    viewModel.onInterstitialAdShownOrSkipped(true)
                                    viewModel.nextLevel()
                                }
                            } else {
                                viewModel.onInterstitialAdShownOrSkipped(false)
                                viewModel.nextLevel()
                            }
                        }
                    },
                    onReplay = {
                        viewModel.restartLevel(clearEchoes = true)
                    }
                )
            }

            // 2. Deadlock / Time-Up Game Over Dialog
            if (state.isGameOverTimeUpDialogVisible || state.gameStatus == GameStatus.DEADLOCK) {
                DeadlockDialog(
                    echoCount = state.echoCountForLevel,
                    isTimeUp = state.isGameOverTimeUpDialogVisible,
                    onClearWithAd = if (!state.isGameOverTimeUpDialogVisible) { { onShowRewardedAd("CLEAR_ECHOES") } } else null,
                    onAddTimeWithAd = if (state.isGameOverTimeUpDialogVisible) { { onShowRewardedAd("EXTRA_TIME_15S") } } else null,
                    onRestartLevel = { viewModel.restartLevel(clearEchoes = true) }
                )
            }

            // 2.5. Level Rule Onboarding Overlay & Milestone System (100 Level Rules)
            if (state.isLevelMechanicIntroVisible) {
                val targetLevelId = state.activeLevelRule?.levelId ?: state.level.levelId
                LevelRuleOnboardingOverlay(
                    currentLevelId = targetLevelId,
                    isDarkTheme = state.isDarkTheme,
                    isTurkish = true,
                    initialEncyclopediaMode = state.isRuleEncyclopediaOpen,
                    onToggleAutoShowForTier = { enabled ->
                        viewModel.setAutoShowLevelRules(enabled)
                    },
                    onDismiss = { viewModel.dismissMechanicGuide() }
                )
            }

            // 3. Shop Dialog (Rewarded Ad Hub & Diamond/Coin Exchange)
            if (state.isShopDialogVisible) {
                ShopDialog(
                    currentTokens = state.tokens,
                    currentDiamonds = state.diamonds,
                    currentBreakers = state.echoBreakers,
                    multiAdWatchCount = state.multiAdWatchCount,
                    doubleTrophiesExpiresAt = state.doubleTrophiesExpiresAt,
                    infiniteBreakersExpiresAt = state.infiniteBreakersExpiresAt,
                    radiusShrinkerExpiresAt = state.radiusShrinkerExpiresAt,
                    diamondRewardCooldownSeconds = state.diamondRewardCooldownSeconds,
                    coinRewardCooldownSeconds = state.coinRewardCooldownSeconds,
                    breakerRewardCooldownSeconds = state.breakerRewardCooldownSeconds,
                    megaChestRewardCooldownSeconds = state.megaChestRewardCooldownSeconds,
                    isDarkTheme = state.isDarkTheme,
                    onWatchRewardedAd = { rewardType ->
                        if (viewModel.canClaimReward(rewardType)) {
                            onShowRewardedAd(rewardType)
                        } else {
                            viewModel.notifyRewardOnCooldown(rewardType)
                        }
                    },
                    onExchangeDiamondsForTokens = { diamonds, tokens ->
                        viewModel.exchangeDiamondsForTokens(diamonds, tokens)
                    },
                    onExchangeDiamondsForBreakers = { diamonds, breakers ->
                        viewModel.exchangeDiamondsForBreakers(diamonds, breakers)
                    },
                    onDismiss = { viewModel.setShopVisible(false) }
                )
            }

            // 4. Level Selector Dialog (100 levels from Room)
            if (state.isLevelSelectVisible) {
                LevelSelectDialog(
                    levels = state.allLevels,
                    currentLevelIndex = state.currentLevelIndex,
                    completedLevels = state.completedLevels,
                    isDarkTheme = state.isDarkTheme,
                    onSelectLevel = { idx ->
                        viewModel.setLevelSelectVisible(false)
                        viewModel.startPlayingLevel(idx)
                    },
                    onOpenRulesGuide = {
                        viewModel.setLevelSelectVisible(false)
                        viewModel.openRuleEncyclopedia()
                    },
                    onDismiss = { viewModel.setLevelSelectVisible(false) }
                )
            }

            // 6. Settings Dialog
            if (state.isSettingsDialogVisible) {
                SettingsDialog(
                    soundEnabled = state.soundEnabled,
                    hapticsEnabled = state.hapticsEnabled,
                    testAdsEnabled = state.testAdsEnabled,
                    isDarkTheme = state.isDarkTheme,
                    currentLanguage = state.language,
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                    onToggleSound = { viewModel.toggleSound(it) },
                    onToggleHaptics = { viewModel.toggleHaptics(it) },
                    onToggleTestAds = { viewModel.toggleTestAds(it) },
                    onResetAllProgress = { viewModel.resetAllGameProgress() },
                    onOpenSupport = {
                        viewModel.setSettingsVisible(false)
                        viewModel.setSupportVisible(true)
                    },
                    onLogout = { viewModel.logout() },
                    onDismiss = { viewModel.setSettingsVisible(false) }
                )
            }

            // 7. Skins / Themes Dialog
            if (state.isSkinsDialogVisible) {
                SkinsDialog(
                    currentStroke = state.strokeTheme,
                    currentEcho = state.echoTheme,
                    unlockedThemes = state.unlockedThemes,
                    totalStars = state.totalStars,
                    isDarkTheme = state.isDarkTheme,
                    onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                    onSelectStroke = { viewModel.setStrokeTheme(it) },
                    onSelectEcho = { viewModel.setEchoTheme(it) },
                    onDismiss = { viewModel.setSkinsVisible(false) }
                )
            }

            // 8. Daily Quests Dialog
            if (state.isDailyQuestsDialogVisible) {
                DailyQuestsDialog(
                    quests = state.dailyQuests,
                    onClaimQuest = { questId -> viewModel.claimDailyQuest(questId) },
                    onDismiss = { viewModel.setDailyQuestsVisible(false) }
                )
            }

            // 9. Daily Login Calendar Dialog
            if (state.isDailyLoginDialogVisible) {
                DailyLoginDialog(
                    days = state.loginDays,
                    currentStreak = state.loginStreak,
                    isRewardAvailableToday = state.isLoginRewardAvailableToday,
                    onClaimToday = { viewModel.claimDailyLoginReward() },
                    onDismiss = { viewModel.setDailyLoginVisible(false) }
                )
            }

            // 10. Mystery Echo Chest Dialog
            if (state.isChestDialogVisible || state.lastOpenedChestReward != null) {
                ChestDialog(
                    isFreeAvailable = state.isFreeChestAvailable,
                    adRemainingToday = state.adChestsRemainingToday,
                    reward = state.lastOpenedChestReward,
                    onOpenFree = { viewModel.openEchoChest(isAd = false) },
                    onOpenWithAd = {
                        onShowRewardedAd("OPEN_CHEST")
                        viewModel.openEchoChest(isAd = true)
                    },
                    onDismissReward = { viewModel.dismissChestReward() },
                    onDismiss = { viewModel.setChestVisible(false) }
                )
            }

            // 11. Ad Loading Overlay Dialog
            if (state.isAdLoading) {
                AdLoadingDialog(
                    onDismiss = { viewModel.setAdLoading(false) }
                )
            }

            // 12. Reward Claimed Celebration Dialog
            state.rewardClaimedData?.let { rewardInfo ->
                RewardClaimedDialog(
                    title = rewardInfo.title,
                    subtitle = rewardInfo.subtitle,
                    tokensAdded = rewardInfo.tokensAdded,
                    breakersAdded = rewardInfo.breakersAdded,
                    currentTokens = state.tokens,
                    currentBreakers = state.echoBreakers,
                    onDismiss = { viewModel.dismissRewardClaimedDialog() }
                )
            }

            // 13. Global Leaderboard Dialog
            if (state.isLeaderboardDialogVisible) {
                LeaderboardDialog(
                    players = state.leaderboardPlayers,
                    isDarkTheme = state.isDarkTheme,
                    isRefreshing = state.isLeaderboardRefreshing,
                    onRefresh = { viewModel.refreshLeaderboard() },
                    onSelectPlayer = { player -> viewModel.openPlayerProfile(player) },
                    onViewMyProfile = { viewModel.openMyProfile() },
                    onDismiss = { viewModel.closeLeaderboard() }
                )
            }

            // 14. Player Profile Dialog (Kaç yankı ve süreyle tamamladığı detayları + fotoğraf ve unvan)
            if (state.isProfileDialogVisible && state.selectedPlayerProfile != null) {
                PlayerProfileDialog(
                    player = state.selectedPlayerProfile!!,
                    isDarkTheme = state.isDarkTheme,
                    onUpdateProfile = { avatarUri, customTitle ->
                        viewModel.updateProfile(avatarUri, customTitle)
                    },
                    onDismiss = { viewModel.closePlayerProfile() }
                )
            }

            // 15. Destek & İletişim Dialog (atagulgamesdestek@gmail.com)
            if (state.isSupportDialogVisible) {
                SupportDialog(
                    isDarkTheme = state.isDarkTheme,
                    onDismiss = { viewModel.setSupportVisible(false) }
                )
            }

            // 16. Hint Unlock Dialog (3 Reklam veya 5 Elmas)
            if (state.isHintPurchaseDialogVisible) {
                com.example.ui.dialogs.HintDialog(
                    hintAdsWatched = state.hintAdsWatched,
                    diamonds = state.diamonds,
                    tokens = state.tokens,
                    isDarkTheme = state.isDarkTheme,
                    onWatchAd = {
                        onShowRewardedAd("HINT_AD")
                    },
                    onUseDiamonds = {
                        viewModel.unlockHintWithDiamonds()
                    },
                    onUseToken = {
                        viewModel.unlockHintWithToken()
                    },
                    onDismiss = {
                        viewModel.dismissHintPurchaseDialog()
                    }
                )
            }

            // 17. Internet & Ad Blocker Gatekeeper Overlay
            if (state.isAdBlockerDetected) {
                val context = LocalContext.current
                com.example.ui.dialogs.AdBlockerWarningOverlay(
                    title = if (state.adBlockerTitle.isNotBlank()) state.adBlockerTitle else "Reklamlar ve İnternet Gerekli",
                    subtitle = if (state.adBlockerSubtitle.isNotBlank()) state.adBlockerSubtitle else "Uygulama çalıştırılamıyor",
                    details = state.adBlockerDetails,
                    isChecking = state.isCheckingAdBlocker,
                    onRecheck = { viewModel.checkInternetAndAdHealth() },
                    onExitApp = {
                        val activity = context as? Activity
                        activity?.finishAffinity()
                    }
                )
            }

            // 18. 5-Second Preview Countdown with Shake Animation (User: 5sn sayarken ekranını görebilelim)
            if (state.isPreviewActive && state.screenState == ScreenState.PLAYING_LEVEL) {
                PreviewCountdownShakeOverlay(
                    countdownSeconds = state.previewCountdownSeconds,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // 19. "Süre Başladı!" Shake Banner with party.mp3 (User: party.mp3 çalıp ekranda shake animasyonu ile Süre Başladı Yazsın)
            if (state.showDurationStartedBanner && state.screenState == ScreenState.PLAYING_LEVEL) {
                DurationStartedShakeBanner(
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun PreviewCountdownShakeOverlay(
    countdownSeconds: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "preview_shake")
    val shakeX by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeX"
    )
    val shakeY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeY"
    )

    // Transparent container so nodes and level canvas remain 100% visible and unblocked
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 76.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .graphicsLayer {
                    translationX = shakeX
                    translationY = shakeY
                }
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE0F172A),
                            Color(0xEE1E293B)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFF59E0B))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Text(
                text = "ÖN İZLEME - BULMACAYI İNCELE",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${countdownSeconds}sn",
                color = Color(0xFFFBBF24),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun DurationStartedShakeBanner(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "started_shake")
    val shakeX by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(45, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "startedShakeX"
    )
    val shakeY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(65, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "startedShakeY"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 76.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .graphicsLayer {
                    translationX = shakeX
                    translationY = shakeY
                }
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xEE10B981),
                            Color(0xEE059669)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 30.dp, vertical = 14.dp)
        ) {
            Text(
                text = "⚡ SÜRE BAŞLADI! ⚡",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "60 Saniyen Var!",
                color = Color(0xFFFEF08A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
