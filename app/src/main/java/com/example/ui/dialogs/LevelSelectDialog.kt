package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.LevelEntity

/**
 * Responsive Level Selection Menu using Jetpack Compose.
 * Displays all 100 levels with clear visual states:
 * - Current active level
 * - Completed with star ratings
 * - Unlocked and playable
 * - Locked with padlock icon
 * Dynamically scales to fit compact, medium, and expanded screens without overflowing.
 */
@Composable
fun LevelSelectDialog(
    levels: List<LevelEntity>,
    currentLevelIndex: Int,
    completedLevels: Set<Int>,
    isDarkTheme: Boolean = false,
    onSelectLevel: (Int) -> Unit,
    onOpenRulesGuide: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    // 0 = All 100 levels, 1 = Tier 1 (1-10), ..., 10 = Tier 10 (91-100)
    var selectedFilterIndex by remember {
        mutableIntStateOf(((currentLevelIndex / 10) + 1).coerceIn(1, 10))
    }

    val filterOptions = listOf(
        "Tümü (100)",
        "1-10 Harmonik",
        "11-20 Yönlü",
        "21-30 Kilit",
        "31-40 Sönen",
        "41-50 Gölge",
        "51-60 Fraktal",
        "61-70 Kuantum",
        "71-80 Zaman",
        "81-90 Usta",
        "91-100 OMEGA"
    )

    val displayedLevels = remember(levels, selectedFilterIndex) {
        if (selectedFilterIndex == 0) {
            levels
        } else {
            val tierIdx = selectedFilterIndex - 1
            val startId = tierIdx * 10 + 1
            val endId = (tierIdx + 1) * 10
            levels.filter { it.id in startId..endId }
        }
    }

    val totalStars = remember(levels) {
        levels.sumOf { it.stars }
    }

    val completedCount = remember(levels, completedLevels) {
        levels.count { it.isCompleted || completedLevels.contains(it.id) }
    }

    val dialogBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val tabContainerBg = if (isDarkTheme) Color(0xFF0B1120) else Color(0xFFEDF2F7)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 620.dp)
                .fillMaxHeight(0.88f)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("level_select_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = dialogBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Bar
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
                                .background(if (isDarkTheme) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "100 Seviye Menüsü",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tamamlanan: $completedCount/100",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "$totalStars",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onOpenRulesGuide != null) {
                            Button(
                                onClick = onOpenRulesGuide,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    contentColor = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("open_rules_from_level_select_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kurallar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_level_select_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedFilterIndex,
                    containerColor = tabContainerBg,
                    contentColor = Color(0xFF0284C7),
                    edgePadding = 6.dp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    filterOptions.forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedFilterIndex == idx,
                            onClick = { selectedFilterIndex = idx },
                            text = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedFilterIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilterIndex == idx) Color(0xFF0284C7) else textSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Responsive Grid of Levels with locked/unlocked states
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 56.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(displayedLevels, key = { it.id }) { levelEntity ->
                        val index = levelEntity.id - 1
                        val isCompleted = levelEntity.isCompleted || completedLevels.contains(levelEntity.id) || completedLevels.contains(index)
                        val isCurrent = index == currentLevelIndex
                        val isUnlocked = levelEntity.isUnlocked || index <= currentLevelIndex || isCompleted

                        val itemBg = when {
                            isCurrent -> Color(0xFF0284C7)
                            isCompleted -> if (isDarkTheme) Color(0xFF064E3B) else Color(0xFFF0FDF4)
                            isUnlocked -> cardBg
                            else -> if (isDarkTheme) Color(0xFF0B1120) else Color(0xFFEDF2F7)
                        }

                        val itemContentColor = when {
                            isCurrent -> Color.White
                            isCompleted -> if (isDarkTheme) Color(0xFF4ADE80) else Color(0xFF15803D)
                            isUnlocked -> textPrimary
                            else -> textSecondary.copy(alpha = 0.5f)
                        }

                        val itemBorderColor = when {
                            isCurrent -> Color(0xFF38BDF8)
                            isCompleted -> if (isDarkTheme) Color(0xFF059669) else Color(0xFF86EFAC)
                            isUnlocked -> borderColor
                            else -> borderColor.copy(alpha = 0.5f)
                        }

                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(itemBg)
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = itemBorderColor,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = isUnlocked) {
                                    onSelectLevel(index)
                                }
                                .testTag("level_item_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isUnlocked) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = itemContentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "${levelEntity.id}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = itemContentColor
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${levelEntity.id}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = itemContentColor
                                    )
                                    if (isCurrent) {
                                        Text(
                                            text = "AKTİF",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    } else if (isCompleted) {
                                        Row(
                                            modifier = Modifier.padding(top = 1.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            repeat(levelEntity.stars.coerceIn(1, 3)) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Legend / Explanation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(tabContainerBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFF0284C7), label = "Aktif", isDark = isDarkTheme)
                    LegendItem(color = Color(0xFF059669), label = "Tamamlandı", isDark = isDarkTheme)
                    LegendItem(color = if (isDarkTheme) Color.White else Color(0xFF0F172A), label = "Açık", isDark = isDarkTheme)
                    LegendItem(color = textSecondary.copy(alpha = 0.6f), label = "Kilitli", isDark = isDarkTheme)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isDark: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
    }
}
