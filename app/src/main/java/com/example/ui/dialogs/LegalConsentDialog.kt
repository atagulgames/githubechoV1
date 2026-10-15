package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Fullscreen User Agreement & Legal Consent Screen
 * Shown immediately after the Intro video completes.
 */
@Composable
fun LegalConsentScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(16.dp)
            .testTag("legal_consent_screen"),
        color = Color(0xFF090D16)
    ) {
        LegalConsentCard(
            onAccept = onAccept,
            onDecline = onDecline
        )
    }
}

/**
 * Dialog wrapper for backwards-compatibility or settings view.
 */
@Composable
fun LegalConsentDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(
        onDismissRequest = {}, // Non-dismissable by tapping outside
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D16))
                .padding(16.dp)
                .testTag("legal_consent_dialog"),
            color = Color.Transparent
        ) {
            LegalConsentCard(
                onAccept = onAccept,
                onDecline = onDecline
            )
        }
    }
}

@Composable
fun LegalConsentCard(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1E293B)),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A8A)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "User Agreement & Legal Consent",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Please review and accept terms before playing",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Legal, User Agreement & Health Warning Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .background(Color(0xFF0B1120))
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // 1. EULA & Terms of Service
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. End User License Agreement & Terms of Service",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "By downloading, installing, or playing this game, you agree to the following terms:\n" +
                            "• In-game assets, level designs, graphics, and audio engine are protected by intellectual property laws.\n" +
                            "• Cheating, third-party tampering, or unfair gameplay modifications are strictly prohibited.\n" +
                            "• In-game rewards, tokens, and diamonds are virtual digital assets with no real-world cash or exchange value.\n" +
                            "• Advertisements served in the application are provided by third-party networks and are subject to their policies.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Privacy Policy & Data Protection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2. Privacy Policy & Data Protection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF34D399)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your username, solved puzzle records, scores, and in-game progression are stored locally and in secure cloud backups solely to preserve your game profile. Your data is never sold to third parties for commercial advertising.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Photosensitivity & Epilepsy Warning
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3. Photosensitivity & Epilepsy Warning",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFFBBF24)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CAUTION: This game contains dynamic lighting, visual animations, pulsating patterns, and rhythmic sound effects. A small percentage of individuals may experience seizures when exposed to flashing lights. Please play in a well-lit environment and take breaks immediately if experiencing discomfort or dizziness.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Disclaimer of Warranties
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "4. Disclaimer of Warranties",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFA78BFA)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "This game is provided for casual entertainment and puzzle solving. The developer shall not be held liable for personal health reactions or hardware misuse. By proceeding, you acknowledge and agree to these terms.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 5. Ad Disclaimer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "5. Advertisements & Third-Party Disclaimer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFFF9100)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Advertisements displayed in this game are delivered by external ad networks. The developer does not control specific ad content or destinations. Advertisers are solely responsible for their promotional materials.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "✓ This consent is required only once and will not be prompted again.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34D399)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("decline_consent_button")
                ) {
                    Text(
                        text = "Decline & Exit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("accept_consent_button")
                ) {
                    Text(
                        text = "I Read and Agree",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
