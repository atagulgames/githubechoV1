package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LeaderboardPlayer

@Composable
fun LeaderboardDialog(
    players: List<LeaderboardPlayer>,
    isDarkTheme: Boolean = true,
    onSelectPlayer: (LeaderboardPlayer) -> Unit,
    onViewMyProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    var selectedCategory by remember { mutableStateOf(0) } // 0: Global, 1: Türkiye, 2: Top 10
    val filteredPlayers = remember(players, selectedCategory) {
        when (selectedCategory) {
            1 -> players.filter { it.username.contains("🇹🇷") || it.username.contains("TR") || it.isCurrentUser }
            2 -> players.take(10)
            else -> players
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 700.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("leaderboard_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Liderlik Tablosu",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Gerçek Global Oyuncu Sıralaması",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("leaderboard_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Tabs: Global, Türkiye, Top 10
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("🌍 Global", "🇹🇷 Türkiye", "🏆 Top 10")
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedCategory == index
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF3B82F6) else if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF60A5FA) else borderColor),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = index }
                        ) {
                            Text(
                                text = tabTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else textSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Top 3 Podium (Shown on Global view if enough players)
                if (selectedCategory == 0 && players.size >= 3) {
                    val p1 = players.getOrNull(0)
                    val p2 = players.getOrNull(1)
                    val p3 = players.getOrNull(2)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd Place (Silver)
                        p2?.let {
                            PodiumColumn(
                                player = it,
                                rank = 2,
                                heightDp = 64,
                                color = Color(0xFF94A3B8),
                                isDarkTheme = isDarkTheme,
                                onClick = { onSelectPlayer(it) }
                            )
                        }

                        // 1st Place (Gold, Tallest)
                        p1?.let {
                            PodiumColumn(
                                player = it,
                                rank = 1,
                                heightDp = 80,
                                color = Color(0xFFF59E0B),
                                isDarkTheme = isDarkTheme,
                                onClick = { onSelectPlayer(it) }
                            )
                        }

                        // 3rd Place (Bronze)
                        p3?.let {
                            PodiumColumn(
                                player = it,
                                rank = 3,
                                heightDp = 52,
                                color = Color(0xFFD97706),
                                isDarkTheme = isDarkTheme,
                                onClick = { onSelectPlayer(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Quick Action: "Benim Profilim" Button
                OutlinedButton(
                    onClick = onViewMyProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("view_my_profile_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF3B82F6)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Profilimi ve Bölüm İstatistiklerimi Görüntüle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Table Column Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SIRA / OYUNCU",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "YANKI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                        Text(
                            text = "KUPA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                    }
                }

                // Players List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredPlayers, key = { it.id }) { player ->
                        LeaderboardPlayerItem(
                            player = player,
                            isDarkTheme = isDarkTheme,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            borderColor = borderColor,
                            onClick = { onSelectPlayer(player) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("leaderboard_done_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )
                ) {
                    Text(
                        text = "Kapat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPlayerItem(
    player: LeaderboardPlayer,
    isDarkTheme: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    val rankBadgeColor = when (player.rank) {
        1 -> Color(0xFFF59E0B) // Gold
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFD97706) // Bronze
        else -> textSecondary
    }

    val itemBg = if (player.isCurrentUser) {
        if (isDarkTheme) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)
    } else {
        if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    }

    val itemBorder = if (player.isCurrentUser) {
        BorderStroke(1.5.dp, Color(0xFF3B82F6))
    } else {
        BorderStroke(1.dp, borderColor)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = itemBg,
        border = itemBorder,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("leaderboard_player_${player.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Rank & Avatar & Username + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank number
                Box(
                    modifier = Modifier.width(28.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "#${player.rank}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = rankBadgeColor
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = player.avatarEmoji, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.username,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    text = "SEN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = player.title,
                            fontSize = 10.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "•", fontSize = 10.sp, color = textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatShortTime(player.totalPlayTimeSec),
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }

            // Right: Echoes & Trophies
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Total Echoes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = "Yankı",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${player.totalEchoes}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF06B6D4)
                    )
                }

                // Trophies
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${player.trophies} 🏆",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

private fun formatShortTime(seconds: Long): String {
    if (seconds <= 0) return "0 dk"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}s ${minutes}d" else "${minutes} dk"
}

@Composable
private fun PodiumColumn(
    player: LeaderboardPlayer,
    rank: Int,
    heightDp: Int,
    color: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        // Avatar + Crown
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.avatarEmoji, fontSize = 20.sp)
            }
            if (rank == 1) {
                Text(
                    text = "👑",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = player.username.replace("👑 ", "").replace("⚡ ", "").replace("🚀 ", ""),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${player.trophies} 🏆",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Pedestal
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = color.copy(alpha = 0.25f),
            border = BorderStroke(1.dp, color),
            modifier = Modifier
                .width(68.dp)
                .height(heightDp.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "#$rank",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
        }
    }
}
