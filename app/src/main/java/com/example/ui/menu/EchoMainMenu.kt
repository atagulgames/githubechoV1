package com.example.ui.menu

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicAudioEngine
import com.example.audio.hapticClick
import com.example.data.LevelCatalog
import com.example.data.local.LevelEntity
import com.example.viewmodel.EchoUiState

@Composable
fun EchoMainMenu(
    state: EchoUiState,
    onPlay: () -> Unit,
    onDailyChallenge: () -> Unit = {},
    onOpenLevelSelect: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onOpenSkins: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenLeaderboard: () -> Unit = {},
    onOpenMyProfile: () -> Unit = {},
    onToggleDarkTheme: () -> Unit = {},
    onOpenDailyQuests: () -> Unit = {},
    onOpenDailyLogin: () -> Unit = {},
    onOpenChest: () -> Unit = {},
    onWatchRewardedAd: () -> Unit = {},
    onOpenCartoonStudio: () -> Unit = {},
    onOpenSeason2Info: () -> Unit = {},
    onOpenSeason2Themes: () -> Unit = {},
    onBannerAdLoaded: () -> Unit = {},
    onBannerAdFailed: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Start background music in main menu
    LaunchedEffect(Unit) {
        HarmonicAudioEngine.setIntroActive(false)
        HarmonicAudioEngine.startBgm()
    }

    val currentLevel = state.currentLevelIndex + 1

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_menu_screen"),
        color = Color(0xFF061864)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenHeight = maxHeight
            val isShortScreen = screenHeight < 720.dp
            val isVeryShort = screenHeight < 640.dp

            // Background infinite animations for flowing symbols & gentle ambient pulse
            val infiniteTransition = rememberInfiniteTransition(label = "MainMenuBackgroundFlow")
            val flowProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 60000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "MainMenuFlowProgress"
            )
            val pulseProgress by infiniteTransition.animateFloat(
                initialValue = 0.90f,
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "MainMenuPulseProgress"
            )

            // 1. Vibrant Royal Blue Deep Background Gradient & Flowing Cartoon Arcade Symbols
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF031454),
                        Color(0xFF06227D),
                        Color(0xFF0930A4),
                        Color(0xFF052074),
                        Color(0xFF020E3E)
                    )
                )
                drawRect(brush = gradient)
                // Draw animated flowing cartoon game symbols (Trophies, Crowns, Stars, Gamepads, Gems, Rockets, Lightning, Coins)
                drawFlowingCasualGameSymbols(
                    canvasSize = size,
                    flowProgress = flowProgress,
                    pulseProgress = pulseProgress
                )
            }

            // 2. Strict Fit Column (No Scroll - Everything strictly fits on screen)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = if (isVeryShort) 4.dp else 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP BAR: Settings Gear (Left) | Energy Pill (Center) | Coins Pill (Right)
                TopMenuNavBar(
                    energyText = "FULL",
                    coins = state.coins,
                    onOpenSettings = onOpenSettings,
                    onOpenShop = onOpenShop,
                    onRefillEnergy = onOpenShop
                )

                Spacer(modifier = Modifier.height(if (isVeryShort) 2.dp else 6.dp))

                // MID SECTION: Left Badges | Center Vertical Progression Path | Right Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT COLUMN: Gift Box + Coin Quest
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isShortScreen) 12.dp else 22.dp),
                        modifier = Modifier.width(84.dp)
                    ) {
                        val isTr = state.language == com.example.localization.Language.TR
                        SideBadgeItem(
                            title = if (isTr) "Hediye" else "Gift",
                            badgeType = BadgeType.GIFT,
                            testTag = "btn_badge_hediye",
                            showNotificationDot = state.isFreeChestAvailable,
                            onClick = onOpenChest
                        )

                        SideBadgeItem(
                            title = if (isTr) "Görevler" else "Quests",
                            badgeType = BadgeType.COIN_QUEST,
                            testTag = "btn_badge_jeton_gorevi",
                            notificationCount = state.unclaimedQuestsCount,
                            onClick = onOpenDailyQuests
                        )
                    }

                    // CENTER: Vertical Level Progress Nodes (Connected via yellow pipe)
                    VerticalLevelProgression(
                        currentLevel = currentLevel,
                        completedLevels = state.completedLevels,
                        allLevels = state.allLevels,
                        isCompact = isShortScreen,
                        onSelectLevel = onOpenLevelSelect
                    )

                    // RIGHT COLUMN: Daily Reward (Günlük Ödül) & Karikatür Stüdyosu
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isShortScreen) 12.dp else 22.dp),
                        modifier = Modifier.width(84.dp)
                    ) {
                        val isTr = state.language == com.example.localization.Language.TR
                        SideBadgeItem(
                            title = if (isTr) "Günlük Ödül" else "Daily Reward",
                            badgeType = BadgeType.DAILY_REWARD,
                            testTag = "btn_badge_gunluk_odul",
                            showNotificationDot = state.isLoginRewardAvailableToday,
                            onClick = onOpenDailyLogin
                        )

                        SideBadgeItem(
                            title = if (isTr) "Karikatür" else "Cartoon",
                            badgeType = BadgeType.CARTOON,
                            testTag = "btn_badge_karikatur",
                            onClick = onOpenCartoonStudio
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isVeryShort) 4.dp else 8.dp))

                // PLAY BUTTON & ENERGY COST BADGE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BigPlayButton(
                        onClick = onPlay,
                        isCompact = isVeryShort
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // "⚡ Enerji" Pill
                    EnergyCostPill(
                        onClick = onOpenShop
                    )
                }

                Spacer(modifier = Modifier.height(if (isVeryShort) 4.dp else 8.dp))

                // BOTTOM DOCK: "Quests" (Left) | "Levels" (Elevated Center) | "Leaderboard" (Right)
                BottomGameDock(
                    unclaimedQuestsCount = state.unclaimedQuestsCount,
                    isGuest = !state.isAuthenticated,
                    onOpenQuests = onOpenDailyQuests,
                    onOpenLevels = onOpenLevelSelect,
                    onOpenLeaderboard = onOpenLeaderboard
                )

                // Docked Live Yandex Banner Ad at bottom of Main Menu (RM-20228995-1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.ads.YandexBannerAdView(
                        onAdLoaded = onBannerAdLoaded,
                        onAdFailedToLoad = onBannerAdFailed
                    )
                }
            }
        }
    }
}

