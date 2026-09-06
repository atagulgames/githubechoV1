package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object HarmonicAudioEngine {

    var isSoundEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                pauseBgm()
            } else {
                if (!isIntroActive) {
                    resumeBgm()
                }
            }
        }

    /**
     * Tracks whether the intro sequence or intro video is actively playing.
     * When true, background music (fon müziği) is strictly inhibited.
     */
    var isIntroActive: Boolean = true
        private set

    @Synchronized
    fun setIntroActive(active: Boolean) {
        isIntroActive = active
        if (active) {
            pauseBgm()
        }
    }

    /**
     * Called strictly after the intro finishes or is skipped.
     * Starts the background music smoothly.
     */
    @Synchronized
    fun onIntroFinished() {
        isIntroActive = false
        startBgm()
    }

    private var appContext: Context? = null
    private var bgmPlayer: MediaPlayer? = null
    private var activeSfxPlayer: MediaPlayer? = null
    private var savedBgmPosition: Int = 0
    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val activeSfxCount = java.util.concurrent.atomic.AtomicInteger(0)

    private var currentBgmVolume = 0.70f
    private const val NORMAL_BGM_VOLUME = 0.70f
    private const val DUCKED_BGM_VOLUME = 0.15f
    private var duckJob: Job? = null

    private val pentatonicScale = floatArrayOf(
        261.63f, // C4
        293.66f, // D4
        329.63f, // E4
        392.00f, // G4
        440.00f, // A4
        523.25f, // C5
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f  // A5
    )

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Starts continuous ambient background music in an infinite loop.
     * Keeps steady natural playback speed and never restarts from beginning when navigating.
     * Sourced from assets/audio/gamemusıc.mp3 or res/raw/gamemusic.mp3.
     */
    @Synchronized
    fun startBgm() {
        if (!isSoundEnabled || isIntroActive) return
        val ctx = appContext ?: return
        try {
            if (bgmPlayer != null) {
                if (bgmPlayer?.isPlaying == false) {
                    applyBgmVolume(NORMAL_BGM_VOLUME)
                    bgmPlayer?.start()
                }
                return
            }

            var resId = ctx.resources.getIdentifier("gamemusic", "raw", ctx.packageName)
            if (resId == 0) {
                resId = ctx.resources.getIdentifier("bgm_game_music", "raw", ctx.packageName)
            }

            val player = if (resId != 0) {
                MediaPlayer.create(ctx, resId)
            } else {
                // Asset loading fallback
                try {
                    val assetNames = listOf(
                        "audio/gamemusıc.mp3",
                        "audio/gamemusic.mp3",
                        "audio/gamemusic.ogg"
                    )
                    var afd: android.content.res.AssetFileDescriptor? = null
                    for (name in assetNames) {
                        try {
                            afd = ctx.assets.openFd(name)
                            break
                        } catch (_: Exception) {}
                    }
                    if (afd != null) {
                        MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                            prepare()
                        }
                    } else null
                } catch (_: Exception) {
                    null
                }
            }

            if (player != null) {
                bgmPlayer = player.apply {
                    isLooping = true
                    currentBgmVolume = NORMAL_BGM_VOLUME
                    setVolume(currentBgmVolume, currentBgmVolume)
                    start()
                }
            }
        } catch (_: Exception) {
            // Audio setup fallback
        }
    }

    @Synchronized
    fun pauseBgm() {
        try {
            if (bgmPlayer?.isPlaying == true) {
                savedBgmPosition = bgmPlayer?.currentPosition ?: 0
                bgmPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    fun resumeBgm() {
        if (!isSoundEnabled || isIntroActive) return
        try {
            if (bgmPlayer == null) {
                startBgm()
            } else if (bgmPlayer?.isPlaying == false) {
                applyBgmVolume(NORMAL_BGM_VOLUME)
                bgmPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    /**
     * Smoothly ducks the background music volume (e.g. from 0.70f to 0.15f)
     * so that foreground sound effects stand out with crystal clarity.
     */
    fun duckBgm(targetVolume: Float = DUCKED_BGM_VOLUME, durationMs: Long = 180L) {
        if (!isSoundEnabled || bgmPlayer?.isPlaying != true) return
        duckJob?.cancel()
        duckJob = audioScope.launch {
            val steps = 10
            val startVol = currentBgmVolume
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            for (i in 1..steps) {
                val vol = startVol + (targetVolume - startVol) * (i.toFloat() / steps)
                applyBgmVolume(vol)
                delay(stepDelay)
            }
            applyBgmVolume(targetVolume)
        }
    }

    /**
     * Smoothly restores the background music volume back to normal.
     */
    fun restoreBgm(targetVolume: Float = NORMAL_BGM_VOLUME, durationMs: Long = 300L) {
        if (!isSoundEnabled || bgmPlayer?.isPlaying != true) return
        duckJob?.cancel()
        duckJob = audioScope.launch {
            val steps = 12
            val startVol = currentBgmVolume
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            for (i in 1..steps) {
                val vol = startVol + (targetVolume - startVol) * (i.toFloat() / steps)
                applyBgmVolume(vol)
                delay(stepDelay)
            }
            applyBgmVolume(targetVolume)
        }
    }

    private fun applyBgmVolume(volume: Float) {
        currentBgmVolume = volume.coerceIn(0f, 1f)
        try {
            bgmPlayer?.setVolume(currentBgmVolume, currentBgmVolume)
        } catch (_: Exception) {}
    }

    /**
     * Plays an intervening SFX with intelligent audio ducking:
     * 1. Automatically ducks background music down.
     * 2. Plays the requested SFX from res/raw or assets/audio/.
     * 3. When SFX finishes, smoothly restores background music to full volume.
     */
    private fun playInterveningSfx(
        resName: String,
        sfxDurationMs: Long = 1200L,
        fallbackTone: () -> Unit
    ) {
        if (!isSoundEnabled) return
        val ctx = appContext ?: return

        duckBgm(DUCKED_BGM_VOLUME, durationMs = 140L)
        activeSfxCount.incrementAndGet()

        try {
            var resId = ctx.resources.getIdentifier(resName, "raw", ctx.packageName)
            if (resId == 0 && resName == "brokenredline") {
                resId = ctx.resources.getIdentifier("broken_red_line", "raw", ctx.packageName)
            }
            if (resId == 0 && resName == "nextlevel") {
                resId = ctx.resources.getIdentifier("next_level", "raw", ctx.packageName)
            }

            var player: MediaPlayer? = if (resId != 0) {
                MediaPlayer.create(ctx, resId)
            } else null

            if (player == null) {
                val assetCandidates = listOf(
                    "audio/$resName.mp3",
                    "audio/${resName.lowercase()}.mp3"
                )
                for (path in assetCandidates) {
                    try {
                        val afd = ctx.assets.openFd(path)
                        player = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                            prepare()
                        }
                        break
                    } catch (_: Exception) {}
                }
            }

            if (player != null) {
                try {
                    activeSfxPlayer?.stop()
                    activeSfxPlayer?.release()
                } catch (_: Exception) {}

                activeSfxPlayer = player
                player.setVolume(0.95f, 0.95f)
                player.setOnCompletionListener { mp ->
                    try {
                        mp.release()
                    } catch (_: Exception) {}
                    if (activeSfxPlayer == mp) {
                        activeSfxPlayer = null
                    }
                    if (activeSfxCount.decrementAndGet() <= 0) {
                        activeSfxCount.set(0)
                        restoreBgm(NORMAL_BGM_VOLUME)
                    }
                }
                player.setOnErrorListener { mp, _, _ ->
                    try { mp.release() } catch (_: Exception) {}
                    if (activeSfxCount.decrementAndGet() <= 0) {
                        activeSfxCount.set(0)
                        restoreBgm(NORMAL_BGM_VOLUME)
                    }
                    true
                }
                player.start()
                return
            }
        } catch (_: Exception) {}

        // Fallback procedural sound + scheduled restore
        fallbackTone()
        audioScope.launch {
            delay(sfxDurationMs)
            if (activeSfxCount.decrementAndGet() <= 0) {
                activeSfxCount.set(0)
                restoreBgm(NORMAL_BGM_VOLUME)
            }
        }
    }

    /**
     * Plays Next Level voice and sound effect cleanly.
     * Background music volume is ducked while voice plays, then restored.
     */
    fun playNextLevel() {
        playInterveningSfx("nextlevel", sfxDurationMs = 1400L) {
            audioScope.launch {
                playSynthTone(523.25f, 120, 0.60f)
                delay(90)
                playSynthTone(659.25f, 120, 0.65f)
                delay(90)
                playSynthTone(783.99f, 260, 0.70f)
            }
        }
    }

    /**
     * Plays Win / Victory audio fanfare.
     * Background music volume is ducked, harmonic victory chime plays, then BGM smoothly returns.
     */
    fun playVictoryCascade() {
        if (!isSoundEnabled) return
        duckBgm(DUCKED_BGM_VOLUME, durationMs = 150L)
        audioScope.launch {
            try {
                playSynthTone(392.00f, 110, 0.55f)
                delay(90)
                playSynthTone(523.25f, 110, 0.60f)
                delay(90)
                playSynthTone(659.25f, 130, 0.65f)
                delay(110)
                playSynthTone(783.99f, 180, 0.70f)
                delay(120)
                playSynthTone(1046.50f, 400, 0.75f)
                delay(450)
            } finally {
                restoreBgm(NORMAL_BGM_VOLUME, durationMs = 300L)
            }
        }
    }

    /**
     * Plays win.mp3 audio fanfare when all 100 levels are completed.
     * Background music volume is ducked, win fanfare plays, then BGM smoothly returns.
     */
    fun playWinAllLevels() {
        playInterveningSfx("win", sfxDurationMs = 3000L) {
            playVictoryCascade()
        }
    }

    /**
     * Plays Collision Error / Hata buzz sound.
     */
    fun playCollisionBuzz() {
        playInterveningSfx("hata", sfxDurationMs = 500L) {
            audioScope.launch {
                playSynthTone(130.81f, 160, 0.55f)
            }
        }
    }

    /**
     * Plays error effect sound (hata.mp3).
     */
    fun playHataSound() {
        playCollisionBuzz()
    }

    /**
     * Plays broken echo line cleared sound (brokenredline.mp3).
     */
    fun playBrokenRedLine() {
        playInterveningSfx("brokenredline", sfxDurationMs = 600L) {
            audioScope.launch {
                playSynthTone(349.23f, 90, 0.55f)
                delay(70)
                playSynthTone(174.61f, 130, 0.50f)
            }
        }
    }

    fun playDrillBeam() {
        playBrokenRedLine()
    }

    /**
     * Plays the user-provided MP3 sound effect directly on the login screen.
     * Strictly avoids any synthetic or placeholder sound if the user's file is not present.
     */
    fun playLoginEffect(context: Context) {
        if (!isSoundEnabled) return
        try {
            if (com.example.media.GameMediaAssets.isAssetAvailable(context, com.example.media.GameMediaAssets.LOGIN_AUDIO_PATH)) {
                val afd = context.assets.openFd(com.example.media.GameMediaAssets.LOGIN_AUDIO_PATH)
                val mp = MediaPlayer()
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mp.prepare()
                mp.setOnCompletionListener { it.release() }
                mp.start()
                return
            }
            if (com.example.media.GameMediaAssets.isAssetAvailable(context, com.example.media.GameMediaAssets.LOGIN_AUDIO_ROOT_PATH)) {
                val afd = context.assets.openFd(com.example.media.GameMediaAssets.LOGIN_AUDIO_ROOT_PATH)
                val mp = MediaPlayer()
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mp.prepare()
                mp.setOnCompletionListener { it.release() }
                mp.start()
                return
            }
            val resId = context.resources.getIdentifier("effect", "raw", context.packageName)
            if (resId != 0) {
                val mp = MediaPlayer.create(context, resId)
                mp?.setOnCompletionListener { it.release() }
                mp?.start()
                return
            }
        } catch (_: Exception) {
            // Strictly respect mandate: no AI synthesis or placeholder sound
        }
    }

    /**
     * Plays Atagul Games Intro fanfare chime.
     */
    fun playIntroJingle() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Elegant chord progression for intro video sequence
            playSynthTone(261.63f, 200, 0.50f) // C4
            delay(140)
            playSynthTone(329.63f, 220, 0.55f) // E4
            delay(150)
            playSynthTone(392.00f, 260, 0.60f) // G4
            delay(160)
            playSynthTone(523.25f, 700, 0.70f) // C5
        }
    }

    /**
     * Plays melodic harmonic tone when a node is connected during drawing.
     */
    fun playNodeTone(sequenceIndex: Int) {
        if (!isSoundEnabled) return
        val freq = pentatonicScale[sequenceIndex % pentatonicScale.size]
        audioScope.launch {
            playSynthTone(freq, durationMs = 120, volume = 0.55f)
        }
    }

    private fun playSynthTone(freq: Float, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = 1.0 - (i.toDouble() / numSamples)
            val sample = sin(2 * PI * freq * t) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playPcm(buffer, sampleRate)
    }

    private fun playPcm(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) {}
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
        } catch (_: Exception) {}
    }
}
