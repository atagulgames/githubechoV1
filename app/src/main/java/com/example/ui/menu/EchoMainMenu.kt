package com.example.ui.menu

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.localization.EchoStrings
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onOpenDailyQuests: () -> Unit = {},
    onOpenDailyLogin: () -> Unit = {},
    onOpenChest: () -> Unit = {},
    onWatchRewardedAd: () -> Unit = {},
    onBannerAdLoaded: () -> Unit = {},
    onBannerAdFailed: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
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

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_menu_screen"),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Stars, Tokens, Settings (Modern White Surface Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars & Progress Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Yıldızlar",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.totalStars} / 750",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Tokens & Settings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .clickable { onOpenShop() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("menu_token_chip")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "Jetonlar",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${state.tokens}",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Middle: Official ECHO Brand Identity & Living Modern Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Official ECHO Logo with animated cyber aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size((140 * pulseScale).dp)
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
                    text = "E C H O",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color = Color(0xFF0F172A),
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
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
            }

            // Daily Challenge Card (Modern White Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clickable { onDailyChallenge() }
                    .testTag("daily_challenge_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                color = Color(0xFF0F172A),
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.unclaimedQuestsCount > 0) Color(0xFF0284C7) else Color(0xFFE2E8F0)
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
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (state.unclaimedQuestsCount > 0) "${state.unclaimedQuestsCount} Hazır" else "Günlük",
                            fontSize = 10.sp,
                            color = if (state.unclaimedQuestsCount > 0) Color(0xFF10B981) else Color(0xFF64748B),
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.isLoginRewardAvailableToday) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
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
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (state.isLoginRewardAvailableToday) "Ödül Al!" else "Alındı ✓",
                            fontSize = 10.sp,
                            color = if (state.isLoginRewardAvailableToday) Color(0xFFD97706) else Color(0xFF64748B),
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.isFreeChestAvailable) Color(0xFF9333EA) else Color(0xFFE2E8F0)
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
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (state.isFreeChestAvailable) "ÜCRETSİZ" else "Aç (${state.adChestsRemainingToday})",
                            fontSize = 10.sp,
                            color = if (state.isFreeChestAvailable) Color(0xFF9333EA) else Color(0xFF64748B),
                            fontWeight = if (state.isFreeChestAvailable) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Start.io Rewarded Video Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clickable { onWatchRewardedAd() }
                    .testTag("startio_reward_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
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
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ücretsiz Günlük Ödül",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "+1 Matkap Lazeri & +2 Jeton Kazan",
                                color = Color(0xFF0284C7),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0284C7))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "İZLE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Bottom Actions: Primary Play Button & Modern Navigation Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary "OYNA" Button
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp))
                        .testTag("play_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Oyna",
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${EchoStrings.get("play", state.language)} (${EchoStrings.get("level", state.language)} ${state.currentLevelIndex + 1})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Row: Bölümler (250 Seviye), Temalar, Mağaza (Modern White Buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Level Select
                    Button(
                        onClick = onOpenLevelSelect,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .testTag("main_levels_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("250 ${EchoStrings.get("levels", state.language)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Themes / Skins
                    Button(
                        onClick = onOpenSkins,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .testTag("main_skins_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(EchoStrings.get("themes", state.language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Shop
                    Button(
                        onClick = onOpenShop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .testTag("main_shop_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(EchoStrings.get("shop", state.language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Start.io Banner Ad
                com.example.ads.StartAppBannerView(
                    modifier = Modifier.padding(top = 4.dp),
                    onAdLoaded = onBannerAdLoaded,
                    onAdFailedToLoad = onBannerAdFailed
                )
            }
        }
    }
}