/* ========================================================================== */
/*                              TOP NAVBAR                                    */
/* ========================================================================== */

@Composable
private fun TopMenuNavBar(
    energyText: String,
    coins: Int,
    onOpenSettings: () -> Unit,
    onOpenShop: () -> Unit,
    onRefillEnergy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings Gear Icon (White 3D circular button)
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF0B216B).copy(alpha = 0.85f))
                .border(2.dp, Color(0xFF1E4FB8), CircleShape)
                .clickable(onClick = hapticClick(action = onOpenSettings))
                .testTag("menu_settings_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Energy Indicator Pill (Fire badge + "FULL" + red + button)
        Row(
            modifier = Modifier
                .height(38.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF071954))
                .border(2.dp, Color(0xFF0F328E), RoundedCornerShape(20.dp))
                .clickable(onClick = hapticClick(action = onRefillEnergy))
                .padding(start = 2.dp, end = 4.dp)
                .testTag("menu_energy_pill"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flame Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE51D24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = energyText,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Red '+' button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE51D24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Energy",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Coin Indicator Pill (3D Gold Coin + 1200 + orange + button)
        Row(
            modifier = Modifier
                .height(38.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF071954))
                .border(2.dp, Color(0xFF0F328E), RoundedCornerShape(20.dp))
                .clickable(onClick = hapticClick(action = onOpenShop))
                .padding(start = 4.dp, end = 4.dp)
                .testTag("menu_coins_pill"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Coin Graphic
            Canvas(modifier = Modifier.size(28.dp)) {
                // Outer gold coin
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFEA79), Color(0xFFFFB800), Color(0xFFD47C00))
                    ),
                    radius = size.minDimension / 2f
                )
                drawCircle(
                    color = Color(0xFF8B4700),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Inner ring
                drawCircle(
                    color = Color(0xFFFFE665),
                    radius = size.minDimension / 2.8f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "$coins",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Orange '+' button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Coins",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/* ========================================================================== */
/*                             SIDE BADGES                                    */
/* ========================================================================== */

private enum class BadgeType {
    GIFT, COIN_QUEST, DAILY_REWARD, CARTOON
}

@Composable
private fun SideBadgeItem(
    title: String,
    badgeType: BadgeType,
    testTag: String,
    showNotificationDot: Boolean = false,
    notificationCount: Int = 0,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.93f else 1.0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = hapticClick(action = onClick)
            )
            .testTag(testTag)
    ) {
        // Upper Icon Container
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            when (badgeType) {
                BadgeType.GIFT -> {
                    GiftBoxArt()
                }
                BadgeType.COIN_QUEST -> {
                    TrophyArt()
                }
                BadgeType.DAILY_REWARD -> {
                    DailyRewardChestArt()
                }
                BadgeType.CARTOON -> {
                    CartoonStudioBadgeArt()
                }
            }

            // Notification indicator
            if (showNotificationDot || notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(if (notificationCount > 0) 20.dp else 14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (notificationCount > 0) {
                        Text(
                            text = if (notificationCount > 9) "9+" else "$notificationCount",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Glossy Pill Label
        val badgeColor = when (badgeType) {
            BadgeType.GIFT -> Color(0xFFD4141E)
            BadgeType.COIN_QUEST -> Color(0xFF091F60)
            BadgeType.DAILY_REWARD -> Color(0xFF091F60)
            BadgeType.CARTOON -> Color(0xFF6A1B9A)
        }
        val borderColor = when (badgeType) {
            BadgeType.GIFT -> Color(0xFFFF6B6B)
            BadgeType.COIN_QUEST -> Color(0xFF2962FF)
            BadgeType.DAILY_REWARD -> Color(0xFF00B0FF)
            BadgeType.CARTOON -> Color(0xFFFFD54F)
        }

        Box(
            modifier = Modifier
                .shadow(3.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(badgeColor)
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// 3D Cartoon Red Gift Box Art
@Composable
private fun GiftBoxArt() {
    Canvas(modifier = Modifier.size(54.dp)) {
        val w = size.width
        val h = size.height

        // Sparkle stars around box
        drawCircle(Color(0xFFFFD54F), radius = 2.5.dp.toPx(), center = Offset(w * 0.18f, h * 0.2f))
        drawCircle(Color(0xFFFFD54F), radius = 2.5.dp.toPx(), center = Offset(w * 0.82f, h * 0.25f))

        // Red Box Lid
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFFF3333), Color(0xFFB71C1C))),
            topLeft = Offset(w * 0.12f, h * 0.28f),
            size = Size(w * 0.76f, h * 0.24f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        // Red Box Body
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFD32F2F), Color(0xFF880E4F))),
            topLeft = Offset(w * 0.18f, h * 0.48f),
            size = Size(w * 0.64f, h * 0.44f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        // Golden Ribbon (Vertical)
        drawRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFB300))),
            topLeft = Offset(w * 0.44f, h * 0.28f),
            size = Size(w * 0.12f, h * 0.64f)
        )
        // Golden Ribbon Bow (Top)
        drawCircle(Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(w * 0.42f, h * 0.22f))
        drawCircle(Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(w * 0.58f, h * 0.22f))
        drawCircle(Color(0xFFFFB300), radius = 4.dp.toPx(), center = Offset(w * 0.50f, h * 0.24f))
    }
}

