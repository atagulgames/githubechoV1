package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicAudioEngine
import com.example.data.LevelRuleCatalog
import com.example.model.LevelRule
import com.example.ui.components.RuleVisualizerPreview

/**
 * Modern Onboarding Overlay System introducing the 100 Level Rules and Milestones.
 * Features:
 * - Dynamic milestone celebration header
 * - Live animated mini-canvas visualization (RuleVisualizerPreview)
 * - 3-phase structured tactical breakdown (Rule, Hazard, Strategy)
 * - Interactive milestone navigator to inspect upcoming or past level rules
 * - Full 100-Level Rules Encyclopedia view
 */
@Composable
fun LevelRuleOnboardingOverlay(
    currentLevelId: Int,
    isDarkTheme: Boolean,
    isTurkish: Boolean = false,
    autoShowForTierChecked: Boolean = true,
    initialEncyclopediaMode: Boolean = false,
    onToggleAutoShowForTier: ((Boolean) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedLevelId by remember { mutableIntStateOf(currentLevelId) }
    var isEncyclopediaOpen by remember { mutableStateOf(initialEncyclopediaMode) }
    var rememberChoiceChecked by remember { mutableStateOf(autoShowForTierChecked) }

    val activeRule = remember(selectedLevelId, isTurkish) {
        LevelRuleCatalog.getRuleForLevel(selectedLevelId, isTurkish)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "overlay_pulse")
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_pulse"
    )

    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val tipBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val accentColor = Color(activeRule.badgeColor)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .shadow(28.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(28.dp))
                .testTag("level_rule_onboarding_overlay"),
            color = cardBg
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!isEncyclopediaOpen) {
                    // --- PRIMARY LEVEL RULE ONBOARDING VIEW ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Milestone Badge Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.16f))
                                    .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (activeRule.isMajorMilestone) {
                                        if (isTurkish) "🏆 BÖLÜM ${activeRule.levelId} MİLAT NOKTASI" else "🏆 LEVEL ${activeRule.levelId} MILESTONE"
                                    } else {
                                        if (isTurkish) "✨ BÖLÜM ${activeRule.levelId} KURALI" else "✨ LEVEL ${activeRule.levelId} RULE"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    HarmonicAudioEngine.playCandyPop(1)
                                    onDismiss()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Interactive Visualizer Preview Canvas
                        RuleVisualizerPreview(
                            previewType = activeRule.visualPreviewType,
                            accentColor = accentColor,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Icon, Rule Title & Subtitle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = activeRule.icon,
                                fontSize = 28.sp,
                                modifier = Modifier.scale(badgePulse)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeRule.ruleTitle,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = activeRule.subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Milestone Selector Quick Bar (Allows previewing all 12 Grand Milestones)
                        Text(
                            text = if (isTurkish) "⚡ Seviye Milatları (1 - 100):" else "⚡ Level Milestones (1 - 100):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(LevelRuleCatalog.MILESTONE_LEVELS) { mId ->
                                val isSelected = mId == selectedLevelId
                                val mRule = LevelRuleCatalog.getRuleForLevel(mId, isTurkish)
                                val mColor = Color(mRule.badgeColor)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) mColor else mColor.copy(alpha = 0.12f))
                                        .border(
                                            1.dp,
                                            if (isSelected) mColor else mColor.copy(alpha = 0.35f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedLevelId = mId
                                            HarmonicAudioEngine.playCandyPop(1)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${mRule.icon} ${mId}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else mColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. CORE MECHANIC RULE CARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(tipBg)
                                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isTurkish) "Nasıl Çalışır?" else "How It Works",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = activeRule.mechanicRule,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = textPrimary.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. ECHO HAZARD CARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDarkTheme) Color(0xFF450A0A).copy(alpha = 0.4f) else Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isTurkish) "Yankı & Hata Tehlikesi (+1 Yankı)" else "Echo & Collision Hazard (+1 Echo)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = activeRule.echoHazard,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = if (isDarkTheme) Color(0xFFFECACA) else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. PRO STRATEGY CARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDarkTheme) Color(0xFF422006).copy(alpha = 0.35f) else Color(0xFFFFFBEB))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isTurkish) "Usta Taktiği (3 Yıldız Rehberi)" else "Master Strategy (3-Star Tip)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = activeRule.proStrategy,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = if (isDarkTheme) Color(0xFFFDE68A) else Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Don't auto-show toggle
                        if (onToggleAutoShowForTier != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        rememberChoiceChecked = !rememberChoiceChecked
                                        onToggleAutoShowForTier(rememberChoiceChecked)
                                    }
                            ) {
                                Checkbox(
                                    checked = rememberChoiceChecked,
                                    onCheckedChange = { checked ->
                                        rememberChoiceChecked = checked
                                        onToggleAutoShowForTier(checked)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = accentColor),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTurkish) "Bu kuralı otomatik tekrar göster" else "Auto-show rules for new milestones",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // ACTION BUTTONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Encyclopedia button
                            OutlinedButton(
                                onClick = {
                                    HarmonicAudioEngine.playCandyPop(1)
                                    isEncyclopediaOpen = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.6f))))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTurkish) "100 Kural" else "All Rules",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }

                            // Start / Play button
                            Button(
                                onClick = {
                                    HarmonicAudioEngine.playCandyPop(2)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .shadow(6.dp, RoundedCornerShape(24.dp))
                                    .testTag("onboarding_overlay_play_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTurkish) "Bölüme Başla!" else "Start Level!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // --- 100 LEVEL RULES ENCYCLOPEDIA VIEW ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        // Header with Back
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        HarmonicAudioEngine.playCandyPop(1)
                                        isEncyclopediaOpen = false
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text(text = "◀", fontSize = 16.sp, color = accentColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (isTurkish) "100 Seviye Kural Ansiklopedisi" else "100 Level Rules Encyclopedia",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = if (isTurkish) "12 Büyük Kademe & Tüm Kurallar" else "12 Grand Tiers & All Rules",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    HarmonicAudioEngine.playCandyPop(1)
                                    onDismiss()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable List of All 100 Rules grouped by Milestones
                        val allRules = remember(isTurkish) { LevelRuleCatalog.getAll100Rules(isTurkish) }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allRules) { rule ->
                                val isCurrent = rule.levelId == currentLevelId
                                val rColor = Color(rule.badgeColor)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isCurrent) rColor.copy(alpha = 0.16f) else tipBg)
                                        .border(
                                            if (isCurrent) 1.5.dp else 1.dp,
                                            if (isCurrent) rColor else rColor.copy(alpha = 0.25f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            selectedLevelId = rule.levelId
                                            isEncyclopediaOpen = false
                                            HarmonicAudioEngine.playCandyPop(1)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(text = rule.icon, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Bölüm ${rule.levelId}: ${rule.ruleTitle}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (rule.isMajorMilestone) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "⭐ MİLAT",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = rColor
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = rule.summary,
                                                    fontSize = 11.sp,
                                                    color = textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Text(
                                            text = "İncele ▶",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = rColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                HarmonicAudioEngine.playCandyPop(1)
                                isEncyclopediaOpen = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text(
                                text = if (isTurkish) "Seçili Kurala Dön" else "Back to Selected Rule",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
