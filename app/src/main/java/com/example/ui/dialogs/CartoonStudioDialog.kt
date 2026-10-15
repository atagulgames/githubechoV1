package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

/**
 * Interactive Cartoon Studio Dialog (ECHO FLUX - Karikatür).
 * Allows users to watch Yandex rewarded ads to earn free cartoon creation credits,
 * and design / transform cartoon avatars with comic effects.
 */
@Composable
fun CartoonStudioDialog(
    credits: Int,
    onWatchRewardedAd: () -> Unit,
    onUseCredit: () -> Boolean,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedStyleIndex by remember { mutableIntStateOf(0) }
    var selectedAccessoryIndex by remember { mutableIntStateOf(0) }
    var speechText by remember { mutableStateOf("ECHO FLUX Karikatürüm!") }
    var isTransforming by remember { mutableStateOf(false) }
    var cartoonVersion by remember { mutableIntStateOf(1) }

    val styles = listOf(
        "Mizah Çizgisi" to Color(0xFFFFB300),
        "Neon Komik" to Color(0xFF00E5FF),
        "Kahraman" to Color(0xFFFF3D00),
        "Retro Pop" to Color(0xFFE040FB)
    )

    val accessories = listOf(
        "Yok" to "✨",
        "Süper Gözlük" to "🕶️",
        "Gülümseyen Bıyık" to "🥸",
        "Altın Taç" to "👑",
        "Parti Şapkası" to "🎉"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "CartoonGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("cartoon_studio_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A153A),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F1E4A), Color(0xFF070E28))
                        )
                    )
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎨 KARİKATÜR STÜDYOSU",
                            color = Color(0xFFFFD54F),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Yandex Ödüllü Reklamları ile Ücretsiz Oluştur",
                            color = Color(0xFF90CAF9),
                            fontSize = 12.sp
                        )
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF263238))
                            .clickable { onDismiss() }
                            .testTag("close_cartoon_studio_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Credits Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA))
                            )
                        )
                        .border(2.dp, Color(0xFFBA68C8), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎨", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Kalan Karikatür Hakkı",
                                    color = Color(0xFFE1BEE7),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$credits Ücretsiz Hak",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Watch Rewarded Ad Action Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFB300))
                                .clickable { onWatchRewardedAd() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("watch_rewarded_ad_studio_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+1 KAZAN",
                                    color = Color(0xFF1A1A1A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Cartoon Canvas Preview Board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .scale(if (isTransforming) 1.05f else glowScale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF131D42))
                        .border(3.dp, styles[selectedStyleIndex].second, RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Cartoon Character Drawing
                    CartoonCharacterCanvas(
                        styleColor = styles[selectedStyleIndex].second,
                        accessoryEmoji = accessories[selectedAccessoryIndex].second,
                        speechBubbleText = speechText,
                        version = cartoonVersion
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Style Selector Chips
                Text(
                    text = "Karikatür Tarzını Seç:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styles.forEachIndexed { index, (name, color) ->
                        val isSelected = index == selectedStyleIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color else Color(0xFF1C274C))
                                .clickable { selectedStyleIndex = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) Color(0xFF111111) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Accessory Selector
                Text(
                    text = "Aksesuar Ekle:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accessories.forEachIndexed { index, (label, emoji) ->
                        val isSelected = index == selectedAccessoryIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF00E5FF) else Color(0xFF1C274C))
                                .clickable { selectedAccessoryIndex = index }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = emoji, fontSize = 18.sp)
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.LightGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button 1: TRANSFORM / CREATE CARTOON (Uses 1 Credit)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (credits > 0) listOf(Color(0xFFFF9100), Color(0xFFFF3D00))
                                else listOf(Color(0xFF546E7A), Color(0xFF37474F))
                            )
                        )
                        .clickable {
                            if (credits > 0) {
                                val used = onUseCredit()
                                if (used) {
                                    isTransforming = true
                                    cartoonVersion++
                                    speechText = "Karikatürüm Yenilendi! 🎨"
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(600)
                                        isTransforming = false
                                    }
                                }
                            } else {
                                onWatchRewardedAd()
                            }
                        }
                        .testTag("create_cartoon_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (credits > 0) "✨" else "🔒", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (credits > 0) "KARİKATÜRÜ OLUŞTUR (1 Hak Harca)"
                            else "HAK BİTTİ - REKLAM İZLE & KAZAN",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Action Button 2: WATCH REWARDED AD DIRECTLY (Always unlimited)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00C853), Color(0xFF64DD17))
                            )
                        )
                        .clickable { onWatchRewardedAd() }
                        .testTag("watch_rewarded_ad_direct_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ÖDÜLLÜ REKLAM İZLE (+1 Hak)",
                            color = Color(0xFF052B0E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartoonCharacterCanvas(
    styleColor: Color,
    accessoryEmoji: String,
    speechBubbleText: String,
    version: Int
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.42f
            val cy = size.height * 0.58f

            // Character Body (Comic Style)
            drawCircle(
                color = styleColor,
                radius = 48.dp.toPx(),
                center = Offset(cx, cy + 30.dp.toPx())
            )

            // Cartoon Head / Face
            drawCircle(
                color = Color(0xFFFFCC80),
                radius = 40.dp.toPx(),
                center = Offset(cx, cy - 10.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF3E2723),
                radius = 40.dp.toPx(),
                center = Offset(cx, cy - 10.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // Giant Cartoon Eyes
            val eyeOffsetX = 14.dp.toPx()
            val eyeOffsetY = (cy - 16.dp.toPx())
            drawCircle(
                color = Color.White,
                radius = 11.dp.toPx(),
                center = Offset(cx - eyeOffsetX, eyeOffsetY)
            )
            drawCircle(
                color = Color.White,
                radius = 11.dp.toPx(),
                center = Offset(cx + eyeOffsetX, eyeOffsetY)
            )

            // Pupils
            drawCircle(
                color = Color.Black,
                radius = 6.dp.toPx(),
                center = Offset(cx - eyeOffsetX + 2.dp.toPx(), eyeOffsetY)
            )
            drawCircle(
                color = Color.Black,
                radius = 6.dp.toPx(),
                center = Offset(cx + eyeOffsetX + 2.dp.toPx(), eyeOffsetY)
            )

            // Eye sparkle
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(cx - eyeOffsetX + 4.dp.toPx(), eyeOffsetY - 2.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(cx + eyeOffsetX + 4.dp.toPx(), eyeOffsetY - 2.dp.toPx())
            )

            // Big Comic Grin
            val smilePath = Path().apply {
                moveTo(cx - 16.dp.toPx(), cy + 6.dp.toPx())
                quadraticBezierTo(
                    cx, cy + 24.dp.toPx(),
                    cx + 16.dp.toPx(), cy + 6.dp.toPx()
                )
            }
            drawPath(
                path = smilePath,
                color = Color(0xFFD50000)
            )
            drawPath(
                path = smilePath,
                color = Color.Black,
                style = Stroke(width = 3.dp.toPx())
            )

            // Cheerful rosy cheeks
            drawCircle(
                color = Color(0xFFFF5252).copy(alpha = 0.6f),
                radius = 6.dp.toPx(),
                center = Offset(cx - 24.dp.toPx(), cy + 2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFF5252).copy(alpha = 0.6f),
                radius = 6.dp.toPx(),
                center = Offset(cx + 24.dp.toPx(), cy + 2.dp.toPx())
            )
        }

        // Accessory Emoji Overlay (Taç, Gözlük, Şapka)
        if (accessoryEmoji != "✨") {
            Text(
                text = accessoryEmoji,
                fontSize = 32.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 85.dp, end = 40.dp)
            )
        }

        // Comic Speech Bubble
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = speechBubbleText,
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
