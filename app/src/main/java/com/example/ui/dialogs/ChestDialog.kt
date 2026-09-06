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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ChestReward
import com.example.model.ThemeRarity

@Composable
fun ChestDialog(
    isFreeAvailable: Boolean,
    adRemainingToday: Int,
    reward: ChestReward?,
    onOpenFree: () -> Unit,
    onOpenWithAd: () -> Unit,
    onDismissReward: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = {
        if (reward != null) onDismissReward() else onDismiss()
    }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("chest_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            if (reward != null) {
                // Reward Reveal View
                ChestRewardRevealView(
                    reward = reward,
                    onCollect = onDismissReward
                )
            } else {
                // Main Chest View with Transparent Odds
                ChestMainView(
                    isFreeAvailable = isFreeAvailable,
                    adRemainingToday = adRemainingToday,
                    onOpenFree = onOpenFree,
                    onOpenWithAd = onOpenWithAd,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ChestMainView(
    isFreeAvailable: Boolean,
    adRemainingToday: Int,
    onOpenFree: () -> Unit,
    onOpenWithAd: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                        .background(Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Redeem,
                        contentDescription = null,
                        tint = Color(0xFF9333EA),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Gizemli Yankı Sandığı",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Gerçek Olasılıklar & Adil Dağılım",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Chest Graphic badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFFAF5FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = Color(0xFF9333EA),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Her sandıkta garanti ödül bulunur!",
            fontSize = 13.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Transparent Odds Table
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Düşme Olasılıkları:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                RarityOddRow("Yaygın (%60)", "2-3 İpucu Jetonu", ThemeRarity.COMMON)
                RarityOddRow("Nadir (%25)", "1 Matkap Lazeri & 1 Jeton", ThemeRarity.RARE)
                RarityOddRow("Epik (%12)", "5 Jeton, 2 Matkap & Zümrüt Teması", ThemeRarity.EPIC)
                RarityOddRow("Efsanevi (%3)", "10 Jeton, 4 Matkap & Altın Teması", ThemeRarity.LEGENDARY)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action 1: Ücretsiz Sandık (günde 1)
        if (isFreeAvailable) {
            Button(
                onClick = onOpenFree,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Redeem, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ücretsiz Sandığı Aç (Günde 1)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ücretsiz sandık yarın 00:00'da açılır", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: Reklamlı Sandık (kalan: X/2)
        if (adRemainingToday > 0) {
            OutlinedButton(
                onClick = onOpenWithAd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF9333EA)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9333EA))
            ) {
                Icon(imageVector = Icons.Default.OndemandVideo, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Reklam İzle & Aç (Kalan: $adRemainingToday/2)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RarityOddRow(label: String, rewardDesc: String, rarity: ThemeRarity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = rarity.backgroundColor,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = rarity.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = rarity.color,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Text(text = label, fontSize = 11.sp, color = Color(0xFF334155))
        }
        Text(text = rewardDesc, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
    }
}

@Composable
private fun ChestRewardRevealView(
    reward: ChestReward,
    onCollect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = reward.rarity.backgroundColor
        ) {
            Text(
                text = "${reward.rarity.title} Ödül!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = reward.rarity.color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = reward.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = reward.subtitle,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Rewarded items cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (reward.tokens > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "+${reward.tokens} Jeton", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    }
                }
            }

            if (reward.breakers > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "+${reward.breakers} Matkap", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    }
                }
            }

            if (reward.unlockedThemeName != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF3E8FF),
                    border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = reward.unlockedThemeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9333EA))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCollect,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            )
        ) {
            Text(text = "Cüzdana Ekle & Devam Et", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
