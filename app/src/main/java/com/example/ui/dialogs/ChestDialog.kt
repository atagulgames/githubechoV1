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
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.audio.hapticClick
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
import com.example.ui.components.MeteorStoneButton
import com.example.ui.components.MeteorStoneTheme

@Composable
fun ChestDialog(
    isFreeAvailable: Boolean,
    isWeeklyAdAvailable: Boolean = true,
    weeklyAdRemainingTimeText: String = "",
    userTokens: Int = 0,
    adRemainingToday: Int = 1,
    reward: ChestReward?,
    onOpenFree: () -> Unit,
    onOpenWithAd: () -> Unit,
    onOpenWithCoins: () -> Unit = {},
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
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF334155))
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
                    isWeeklyAdAvailable = isWeeklyAdAvailable,
                    weeklyAdRemainingTimeText = weeklyAdRemainingTimeText,
                    userTokens = userTokens,
                    onOpenFree = onOpenFree,
                    onOpenWithAd = onOpenWithAd,
                    onOpenWithCoins = onOpenWithCoins,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ChestMainView(
    isFreeAvailable: Boolean,
    isWeeklyAdAvailable: Boolean,
    weeklyAdRemainingTimeText: String,
    userTokens: Int,
    onOpenFree: () -> Unit,
    onOpenWithAd: () -> Unit,
    onOpenWithCoins: () -> Unit,
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
                        .background(Color(0xFF312E81)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Redeem,
                        contentDescription = null,
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Gizemli Kozmik Sandık",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Adil Dağılım & Garantili Kristal",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            IconButton(
                onClick = hapticClick(action = onDismiss),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Chest Graphic badge
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1B4B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mevcut Bakiyeniz: $userTokens Jeton",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF38BDF8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Transparent Odds Table
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155)),
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
                    color = Color(0xFF94A3B8)
                )
                RarityOddRow("Yaygın (%50)", "10-20 Jeton & 2-5 Elmas", ThemeRarity.COMMON)
                RarityOddRow("Nadir (%30)", "25-40 Jeton, 2 Matkap, 6-10 Elmas", ThemeRarity.RARE)
                RarityOddRow("Epik (%15)", "60 Jeton, 4 Matkap, Kutup Zümrüdü", ThemeRarity.EPIC)
                RarityOddRow("Efsanevi (%5)", "150 Jeton, 8 Matkap, Altın Lazer", ThemeRarity.LEGENDARY)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action 1: Ücretsiz Sandık (günde 1)
        if (isFreeAvailable) {
            MeteorStoneButton(
                text = "GÜNLÜK ÜCRETSİZ SANDIK",
                subtitle = "Her gün 1 kez ücretsiz hak",
                icon = Icons.Default.Redeem,
                stoneTheme = MeteorStoneTheme.EMERALD_CRYSTAL,
                onClick = onOpenFree,
                modifier = Modifier.fillMaxWidth(),
                testTag = "open_free_chest_button"
            )
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ücretsiz sandık yarın 00:00'da açılır", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: 250 Jeton ile Sandık Aç (User request: "250 jeton ile de sandık açılabilsin")
        MeteorStoneButton(
            text = "250 JETON İLE SANDIK AÇ",
            subtitle = if (userTokens >= 250) "Limitsiz Açılabilir (Mevcut: $userTokens)" else "Yetersiz Jeton ($userTokens/250)",
            icon = Icons.Default.MonetizationOn,
            stoneTheme = MeteorStoneTheme.SOLAR_AMBER,
            enabled = userTokens >= 250,
            onClick = onOpenWithCoins,
            modifier = Modifier.fillMaxWidth(),
            testTag = "open_coin_chest_button"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 3: Reklamlı Sandık (User request: "sandık 1 hafta içerisinde sadece birkere reklam ile açılabilecek")
        MeteorStoneButton(
            text = if (isWeeklyAdAvailable) "HAFTALIK REKLAM İLE AÇ" else "HAFTALIK REKLAM KULLANILDI",
            subtitle = if (isWeeklyAdAvailable) "Haftada 1 kez reklamla açma hakkı" else "Kalan süre: $weeklyAdRemainingTimeText (Haftada 1)",
            icon = Icons.Default.OndemandVideo,
            stoneTheme = MeteorStoneTheme.COSMIC_PURPLE,
            enabled = isWeeklyAdAvailable,
            onClick = onOpenWithAd,
            modifier = Modifier.fillMaxWidth(),
            testTag = "open_ad_chest_button"
        )
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
            Text(text = label, fontSize = 11.sp, color = Color(0xFFCBD5E1))
        }
        Text(text = rewardDesc, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
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
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = reward.subtitle,
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
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
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "+${reward.tokens} Jeton", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE68A))
                    }
                }
            }

            if (reward.breakers > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "+${reward.breakers} Matkap", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                    }
                }
            }

            if (reward.unlockedThemeName != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFFA855F7)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = reward.unlockedThemeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE9D5FF))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        MeteorStoneButton(
            text = "CÜZDANA EKLE & DEVAM ET",
            stoneTheme = MeteorStoneTheme.CYAN_PULSE,
            onClick = onCollect,
            modifier = Modifier.fillMaxWidth(),
            testTag = "chest_collect_reward_button"
        )
    }
}
