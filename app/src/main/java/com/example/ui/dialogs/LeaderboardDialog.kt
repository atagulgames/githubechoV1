package com.example.ui.dialogs

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.LeaderboardPlayer

/**
 * Casual Arcade Leaderboard Dialog matching the vibrant 3D cartoon visual design
 * of the main menu (deep royal blue gradient, doodles, 3D golden titles,
 * podium for top 3, chunky badges, and highlighted user rank card).
 */
@Composable
fun LeaderboardDialog(
    players: List<LeaderboardPlayer>,
    isDarkTheme: Boolean = true,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onSelectPlayer: (LeaderboardPlayer) -> Unit,
    onViewMyProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(0) } // 0: Global, 1: Regional, 2: Top 10

    val filteredPlayers = remember(players, selectedCategory) {
        when (selectedCategory) {
            1 -> players.filter { it.isCurrentUser || it.trophies >= 100 }
            2 -> players.take(10)
            else -> players
        }
    }

    val currentUserPlayer = remember(players) {
        players.find { it.isCurrentUser }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 820.dp)
                .testTag("leaderboard_dialog"),
            contentAlignment = Alignment.Center
        ) {
            val maxHeight = maxHeight
            val isCompactScreen = maxHeight < 680.dp

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .shadow(28.dp, RoundedCornerShape(26.dp)),
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
                                    Color(0xFF091436)
                                )
                            )
                        )
                ) {
                    // Casual cartoon game background doodles
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawLeaderboardDoodles(size)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        // 1. TOP HEADER BAR: Back/Close | Title Plaque | Refresh
                        LeaderboardHeaderBar(
                            isRefreshing = isRefreshing,
                            onRefresh = onRefresh,
                            onDismiss = onDismiss
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. SEGMENTED CATEGORY TABS: Global | Regional | Top 10
                        LeaderboardCategoryTabs(
                            selectedCategory = selectedCategory,
                            onSelectCategory = { selectedCategory = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. TOP 3 PODIUM (Visible in Global or Top 10 when at least 3 players exist)
                        if (!isCompactScreen && (selectedCategory == 0 || selectedCategory == 2) && filteredPlayers.size >= 3) {
                            LeaderboardPodiumSection(
                                p1 = filteredPlayers.getOrNull(0),
                                p2 = filteredPlayers.getOrNull(1),
                                p3 = filteredPlayers.getOrNull(2),
                                onSelectPlayer = onSelectPlayer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // 4. COLUMN LABELS HEADER
                        LeaderboardTableLabels()

                        Spacer(modifier = Modifier.height(6.dp))

                        // 5. PLAYER LIST (LazyColumn)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (filteredPlayers.isEmpty()) {
                                LeaderboardEmptyState()
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    items(filteredPlayers, key = { it.id }) { player ->
                                        LeaderboardChunkyRow(
                                            player = player,
                                            onClick = { onSelectPlayer(player) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 6. STICKY USER STATUS & CLOSE BUTTON FOOTER
                        LeaderboardFooter(
                            currentUser = currentUserPlayer,
                            onViewMyProfile = onViewMyProfile,
                            onDismiss = onDismiss
                        )
                    }
                }
            }
        }
    }
}

/* ========================================================================== */
/*                             HEADER BAR                                     */
/* ========================================================================== */

@Composable
private fun LeaderboardHeaderBar(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red 3D Chunky Close Button (Left)
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF5252), Color(0xFFC62828))
                    )
                )
                .border(2.5.dp, Color(0xFF7F1D1D), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onDismiss
                )
                .testTag("leaderboard_close_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Center: 3D Golden Plaque Title Banner
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                    )
                )
                .border(2.dp, Color(0xFF3B82F6), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Golden Trophy Icon with Bevel
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300))
                            )
                        )
                        .border(1.5.dp, Color(0xFFB45309), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFF78350F),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LEADERBOARD",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "GLOBAL RANKINGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

        // Refresh 3D Chunky Button (Right)
        val infiniteTransition = rememberInfiniteTransition(label = "refreshRotation")
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin"
        )

        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                    )
                )
                .border(2.5.dp, Color(0xFF0369A1), CircleShape)
                .clickable(
                    enabled = !isRefreshing,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onRefresh
                )
                .testTag("leaderboard_refresh_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isRefreshing) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Yenileniyor",
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotationAngle)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/* ========================================================================== */
/*                          CATEGORY TABS                                     */
/* ========================================================================== */

