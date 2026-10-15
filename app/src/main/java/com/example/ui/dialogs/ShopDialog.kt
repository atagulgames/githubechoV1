package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ShopDialog(
    currentTokens: Int,
    currentDiamonds: Int = 0,
    currentBreakers: Int,
    multiAdWatchCount: Int = 0,
    doubleTrophiesExpiresAt: Long = 0L,
    infiniteBreakersExpiresAt: Long = 0L,
    radiusShrinkerExpiresAt: Long = 0L,
    diamondRewardCooldownSeconds: Long = 0L,
    coinRewardCooldownSeconds: Long = 0L,
    breakerRewardCooldownSeconds: Long = 0L,
    megaChestRewardCooldownSeconds: Long = 0L,
    isDarkTheme: Boolean = true,
    onWatchRewardedAd: (String) -> Unit = {},
    onExchangeDiamondsForTokens: (Int, Int) -> Unit = { _, _ -> },
    onExchangeDiamondsForBreakers: (Int, Int) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .testTag("shop_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rewards & Exchange Center",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Watch Ads & Earn Instantly",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_shop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Currency Balances Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = cardBgColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Diamonds
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentDiamonds Diamonds",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        // Tokens / Coins
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentTokens Tokens",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        // Breakers
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentBreakers Drills",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: WATCH ADS & EARN
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OndemandVideo,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WATCH ADS & CLAIM REWARDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6),
                        letterSpacing = 0.5.sp
                    )
                }

                // 3 ADS REWARD MECHANIC
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkTheme) Color(0xFF1E1B4B) else Color(0xFFEEF2FF),
                    border = BorderStroke(1.5.dp, Color(0xFF6366F1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎁 WATCH 3 ADS: MEGA CHEST",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF818CF8)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6366F1)
                            ) {
                                Text(
                                    text = "$multiAdWatchCount / 3",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Watch 3 ads to unlock the mega bundle! (+3 💎, +500 🪙, +3 ⚡, +5 Tokens & 15m 2x Trophies)",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (slot in 1..3) {
                                val isDone = multiAdWatchCount >= slot
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDone) Color(0xFF10B981) else if (isDarkTheme) Color(0xFF312E81) else Color(0xFFC7D2FE),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (isDone) "✓ $slot. Watched" else "Ad $slot",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDone) Color.White else if (isDarkTheme) Color(0xFFA5B4FC) else Color(0xFF3730A3),
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val isMegaCooldown = megaChestRewardCooldownSeconds > 0
                        val megaCooldownFormatted = if (isMegaCooldown) {
                            val h = megaChestRewardCooldownSeconds / 3600
                            val m = (megaChestRewardCooldownSeconds % 3600) / 60
                            val s = megaChestRewardCooldownSeconds % 60
                            String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
                        } else ""

                        Button(
                            onClick = { onWatchRewardedAd("WATCH_3_ADS_REWARD") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMegaCooldown) Color(0xFF475569) else Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isMegaCooldown) {
                                    "⏳ 3-Hour Cooldown ($megaCooldownFormatted)"
                                } else {
                                    when (multiAdWatchCount) {
                                        0 -> "Watch Ad 1 (1/3)"
                                        1 -> "Watch Ad 2 (2/3)"
                                        else -> "🎉 Watch Final Ad & Open Mega Chest! (3/3)"
                                    }
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION: TIMED BOOSTERS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "⏱️ TIMED SPECIAL BOOSTERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B),
                        letterSpacing = 0.5.sp
                    )
                }

                // Timed Booster 1: 2x Trophies (15 Min)
                val doubleRemainingSec = ((doubleTrophiesExpiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                val isDoubleActive = doubleRemainingSec > 0
                TimedBoosterCard(
                    title = "⏱️ 2x Trophy Multiplier (15 Min)",
                    description = "Earn double trophies on level completions to climb the leaderboard faster.",
                    isActive = isDoubleActive,
                    remainingSeconds = doubleRemainingSec,
                    buttonText = "Watch Ad & Activate (15 Min)",
                    accentColor = Color(0xFFF59E0B),
                    isDarkTheme = isDarkTheme,
                    onClick = { onWatchRewardedAd("BOOSTER_DOUBLE_TROPHIES") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timed Booster 2: Unlimited Drills (10 Min)
                val infiniteRemainingSec = ((infiniteBreakersExpiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                val isInfiniteActive = infiniteRemainingSec > 0
                TimedBoosterCard(
                    title = "⏱️ Unlimited Drill Laser (10 Min)",
                    description = "Drill through obstacles freely for 10 minutes without depleting ammo.",
                    isActive = isInfiniteActive,
                    remainingSeconds = infiniteRemainingSec,
                    buttonText = "Watch Ad & Activate (10 Min)",
                    accentColor = Color(0xFFEF4444),
                    isDarkTheme = isDarkTheme,
                    onClick = { onWatchRewardedAd("BOOSTER_INFINITE_BREAKERS") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timed Booster 3: Flexible Shield (20 Min)
                val shrinkerRemainingSec = ((radiusShrinkerExpiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                val isShrinkerActive = shrinkerRemainingSec > 0
                TimedBoosterCard(
                    title = "⏱️ Flexible Echo Shield (20 Min)",
                    description = "Shrinks echo obstacles by 50% for 20 minutes to ease navigation.",
                    isActive = isShrinkerActive,
                    remainingSeconds = shrinkerRemainingSec,
                    buttonText = "Watch Ad & Activate (20 Min)",
                    accentColor = Color(0xFF10B981),
                    isDarkTheme = isDarkTheme,
                    onClick = { onWatchRewardedAd("BOOSTER_SHIELD") }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Ad 1: Rare Diamond
                AdRewardCard(
                    title = "Rare Diamond (+1 💎)",
                    description = "Earn rare diamonds. Used for chests and exchanges.",
                    buttonText = "Watch Ad (+1 💎)",
                    icon = Icons.Default.Diamond,
                    accentColor = Color(0xFF0284C7),
                    isDarkTheme = isDarkTheme,
                    cooldownSeconds = diamondRewardCooldownSeconds,
                    onClick = { onWatchRewardedAd("FREE_DIAMOND") },
                    testTag = "ad_free_diamond_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ad 2: Free 100 Coins
                AdRewardCard(
                    title = "100x Free Coins (+100 🪙)",
                    description = "Watch a short video and receive 100 coins immediately.",
                    buttonText = "Watch Ad (+100 🪙)",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = Color(0xFFD97706),
                    isDarkTheme = isDarkTheme,
                    cooldownSeconds = coinRewardCooldownSeconds,
                    onClick = { onWatchRewardedAd("FREE_COINS") },
                    testTag = "ad_free_coins_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ad 3: 1x Drill Laser
                AdRewardCard(
                    title = "1x Drill Laser (+1 ⚡)",
                    description = "Powerful laser ammo to drill through red echo barriers.",
                    buttonText = "Watch Ad (+1 ⚡)",
                    icon = Icons.Default.Bolt,
                    accentColor = Color(0xFFDC2626),
                    isDarkTheme = isDarkTheme,
                    cooldownSeconds = breakerRewardCooldownSeconds,
                    onClick = { onWatchRewardedAd("FREE_BREAKER") },
                    testTag = "ad_free_breaker_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ad 4: Clear Echoes
                AdRewardCard(
                    title = "Clear All Echoes (🔄)",
                    description = "Instantly destroy all red echo lines in the current level.",
                    buttonText = "Watch & Clear",
                    icon = Icons.Default.Refresh,
                    accentColor = Color(0xFF059669),
                    isDarkTheme = isDarkTheme,
                    cooldownSeconds = 0L,
                    onClick = { onWatchRewardedAd("CLEAR_ECHOES") },
                    testTag = "ad_clear_echoes_button"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 2: DIAMOND EXCHANGE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "IN-GAME DIAMOND EXCHANGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6),
                        letterSpacing = 0.5.sp
                    )
                }

                // Exchange 1: 1 Diamond -> 5 Tokens
                ExchangeCard(
                    title = "5x Hint Tokens Pack",
                    costText = "1 💎 Diamond",
                    canAfford = currentDiamonds >= 1,
                    isDarkTheme = isDarkTheme,
                    icon = Icons.Default.Lightbulb,
                    accentColor = Color(0xFFD97706),
                    onExchange = { onExchangeDiamondsForTokens(1, 5) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Exchange 2: 1 Diamond -> 2 Breakers
                ExchangeCard(
                    title = "2x Drill Laser Pack",
                    costText = "1 💎 Diamond",
                    canAfford = currentDiamonds >= 1,
                    isDarkTheme = isDarkTheme,
                    icon = Icons.Default.Bolt,
                    accentColor = Color(0xFFEF4444),
                    onExchange = { onExchangeDiamondsForBreakers(1, 2) }
                )
            }
        }
    }
}

@Composable
private fun AdRewardCard(
    title: String,
    description: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isDarkTheme: Boolean,
    cooldownSeconds: Long = 0L,
    onClick: () -> Unit,
    testTag: String
) {
    val isOnCooldown = cooldownSeconds > 0
    val formattedCooldown = if (isOnCooldown) {
        val h = cooldownSeconds / 3600
        val m = (cooldownSeconds % 3600) / 60
        val s = cooldownSeconds % 60
        String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    } else ""

    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, if (isOnCooldown) borderColor else accentColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isOnCooldown) Color(0xFF475569).copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isOnCooldown) Color(0xFF94A3B8) else accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnCooldown) textSecondary else textPrimary
                    )
                    Text(
                        text = if (isOnCooldown) "$description • (Cooldown: $formattedCooldown)" else description,
                        fontSize = 10.sp,
                        color = if (isOnCooldown) Color(0xFFEAB308) else textSecondary,
                        lineHeight = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnCooldown) Color(0xFF475569) else accentColor,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag(testTag)
            ) {
                Text(
                    text = if (isOnCooldown) formattedCooldown else buttonText,
                    fontSize = if (isOnCooldown) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExchangeCard(
    title: String,
    costText: String,
    canAfford: Boolean,
    isDarkTheme: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onExchange: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onExchange,
                enabled = canAfford,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6),
                    contentColor = Color.White,
                    disabledContainerColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = costText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TimedBoosterCard(
    title: String,
    description: String,
    isActive: Boolean,
    remainingSeconds: Long,
    buttonText: String,
    accentColor: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, if (isActive) Color(0xFF10B981) else if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                if (isActive) {
                    val m = remainingSeconds / 60
                    val s = remainingSeconds % 60
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981)
                    ) {
                        Text(
                            text = String.format("ACTIVE %02d:%02d", m, s),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (!isActive) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
