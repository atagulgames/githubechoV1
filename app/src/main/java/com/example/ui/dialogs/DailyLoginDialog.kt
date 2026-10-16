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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.model.DailyLoginDay

@Composable
fun DailyLoginDialog(
    days: List<DailyLoginDay>,
    currentStreak: Int,
    isRewardAvailableToday: Boolean,
    onClaimToday: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("daily_login_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(2.dp, Color(0xFF1E293B))
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF78350F),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Günlük Ödül (Daily Reward)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isRewardAvailableToday) "Bugün Alınabilir • Günde 1 Kez (Seri: Gün $currentStreak)" else "Bugünkü Ödül Alındı • Yarın Tekrar Gelin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isRewardAvailableToday) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days grid: days 1..6 in 3 columns, day 7 spanning across
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(days.filter { it.dayNumber < 7 }) { day ->
                        DailyDayCard(day = day)
                    }

                    // Day 7 spanning full width
                    days.find { it.dayNumber == 7 }?.let { day7 ->
                        item(span = { GridItemSpan(3) }) {
                            Day7SpecialCard(day = day7)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button
                if (isRewardAvailableToday) {
                    val todayDay = days.find { it.isToday }
                    val coinText = todayDay?.coins ?: 100
                    Button(
                        onClick = onClaimToday,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .testTag("btn_claim_daily_reward"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bugünün Ödülünü Al (+$coinText Coin)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF94A3B8)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bugün Alındı (Yarın Tekrar Gelin)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDayCard(day: DailyLoginDay) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                day.isClaimed -> Color(0xFF1E293B)
                day.isToday -> Color(0xFF0C4A6E)
                else -> Color(0xFF161F36)
            }
        ),
        border = BorderStroke(
            if (day.isToday) 2.dp else 1.dp,
            when {
                day.isToday -> Color(0xFF38BDF8)
                day.isClaimed -> Color(0xFF22C55E).copy(alpha = 0.5f)
                else -> Color(0xFF334155)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day label & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${day.dayNumber}. Gün",
                    fontSize = 11.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (day.isToday) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                )

                if (day.isClaimed) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF14532D)
                    ) {
                        Text(
                            text = "Alındı",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF86EFAC),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else if (day.isToday) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0369A1)
                    ) {
                        Text(
                            text = "Bugün",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Prominent Coin Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (day.isClaimed) Color(0xFF334155) else Color(0xFF78350F),
                border = BorderStroke(1.dp, if (day.isClaimed) Color(0xFF475569) else Color(0xFFFFB703))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (day.isClaimed) Icons.Default.Check else Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (day.isClaimed) Color(0xFF86EFAC) else Color(0xFFFFD54F),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+${day.coins} Coin",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (day.isClaimed) Color(0xFFCBD5E1) else Color(0xFFFFE082)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary rewards text (Tokens / Breakers / Diamonds)
            val subReward = buildString {
                if (day.tokens > 0) append("+${day.tokens} Jeton ")
                if (day.breakers > 0) append("+${day.breakers} ⚡ ")
                if (day.diamonds > 0) append("+${day.diamonds} 💎")
            }
            Text(
                text = subReward,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (day.isClaimed) Color(0xFF64748B) else Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Day7SpecialCard(day: DailyLoginDay) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (day.isClaimed) Color(0xFF1E293B) else Color(0xFF451A03)
        ),
        border = BorderStroke(
            if (day.isToday) 2.dp else 1.5.dp,
            if (day.isClaimed) Color(0xFF475569) else Color(0xFFFFB703)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .then(
                            if (day.isClaimed) Modifier.background(Color(0xFF334155))
                            else Modifier.background(Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (day.isClaimed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Claimed",
                            tint = Color(0xFF86EFAC),
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFF78350F),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "7. Gün Büyük Ödül",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (day.isClaimed) Color(0xFF94A3B8) else Color(0xFFFFE082)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFB45309)
                        ) {
                            Text(
                                text = "EFSANEVİ",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "+1000 🪙 Coin, +100 Jeton, +50 💎 & 'Altın Lazer' Teması",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (day.isClaimed) Color(0xFF64748B) else Color(0xFFFFD54F)
                    )
                }
            }

            if (day.isClaimed) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF14532D)
                ) {
                    Text(
                        text = "Alındı",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF86EFAC),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
