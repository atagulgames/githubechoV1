package com.example.ui.menu

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.Image
import com.example.audio.HapticEngine
import com.example.audio.hapticClick
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.localization.EchoStrings
import com.example.ui.components.Season2BackgroundPatternCanvas
import com.example.ui.components.MeteorStoneButton
import com.example.ui.components.MeteorStoneTheme
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.audio.HarmonicAudioEngine
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.EchoUiState

@Composable
fun EchoMainMenu(
    state: EchoUiState,
    onPlay: () -> Unit,
    onDailyChallenge: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSkins: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSupport: () -> Unit = {},
    onOpenLeaderboard: () -> Unit = {},
    onOpenMyProfile: () -> Unit = {},
    onToggleDarkTheme: () -> Unit = {},
    onOpenDailyQuests: () -> Unit = {},
    onOpenDailyLogin: () -> Unit = {},
    onOpenChest: () -> Unit = {},
    onWatchRewardedAd: () -> Unit = {},
    onOpenSeason2Info: () -> Unit = {},
    onOpenSeason2Themes: () -> Unit = {},
    onBannerAdLoaded: () -> Unit = {},
    onBannerAdFailed: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // User requirement: "müzik ana menüdede olsun"
    LaunchedEffect(Unit) {
        HarmonicAudioEngine.setIntroActive(false)
        HarmonicAudioEngine.startBgm()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "PulseLogo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val s2Theme = state.selectedSeason2Theme
    val isS2 = state.isSeason2ThemeActive

    val bgTheme = if (isS2) s2Theme.getBackgroundTop(state.isDarkTheme) else if (state.isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isS2) s2Theme.getCardBackground(state.isDarkTheme) else if (state.isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (state.isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (state.isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isS2) s2Theme.getBorderColor(state.isDarkTheme) else if (state.isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_menu_screen"),
        color = bgTheme
    ) {
        val scrollState = rememberScrollState()
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Season 2 Dynamic Background Pattern Canvas
            if (isS2 && state.isSeason2BackgroundEffectsEnabled) {
                Season2BackgroundPatternCanvas(
                    theme = s2Theme,
                    isDarkTheme = state.isDarkTheme,
                    modifier = Modifier.fillMaxSize(),
                    alphaMultiplier = 0.85f
                )
            }
            val isNarrow = maxWidth < 380.dp
            val isShort = maxHeight < 700.dp
            val horizontalPadding = if (isNarrow) 12.dp else 16.dp
            val logoBaseSize = if (isNarrow || isShort) 100.dp else 130.dp

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = horizontalPadding, vertical = if (isShort) 12.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isShort) 12.dp else 16.dp)
            ) {
                // Top Navigation Bar: Profile/Player Pill on Left, Action Buttons on Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Player Profile Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBg)
                            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                            .clickable { onOpenMyProfile() }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("profile_icon_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.userAvatarUri.isNotBlank()) state.userAvatarUri else "⚡",
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = state.authenticatedUser,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (state.userCustomTitle.isNotBlank()) state.userCustomTitle else "Ses Kaşifi",
                                fontSize = 10.sp,
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right: Quick Action Buttons (Leaderboard, Theme, Support, Settings)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Leaderboard
                        IconButton(
                            onClick = hapticClick(action = onOpenLeaderboard),
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(cardBg)
                                .border(1.dp, borderColor, CircleShape)
                                .testTag("leaderboard_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = "Liderlik Tablosu",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Theme Toggle
                        IconButton(
                            onClick = hapticClick(action = onToggleDarkTheme),
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(cardBg)
                                .border(1.dp, borderColor, CircleShape)
                                .testTag("menu_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (state.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Tema Değiştir",
                                tint = if (state.isDarkTheme) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Support
                        IconButton(
                            onClick = hapticClick(action = onOpenSupport),
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(cardBg)
                                .border(1.dp, borderColor, CircleShape)
                                .testTag("support_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Destek & İletişim",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Settings Button - Ample spacing, unconstrained, clean top-right corner
                        IconButton(
                            onClick = hapticClick(action = onOpenSettings),
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(cardBg)
                                .border(1.dp, borderColor, CircleShape)
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ayarlar",
                                tint = textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                // Economy & Resource Badges: Stars, Trophies, Tokens
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stars Chip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Yıldızlar",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.totalStars}/300",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Trophies Chip (Click to open profile)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenMyProfile() }
                            .testTag("menu_trophies_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Kupalar",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.trophies} 🏆",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Tokens Chip (Click to open shop)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenShop() }
                            .testTag("menu_token_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = "Jetonlar",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.tokens} Jeton",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Middle: Official ECHO Brand Identity & Living Modern Logo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // Official ECHO Logo with animated cyber aura
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(logoBaseSize * pulseScale)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0x5500E5FF), Color(0x227C4DFF), Color.Transparent)
                                )
                            )
                            .border(2.dp, Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))), RoundedCornerShape(32.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.echo_logo),
                            contentDescription = "ECHO Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(30.dp))
                        )
                    }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ECHO FLUX",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "TEK ÇİZGİ STRATEJİ & BULMACA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = Color(0xFF0284C7),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Hataların kırmızı lazer bariyerlere dönüşür.\nZekanı kullan, yolu tek seferde çöz!",
                    fontSize = 12.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
            }

            // Season 2 Glowing Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .clickable { onOpenSeason2Info() }
                    .testTag("season_2_banner_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isDarkTheme) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFFFB703), Color(0xFF38BDF8), Color(0xFFEC4899)))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB703).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SEZON 2: YENİ ÇAĞ",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (state.isDarkTheme) Color.White else Color(0xFF1E1B4B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFB703))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("AKTİF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                            Text(
                                text = "100 Yeni Bölüm • Sıfırlanan Seviyeler • 2.5x Ödül",
                                fontSize = 11.sp,
                                color = if (state.isDarkTheme) Color(0xFFC7D2FE) else Color(0xFF4338CA)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Season 2 Theme Selector Card (Active after Season 2 Welcome screen)
            if (state.isSeason2ThemeActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(16.dp))
                        .clickable { onOpenSeason2Themes() }
                        .testTag("season_2_theme_selector_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = s2Theme.primaryAccent.copy(alpha = if (state.isDarkTheme) 0.16f else 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        s2Theme.primaryAccent.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = s2Theme.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Sezon 2 Teması: ${s2Theme.displayName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(s2Theme.secondaryAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "DEĞİŞTİR",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = s2Theme.secondaryAccent
                                        )
                                    }
                                }
                                Text(
                                    text = s2Theme.subtitle,
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Temaları Aç",
                            tint = s2Theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Daily Challenge Card (Dynamic Theme)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clickable { onDailyChallenge() }
                    .testTag("daily_challenge_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.isDailyCompletedToday) Color(0xFF86EFAC) else Color(0xFFFDE68A)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (state.isDailyCompletedToday) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (state.isDailyCompletedToday) Color(0xFF16A34A) else Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Günün Özel Bulmacası",
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (state.isDailyCompletedToday) "Bugün Tamamlandı ✓" else "Çift Jeton Ödülü (2x)",
                                color = if (state.isDailyCompletedToday) Color(0xFF16A34A) else Color(0xFFD97706),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = if (state.isDailyCompletedToday) "Tamam" else "BAŞLA →",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        fontSize = 13.sp
                    )
                }
            }

            // Game Systems Quick Access: Görevler, Giriş Takvimi, Gizemli Sandık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Görevler Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clickable { onOpenDailyQuests() }
                        .testTag("menu_daily_quests_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.unclaimedQuestsCount > 0) Color(0xFF0284C7) else borderColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.CrisisAlert,
                                contentDescription = "Görevler",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                            if (state.unclaimedQuestsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Görevler",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = if (state.unclaimedQuestsCount > 0) "${state.unclaimedQuestsCount} Hazır" else "Günlük",
                            fontSize = 10.sp,
                            color = if (state.unclaimedQuestsCount > 0) Color(0xFF10B981) else textSecondary,
                            fontWeight = if (state.unclaimedQuestsCount > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // 2. Giriş Takvimi Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clickable { onOpenDailyLogin() }
                        .testTag("menu_daily_login_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.isLoginRewardAvailableToday) Color(0xFFF59E0B) else borderColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Giriş",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                            if (state.isLoginRewardAvailableToday) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.loginStreak}. Gün",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = if (state.isLoginRewardAvailableToday) "Ödül Al!" else "Alındı ✓",
                            fontSize = 10.sp,
                            color = if (state.isLoginRewardAvailableToday) Color(0xFFD97706) else textSecondary,
                            fontWeight = if (state.isLoginRewardAvailableToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // 3. Sandık Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clickable { onOpenChest() }
                        .testTag("menu_mystery_chest_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.isFreeChestAvailable) Color(0xFF9333EA) else borderColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Redeem,
                                contentDescription = "Sandık",
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(22.dp)
                            )
                            if (state.isFreeChestAvailable) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF9333EA))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sandık",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = if (state.isFreeChestAvailable) "ÜCRETSİZ" else "Aç (${state.adChestsRemainingToday})",
                            fontSize = 10.sp,
                            color = if (state.isFreeChestAvailable) Color(0xFF9333EA) else textSecondary,
                            fontWeight = if (state.isFreeChestAvailable) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            val isRewardCooldown = state.rewardCooldownSeconds > 0
            val rewardCooldownFormatted = if (isRewardCooldown) {
                val h = state.rewardCooldownSeconds / 3600
                val m = (state.rewardCooldownSeconds % 3600) / 60
                val s = state.rewardCooldownSeconds % 60
                String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
            } else ""

            // Start.io Rewarded Video Card (3-Saatlik Yenilenen Ödül)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clickable { onWatchRewardedAd() }
                    .testTag("startio_reward_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isRewardCooldown) {
                        if (state.isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
                    } else {
                        if (state.isDarkTheme) Color(0xFF0369A1) else Color(0xFFBAE6FD)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isRewardCooldown) {
                                        if (state.isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                    } else {
                                        if (state.isDarkTheme) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRewardCooldown) Icons.Default.Redeem else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isRewardCooldown) Color(0xFF94A3B8) else Color(0xFF0284C7),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isRewardCooldown) "3 Saatlik Ödül Beklemede" else "Ücretsiz 3 Saatlik Ödül",
                                fontWeight = FontWeight.Bold,
                                color = if (isRewardCooldown) textSecondary else textPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isRewardCooldown) {
                                    "Yenilenme: $rewardCooldownFormatted (3 Saatte Bir)"
                                } else {
                                    "+1 Matkap Lazeri & +2 Jeton Kazan"
                                },
                                color = if (isRewardCooldown) Color(0xFFEAB308) else Color(0xFF0284C7),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRewardCooldown) Color(0xFF475569) else Color(0xFF0284C7))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isRewardCooldown) rewardCooldownFormatted else "İZLE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = if (isRewardCooldown) 10.sp else 11.sp
                        )
                    }
                }
            }

            // Leaderboard & Player Profile Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clickable { onOpenLeaderboard() }
                        .testTag("menu_leaderboard_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, if (state.isDarkTheme) Color(0xFFD97706).copy(alpha = 0.5f) else Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Liderlik",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Global Sıralama",
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clickable { onOpenMyProfile() }
                        .testTag("menu_profile_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, if (state.isDarkTheme) Color(0xFF3B82F6).copy(alpha = 0.5f) else Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (state.isDarkTheme) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Profilim",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Süre & Kayıtlar",
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }

            // Bottom Actions: Primary Play Button & Modern Navigation Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary "OYNA" Meteor Stone Button
                MeteorStoneButton(
                    text = "${EchoStrings.get("play", state.language).uppercase()} (${EchoStrings.get("level", state.language).uppercase()} ${state.currentLevelIndex + 1})",
                    subtitle = "Tek Çizgi Strateji & Kozmik Bulmaca",
                    icon = Icons.Default.PlayArrow,
                    stoneTheme = MeteorStoneTheme.CYAN_PULSE,
                    height = 62.dp,
                    onClick = onPlay,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "play_button"
                )

                // Row: Bölümler (100 Seviye), Temalar, Mağaza (Meteor Stone Styling)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeteorStoneButton(
                        text = "BÖLÜMLER",
                        subtitle = "100 Seviye",
                        icon = Icons.Default.GridOn,
                        stoneTheme = MeteorStoneTheme.OBSIDIAN_SLATE,
                        height = 54.dp,
                        onClick = onOpenLevelSelect,
                        modifier = Modifier.weight(1f),
                        testTag = "main_levels_button"
                    )

                    MeteorStoneButton(
                        text = "TEMALAR",
                        subtitle = "Görünümler",
                        icon = Icons.Default.Palette,
                        stoneTheme = MeteorStoneTheme.COSMIC_PURPLE,
                        height = 54.dp,
                        onClick = onOpenSkins,
                        modifier = Modifier.weight(1f),
                        testTag = "main_skins_button"
                    )

                    MeteorStoneButton(
                        text = "MAĞAZA",
                        subtitle = "Jeton & Güç",
                        icon = Icons.Default.ShoppingCart,
                        stoneTheme = MeteorStoneTheme.SOLAR_AMBER,
                        height = 54.dp,
                        onClick = onOpenShop,
                        modifier = Modifier.weight(1f),
                        testTag = "main_shop_button"
                    )
                }

                // Start.io Banner Ad - always visible
                com.example.ads.StartAppBannerView(
                    modifier = Modifier.padding(top = 4.dp),
                    onAdLoaded = onBannerAdLoaded,
                    onAdFailedToLoad = onBannerAdFailed
                )
            }
            }
        }
    }
}

