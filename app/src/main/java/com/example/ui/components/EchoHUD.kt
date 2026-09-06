package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.viewmodel.EchoUiState

@Composable
fun EchoTopHUD(
    state: EchoUiState,
    onBackToMenu: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onOpenShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hudCardBg = if (state.isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (state.isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSecondary = if (state.isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val hudBorder = if (state.isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_hud_bar"),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button & Level Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToMenu,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(hudCardBg)
                            .border(1.dp, hudBorder, CircleShape)
                            .testTag("back_to_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Menüye Dön",
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(hudCardBg)
                            .border(1.dp, hudBorder, RoundedCornerShape(20.dp))
                            .clickable { onOpenLevelSelect() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("level_select_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Bölüm Seç",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isDailyChallenge) "Günün Bulmacası" else "Bölüm ${state.level.levelId}/100",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                // Right: Tokens & Echoes count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tokens chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(hudCardBg)
                            .border(1.dp, hudBorder, RoundedCornerShape(20.dp))
                            .clickable { onOpenShop() }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("shop_token_chip")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "Jetonlar",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.tokens}",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Mağaza",
                            tint = textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Trophies chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(hudCardBg)
                            .border(1.dp, hudBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 7.dp)
                            .testTag("top_hud_trophies_chip")
                    ) {
                        Text(
                            text = "${state.trophies} 🏆",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Echo count chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (state.echoCountForLevel > 0) (if (state.isDarkTheme) Color(0xFF4C0519) else Color(0xFFFFF1F2)) else hudCardBg)
                            .border(
                                1.dp,
                                if (state.echoCountForLevel > 0) Color(0xFFE11D48) else hudBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("echo_counter_chip")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (state.echoCountForLevel > 0) Color(0xFFE11D48) else Color(0xFF0284C7))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Yankı: ${state.echoCountForLevel}",
                            fontWeight = FontWeight.Bold,
                            color = if (state.echoCountForLevel > 0) Color(0xFFE11D48) else textPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Subtitle & Mechanic Info Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.level.title,
                        color = textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (state.currentCombo > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ ${state.currentCombo}x Kombo",
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.level.mechanicType != "STANDARD") {
                        val badgeText = when (state.level.mechanicType) {
                            "DECAYING" -> "⏱ Zaman Ayarlı"
                            "LOCK_KEY" -> "🔑 Kilit & Anahtar"
                            "GHOST" -> "👻 Hayalet Yankı"
                            "ONE_WAY" -> "➔ Yönlü Kenar"
                            else -> state.level.mechanicType
                        }
                        Text(
                            text = badgeText,
                            color = Color(0xFF0284C7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (state.isDarkTheme) Color(0xFF0C4A6E) else Color(0xFFE0F2FE))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Hedef: ${state.level.parEchoes}",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EchoBottomHUD(
    state: EchoUiState,
    onReset: () -> Unit,
    onClearEchoes: () -> Unit,
    onUseBreaker: () -> Unit,
    onActivateShrinker: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBg = if (state.isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (state.isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSecondary = if (state.isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (state.isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("bottom_hud_bar"),
        color = bottomBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Row 1: Tactical Power-ups (Matkap Lazer, Esnek Alan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Echo Breaker (Matkap Lazer)
                FilledTonalButton(
                    onClick = onUseBreaker,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(1.dp, if (state.echoBreakers > 0) Color(0xFFFED7AA) else borderColor, RoundedCornerShape(12.dp))
                        .testTag("echo_breaker_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.echoBreakers > 0) (if (state.isDarkTheme) Color(0xFF431407) else Color(0xFFFFF7ED)) else (if (state.isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                        contentColor = Color(0xFFF97316)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Matkap Lazeri",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Matkap (${state.echoBreakers})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Echo Shrinker (Esnek Alan)
                FilledTonalButton(
                    onClick = onActivateShrinker,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(
                            1.dp,
                            if (state.isEchoShrinkerActive) Color(0xFF86EFAC) else borderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("echo_shrinker_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.isEchoShrinkerActive) (if (state.isDarkTheme) Color(0xFF064E3B) else Color(0xFFDCFCE7)) else (if (state.isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                        contentColor = if (state.isEchoShrinkerActive) Color(0xFF4ADE80) else textSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterCenterFocus,
                        contentDescription = "Esnek Alan",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (state.isEchoShrinkerActive) "Esnek Aktif" else if (state.shrinkerAdsWatched in 1..2) "Esnek (${state.shrinkerAdsWatched}/3)" else "Esnek (3🎬+5💎)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Reset, Clear Echoes, Hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Yeniden" (Reset)
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("reset_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yeniden Başla",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Yeniden",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // "Yankı Sil" (Clear Echoes)
                FilledTonalButton(
                    onClick = onClearEchoes,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .border(
                            1.dp,
                            if (state.echoes.isNotEmpty()) Color(0xFFE11D48) else borderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("clear_echoes_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.echoes.isNotEmpty()) (if (state.isDarkTheme) Color(0xFF4C0519) else Color(0xFFFFF1F2)) else (if (state.isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                        contentColor = if (state.echoes.isNotEmpty()) Color(0xFFE11D48) else textSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = "Yankı Sil",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.echoes.isNotEmpty()) "Yankı Sil (${state.echoes.size})" else "Yankı Sil",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // "İpucu" (Hint)
                Button(
                    onClick = onHint,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("hint_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isHintActive) Color(0xFFF59E0B) else Color(0xFF0284C7),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "İpucu",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.isHintActive) "İpucu Aktif" else if (state.tokens > 0) "İpucu (${state.tokens})" else "İpucu (🎬)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
