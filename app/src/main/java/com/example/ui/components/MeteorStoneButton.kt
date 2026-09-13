package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticEngine

enum class MeteorStoneTheme(
    val baseDark: Color,
    val baseMid: Color,
    val baseLight: Color,
    val veinColor: Color,
    val rimHighlight: Color,
    val textColor: Color
) {
    CYAN_PULSE(
        baseDark = Color(0xFF0F172A),
        baseMid = Color(0xFF1E293B),
        baseLight = Color(0xFF0284C7),
        veinColor = Color(0xFF38BDF8),
        rimHighlight = Color(0xFF7DD3FC),
        textColor = Color.White
    ),
    SOLAR_AMBER(
        baseDark = Color(0xFF1C1304),
        baseMid = Color(0xFF2C1E08),
        baseLight = Color(0xFFB45309),
        veinColor = Color(0xFFF59E0B),
        rimHighlight = Color(0xFFFDE68A),
        textColor = Color.White
    ),
    OBSIDIAN_SLATE(
        baseDark = Color(0xFF0B0F19),
        baseMid = Color(0xFF1B2234),
        baseLight = Color(0xFF334155),
        veinColor = Color(0xFF818CF8),
        rimHighlight = Color(0xFFC7D2FE),
        textColor = Color(0xFFF1F5F9)
    ),
    EMERALD_CRYSTAL(
        baseDark = Color(0xFF062117),
        baseMid = Color(0xFF0E3827),
        baseLight = Color(0xFF059669),
        veinColor = Color(0xFF34D399),
        rimHighlight = Color(0xFFA7F3D0),
        textColor = Color.White
    ),
    COSMIC_PURPLE(
        baseDark = Color(0xFF150A24),
        baseMid = Color(0xFF26123D),
        baseLight = Color(0xFF7C3AED),
        veinColor = Color(0xFFA855F7),
        rimHighlight = Color(0xFFE9D5FF),
        textColor = Color.White
    )
}

/**
 * MeteorStoneButton: Custom faceted cosmic meteorite stone button.
 * Hand-crafted with asymmetric cut-corner facets, basalt rock texture,
 * glowing magma/plasma fissure veins, and beveled specular rim lighting.
 */
@Composable
fun MeteorStoneButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    stoneTheme: MeteorStoneTheme = MeteorStoneTheme.CYAN_PULSE,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    testTag: String = "meteor_stone_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "MeteorPressScale"
    )

    val stoneShape = CutCornerShape(
        topStart = 16.dp,
        bottomEnd = 16.dp,
        topEnd = 6.dp,
        bottomStart = 6.dp
    )

    val effectiveDark = if (enabled) stoneTheme.baseDark else Color(0xFF1E293B)
    val effectiveMid = if (enabled) stoneTheme.baseMid else Color(0xFF334155)
    val effectiveVein = if (enabled) stoneTheme.veinColor else Color(0xFF64748B)
    val effectiveRim = if (enabled) stoneTheme.rimHighlight else Color(0xFF475569)
    val effectiveText = if (enabled) stoneTheme.textColor else Color(0xFF94A3B8)

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .shadow(
                elevation = if (enabled) 6.dp else 1.dp,
                shape = stoneShape,
                spotColor = stoneTheme.veinColor
            )
            .clip(stoneShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(effectiveMid, effectiveDark),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 400f)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    HapticEngine.triggerButtonClick()
                    onClick()
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Meteor Stone Surface Details: Plasma Veins, Crater Texture & Bevel Highlight
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Glowing Magma / Plasma Vein Fissures inside the rock
            val veinPath = Path().apply {
                moveTo(w * 0.12f, 0f)
                lineTo(w * 0.28f, h * 0.45f)
                lineTo(w * 0.52f, h * 0.38f)
                lineTo(w * 0.88f, h)
            }
            drawPath(
                path = veinPath,
                color = effectiveVein.copy(alpha = if (enabled) 0.35f else 0.15f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            val veinBranch = Path().apply {
                moveTo(w * 0.52f, h * 0.38f)
                lineTo(w * 0.72f, h * 0.15f)
                lineTo(w * 0.85f, 0f)
            }
            drawPath(
                path = veinBranch,
                color = effectiveVein.copy(alpha = if (enabled) 0.25f else 0.10f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round)
            )

            // 2. Cosmic Stardust / Meteor Mineral Specks
            val speckles = listOf(
                Offset(w * 0.18f, h * 0.75f),
                Offset(w * 0.38f, h * 0.22f),
                Offset(w * 0.65f, h * 0.82f),
                Offset(w * 0.82f, h * 0.32f),
                Offset(w * 0.48f, h * 0.68f)
            )
            speckles.forEach { sp ->
                drawCircle(
                    color = effectiveVein.copy(alpha = if (enabled) 0.55f else 0.2f),
                    radius = 1.5f,
                    center = sp
                )
            }

            // 3. Faceted Crystalline Rim Border
            val rimStroke = Stroke(width = 1.5f)
            // Top and left bevel highlight
            drawLine(
                color = effectiveRim.copy(alpha = if (enabled) 0.75f else 0.3f),
                start = Offset(0f, 0f),
                end = Offset(w, 0f),
                strokeWidth = 2f
            )
            drawLine(
                color = effectiveRim.copy(alpha = if (enabled) 0.60f else 0.25f),
                start = Offset(0f, 0f),
                end = Offset(0f, h),
                strokeWidth = 2f
            )
            // Bottom and right shadow rim
            drawLine(
                color = Color.Black.copy(alpha = 0.65f),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.55f),
                start = Offset(w, 0f),
                end = Offset(w, h),
                strokeWidth = 2f
            )
        }

        // Button Content: Icon + Typography
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = effectiveVein,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = if (subtitle != null) Alignment.Start else Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    fontSize = if (subtitle != null) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = effectiveText,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = effectiveVein.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
