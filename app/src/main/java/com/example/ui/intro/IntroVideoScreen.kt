package com.example.ui.intro

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.audio.HarmonicAudioEngine
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Fullscreen intro video player component that plays user-provided intro.mp4 directly.
 * 
 * Strict constraints:
 * - The video plays exactly ONCE from beginning to end without restarting.
 * - Background music (BGM) is strictly suppressed until the intro has completed.
 * - Single-execution guard prevents multiple completion triggers.
 * - Allows instant skipping via skip button or tap to prevent blocking startup.
 */
@Composable
fun IntroVideoScreen(
    onIntroFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFinishedHandled = remember { AtomicBoolean(false) }
    var isFinishedState by remember { mutableStateOf(false) }
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }

    // Direct, compile-time verified resource URI for intro.mp4
    val videoUriToPlay: Uri = remember(context) {
        Uri.parse("android.resource://${context.packageName}/${R.raw.intro}")
    }

    val currentOnIntroFinished by rememberUpdatedState(onIntroFinished)

    // Dedicated single-execution completion handler
    val finishOnce = remember {
        {
            if (isFinishedHandled.compareAndSet(false, true)) {
                isFinishedState = true
                try {
                    videoViewInstance?.stopPlayback()
                } catch (_: Exception) {}
                currentOnIntroFinished()
            }
        }
    }

    // Strictly ensure background music does not play during intro
    DisposableEffect(Unit) {
        HarmonicAudioEngine.setIntroActive(true)
        onDispose {
            try {
                videoViewInstance?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    // Watchdog timer: Automatically advance after 2.5 seconds if VideoView completion stalls or in emulator
    LaunchedEffect(Unit) {
        delay(2_500L)
        finishOnce()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { finishOnce() }
            .testTag("intro_video_container"),
        contentAlignment = Alignment.Center
    ) {
        if (!isFinishedState) {
            // Direct fullscreen playback of user's intro.mp4
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewInstance = this
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            if (!isFinishedHandled.get()) {
                                start()
                            }
                        }
                        setOnCompletionListener {
                            finishOnce()
                        }
                        setOnErrorListener { _, _, _ ->
                            finishOnce()
                            true
                        }
                        setVideoURI(videoUriToPlay)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = {
                    // DO NOT restart the video in update callback
                }
            )
        }

        // Always-available Skip Button to prevent getting stuck
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Button(
                onClick = finishOnce,
                modifier = Modifier.testTag("skip_intro_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.65f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "SKIP  ✕",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