// 3D Cartoon Trophy Art with Laurel Wreath
@Composable
private fun TrophyArt() {
    Canvas(modifier = Modifier.size(54.dp)) {
        val w = size.width
        val h = size.height

        // Laurel wreath wings (Golden leaves)
        drawCircle(Color(0xFFFFD54F), radius = 18.dp.toPx(), center = Offset(w * 0.5f, h * 0.46f), style = Stroke(width = 3.dp.toPx()))

        // Golden Cup Body
        val cupPath = Path().apply {
            moveTo(w * 0.28f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.28f)
            lineTo(w * 0.62f, h * 0.60f)
            lineTo(w * 0.38f, h * 0.60f)
            close()
        }
        drawPath(
            path = cupPath,
            brush = Brush.verticalGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFF9800)))
        )

        // Cup handles
        drawArc(
            color = Color(0xFFFFC107),
            startAngle = 135f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.30f),
            size = Size(w * 0.24f, h * 0.22f),
            style = Stroke(width = 2.5.dp.toPx())
        )
        drawArc(
            color = Color(0xFFFFC107),
            startAngle = 225f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.60f, h * 0.30f),
            size = Size(w * 0.24f, h * 0.22f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Trophy Stem & Base
        drawRect(Color(0xFFFF9800), topLeft = Offset(w * 0.46f, h * 0.60f), size = Size(w * 0.08f, h * 0.12f))
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))),
            topLeft = Offset(w * 0.30f, h * 0.72f),
            size = Size(w * 0.40f, h * 0.10f),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        // Little white star on cup
        drawCircle(Color.White, radius = 2.5.dp.toPx(), center = Offset(w * 0.5f, h * 0.42f))
    }
}

// 3D Golden Star & Cyan Daily Reward Chest (Calendar/Gift)
@Composable
private fun DailyRewardChestArt() {
    Canvas(modifier = Modifier.size(54.dp)) {
        val w = size.width
        val h = size.height

        // Sparkle stars
        drawCircle(Color(0xFFFFD54F), radius = 2.dp.toPx(), center = Offset(w * 0.15f, h * 0.20f))
        drawCircle(Color(0xFFFFD54F), radius = 2.dp.toPx(), center = Offset(w * 0.85f, h * 0.24f))

        // Cyan Chest Body
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0xFF0091EA))),
            topLeft = Offset(w * 0.14f, h * 0.24f),
            size = Size(w * 0.72f, h * 0.58f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFE0F7FA),
            topLeft = Offset(w * 0.14f, h * 0.24f),
            size = Size(w * 0.72f, h * 0.58f),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Dark Screen / Gold Badge Center
        drawRoundRect(
            color = Color(0xFF002244),
            topLeft = Offset(w * 0.26f, h * 0.34f),
            size = Size(w * 0.48f, h * 0.38f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Golden Coin in center representing Daily Reward
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFEA79), Color(0xFFFFB800), Color(0xFFD47C00))
            ),
            radius = 8.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.53f)
        )
        drawCircle(
            color = Color(0xFF8B4700),
            radius = 8.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.53f),
            style = Stroke(width = 1.dp.toPx())
        )

        // Chest Top Golden Clasp
        drawCircle(Color(0xFFFFD54F), radius = 3.dp.toPx(), center = Offset(w * 0.5f, h * 0.24f))
    }
}

// 3D Cartoon Studio Artist Badge
@Composable
private fun CartoonStudioBadgeArt() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A))
                )
            )
            .border(2.dp, Color(0xFFFFD54F), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("🎨", fontSize = 28.sp)
    }
}

/* ========================================================================== */
/*                    VERTICAL LEVEL PROGRESSION PATH                         */
/* ========================================================================== */

