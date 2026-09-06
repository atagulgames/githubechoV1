package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EchoTheme
import com.example.model.StrokeTheme

@Composable
fun SkinsDialog(
    currentStroke: StrokeTheme,
    currentEcho: EchoTheme,
    unlockedThemes: Set<String> = emptySet(),
    totalStars: Int = 0,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onSelectStroke: (StrokeTheme) -> Unit,
    onSelectEcho: (EchoTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val sectionCardBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("skins_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = dialogBg,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                                .background(if (isDarkTheme) Color(0xFF3B0764) else Color(0xFFEDE9FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Temalar & Renkler",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$totalStars Yıldız Toplandı",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("skins_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Requirement: "tema dark/white sadece iki tane seçenek olacak"
                Text(
                    text = "Arayüz Teması (Dark / White)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // White Theme Choice
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isDarkTheme) Color(0xFFE0F2FE) else sectionCardBg)
                            .border(
                                2.dp,
                                if (!isDarkTheme) Color(0xFF0284C7) else borderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleDarkTheme(false) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Açık (White)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (!isDarkTheme) Color(0xFF0284C7) else textSecondary
                            )
                            if (!isDarkTheme) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Seçildi ✓", fontSize = 11.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Dark Theme Choice
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkTheme) Color(0xFF0C4A6E) else sectionCardBg)
                            .border(
                                2.dp,
                                if (isDarkTheme) Color(0xFF0284C7) else borderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleDarkTheme(true) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Koyu (Dark)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDarkTheme) Color(0xFF38BDF8) else textSecondary
                            )
                            if (isDarkTheme) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Seçildi ✓", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Stroke Themes
                Text(
                    text = "Çizgi Rengi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StrokeTheme.entries.forEach { theme ->
                        val isUnlocked = theme.requiredStars == 0 ||
                                totalStars >= theme.requiredStars ||
                                unlockedThemes.contains(theme.name)
                        val isSelected = theme == currentStroke

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = isUnlocked) { onSelectStroke(theme) },
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                !isUnlocked -> Color(0xFFF8FAFC)
                                isSelected -> Color(0xFFF0FDF4)
                                else -> Color(0xFFFFFFFF)
                            },
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                when {
                                    isSelected -> Color(0xFF22C55E)
                                    !isUnlocked -> Color(0xFFE2E8F0)
                                    else -> Color(0xFFE2E8F0)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(theme.primaryColor)
                                            .border(2.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = theme.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (!isUnlocked) Color(0xFF94A3B8) else if (isSelected) Color(0xFF15803D) else Color(0xFF1E293B)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.rarity.backgroundColor
                                            ) {
                                                Text(
                                                    text = theme.rarity.title,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.rarity.color,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        if (!isUnlocked) {
                                            Text(
                                                text = if (theme.requiredStars > 0) "${theme.requiredStars} Yıldız ile açılır" else "Giriş / Sandık Ödülü",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçili",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Echo Barrier Themes
                Text(
                    text = "Yankı Bariyeri Rengi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EchoTheme.entries.forEach { theme ->
                        val isUnlocked = theme.requiredStars == 0 ||
                                totalStars >= theme.requiredStars ||
                                unlockedThemes.contains(theme.name)
                        val isSelected = theme == currentEcho

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = isUnlocked) { onSelectEcho(theme) },
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                !isUnlocked -> Color(0xFFF8FAFC)
                                isSelected -> Color(0xFFF0FDF4)
                                else -> Color(0xFFFFFFFF)
                            },
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                when {
                                    isSelected -> Color(0xFF22C55E)
                                    !isUnlocked -> Color(0xFFE2E8F0)
                                    else -> Color(0xFFE2E8F0)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(theme.echoColor)
                                            .border(2.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = theme.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (!isUnlocked) Color(0xFF94A3B8) else if (isSelected) Color(0xFF15803D) else Color(0xFF1E293B)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.rarity.backgroundColor
                                            ) {
                                                Text(
                                                    text = theme.rarity.title,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.rarity.color,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        if (!isUnlocked) {
                                            Text(
                                                text = if (theme.requiredStars > 0) "${theme.requiredStars} Yıldız ile açılır" else "Giriş / Sandık Ödülü",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçili",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

