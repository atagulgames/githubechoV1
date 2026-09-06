package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.data.LevelCatalog
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LeaderboardPlayer
import com.example.model.LevelRecord

@Composable
fun PlayerProfileDialog(
    player: LeaderboardPlayer,
    isDarkTheme: Boolean = true,
    onDismiss: () -> Unit
) {
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val itemBg = if (isDarkTheme) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    val formattedPlayTime = formatPlayTime(player.totalPlayTimeSec)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("player_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Avatar, Name, Title, and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFF312E81) else Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = player.avatarEmoji, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.username,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (player.isCurrentUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "SEN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${player.title} • Sıra #${player.rank}",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("profile_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metric Cards Grid (2x2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileMetricCard(
                            icon = Icons.Default.EmojiEvents,
                            iconColor = Color(0xFFF59E0B),
                            title = "Toplam Kupa",
                            value = "${player.trophies} 🏆",
                            textColor = textPrimary,
                            subTextColor = textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetricCard(
                            icon = Icons.Default.Waves,
                            iconColor = Color(0xFF06B6D4),
                            title = "Toplam Yankı",
                            value = "${player.totalEchoes}",
                            textColor = textPrimary,
                            subTextColor = textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileMetricCard(
                            icon = Icons.Default.Schedule,
                            iconColor = Color(0xFF8B5CF6),
                            title = "Oyun Süresi",
                            value = formattedPlayTime,
                            textColor = textPrimary,
                            subTextColor = textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetricCard(
                            icon = Icons.Default.Whatshot,
                            iconColor = Color(0xFFEF4444),
                            title = "Maksimum Kombo",
                            value = "${player.maxCombo}x",
                            textColor = textPrimary,
                            subTextColor = textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Completed Levels Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tamamlanan Bölümler Detayı",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "${player.levelRecords.size} / 100 Bölüm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF06B6D4)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Level Records List
                if (player.levelRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz tamamlanmış bölüm kaydı bulunmuyor.\nBölümleri geçtikçe detaylı süre ve yankı istatistikleri burada listelenir.",
                            fontSize = 12.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(player.levelRecords, key = { it.levelId }) { record ->
                            LevelRecordRow(
                                record = record,
                                isDarkTheme = isDarkTheme,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                borderColor = borderColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("profile_done_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFF3B82F6) else Color(0xFF2563EB)
                    )
                ) {
                    Text(
                        text = "Tamam",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                color = subTextColor
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LevelRecordRow(
    record: LevelRecord,
    isDarkTheme: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    borderColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bölüm ${record.levelId}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = LevelCatalog.buildLevelData(record.levelId).title,
                        fontSize = 11.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Echo count badge
                    Text(
                        text = if (record.echoesUsed == 0) "0 Yankı (Kusursuz)" else "${record.echoesUsed} Yankı",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (record.echoesUsed == 0) Color(0xFF10B981) else Color(0xFF0284C7)
                    )

                    Text(text = "•", fontSize = 11.sp, color = textSecondary)

                    // Completion time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        val formattedTime = if (record.timeTakenSec < 10f) {
                            String.format(java.util.Locale.US, "%.1f sn", record.timeTakenSec)
                        } else {
                            "${record.timeTakenSec.toInt()} sn"
                        }
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                }
            }

            // Stars and trophies on right side
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..3) {
                        val isEarned = i <= record.stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isEarned) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "+${record.trophiesEarned} 🏆",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
            }
        }
    }
}

private fun formatPlayTime(seconds: Long): String {
    if (seconds <= 0) return "0 dk"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val sec = seconds % 60
    return when {
        hours > 0 -> "${hours} sa ${minutes} dk"
        minutes > 0 -> "${minutes} dk ${sec} sn"
        else -> "${sec} sn"
    }
}