@Composable
private fun VerticalLevelProgression(
    currentLevel: Int,
    completedLevels: Set<Int>,
    allLevels: List<LevelEntity>,
    isCompact: Boolean,
    onSelectLevel: () -> Unit
) {
    val nodeSizeTop = if (isCompact) 38.dp else 44.dp
    val nodeSizeMid = if (isCompact) 42.dp else 48.dp
    val nodeSizeActive = if (isCompact) 64.dp else 74.dp
    val nodeSizeCompleted = if (isCompact) 44.dp else 50.dp
    val pipeHeight = if (isCompact) 8.dp else 12.dp

    fun getStarsFor(lvl: Int): Int {
        val ent = allLevels.find { it.id == lvl }
        return ent?.stars?.takeIf { it > 0 } ?: if (completedLevels.contains(lvl) || lvl < currentLevel) 3 else 0
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        if (currentLevel <= 1) {
            // Level 1: No completed levels yet. Show 4, 3, 2 ahead and 1 Active at bottom
            val lvl4 = (currentLevel + 3).coerceAtMost(LevelCatalog.TOTAL_LEVELS)
            val lvl3 = (currentLevel + 2).coerceAtMost(LevelCatalog.TOTAL_LEVELS)
            val lvl2 = (currentLevel + 1).coerceAtMost(LevelCatalog.TOTAL_LEVELS)
            val lvl1 = currentLevel

            LevelProgressNode(
                levelNumber = lvl4,
                size = nodeSizeTop,
                isActive = false,
                isCompleted = false,
                onClick = onSelectLevel
            )
            YellowConnectorPipe(height = pipeHeight)
            LevelProgressNode(
                levelNumber = lvl3,
                size = nodeSizeMid,
                isActive = false,
                isCompleted = false,
                onClick = onSelectLevel
            )
            YellowConnectorPipe(height = pipeHeight)
            LevelProgressNode(
                levelNumber = lvl2,
                size = nodeSizeMid,
                isActive = false,
                isCompleted = false,
                onClick = onSelectLevel
            )
            YellowConnectorPipe(height = pipeHeight)
            LevelProgressNode(
                levelNumber = lvl1,
                size = nodeSizeActive,
                isActive = true,
                isCompleted = false,
                onClick = onSelectLevel
            )
        } else if (currentLevel >= LevelCatalog.TOTAL_LEVELS) {
            // Max level reached (Level 150)
            val lvlTop = currentLevel
            val lvlMid1 = currentLevel - 1
            val lvlMid2 = currentLevel - 2
            val lvlBot = currentLevel - 3

            LevelProgressNode(
                levelNumber = lvlTop,
                size = nodeSizeActive,
                isActive = true,
                isCompleted = completedLevels.contains(lvlTop),
                starsCount = getStarsFor(lvlTop),
                onClick = onSelectLevel
            )
            YellowConnectorPipe(height = pipeHeight)
            LevelProgressNode(
                levelNumber = lvlMid1,
                size = nodeSizeCompleted,
                isActive = false,
                isCompleted = true,
                starsCount = getStarsFor(lvlMid1),
                onClick = onSelectLevel
            )
            YellowConnectorPipe(height = pipeHeight)
            LevelProgressNode(
                levelNumber = lvlMid2,
                size = nodeSizeCompleted,
                isActive = false,
                isCompleted = true,
                starsCount = getStarsFor(lvlMid2),
                onClick = onSelectLevel
            )
            YellowConnectorPipe(height = pipeHeight)
            LevelProgressNode(
                levelNumber = lvlBot,
                size = nodeSizeCompleted,
                isActive = false,
                isCompleted = true,
                starsCount = getStarsFor(lvlBot),
                onClick = onSelectLevel
            )
        } else {
            // Progression: Level 2+ (Display upcoming levels, current active, and previous completed with stars)
            val lvlAhead2 = (currentLevel + 2).coerceAtMost(LevelCatalog.TOTAL_LEVELS)
            val lvlAhead1 = (currentLevel + 1).coerceAtMost(LevelCatalog.TOTAL_LEVELS)
            val lvlCurrent = currentLevel
            val lvlCompleted = currentLevel - 1

            // TOP: Level + 2 (e.g. 4)
            LevelProgressNode(
                levelNumber = lvlAhead2,
                size = nodeSizeTop,
                isActive = false,
                isCompleted = false,
                onClick = onSelectLevel
            )

            YellowConnectorPipe(height = pipeHeight)

            // MID: Level + 1 (e.g. 3)
            LevelProgressNode(
                levelNumber = lvlAhead1,
                size = nodeSizeMid,
                isActive = false,
                isCompleted = false,
                onClick = onSelectLevel
            )

            YellowConnectorPipe(height = pipeHeight)

            // ACTIVE: Current Level (e.g. 2 - prominent glowing rings)
            LevelProgressNode(
                levelNumber = lvlCurrent,
                size = nodeSizeActive,
                isActive = true,
                isCompleted = false,
                onClick = onSelectLevel
            )

            YellowConnectorPipe(height = pipeHeight)

            // BOTTOM: Completed Level (e.g. 1 with stars)
            LevelProgressNode(
                levelNumber = lvlCompleted,
                size = nodeSizeCompleted,
                isActive = false,
                isCompleted = true,
                starsCount = getStarsFor(lvlCompleted),
                onClick = onSelectLevel
            )
        }
    }
}

@Composable
private fun YellowConnectorPipe(height: Dp) {
    Box(
        modifier = Modifier
            .width(12.dp)
            .height(height)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFC107), Color(0xFFFFD54F), Color(0xFFFFA000))
                )
            )
            .border(2.dp, Color(0xFF8D5300))
    )
}

