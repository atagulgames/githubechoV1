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
        return com.example.data.LevelRuleCatalog.getRuleForLevel(levelId, isTurkish = true).toMechanicInfo()
    }
}

@Composable
fun LevelMechanicIntroDialog(
    mechanicInfo: LevelMechanicInfo,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    val levelId = when (mechanicInfo.tier) {
        1 -> 1
        2 -> 4
        3 -> 11
        4 -> 21
        5 -> 31
        6 -> 41
        7 -> 51
        8 -> 61
        9 -> 71
        10 -> 81
        11 -> 91
        else -> 100
    }
    LevelRuleOnboardingOverlay(
        currentLevelId = levelId,
        isDarkTheme = isDarkTheme,
        isTurkish = true,
        onDismiss = onDismiss
    )
}
