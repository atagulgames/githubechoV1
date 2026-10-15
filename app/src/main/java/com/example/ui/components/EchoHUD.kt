package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticEngine
import com.example.audio.hapticClick
import com.example.viewmodel.EchoUiState

/**
 * Cartoon Game UI / Cartoony Game GUI HUD components.
 * Bold 3D chunky cartoon styling with rich vibrant gradients, thick borders,
 * playful bevels, and distinct game action states.
 */
@Composable
fun EchoTopHUD(
    state: EchoUiState,
    onBackToMenu: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onOpenShop: () -> Unit,
    onAddTimeWithAd: (() -> Unit)? = null,
    onOpenMechanicGuide: (() -> Unit)? = null,
    onToggleGhostRace: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_hud_bar"),
        color = Color.Transparent
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 400.dp
            val horizontalPadding = if (isCompact) 8.dp else 14.dp
            val isTr = state.language == com.example.localization.Language.TR

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.Center)
                    .padding(horizontal = horizontalPadding, vertical = 6.dp)
            ) {
                // ROW 1: Back Button & Level Plaque | Timer, Tokens, Trophies, Stars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Chunky 3D Back button & Chunky Level Plaque
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp)
                    ) {
                        // 3D Cartoon Circular Back Button
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 38.dp else 42.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E40AF), Color(0xFF0F172A))
                                    )
                                )
                                .border(2.5.dp, Color(0xFF38BDF8), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = hapticClick(action = onBackToMenu)
                                )
                                .testTag("back_to_menu_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Return to Menu",
                                tint = Color.White,
                                modifier = Modifier.size(if (isCompact) 20.dp else 22.dp)
                            )
                        }

                        // 3D Cartoon Level Plaque
                        Box(
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFC700), Color(0xFFD97706))
                                    )
                                )
                                .border(2.dp, Color(0xFF78350F), RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = onOpenLevelSelect
                                )
                                .padding(
                                    horizontal = if (isCompact) 10.dp else 14.dp,
                                    vertical = if (isCompact) 5.dp else 7.dp
                                )
                                .testTag("level_select_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Select Level",
                                    tint = Color(0xFF451A03),
                                    modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (state.isDailyChallenge) "DAILY" else "LEVEL ${state.level.levelId}",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF451A03),
                                    fontSize = if (isCompact) 12.sp else 14.sp
                                )
                            }
                        }
                    }

                    // Right: Timer, Tokens & Echoes count
                    val timerShake = rememberInfiniteTransition(label = "timerShake")
                    val timerShakeOffset by if (state.levelRemainingTimeSec <= 10) {
                        timerShake.animateFloat(
                            initialValue = -3f,
                            targetValue = 3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(70, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "urgentShake"
                        )
                    } else {
                        timerShake.animateFloat(
                            initialValue = 0f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "steady"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)
                    ) {
                        // 60s Level Countdown Timer 3D Pill
                        val isUrgent = state.levelRemainingTimeSec <= 10
                        Box(
                            modifier = Modifier
                                .graphicsLayer { translationX = timerShakeOffset }
                                .shadow(3.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isUrgent) {
                                        Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                    }
                                )
                                .border(
                                    width = if (isUrgent) 2.dp else 1.5.dp,
                                    color = if (isUrgent) Color(0xFFFCA5A5) else Color(0xFF334155),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = onAddTimeWithAd != null) {
                                    onAddTimeWithAd?.invoke()
                                }
                                .padding(
                                    horizontal = if (isCompact) 7.dp else 9.dp,
                                    vertical = if (isCompact) 5.dp else 6.dp
                                )
                                .testTag("top_hud_timer_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isUrgent) "🔥" else "⏱️",
                                    fontSize = if (isCompact) 11.sp else 12.sp
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${state.levelRemainingTimeSec}s",
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = if (isCompact) 11.sp else 13.sp
                                )
                                if (onAddTimeWithAd != null && state.levelRemainingTimeSec <= 20) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+15s🎬",
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF34D399),
                                        fontSize = if (isCompact) 9.sp else 10.sp
                                    )
                                }
                            }
                        }

                        // Tokens 3D Chunky Pill
                        Box(
                            modifier = Modifier
                                .shadow(3.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                    )
                                )
                                .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
                                .clickable { onOpenShop() }
                                .padding(
                                    horizontal = if (isCompact) 7.dp else 9.dp,
                                    vertical = if (isCompact) 5.dp else 6.dp
                                )
                                .testTag("shop_token_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${state.tokens}",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    fontSize = if (isCompact) 11.sp else 13.sp
                                )
                            }
                        }

                        // Trophies 3D Chunky Pill
                        Box(
                            modifier = Modifier
                                .shadow(3.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                    )
                                )
                                .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
                                .padding(
                                    horizontal = if (isCompact) 6.dp else 8.dp,
                                    vertical = if (isCompact) 5.dp else 6.dp
                                )
                                .testTag("top_hud_trophies_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🏆", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${state.trophies}",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    fontSize = if (isCompact) 11.sp else 12.sp
                                )
                            }
                        }

                        // 3-Star Rating 3D Pill (Echo indicator completely removed per user request)
                        val currentStars = when {
                            state.echoCountForLevel == 0 -> 3
                            state.echoCountForLevel <= 2 -> 2
                            else -> 1
                        }

                        Box(
                            modifier = Modifier
                                .shadow(3.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(listOf(Color(0xFF3B0764), Color(0xFF1E1B4B)))
                                )
                                .border(
                                    1.5.dp,
                                    Color(0xFFA855F7),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(
                                    horizontal = if (isCompact) 8.dp else 10.dp,
                                    vertical = if (isCompact) 5.dp else 6.dp
                                )
                                .testTag("star_rating_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (currentStars) {
                                        3 -> "★★★"
                                        2 -> "★★☆"
                                        else -> "★☆☆"
                                    },
                                    color = when (currentStars) {
                                        3 -> Color(0xFFFFD700)
                                        2 -> Color(0xFFFBBF24)
                                        else -> Color(0xFFC084FC)
                                    },
                                    fontSize = if (isCompact) 13.sp else 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ROW 2: Dynamic Level Rule & Onboarding Guide Pill
                val levelRule = com.example.data.LevelRuleCatalog.getRuleForLevel(state.level.levelId, isTurkish = false)
                val badgeColor = Color(levelRule.badgeColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A1538).copy(alpha = 0.9f))
                            .border(1.5.dp, badgeColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = onOpenMechanicGuide != null) { onOpenMechanicGuide?.invoke() }
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                            .testTag("tier_mechanic_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${levelRule.icon} Rule: ${levelRule.ruleTitle}",
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (state.playerBehaviorInsight.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🧠 ${state.playerBehaviorInsight}",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/* ========================================================================== */
/*                          CARTOON BOTTOM HUD                                */
/* ========================================================================== */

@Composable
fun EchoBottomHUD(
    state: EchoUiState,
    onReset: () -> Unit,
    onUseArcaneBlast: () -> Unit,
    onUseTimeFreeze: () -> Unit,
    onUseNumberShift: () -> Unit,
    onUseLuckyCrystal: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTr = state.language == com.example.localization.Language.TR

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .testTag("bottom_hud_bar"),
        color = Color(0xFF070F2B),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = BorderStroke(2.dp, Color(0xFF1E3A8A))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 400.dp
            val horizontalPadding = if (isCompact) 6.dp else 12.dp
            val btnHeight = if (isCompact) 40.dp else 44.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.Center)
                    .padding(horizontal = horizontalPadding, vertical = if (isCompact) 6.dp else 8.dp)
            ) {
                // ROW 1: FANTASTIC MAGIC BOOSTERS (Arcane Blast, Time Freeze, Number Shift, Lucky Crystal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp)
                ) {
                    // 1. Arcane Blast
                    CartoonChunkyButton(
                        onClick = hapticClick(isHeavy = true, action = onUseArcaneBlast),
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .testTag("arcane_blast_button"),
                        gradientColors = listOf(Color(0xFF9333EA), Color(0xFF6B21A8)),
                        borderColor = Color(0xFFC084FC),
                        textColor = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Arcane Blast",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Blast",
                                fontSize = if (isCompact) 10.sp else 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 2. Time Freeze
                    CartoonChunkyButton(
                        onClick = hapticClick(isHeavy = true, action = onUseTimeFreeze),
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .testTag("time_freeze_button"),
                        gradientColors = if (state.isTimeFrozen) {
                            listOf(Color(0xFF06B6D4), Color(0xFF0891B2))
                        } else {
                            listOf(Color(0xFF0E7490), Color(0xFF155E75))
                        },
                        borderColor = if (state.isTimeFrozen) Color(0xFF67E8F9) else Color(0xFF22D3EE),
                        textColor = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Time Freeze",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Freeze",
                                fontSize = if (isCompact) 10.sp else 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 3. Number Shift
                    CartoonChunkyButton(
                        onClick = hapticClick(isHeavy = true, action = onUseNumberShift),
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .testTag("number_shift_button"),
                        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4338CA)),
                        borderColor = Color(0xFFA5B4FC),
                        textColor = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = "Number Shift",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Shift",
                                fontSize = if (isCompact) 10.sp else 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 4. Lucky Crystal
                    CartoonChunkyButton(
                        onClick = hapticClick(isHeavy = true, action = onUseLuckyCrystal),
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .testTag("lucky_crystal_button"),
                        gradientColors = if (state.isLuckyCrystalActive) {
                            listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                        } else {
                            listOf(Color(0xFFB45309), Color(0xFF78350F))
                        },
                        borderColor = if (state.isLuckyCrystalActive) Color(0xFFFDE68A) else Color(0xFFF59E0B),
                        textColor = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = "Lucky Crystal",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Lucky",
                                fontSize = if (isCompact) 10.sp else 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 7.dp))

                // ROW 2: Reset & Hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    CartoonChunkyButton(
                        onClick = {
                            HapticEngine.triggerButtonDoublePulse()
                            onReset()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .testTag("reset_button"),
                        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                        borderColor = Color(0xFF1E3A8A),
                        textColor = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Level",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Hint Button
                    CartoonChunkyButton(
                        onClick = hapticClick(action = onHint),
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .testTag("hint_button"),
                        gradientColors = if (state.isHintActive) {
                            listOf(Color(0xFFFFD54F), Color(0xFFF59E0B))
                        } else {
                            listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                        },
                        borderColor = if (state.isHintActive) Color(0xFFB45309) else Color(0xFF075985),
                        textColor = if (state.isHintActive) Color(0xFF78350F) else Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = if (state.isHintActive) Color(0xFF78350F) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isHintActive) "💡 Active" else "Hint",
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (state.isHintActive) Color(0xFF78350F) else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ========================================================================== */
/*                       REUSABLE CARTOON 3D BUTTON                           */
/* ========================================================================== */

@Composable
private fun CartoonChunkyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    borderColor: Color,
    textColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(colors = gradientColors))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
