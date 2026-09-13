package com.example.ui.intro

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

    // Direct, compile-time verified resource URI for intro.mp4 (duration: ~11s)
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

    // Watchdog timer: Automatically advance after 14 seconds if VideoView completion stalls
    // Video is 11s, so 14s gives plenty of buffer without trapping the user
    LaunchedEffect(Unit) {
        delay(14_000L)
        finishOnce()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
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
                    // DO NOT restart the video in update callback!
                    // Recomposition must never re-trigger video playback.
                }
            )
        }
    }
}

