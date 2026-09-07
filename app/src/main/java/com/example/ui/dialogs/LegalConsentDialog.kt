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
                        contentDescription = "Güvenlik",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Kullanıcı Sözleşmesi & Yasal Onay",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Oyuna başlamadan önce lütfen koşulları onaylayınız",
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
                // 1. Kullanıcı Sözleşmesi (EULA & Terms of Service)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. Kullanıcı Sözleşmesi & Hizmet Şartları",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bu oyunu indirerek, kurarak veya oynayarak aşağıdaki kuralları kabul etmiş sayılırsınız:\n" +
                            "• Oyun içi içerikler, seviye tasarımları, grafikler ve ses motoru fikri mülkiyet hukuku ile korunmaktadır.\n" +
                            "• Hile, üçüncü parti müdahale yazılımları veya adil rekabeti bozan yöntemler kesinlikle yasaktır ve hesap sıfırlanmasına sebep olabilir.\n" +
                            "• Oyun içi ödüller, jetonlar ve elmaslar dijital oyun içi varlıklardır, gerçek para karşılığı veya takas değeri taşımaz.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2. KVKK Aydınlatma Metni
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2. KVKK & Gizlilik Aydınlatma Metni",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF34D399)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "6698 sayılı Kişisel Verilerin Korunması Kanunu (\"KVKK\") kapsamında; kullanıcı adınız, çözülen bulmaca verileriniz, skorlarınız ve oyun içi ilerleme kayıtlarınız yalnızca yerel profilinizin korunması, cihazınızda Room veritabanında saklanması ve güvenli yedek alanına aktarılması amacıyla işlenir. Verileriniz hiçbir surette üçüncü şahıslara ticari amaçla satılmaz.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Epilepsi ve Işığa Duyarlılık Uyarısı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3. Epilepsi & Işığa Duyarlılık Uyarısı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFFBBF24)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "DİKKAT: Bu oyun hızlı yanıp sönen neon ışıklar, ritmik titreşimler, dinamik ses dalgaları ve görsel animasyonlar içermektedir. Işığa duyarlı epilepsi öyküsü bulunan bireylerde nadiren nöbet tetiklenebilir. Oyunu iyi aydınlatılmış bir ortamda oynayınız. Rahatsızlık, göz seyirmesi veya baş dönmesi hissettiğinizde oyunu kapatınız.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Sorumluluk Reddi Beyanı (Disclaimer)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "4. Sorumluluk Reddi Beyanı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFA78BFA)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bu mobil oyun eğlence ve zeka bulmacası maksadıyla sunulmaktadır. Kullanıcının oyunu oynarken karşılaşabileceği kişisel sağlık reaksiyonlarından veya cihaz kullanım hatalarından geliştirici sorumlu tutulamaz. Oyuna devam ederek bu koşulları bizzat kabul etmiş sayılırsınız.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "✓ Bu sözleşme ve onay bir defaya mahsus alınır ve daha sonra tekrar sorulmaz.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34D399)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Reddet ve Çıkış Yap / Kabul Et)
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
                        text = "Reddet & Çık",
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
                        text = "Okudum, Onaylıyorum",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