@Composable
private fun LeaderboardCategoryTabs(
    selectedCategory: Int,
    onSelectCategory: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B173D).copy(alpha = 0.9f))
            .border(2.dp, Color(0xFF1E3A8A), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val tabs = listOf("🌍 Global", "🌟 Regional", "🏆 Top 10")
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedCategory == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .shadow(if (isSelected) 6.dp else 0.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFC700), Color(0xFFD97706))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF132356), Color(0xFF0D183E))
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
                        onClick = { onSelectCategory(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    color = if (isSelected) Color(0xFF451A03) else Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/* ========================================================================== */
/*                             TOP 3 PODIUM                                   */
/* ========================================================================== */

@Composable
private fun LeaderboardPodiumSection(
    p1: LeaderboardPlayer?,
    p2: LeaderboardPlayer?,
    p3: LeaderboardPlayer?,
    onSelectPlayer: (LeaderboardPlayer) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0B173E).copy(alpha = 0.75f),
        border = BorderStroke(1.5.dp, Color(0xFF1D3B82))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // #2 Silver (Left)
            p2?.let {
                PodiumSlot(
                    player = it,
                    rank = 2,
                    pedestalHeight = 62,
                    primaryColor = Color(0xFFCBD5E1),
                    gradientColors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B)),
                    borderColor = Color(0xFF475569),
                    badgeBg = Color(0xFF64748B),
                    crownEmoji = null,
                    onClick = { onSelectPlayer(it) }
                )
            }

            // #1 Gold (Center, Elevated)
            p1?.let {
                PodiumSlot(
                    player = it,
                    rank = 1,
                    pedestalHeight = 84,
                    primaryColor = Color(0xFFFFD54F),
                    gradientColors = listOf(Color(0xFFFFE082), Color(0xFFF59E0B)),
                    borderColor = Color(0xFFB45309),
                    badgeBg = Color(0xFFD97706),
                    crownEmoji = "👑",
                    onClick = { onSelectPlayer(it) }
                )
            }

            // #3 Bronze (Right)
            p3?.let {
                PodiumSlot(
                    player = it,
                    rank = 3,
                    pedestalHeight = 50,
                    primaryColor = Color(0xFFFB923C),
                    gradientColors = listOf(Color(0xFFFDBA74), Color(0xFFEA580C)),
                    borderColor = Color(0xFF9A3412),
                    badgeBg = Color(0xFFC2410C),
                    crownEmoji = null,
                    onClick = { onSelectPlayer(it) }
                )
            }
        }
    }
}