@Composable
private fun LevelProgressNode(
    levelNumber: Int,
    size: Dp,
    isActive: Boolean,
    isCompleted: Boolean = false,
    starsCount: Int = 3,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodePulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActive) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    val backgroundBrush = when {
        isActive -> Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF0D47A1), Color(0xFF061864))
        )
        isCompleted -> Brush.radialGradient(
            // "gri olsun" - Cool sleek slate gray cartoon brushed gradient
            colors = listOf(Color(0xFF64748B), Color(0xFF475569), Color(0xFF334155))
        )
        else -> Brush.radialGradient(
            colors = listOf(Color(0xFF0091EA), Color(0xFF0D47A1), Color(0xFF081A54))
        )
    }

    val borderColor = when {
        isActive -> Color(0xFF00E5FF)
        isCompleted -> Color(0xFF94A3B8)
        else -> Color(0xFF2979FF)
    }

    val borderWidth = if (isActive) 3.5.dp else 2.5.dp

    Box(
        modifier = Modifier
            .size(size)
            .scale(if (isActive) glowScale else 1.0f)
            .shadow(if (isActive) 8.dp else 4.dp, CircleShape)
            .clip(CircleShape)
            .background(brush = backgroundBrush)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = hapticClick(action = onClick))
            .testTag(
                when {
                    isActive -> "active_level_node"
                    isCompleted -> "completed_level_node_$levelNumber"
                    else -> "level_node_$levelNumber"
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$levelNumber",
                color = if (isCompleted) Color(0xFFF1F5F9) else Color.White,
                fontSize = if (isActive) 24.sp else 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            // Stars underneath
            val starTint = when {
                isActive -> Color(0xFF00E5FF)
                isCompleted -> Color(0xFFFFD54F) // Vibrant Golden Yellow for completed level!
                else -> Color(0xFF82B1FF)
            }

            val starSize = if (isActive) 10.dp else 8.dp

            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(starsCount.coerceIn(1, 3)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = starTint,
                        modifier = Modifier.size(starSize)
                    )
                }
            }
        }

        // Green Checkmark badge on completed level
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .size(if (size < 46.dp) 13.dp else 16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(if (size < 46.dp) 9.dp else 11.dp)
                )
            }
        }
    }
}

/* ========================================================================== */
/*                       BIG PLAY BUTTON & ENERGY PILL                        */
/* ========================================================================== */

@Composable
private fun BigPlayButton(
    onClick: () -> Unit,
    isCompact: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale = if (isPressed) 0.94f else 1.0f

    val btnWidth = if (isCompact) 210.dp else 240.dp
    val btnHeight = if (isCompact) 56.dp else 66.dp

    Box(
        modifier = Modifier
            .width(btnWidth)
            .height(btnHeight)
            .scale(buttonScale)
            .shadow(10.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFFF6D00))
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFEE55),
                        Color(0xFFFFC400),
                        Color(0xFFFF8F00)
                    )
                )
            )
            .border(3.5.dp, Color(0xFF8A3B00), RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = hapticClick(action = onClick)
            )
            .testTag("play_game_button"),
        contentAlignment = Alignment.Center
    ) {
        // Inner Gloss highlight on top half
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(4.dp.toPx(), 2.dp.toPx()),
                size = Size(size.width - 8.dp.toPx(), size.height * 0.42f),
                cornerRadius = CornerRadius(20.dp.toPx())
            )
        }

        // Bold Play Text with Shadow
        Text(
            text = "Play",
            color = Color.White,
            fontSize = if (isCompact) 26.sp else 30.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnergyCostPill(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .shadow(3.dp, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF8E24AA), Color(0xFF6A1B9A))
                )
            )
            .border(1.5.dp, Color(0xFF4A148C), RoundedCornerShape(15.dp))
            .clickable(onClick = hapticClick(action = onClick))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = Color(0xFFFFEB3B),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Energy",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ========================================================================== */
/*                         BOTTOM GAME NAVIGATION DOCK                        */
/* ========================================================================== */

@Composable
private fun BottomGameDock(
    unclaimedQuestsCount: Int,
    isGuest: Boolean = false,
    onOpenQuests: () -> Unit,
    onOpenLevels: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Dark Blue Dock Surface
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF091E60), Color(0xFF041038))
                    )
                )
                .border(1.5.dp, Color(0xFF1E4FB8), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT TAB: Quests (Dartboard with Red exclamation badge or Lock)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(onClick = hapticClick(action = onOpenQuests))
                    .testTag("nav_tab_gorevler")
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dartboard Bullseye Graphic
                    DartboardArt()

                    // Lock badge or Exclamation Notification Badge on top right
                    if (isGuest) {
                        Box(
                            modifier = Modifier
                                .offset(x = 8.dp, y = (-6).dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF64748B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔒",
                                fontSize = 8.sp
                            )
                        }
                    } else if (unclaimedQuestsCount > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 8.dp, y = (-6).dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE51D24)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "!",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Text(
                    text = if (isGuest) "Quests 🔒" else "Quests",
                    color = if (isGuest) Color(0xFF94A3B8) else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dummy Spacer for elevated center tab
            Spacer(modifier = Modifier.width(70.dp))

            // RIGHT TAB: Leaderboard (Golden Trophy or Lock)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(onClick = hapticClick(action = onOpenLeaderboard))
                    .testTag("nav_tab_siralama")
            ) {
                // Golden Trophy Graphic
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val w = size.width
                        val h = size.height
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                if (isGuest) listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                else listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                            ),
                            topLeft = Offset(w * 0.22f, h * 0.15f),
                            size = Size(w * 0.56f, h * 0.50f),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        drawRect(
                            if (isGuest) Color(0xFF64748B) else Color(0xFFFF8F00),
                            topLeft = Offset(w * 0.42f, h * 0.65f),
                            size = Size(w * 0.16f, h * 0.15f)
                        )
                        drawRoundRect(
                            if (isGuest) Color(0xFF94A3B8) else Color(0xFFFFC107),
                            topLeft = Offset(w * 0.28f, h * 0.80f),
                            size = Size(w * 0.44f, h * 0.14f),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }

                    if (isGuest) {
                        Box(
                            modifier = Modifier
                                .offset(x = 8.dp, y = (-6).dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF64748B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔒",
                                fontSize = 8.sp
                            )
                        }
                    }
                }
                Text(
                    text = if (isGuest) "Rank 🔒" else "Leaderboard",
                    color = if (isGuest) Color(0xFF94A3B8) else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // CENTER ELEVATED TAB: "Levels" (Parchment/Scroll Map Icon, Elevated Blue Container)
        Box(
            modifier = Modifier
                .offset(y = (-10).dp)
                .width(84.dp)
                .height(68.dp)
                .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF00E5FF))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0091EA), Color(0xFF0D47A1))
                    )
                )
                .border(2.5.dp, Color(0xFF40C4FF), RoundedCornerShape(18.dp))
                .clickable(onClick = hapticClick(action = onOpenLevels))
                .testTag("nav_tab_jener"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Rolled Scroll / Map Graphic
                ScrollParchmentArt()
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Levels",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// Dartboard Art for "Quests"
@Composable
private fun DartboardArt() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer red ring
        drawCircle(color = Color(0xFFE51D24), radius = radius, center = center)
        // White ring
        drawCircle(color = Color.White, radius = radius * 0.72f, center = center)
        // Red inner ring
        drawCircle(color = Color(0xFFE51D24), radius = radius * 0.44f, center = center)
        // White center dot
        drawCircle(color = Color.White, radius = radius * 0.18f, center = center)
    }
}

