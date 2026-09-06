package com.example.ui.dialogs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicAudioEngine

data class LevelMechanicInfo(
    val tier: Int,
    val icon: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val tip: String,
    val badgeColor: Long
)

object MechanicCatalog {
    fun getMechanicInfoForLevel(levelId: Int): LevelMechanicInfo {
        val tier = (((levelId - 1) / 10) + 1).coerceIn(1, 10)
        return when (tier) {
            1 -> LevelMechanicInfo(
                tier = 1,
                icon = "🎯",
                title = "Temel Yankı Kuralları",
                subtitle = "Harmonik Başlangıç (Bölüm 1 - 10)",
                description = "Parmağını basılı tutarak tüm düğümleri tek kesintisiz rotada birleştir! Çizdiğin rotadan oluşan kırmızı yankı çizgileri sonraki hamlede tehlikeli hale gelir; onlara çarpmadan bölümü tamamla!",
                tip = "Çapraz ve birbirini kesmeyen geniş yaylar çizerek sonraki hamlelerin için tahtada açık alan bırak!",
                badgeColor = 0xFF0284C7
            )
            2 -> LevelMechanicInfo(
                tier = 2,
                icon = "⚡",
                title = "Vektör Akışı & Dar Açı",
                subtitle = "Hassas Geometrik Hatlar (Bölüm 11 - 20)",
                description = "Düğümler artık daha dar ve köşeli dizilimlerde! Hızlı hareket etmek yerine parmağını sakin ve kontrollü kaydırarak dönüş açılarını iyi ayarla.",
                tip = "Dönüş yaparken düğüm merkezlerini kaçırmamak için ses efektlerini dinle!",
                badgeColor = 0xFF8B5CF6
            )
            3 -> LevelMechanicInfo(
                tier = 3,
                icon = "🗝️",
                title = "Kilit & Anahtar Mekaniği!",
                subtitle = "Gizli Kapılar Açılıyor (Bölüm 21 - 30)",
                description = "Sahada kilitli kapı düğümleri var! Kapıdan güvenle geçebilmek için önce parlak sarı Anahtar düğümünü rotana dahil etmelisin. Anahtarı almadan kapıya dokunursan yankı kırılır!",
                tip = "Rotanı kurgularken ilk hedefin her zaman Anahtar düğümü olsun!",
                badgeColor = 0xFFD97706
            )
            4 -> LevelMechanicInfo(
                tier = 4,
                icon = "⏳",
                title = "Sönen Dalgalar",
                subtitle = "Zamansal Çözünme (Bölüm 31 - 40)",
                description = "Bu seviyelerde eski yankı çizgileri zamanla solarak erir. Ancak acele edip yanlış rotaya sapma; en az yankı hamlesiyle 3 yıldızı kap!",
                tip = "'Yankı Kırıcı' güçlendiricisi ile tıkanan kırmızı hatları tek dokunuşla patlatabilirsin!",
                badgeColor = 0xFFEC4899
            )
            5 -> LevelMechanicInfo(
                tier = 5,
                icon = "👻",
                title = "Gölge Rezonansı",
                subtitle = "Hayalet Düğümler (Bölüm 41 - 50)",
                description = "Simetrik ve yanılsamalı gölge düğümleri sahnede! Hangi düğümün öncelikli olduğunu anlamak için parlayan rezonans halkalarını takip et.",
                tip = "İpucu butonuna dokunarak bir sonraki en doğru adımı önizleyebilirsin!",
                badgeColor = 0xFF6366F1
            )
            6 -> LevelMechanicInfo(
                tier = 6,
                icon = "❄️",
                title = "Fraktal Matris",
                subtitle = "Altıgen Petek Mimarisi (Bölüm 51 - 60)",
                description = "Doğanın en kusursuz geometrisi olan altıgen petekler! Çok yönlü dallanmalar sayesinde birden fazla alternatif çözüm yolu bulabilirsin.",
                tip = "Dış halkadaki düğümlerden başlayıp merkeze doğru ilerlemek rotanı ferahlatır!",
                badgeColor = 0xFF14B8A6
            )
            7 -> LevelMechanicInfo(
                tier = 7,
                icon = "🌀",
                title = "Kuantum Kapıları",
                subtitle = "Çift Geçişli Boyut Tünelleri (Bölüm 61 - 70)",
                description = "Kuantum seviyelerinde birden fazla kilit ve geçit bulunur. Sıralamayı doğru yap: Önce 1. anahtar -> 1. kapı, sonra 2. anahtar -> 2. kapı!",
                tip = "Renk halkalarına dikkat et, kilidi açılmayan kapıya asla dokunma!",
                badgeColor = 0xFF2563EB
            )
            8 -> LevelMechanicInfo(
                tier = 8,
                icon = "⌛",
                title = "Zamansal Akış",
                subtitle = "İleri-Geri Vektör Sınavı (Bölüm 71 - 80)",
                description = "Uzun mesafeli düğüm geçişleri ve spiral hatlar! Yankı izlerinin birbirini kesmemesi için tahtanın kenarlarını akıllıca kullan.",
                tip = "Kenar boşluklarından dolaşarak tahtanın ortasındaki düğümleri sonraya sakla!",
                badgeColor = 0xFFF59E0B
            )
            9 -> LevelMechanicInfo(
                tier = 9,
                icon = "🕸️",
                title = "Usta Ağları",
                subtitle = "Büyük Strateji & Zeka (Bölüm 81 - 90)",
                description = "30'dan fazla düğüm içeren devasa örümcek ağı geometrileri! Burası gerçek ustaların kendini kanıtladığı yerdir.",
                tip = "Hamle yapmadan önce tahtayı birkaç saniye analiz et; rotanı zihninde tamamla!",
                badgeColor = 0xFF8B5CF6
            )
            else -> LevelMechanicInfo(
                tier = 10,
                icon = "👑",
                title = "Omega Zirvesi",
                subtitle = "Büyük Final İmtihanı (Bölüm 91 - 100)",
                description = "Echo evreninin 100. ve nihai zirvesi! Tüm öğrendiğin mekanikleri bir arada kullanarak 100 seviyeyi de fethet ve efsanevi şampiyonluk kupasını kaldır!",
                tip = "Başarılar Yankı Ustası! Şampiyonluk seni bekliyor!",
                badgeColor = 0xFFEF4444
            )
        }
    }
}

@Composable
fun LevelMechanicIntroDialog(
    mechanicInfo: LevelMechanicInfo,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val tipBg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val badgeColor = Color(mechanicInfo.badgeColor)

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
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .testTag("mechanic_intro_dialog"),
            color = cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Feature Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✨ YENİ OYUN MEKANİĞİ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
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

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Feature Icon Badge
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(80.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(badgeColor, badgeColor.copy(alpha = 0.7f))
                            )
                        )
                        .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mechanicInfo.icon,
                        fontSize = 38.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = mechanicInfo.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = mechanicInfo.subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = mechanicInfo.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = textPrimary.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pro Tip Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(tipBg)
                        .border(1.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "İpucu",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Usta İpucu",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mechanicInfo.tip,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = {
                        HarmonicAudioEngine.playCandyPop(2)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(6.dp, RoundedCornerShape(25.dp))
                        .testTag("mechanic_intro_ok_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Harika, Başlayalım!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
