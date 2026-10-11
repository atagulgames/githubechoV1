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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HapticEngine
import com.example.audio.hapticClick
import com.example.data.local.LevelEntity

/**
 * Cartoon Game UI / Cartoony Game GUI Level Selection Menu.
 * Features 3D golden plaques, chunky level blocks, rich star ratings,
 * and playful arcade category pills. All references to "2. Sezon" removed.
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
    // 0 = All 150 levels, 1 = Tier 1 (1-10), ..., 15 = Tier 15 (141-150)
    var selectedFilterIndex by remember {
        mutableIntStateOf(((currentLevelIndex / 10) + 1).coerceIn(1, 15))
    }

    val filterOptions = listOf(
        "All (150)",
        "1-10 Harmonic",
        "11-20 Directional",
        "21-30 Locks",
        "31-40 Fading",
        "41-50 Shadow",
        "51-60 Fractal",
        "61-70 Quantum",
        "71-80 Time",
        "81-90 Master",
        "91-100 OMEGA",
        "101-110 Hyper Flux",
        "111-120 Singularity",
        "121-130 Quantum Entangled",
        "131-140 Chrono Breach",
        "141-150 COSMIC PEAK"
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 640.dp)
                .fillMaxHeight(0.90f)
                .shadow(28.dp, RoundedCornerShape(26.dp))
                .testTag("level_select_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = Color.Transparent,
            border = BorderStroke(3.dp, Color(0xFF1B3B87))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF070F28),
                                Color(0xFF0E1F4D),
                                Color(0xFF0A1438)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // 1. TOP HEADER: Red 3D Close Button & 3D Title Plaque
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title Plaque
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFFFFC700), Color(0xFFD97706))
                                        )
                                    )
                                    .border(2.dp, Color(0xFF78350F), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = null,
                                    tint = Color(0xFF451A03),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "LEVEL SELECT",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F),
                                    letterSpacing = 0.5.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$completedCount/100 Completed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "⭐", fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "$totalStars",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFD54F)
                                        )
                                    }
                                }
                            }
                        }

                        // Right: Guide & Red Close Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onOpenRulesGuide != null) {
                                Box(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .shadow(3.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                            )
                                        )
                                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = Color.White),
                                            onClick = hapticClick(action = onOpenRulesGuide)
                                        )
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Guide",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
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
                                        onClick = onDismiss
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. CHUNKY 3D CHAPTER SCROLL ROW
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        itemsIndexed(filterOptions) { index, title ->
                            val isSelected = selectedFilterIndex == index

                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.verticalGradient(
                                                listOf(Color(0xFFFFC700), Color(0xFFD97706))
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF132356), Color(0xFF0D183E))
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF78350F) else Color(0xFF1E3A8A),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = {
                                            HapticEngine.triggerButtonClick()
                                            selectedFilterIndex = index
                                        }
                                    )
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF451A03) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. CHUNKY 3D LEVEL GRID
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 58.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedLevels, key = { it.id }) { levelEntity ->
                            val index = levelEntity.id - 1
                            val isCompleted = levelEntity.isCompleted || completedLevels.contains(levelEntity.id) || completedLevels.contains(index)
                            val isCurrent = index == currentLevelIndex
                            val isUnlocked = levelEntity.isUnlocked || index <= currentLevelIndex || isCompleted

                            val (bgColors, borderColor, textColor) = when {
                                isCurrent -> Triple(
                                    listOf(Color(0xFF00D2FF), Color(0xFF0072FF)),
                                    Color(0xFF0048A8),
                                    Color.White
                                )
                                isCompleted -> Triple(
                                    listOf(Color(0xFFFFC700), Color(0xFFD98200)),
                                    Color(0xFF944900),
                                    Color(0xFF451A03)
                                )
                                isUnlocked -> Triple(
                                    listOf(Color(0xFF1E3A8A), Color(0xFF172554)),
                                    Color(0xFF3B82F6),
                                    Color.White
                                )
                                else -> Triple(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                                    Color(0xFF334155),
                                    Color(0xFF64748B)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .shadow(if (isUnlocked) 4.dp else 1.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.verticalGradient(colors = bgColors))
                                    .border(
                                        width = if (isCurrent) 2.5.dp else 2.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        enabled = isUnlocked,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = {
                                            HapticEngine.triggerButtonClick()
                                            onSelectLevel(index)
                                        }
                                    )
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
                                            tint = textColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${levelEntity.id}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${levelEntity.id}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textColor
                                        )
                                        if (isCurrent) {
                                            Text(
                                                text = "CURRENT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        } else if (isCompleted) {
                                            Row(
                                                modifier = Modifier.padding(top = 1.dp),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                repeat(levelEntity.stars.coerceIn(1, 3)) {
                                                    Text(text = "⭐", fontSize = 8.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. CHUNKY CARTOON LEGEND BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1E4A))
                            .border(1.5.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF00D2FF), label = "Current")
                        LegendItem(color = Color(0xFFFFC700), label = "Completed")
                        LegendItem(color = Color(0xFF3B82F6), label = "Unlocked")
                        LegendItem(color = Color(0xFF64748B), label = "Locked")
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
    }
}