// Rolled Parchment Scroll Map for "Jener"
@Composable
private fun ScrollParchmentArt() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // White/Ivory Rolled Parchment Paper
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1))),
            topLeft = Offset(w * 0.16f, h * 0.12f),
            size = Size(w * 0.68f, h * 0.58f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        // Golden/Orange Rolled Rod Ends
        drawRoundRect(
            color = Color(0xFFFF9800),
            topLeft = Offset(w * 0.10f, h * 0.58f),
            size = Size(w * 0.80f, h * 0.18f),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        // Red Ribbon Seal
        drawCircle(Color(0xFFE51D24), radius = 3.dp.toPx(), center = Offset(w * 0.24f, h * 0.67f))
    }
}

/* ========================================================================== */
/*           ANIMATED FLOWING CASUAL GAME BACKGROUND SYMBOLS (WATERMARKS)     */
/* ========================================================================== */

internal fun DrawScope.drawFlowingCasualGameSymbols(
    canvasSize: Size,
    flowProgress: Float,
    pulseProgress: Float
) {
    val w = canvasSize.width
    val h = canvasSize.height
    if (w <= 0f || h <= 0f) return

    val stepX = 100.dp.toPx()
    val stepY = 100.dp.toPx()

    // 8x8 periodic repetition period for the 8 cartoon symbols
    val period = 8
    val tileW = period * stepX
    val tileH = period * stepY

    // Perfectly continuous diagonal translation
    // As flowProgress goes 0 -> 1, shift advances by exactly 1 full tile period!
    val shiftX = flowProgress * tileW
    val shiftY = -flowProgress * tileH

    val margin = 50.dp.toPx()
    val minCol = kotlin.math.floor((-margin - shiftX - stepX) / stepX).toInt() - 1
    val maxCol = kotlin.math.ceil((w + margin - shiftX) / stepX).toInt() + 1
    val minRow = kotlin.math.floor((-margin - shiftY) / stepY).toInt() - 1
    val maxRow = kotlin.math.ceil((h + margin - shiftY) / stepY).toInt() + 1

    val strokeWidth = 2.dp.toPx()
    val strokeCap = StrokeCap.Round
    val strokeJoin = StrokeJoin.Round
    val stroke = Stroke(width = strokeWidth, cap = strokeCap, join = strokeJoin)

    for (c in minCol..maxCol) {
        for (r in minRow..maxRow) {
            val staggeredX = if (Math.floorMod(r, 2) != 0) stepX * 0.5f else 0f
            val posX = c * stepX + shiftX + staggeredX
            val posY = r * stepY + shiftY

            // Skip off-screen elements with comfortable padding
            if (posX < -margin || posX > w + margin || posY < -margin || posY > h + margin) {
                continue
            }

            val center = Offset(posX, posY)
            // Periodic symbol index: (c * 3 + r * 7) mod 8
            // When c shifts by -8 and r by +8, delta is -24 + 56 = +32, which is 0 mod 8!
            val symbolIndex = Math.floorMod(c * 3 + r * 7, 8)

            // Periodic rotation matching across tile boundaries
            val baseRot = (((Math.floorMod(c, 8) * 17 + Math.floorMod(r, 8) * 29) % 30) - 15).toFloat()
            val wobbleRot = kotlin.math.sin((flowProgress * 2 * kotlin.math.PI + Math.floorMod(c, 8)).toDouble()).toFloat() * 5f
            val rotation = baseRot + wobbleRot

            // Theme-appropriate cartoon colors with subtle watermark transparency
            val (fillColor, strokeColor) = when (symbolIndex) {
                // Trophies, Crowns, Coins, Stars: Warm Golden Yellow & Amber
                0, 1, 3, 7 -> Pair(
                    Color(0xFFFFD54F).copy(alpha = 0.055f * pulseProgress),
                    Color(0xFFFFE082).copy(alpha = 0.13f * pulseProgress)
                )
                // Gamepads, Diamonds: Neon Cyan & Electric Azure
                2, 4 -> Pair(
                    Color(0xFF38BDF8).copy(alpha = 0.055f * pulseProgress),
                    Color(0xFF7DD3FC).copy(alpha = 0.13f * pulseProgress)
                )
                // Lightning, Rockets: Vibrant Violet & Radiant Purple
                else -> Pair(
                    Color(0xFFA78BFA).copy(alpha = 0.055f * pulseProgress),
                    Color(0xFFC4B5FD).copy(alpha = 0.13f * pulseProgress)
                )
            }

            rotate(degrees = rotation, pivot = center) {
                when (symbolIndex) {
                    0 -> drawTrophySymbol(center, fillColor, strokeColor, stroke)
                    1 -> drawCrownSymbol(center, fillColor, strokeColor, stroke)
                    2 -> drawGamepadSymbol(center, fillColor, strokeColor, stroke)
                    3 -> drawSparkleStarSymbol(center, fillColor, strokeColor, stroke)
                    4 -> drawDiamondSymbol(center, fillColor, strokeColor, stroke)
                    5 -> drawLightningSymbol(center, fillColor, strokeColor, stroke)
                    6 -> drawRocketSymbol(center, fillColor, strokeColor, stroke)
                    7 -> drawCoinSymbol(center, fillColor, strokeColor, stroke)
                }
            }
        }
    }
}

