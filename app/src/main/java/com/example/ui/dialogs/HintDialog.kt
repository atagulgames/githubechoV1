package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun HintDialog(
    hintAdsWatched: Int,
    diamonds: Int,
    tokens: Int,
    isDarkTheme: Boolean,
    onWatchAd: () -> Unit,
    onUseDiamonds: () -> Unit,
    onUseToken: () -> Unit,
    onDismiss: () -> Unit
) {
    val bgCard = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = bgCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("hint_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Icon + Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "İpucu",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rota İpucu Rehberi",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "1 Bölümlük Çözüm Rotası",
                                fontSize = 12.sp,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Bu bölümün ideal çözüm sırasını açmak için 3 reklam izleyebilir veya 5 elmas kullanabilirsiniz:",
                    fontSize = 13.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // OPTION 1: 3 Reklam İzle
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OndemandVideo,
                                    contentDescription = "Reklam",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "3 Reklam İzle",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                            }
                            Text(
                                text = "$hintAdsWatched / 3",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0284C7)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (hintAdsWatched / 3f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF0284C7),
                            trackColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onWatchAd,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("hint_watch_ad_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hintAdsWatched == 0) "Reklam İzle (1/3)" else "Reklam İzle (${hintAdsWatched + 1}/3)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // OPTION 2: 5 Elmas Kullan
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (diamonds >= 5) Color(0xFF67E8F9) else borderColor,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = "Elmas",
                                    tint = Color(0xFF06B6D4),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "5 Elmas Harca",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                            }
                            Text(
                                text = "Mevcut: $diamonds 💎",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (diamonds >= 5) Color(0xFF06B6D4) else textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Reklamsız anında rehber rotayı ve düğüm sırasını açar.",
                            fontSize = 11.sp,
                            color = textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FilledTonalButton(
                            onClick = onUseDiamonds,
                            enabled = diamonds >= 5,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (diamonds >= 5) Color(0xFF0891B2) else Color(0xFF64748B).copy(alpha = 0.3f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("hint_use_diamonds_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (diamonds >= 5) "5 Elmas İle Anında Aç" else "Yetersiz Elmas ($diamonds/5)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Optional Option 3: 1 İpucu Jetonu varsa kullan
                if (tokens > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onUseToken,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("hint_use_token_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Token,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mevcut Jetonu Kullan ($tokens Jeton)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "⚠️ İpucu yalnızca mevcut bölüm için geçerlidir.",
                    fontSize = 11.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