@Composable
private fun PodiumSlot(
    player: LeaderboardPlayer,
    rank: Int,
    pedestalHeight: Int,
    primaryColor: Color,
    gradientColors: List<Color>,
    borderColor: Color,
    badgeBg: Color,
    crownEmoji: String?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, color = primaryColor),
                onClick = onClick
            )
            .padding(horizontal = 2.dp)
    ) {
        // Crown above avatar (if 1st place)
        if (crownEmoji != null) {
            Text(
                text = crownEmoji,
                fontSize = 18.sp,
                modifier = Modifier.offset(y = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Circular Avatar with Chunky Colored Rim
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(if (rank == 1) 48.dp else 42.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.5.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (player.avatarUri.isNotBlank()) {
                    AsyncImage(
                        model = player.avatarUri,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = player.avatarEmoji,
                        fontSize = if (rank == 1) 22.sp else 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Username
        Text(
            text = player.username.replace("👑 ", "").replace("⚡ ", "").replace("🚀 ", ""),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Trophy Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🏆",
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${player.trophies}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = primaryColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3D Pedestal Block with Embossed Rank Number
        Box(
            modifier = Modifier
                .width(if (rank == 1) 78.dp else 68.dp)
                .height(pedestalHeight.dp)
                .shadow(6.dp, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Brush.verticalGradient(colors = gradientColors))
                .border(2.dp, borderColor, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (rank == 1) 32.dp else 26.dp)
                    .clip(CircleShape)
                    .background(badgeBg.copy(alpha = 0.85f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    fontSize = if (rank == 1) 16.sp else 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

/* ========================================================================== */
/*                             TABLE LABELS                                   */
/* ========================================================================== */

@Composable
private fun LeaderboardTableLabels() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F1E4A).copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RANK / PLAYER",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF60A5FA),
            letterSpacing = 0.5.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                text = "ECHOES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF38BDF8),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "TROPHIES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD54F),
                letterSpacing = 0.5.sp
            )
        }
    }
}

/* ========================================================================== */
/*                          CHUNKY PLAYER ROW                                 */
/* ========================================================================== */

@Composable
private fun LeaderboardChunkyRow(
    player: LeaderboardPlayer,
    onClick: () -> Unit
) {
    val isCurrentUser = player.isCurrentUser

    val cardBg = if (isCurrentUser) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF12224D), Color(0xFF0C1738))
        )
    }

    val cardBorderColor = if (isCurrentUser) {
        Color(0xFFFFD700)
    } else {
        when (player.rank) {
            1 -> Color(0xFFFFB300)
            2 -> Color(0xFF94A3B8)
            3 -> Color(0xFFEA580C)
            else -> Color(0xFF1E3875)
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(if (isCurrentUser) 2.dp else 1.5.dp, cardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isCurrentUser) 6.dp else 2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .testTag("leaderboard_player_${player.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Rank Badge | Avatar | Name & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank Shield / Pill
                RankBadge(rank = player.rank)

                Spacer(modifier = Modifier.width(8.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(
                            width = 1.5.dp,
                            color = when (player.rank) {
                                1 -> Color(0xFFFFD54F)
                                2 -> Color(0xFFCBD5E1)
                                3 -> Color(0xFFFB923C)
                                else -> Color(0xFF3B82F6)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (player.avatarUri.isNotBlank()) {
                        AsyncImage(
                            model = player.avatarUri,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = player.avatarEmoji, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Username & Title Chip
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.username,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981))
                                    .border(1.dp, Color(0xFF6EE7B7), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "YOU",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = player.title,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "•", fontSize = 10.sp, color = Color(0xFF64748B))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatShortTime(player.totalPlayTimeSec),
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // RIGHT: Echoes & Trophies
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Echoes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = "Echo",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${player.totalEchoes}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }

                // Trophies Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF59E0B), Color(0xFFB45309))
                            )
                        )
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🏆",
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${player.trophies}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val (bgColors, borderColor, textColor) = when (rank) {
        1 -> Triple(
            listOf(Color(0xFFFFD54F), Color(0xFFF59E0B)),
            Color(0xFFB45309),
            Color(0xFF78350F)
        )
        2 -> Triple(
            listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)),
            Color(0xFF475569),
            Color(0xFF1E293B)
        )
        3 -> Triple(
            listOf(Color(0xFFFDBA74), Color(0xFFEA580C)),
            Color(0xFF9A3412),
            Color(0xFF431407)
        )
        else -> Triple(
            listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
            Color(0xFF334155),
            Color(0xFF94A3B8)
        )
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(colors = bgColors))
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}

/* ========================================================================== */
/*                             EMPTY STATE                                    */
/* ========================================================================== */

@Composable
private fun LeaderboardEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E3A8A).copy(alpha = 0.5f))
                    .border(2.dp, Color(0xFF3B82F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No players have earned trophies yet.",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Complete levels to earn your first trophies and climb to the top!",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

/* ========================================================================== */
/*                        FOOTER: USER RANK & CLOSE                           */
/* ========================================================================== */

@Composable
private fun LeaderboardFooter(
    currentUser: LeaderboardPlayer?,
    onViewMyProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Sticky User Standing Plaque
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F1E4A),
            border = BorderStroke(1.5.dp, Color(0xFF2563EB))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A8A))
                            .border(1.5.dp, Color(0xFF60A5FA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.avatarEmoji ?: "👤",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (currentUser != null) "Rank: #${currentUser.rank} (${currentUser.trophies} 🏆)" else "Unranked",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = if (currentUser != null) "Earn more trophies by beating levels!" else "Play levels to earn trophies!",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Profile Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                            )
                        )
                        .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = onViewMyProfile
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("view_my_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "My Profile",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Chunky 3D Close Button
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .shadow(4.dp, RoundedCornerShape(14.dp))
                .testTag("leaderboard_done_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                        )
                    )
                    .border(2.dp, Color(0xFF475569), RoundedCornerShape(14.dp)),
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

/* ========================================================================== */
/*                             CANVAS DOODLES                                 */
/* ========================================================================== */

private fun DrawScope.drawLeaderboardDoodles(canvasSize: Size) {
    val doodleColor = Color(0xFF38BDF8).copy(alpha = 0.08f)
    val starColor = Color(0xFFFFD54F).copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

    val w = canvasSize.width
    val h = canvasSize.height

    // Trophies / Stars doodles in the background
    drawStarDoodle(Offset(w * 0.12f, h * 0.14f), 10.dp.toPx(), starColor)
    drawStarDoodle(Offset(w * 0.88f, h * 0.12f), 12.dp.toPx(), starColor)
    drawStarDoodle(Offset(w * 0.08f, h * 0.40f), 14.dp.toPx(), starColor)
    drawStarDoodle(Offset(w * 0.92f, h * 0.45f), 11.dp.toPx(), starColor)
    drawStarDoodle(Offset(w * 0.15f, h * 0.70f), 10.dp.toPx(), starColor)
    drawStarDoodle(Offset(w * 0.85f, h * 0.75f), 13.dp.toPx(), starColor)

    // Trophy cup outline doodle
    drawTrophyDoodle(Offset(w * 0.10f, h * 0.28f), doodleColor, stroke)
    drawTrophyDoodle(Offset(w * 0.90f, h * 0.28f), doodleColor, stroke)
    drawCrownDoodle(Offset(w * 0.12f, h * 0.55f), doodleColor, stroke)
    drawCrownDoodle(Offset(w * 0.88f, h * 0.58f), doodleColor, stroke)
}

private fun DrawScope.drawStarDoodle(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.3f, center.y - radius * 0.3f)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + radius * 0.3f, center.y + radius * 0.3f)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.3f, center.y + radius * 0.3f)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - radius * 0.3f, center.y - radius * 0.3f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawTrophyDoodle(center: Offset, color: Color, stroke: Stroke) {
    val path = Path().apply {
        // Cup body
        moveTo(center.x - 12.dp.toPx(), center.y - 10.dp.toPx())
        lineTo(center.x + 12.dp.toPx(), center.y - 10.dp.toPx())
        lineTo(center.x + 8.dp.toPx(), center.y + 2.dp.toPx())
        lineTo(center.x - 8.dp.toPx(), center.y + 2.dp.toPx())
        close()
        // Stem and base
        moveTo(center.x, center.y + 2.dp.toPx())
        lineTo(center.x, center.y + 8.dp.toPx())
        moveTo(center.x - 8.dp.toPx(), center.y + 8.dp.toPx())
        lineTo(center.x + 8.dp.toPx(), center.y + 8.dp.toPx())
    }
    drawPath(path, color, style = stroke)
}

private fun DrawScope.drawCrownDoodle(center: Offset, color: Color, stroke: Stroke) {
    val path = Path().apply {
        moveTo(center.x - 12.dp.toPx(), center.y + 6.dp.toPx())
        lineTo(center.x - 12.dp.toPx(), center.y - 6.dp.toPx())
        lineTo(center.x - 5.dp.toPx(), center.y)
        lineTo(center.x, center.y - 8.dp.toPx())
        lineTo(center.x + 5.dp.toPx(), center.y)
        lineTo(center.x + 12.dp.toPx(), center.y - 6.dp.toPx())
        lineTo(center.x + 12.dp.toPx(), center.y + 6.dp.toPx())
        close()
    }
    drawPath(path, color, style = stroke)
}

private fun formatShortTime(seconds: Long): String {
    if (seconds <= 0) return "0 dk"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}s ${minutes}d" else "${minutes} dk"
}