private fun DrawScope.drawTrophySymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val cupPath = Path().apply {
        moveTo(center.x - 11.dp.toPx(), center.y - 10.dp.toPx())
        lineTo(center.x + 11.dp.toPx(), center.y - 10.dp.toPx())
        lineTo(center.x + 7.dp.toPx(), center.y + 2.dp.toPx())
        lineTo(center.x - 7.dp.toPx(), center.y + 2.dp.toPx())
        close()
    }
    drawPath(cupPath, fill)
    drawPath(cupPath, strokeColor, style = stroke)

    val handleLeft = Path().apply {
        moveTo(center.x - 10.dp.toPx(), center.y - 8.dp.toPx())
        cubicTo(
            center.x - 17.dp.toPx(), center.y - 8.dp.toPx(),
            center.x - 17.dp.toPx(), center.y - 1.dp.toPx(),
            center.x - 8.dp.toPx(), center.y + 1.dp.toPx()
        )
    }
    val handleRight = Path().apply {
        moveTo(center.x + 10.dp.toPx(), center.y - 8.dp.toPx())
        cubicTo(
            center.x + 17.dp.toPx(), center.y - 8.dp.toPx(),
            center.x + 17.dp.toPx(), center.y - 1.dp.toPx(),
            center.x + 8.dp.toPx(), center.y + 1.dp.toPx()
        )
    }
    drawPath(handleLeft, strokeColor, style = stroke)
    drawPath(handleRight, strokeColor, style = stroke)

    val baseStem = Path().apply {
        moveTo(center.x, center.y + 2.dp.toPx())
        lineTo(center.x, center.y + 8.dp.toPx())
        moveTo(center.x - 9.dp.toPx(), center.y + 8.dp.toPx())
        lineTo(center.x + 9.dp.toPx(), center.y + 8.dp.toPx())
    }
    drawPath(baseStem, strokeColor, style = stroke)

    drawCircle(strokeColor, radius = 2.dp.toPx(), center = Offset(center.x, center.y - 4.dp.toPx()))
}

private fun DrawScope.drawCrownSymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val crownPath = Path().apply {
        moveTo(center.x - 12.dp.toPx(), center.y + 7.dp.toPx())
        lineTo(center.x - 12.dp.toPx(), center.y - 4.dp.toPx())
        lineTo(center.x - 6.dp.toPx(), center.y + 1.dp.toPx())
        lineTo(center.x, center.y - 8.dp.toPx())
        lineTo(center.x + 6.dp.toPx(), center.y + 1.dp.toPx())
        lineTo(center.x + 12.dp.toPx(), center.y - 4.dp.toPx())
        lineTo(center.x + 12.dp.toPx(), center.y + 7.dp.toPx())
        close()
    }
    drawPath(crownPath, fill)
    drawPath(crownPath, strokeColor, style = stroke)

    drawCircle(strokeColor, radius = 2.dp.toPx(), center = Offset(center.x - 12.dp.toPx(), center.y - 5.dp.toPx()))
    drawCircle(strokeColor, radius = 2.2.dp.toPx(), center = Offset(center.x, center.y - 9.dp.toPx()))
    drawCircle(strokeColor, radius = 2.dp.toPx(), center = Offset(center.x + 12.dp.toPx(), center.y - 5.dp.toPx()))

    drawLine(
        color = strokeColor,
        start = Offset(center.x - 9.dp.toPx(), center.y + 4.dp.toPx()),
        end = Offset(center.x + 9.dp.toPx(), center.y + 4.dp.toPx()),
        strokeWidth = stroke.width,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawGamepadSymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val gw = 32.dp.toPx()
    val gh = 20.dp.toPx()
    drawRoundRect(
        color = fill,
        topLeft = Offset(center.x - gw / 2f, center.y - gh / 2f),
        size = Size(gw, gh),
        cornerRadius = CornerRadius(7.dp.toPx())
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(center.x - gw / 2f, center.y - gh / 2f),
        size = Size(gw, gh),
        cornerRadius = CornerRadius(7.dp.toPx()),
        style = stroke
    )
    val dpadSize = 3.dp.toPx()
    val dpadCenter = Offset(center.x - 7.dp.toPx(), center.y)
    drawLine(strokeColor, Offset(dpadCenter.x - dpadSize, dpadCenter.y), Offset(dpadCenter.x + dpadSize, dpadCenter.y), stroke.width, StrokeCap.Round)
    drawLine(strokeColor, Offset(dpadCenter.x, dpadCenter.y - dpadSize), Offset(dpadCenter.x, dpadCenter.y + dpadSize), stroke.width, StrokeCap.Round)

    drawCircle(strokeColor, radius = 1.8.dp.toPx(), center = Offset(center.x + 6.dp.toPx(), center.y - 3.dp.toPx()))
    drawCircle(strokeColor, radius = 1.8.dp.toPx(), center = Offset(center.x + 8.5.dp.toPx(), center.y + 1.dp.toPx()))
}

