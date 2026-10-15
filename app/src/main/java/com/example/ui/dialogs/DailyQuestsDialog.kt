package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.hapticClick
import com.example.model.DailyQuest

/**
 * Cartoon Game UI / Cartoony Game GUI Daily Quests Dialog.
 * Rich 3D golden titles, chunky quest cards with drop borders,
 * playful progress bars, and vibrant claim buttons.
 */
@Composable
fun DailyQuestsDialog(
    quests: List<DailyQuest>,
    onClaimQuest: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .shadow(28.dp, RoundedCornerShape(26.dp))
                .testTag("daily_quests_dialog"),
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
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFFC700), Color(0xFFD97706))
                                        )
                                    )
                                    .border(2.dp, Color(0xFF78350F), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CrisisAlert,
                                    contentDescription = null,
                                    tint = Color(0xFF451A03),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DAILY QUESTS",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Resets every day at 00:00",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        // Chunky Red Close Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFF5252), Color(0xFFC62828))
                                    )
                                )
                                .border(2.dp, Color(0xFF7F1D1D), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = hapticClick(action = onDismiss)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(quests, key = { it.id }) { quest ->
                            QuestCard(
                                quest = quest,
                                onClaim = { onClaimQuest(quest.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chunky 3D Close Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                                )
                            )
                            .border(2.dp, Color(0xFF475569), RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White),
                                onClick = hapticClick(action = onDismiss)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLOSE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestCard(
    quest: DailyQuest,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F1E4A),
        border = BorderStroke(
            1.5.dp,
            if (quest.isCompleted && !quest.isClaimed) Color(0xFFFFD54F) else Color(0xFF1E3A8A)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = quest.description,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rewards Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quest.rewardCoins > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFC700), Color(0xFFD97706))
                                    )
                                )
                                .border(1.dp, Color(0xFF78350F), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🪙 +${quest.rewardCoins}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF451A03)
                            )
                        }
                    }
                    if (quest.rewardTokens > 0) {
                        if (quest.rewardCoins > 0) Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                    )
                                )
                                .border(1.dp, Color(0xFF0369A1), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "💡 +${quest.rewardTokens}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    if (quest.rewardBreakers > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFF9800), Color(0xFFE65100))
                                    )
                                )
                                .border(1.dp, Color(0xFF7C2D12), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⚡ +${quest.rewardBreakers}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar and action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA)
                        )
                        Text(
                            text = "${quest.current.coerceAtMost(quest.target)} / ${quest.target}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (quest.current.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (quest.isCompleted) Color(0xFF10B981) else Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                when {
                    quest.isClaimed -> {
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF064E3B))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Claimed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                    quest.isCompleted -> {
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF22C55E), Color(0xFF15803D))
                                    )
                                )
                                .border(1.5.dp, Color(0xFF14532D), RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = hapticClick(isHeavy = true, action = onClaim)
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CLAIM REWARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "In Progress",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
