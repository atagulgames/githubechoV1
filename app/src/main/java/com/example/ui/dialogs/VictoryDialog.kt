package com.example.ui.dialogs

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.hapticClick
import com.example.model.TrophyRewardBreakdown

/**
 * Cartoon Game UI / Cartoony Game GUI Victory Screen.
 * Chunky 3D golden plaques, giant celebratory cartoon stars,
 * rich 3D reward cards, and vibrant arcade buttons.
 */
@Composable
fun VictoryDialog(
    levelId: Int,
    echoCount: Int,
    parEchoes: Int,
    trophyBreakdown: TrophyRewardBreakdown? = null,
    isDoubleClaimed: Boolean = false,
    onClaimDoubleReward: () -> Unit = {},
    onNextLevel: () -> Unit,
    onReplay: () -> Unit
) {
    val stars = when {
        echoCount == 0 -> 3
        echoCount <= 2 -> 2
        else -> 1
    }

    val pulseTransition = rememberInfiniteTransition(label = "starPulse")
    val starScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = { /* Modal choice required */ }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(28.dp, RoundedCornerShape(26.dp))
                .testTag("victory_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = Color.Transparent,
            border = BorderStroke(3.dp, Color(0xFF1B3B87))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF070F28),
                                Color(0xFF0E1F4D),
                                Color(0xFF0A1438)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. TOP TROPHY BADGE WITH GLOW
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300))
                                )
                            )
                            .border(3.dp, Color(0xFFB45309), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Victory",
                            tint = Color(0xFF78350F),
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. CELEBRATORY TITLE
                    Text(
                        text = "LEVEL $levelId COMPLETED!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F),
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. GIANT 3D CARTOON STARS
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .scale(starScale)
                    ) {
                        for (i in 1..3) {
                            val isEarned = i <= stars
                            val starSize = if (i == 2) 46.dp else 38.dp

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(starSize)
                                    .shadow(if (isEarned) 6.dp else 0.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        if (isEarned) {
                                            Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFF59E0B)))
                                        } else {
                                            Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                                        }
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (isEarned) Color(0xFFB45309) else Color(0xFF475569),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $i",
                                    tint = if (isEarned) Color(0xFF78350F) else Color(0xFF64748B),
                                    modifier = Modifier.size(if (i == 2) 30.dp else 24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. STATS SUMMARY PLAQUE (Echo, Time, Par)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F1E4A),
                        border = BorderStroke(1.5.dp, Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ECHO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA)
                                )
                                Text(
                                    text = if (echoCount == 0) "FLAWLESS!" else "$echoCount",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (echoCount == 0) Color(0xFF34D399) else Color(0xFF38BDF8)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TIME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA)
                                )
                                Text(
                                    text = "${trophyBreakdown?.timeTakenSec ?: 6.5f}s",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFA78BFA)
                                )
                            }

                            if (parEchoes > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "HEDEF PAR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF60A5FA)
                                    )
                                    Text(
                                        text = "$parEchoes",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. TROPHIES EARNED CARD
                    if (trophyBreakdown != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F1E4A),
                            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🏆", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Trophies Earned",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "+${trophyBreakdown.totalTrophies} 🏆",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD54F)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (trophyBreakdown.zeroEchoBonus > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF064E3B))
                                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "0 Echo +${trophyBreakdown.zeroEchoBonus}🏆",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF34D399)
                                            )
                                        }
                                    }
                                    if (trophyBreakdown.comboBonus > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF312E81))
                                                .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${trophyBreakdown.currentCombo}x Combo +${trophyBreakdown.comboBonus}🏆",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFA5B4FC)
                                            )
                                        }
                                    }
                                    if (trophyBreakdown.speedBonus > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF581C87))
                                                .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Speed +${trophyBreakdown.speedBonus}🏆",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFE9D5FF)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 6. 2X REWARD BUTTON (CARTOON PURPLE 3D)
                    if (isDoubleClaimed) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2X Reward Claimed (+2 Tokens & +1 Drill)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .shadow(4.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFA855F7), Color(0xFF7E22CE))
                                    )
                                )
                                .border(2.dp, Color(0xFF581C87), RoundedCornerShape(14.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = hapticClick(isHeavy = true, action = onClaimDoubleReward)
                                )
                                .testTag("victory_2x_reward_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OndemandVideo,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EARN 2X REWARD (+2 Tokens, +1 Drill)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. HUGE CHUNKY 3D "NEXT LEVEL" BUTTON
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFC700), Color(0xFFD98200))
                                )
                            )
                            .border(2.5.dp, Color(0xFF944900), RoundedCornerShape(18.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White),
                                onClick = hapticClick(isHeavy = true, action = onNextLevel)
                            )
                            .testTag("victory_next_level_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF451A03),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NEXT LEVEL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF451A03),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 8. CHUNKY 3D "REPLAY" BUTTON
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .shadow(3.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                                )
                            )
                            .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White),
                                onClick = hapticClick(action = onReplay)
                            )
                            .testTag("victory_replay_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Replay",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