private fun DrawScope.drawSparkleStarSymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val r = 13.dp.toPx()
    val innerR = 3.5.dp.toPx()
    val starPath = Path().apply {
        moveTo(center.x, center.y - r)
        quadraticTo(center.x, center.y, center.x + innerR, center.y - innerR)
        lineTo(center.x + r, center.y)
        quadraticTo(center.x, center.y, center.x + innerR, center.y + innerR)
        lineTo(center.x, center.y + r)
        quadraticTo(center.x, center.y, center.x - innerR, center.y + innerR)
        lineTo(center.x - r, center.y)
        quadraticTo(center.x, center.y, center.x - innerR, center.y - innerR)
        close()
    }
    drawPath(starPath, fill)
    drawPath(starPath, strokeColor, style = stroke)
    drawCircle(strokeColor, radius = 2.dp.toPx(), center = center)
}

private fun DrawScope.drawDiamondSymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val gemPath = Path().apply {
        moveTo(center.x - 7.dp.toPx(), center.y - 8.dp.toPx())
        lineTo(center.x + 7.dp.toPx(), center.y - 8.dp.toPx())
        lineTo(center.x + 13.dp.toPx(), center.y - 2.dp.toPx())
        lineTo(center.x, center.y + 11.dp.toPx())
        lineTo(center.x - 13.dp.toPx(), center.y - 2.dp.toPx())
        close()
    }
    drawPath(gemPath, fill)
    drawPath(gemPath, strokeColor, style = stroke)

    val facets = Path().apply {
        moveTo(center.x - 13.dp.toPx(), center.y - 2.dp.toPx())
        lineTo(center.x + 13.dp.toPx(), center.y - 2.dp.toPx())
        moveTo(center.x - 7.dp.toPx(), center.y - 8.dp.toPx())
        lineTo(center.x - 4.dp.toPx(), center.y - 2.dp.toPx())
        lineTo(center.x, center.y + 11.dp.toPx())
        moveTo(center.x + 7.dp.toPx(), center.y - 8.dp.toPx())
        lineTo(center.x + 4.dp.toPx(), center.y - 2.dp.toPx())
        lineTo(center.x, center.y + 11.dp.toPx())
    }
    drawPath(facets, strokeColor, style = stroke)
}

private fun DrawScope.drawLightningSymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val boltPath = Path().apply {
        moveTo(center.x + 2.dp.toPx(), center.y - 12.dp.toPx())
        lineTo(center.x - 7.dp.toPx(), center.y)
        lineTo(center.x - 1.dp.toPx(), center.y)
        lineTo(center.x - 4.dp.toPx(), center.y + 12.dp.toPx())
        lineTo(center.x + 8.dp.toPx(), center.y - 1.dp.toPx())
        lineTo(center.x + 2.dp.toPx(), center.y - 1.dp.toPx())
        close()
    }
    drawPath(boltPath, fill)
    drawPath(boltPath, strokeColor, style = stroke)
}

private fun DrawScope.drawRocketSymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val rocketPath = Path().apply {
        moveTo(center.x, center.y - 13.dp.toPx())
        lineTo(center.x + 6.dp.toPx(), center.y + 4.dp.toPx())
        lineTo(center.x - 6.dp.toPx(), center.y + 4.dp.toPx())
        close()
    }
    drawPath(rocketPath, fill)
    drawPath(rocketPath, strokeColor, style = stroke)

    val finLeft = Path().apply {
        moveTo(center.x - 6.dp.toPx(), center.y + 1.dp.toPx())
        lineTo(center.x - 11.dp.toPx(), center.y + 8.dp.toPx())
        lineTo(center.x - 6.dp.toPx(), center.y + 6.dp.toPx())
        close()
    }
    val finRight = Path().apply {
        moveTo(center.x + 6.dp.toPx(), center.y + 1.dp.toPx())
        lineTo(center.x + 11.dp.toPx(), center.y + 8.dp.toPx())
        lineTo(center.x + 6.dp.toPx(), center.y + 6.dp.toPx())
        close()
    }
    drawPath(finLeft, strokeColor, style = stroke)
    drawPath(finRight, strokeColor, style = stroke)

    drawCircle(strokeColor, radius = 2.5.dp.toPx(), center = Offset(center.x, center.y - 3.dp.toPx()))
}

private fun DrawScope.drawCoinSymbol(center: Offset, fill: Color, strokeColor: Color, stroke: Stroke) {
    val outerR = 10.dp.toPx()
    val innerR = 6.5.dp.toPx()
    drawCircle(fill, radius = outerR, center = center)
    drawCircle(strokeColor, radius = outerR, center = center, style = stroke)
    drawCircle(strokeColor, radius = innerR, center = center, style = stroke)
    drawCircle(strokeColor, radius = 2.dp.toPx(), center = center)
}
