package com.example.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class ConfettiPiece(
    var x: Float,
    var y: Float,
    var speedY: Float,
    var speedX: Float,
    var size: Float,
    var rotation: Float,
    var rotSpeed: Float,
    val color: Color
)

@Composable
fun Season2WelcomeDialog(
    isDarkTheme: Boolean,
    onClaimAndStart: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Season2Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Animated live confetti generator
    var confettiList by remember {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFF38BDF8), // Cyan
            Color(0xFFF43F5E), // Rose
            Color(0xFF10B981), // Emerald
            Color(0xFFA855F7), // Purple
            Color(0xFFFB923C)  // Orange
        )
        val list = List(65) {
            ConfettiPiece(
                x = Random.nextFloat() * 1000f,
                y = Random.nextFloat() * -500f,
                speedY = Random.nextFloat() * 8f + 4f,
                speedX = (Random.nextFloat() - 0.5f) * 4f,
                size = Random.nextFloat() * 8f + 6f,
                rotation = Random.nextFloat() * 360f,
                rotSpeed = (Random.nextFloat() - 0.5f) * 12f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
        mutableStateOf(list)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            confettiList.forEach { c ->
                c.y += c.speedY
                c.x += c.speedX
                c.rotation += c.rotSpeed
                if (c.y > 1800f) {
                    c.y = Random.nextFloat() * -200f
                    c.x = Random.nextFloat() * 1000f
                }
            }
        }
    }

    val cardBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569)
    val borderColor = Color(0xFFFFB703)

    Dialog(
        onDismissRequest = onClaimAndStart,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .testTag("season_2_welcome_dialog"),
            contentAlignment = Alignment.Center
        ) {
            // Live Confetti Rain Canvas Layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                confettiList.forEach { p ->
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(p.x, p.y),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    }
                }
            }

            // Main Season 2 Announcement Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 24.dp)
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(Color(0xFFFFB703), Color(0xFF38BDF8), Color(0xFFF43F5E))),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = cardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Radiant Season 2 Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFB703), Color(0xFFFB8500))
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✨ SEZON 2 BAŞLADI ✨",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "YENİ ÇAĞ: YANKI EVRENİ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tüm bölümler ve ilerleme Sezon 2 için sıfırlandı! Yepyeni 100 bölüm, taze geometriler ve katlanan ödüller seni bekliyor.",
                        fontSize = 13.sp,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Feature highlights container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Season2FeatureRow(
                            icon = Icons.Default.Refresh,
                            iconTint = Color(0xFF38BDF8),
                            title = "Her Şey Sıfırlandı & Yenilendi",
                            desc = "Yeni sezonla birlikte yıldızlar ve aşamalar baştan başladı; zirveye tırman!"
                        )
                        Season2FeatureRow(
                            icon = Icons.Default.AutoAwesome,
                            iconTint = Color(0xFFFFB703),
                            title = "100 Yeni & Benzersiz Bölüm",
                            desc = "Sezon 2'ye özel farklı açılar, yenilenen düğüm dizilimleri ve rotasyonlar."
                        )
                        Season2FeatureRow(
                            icon = Icons.Default.EmojiEvents,
                            iconTint = Color(0xFF10B981),
                            title = "Katlanan Sezon Ödülleri",
                            desc = "Bölüm zaferleri, günlük sandıklar ve görev hediyeleri 2.5 kat artırıldı."
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Welcome Starter Pack Box
                    Text(
                        text = "🎁 SEZON 2 BAŞLANGIÇ PAKETİ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB703),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Season2RewardPill(
                            icon = "🪙",
                            amount = "+500",
                            label = "Jeton",
                            modifier = Modifier.weight(1f),
                            isDark = isDarkTheme
                        )
                        Season2RewardPill(
                            icon = "💎",
                            amount = "+50",
                            label = "Elmas",
                            modifier = Modifier.weight(1f),
                            isDark = isDarkTheme
                        )
                        Season2RewardPill(
                            icon = "⚡",
                            amount = "+5",
                            label = "Kırıcı",
                            modifier = Modifier.weight(1f),
                            isDark = isDarkTheme
                        )
                        Season2RewardPill(
                            icon = "🏆",
                            amount = "+100",
                            label = "Kupa",
                            modifier = Modifier.weight(1f),
                            isDark = isDarkTheme
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Action Button
                    Button(
                        onClick = onClaimAndStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_season_2_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SEZON 2'YE BAŞLA! 🚀",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Season2FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun Season2RewardPill(
    icon: String,
    amount: String,
    label: String,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }
    }
}
