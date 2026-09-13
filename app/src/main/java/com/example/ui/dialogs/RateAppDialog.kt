package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.hapticClick
import com.example.data.EchoPreferences

/**
 * 3. Bölüm tamamlandığında her cihazda yalnızca 1 KERE açılan Derecelendirme Paneli.
 *
 * Başlık: "ECHOFLUX'u değerlendir"
 * Açıklama: "Deneyiminizi bizimle paylaşın."
 * 5 interaktif yıldız (başlangıçta boş ☆ ☆ ☆ ☆ ☆)
 * Ana buton: "Gönder"
 * Altında küçük, sade, altı çizili: "Daha sonra"
 */
@Composable
fun RateAppDialog(
    onRateSubmitted: (stars: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reviewUrl = "https://apkpure.com/tr/reviews/com.aistudio.echo.ykqrvw"

    var selectedStars by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF0284C7),
                            Color(0xFFF59E0B).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(26.dp))
                .testTag("rate_app_dialog"),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glowing Celestial Star Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B).copy(alpha = 0.28f),
                                    Color(0xFF0284C7).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title: "ECHOFLUX'u değerlendir"
                Text(
                    text = "ECHOFLUX'u değerlendir",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description: "Deneyiminizi bizimle paylaşın."
                Text(
                    text = "Deneyiminizi bizimle paylaşın.",
                    fontSize = 14.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5 Interactive Stars (☆ ☆ ☆ ☆ ☆ / ★ ★ ★ ☆ ☆)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("rating_stars_row")
                ) {
                    for (starIndex in 1..5) {
                        val isSelected = starIndex <= selectedStars
                        val starInteractionSource = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = starInteractionSource,
                                    indication = null,
                                    onClick = hapticClick {
                                        selectedStars = starIndex
                                        errorMessage = null
                                    }
                                )
                                .testTag("star_button_$starIndex"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$starIndex Yıldız",
                                tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFF64748B),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Error Message if user attempts to submit with 0 stars
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .testTag("rating_error_text")
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Main Button: "Gönder"
                Button(
                    onClick = hapticClick(isHeavy = true) {
                        if (selectedStars == 0) {
                            errorMessage = "Lütfen bir puan seçin."
                            return@hapticClick
                        }

                        // 1. Save locally & mark prompt shown
                        val prefs = EchoPreferences(context)
                        prefs.userRatingStars = selectedStars
                        prefs.ratingPromptShown = true

                        // 2. Notify view model & close dialog
                        onRateSubmitted(selectedStars)

                        // 3. Open APKPure review page safely without crash
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Safe fallback: URL cannot be opened, game continues without crashing
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rate_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStars > 0) Color(0xFF0284C7) else Color(0xFF334155),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Gönder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Text Button: "Daha sonra" (subtle, small, underlined)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            onClick = hapticClick {
                                val prefs = EchoPreferences(context)
                                prefs.ratingPromptShown = true
                                onDismiss()
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("rate_later_text_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daha sonra",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF94A3B8),
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}

