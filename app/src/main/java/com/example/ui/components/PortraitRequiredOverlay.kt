package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Enforces Portrait Orientation.
 * If the device is detected in landscape orientation (or width > height in split screen/foldable),
 * this overlay completely blocks interaction and displays an animated warning
 * until the user rotates their device back to portrait orientation.
 */
@Composable
fun PortraitRequiredOverlay(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val isLandscapeConfig = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Also check actual aspect ratio of available window space (handles foldables/tablets)
        val isLandscapeAspect = maxWidth > maxHeight
        val isLandscape = isLandscapeConfig || isLandscapeAspect

        // Main app content
        content()

        // Blocking overlay when orientation is NOT portrait
        AnimatedVisibility(
            visible = isLandscape,
            enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.95f),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "rotation_anim")
            val phoneRotation by infiniteTransition.animateFloat(
                initialValue = 90f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "phone_angle"
            )
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.96f,
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF00A0F1D))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {} // Consume clicks so game behind cannot be touched
                    .testTag("portrait_enforcer_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Warning Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33EF4444))
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YATAY MOD ENGELLENDİ",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Device Rotation Icon with Glow Aura
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x440284C7),
                                        Color(0x2238BDF8),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(2.dp, Color(0x6638BDF8), CircleShape)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.rotate(phoneRotation)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "Dikey Çevir",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(68.dp)
                            )
                        }

                        // Small rotation indicator icon
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Title
                    Text(
                        text = "Lütfen Cihazınızı Dikey Tutun",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtitle / explanation
                    Text(
                        text = "ECHO'nun eşsiz akışkan bulmaca tahtası ve hassas dokunmatik mekanikleri yalnızca dikey (portre) mod için tasarlanmıştır. Devam etmek için telefonunuzu dikey konuma çevirin.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Force Portrait Action Button (in case system auto-rotation is locked)
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                            .testTag("force_portrait_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dikey Moda Çevir",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
