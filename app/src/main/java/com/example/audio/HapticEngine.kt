package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.data.EchoPreferences

/**
 * Universal High-Compatibility Haptic Engine for Echo.
 * Guarantees tactile feedback on ALL physical Android devices (Samsung, Xiaomi, Redmi,
 * Pixel, Oppo, Vivo, Motorola, etc.) across all motor types (ERM and LRA).
 *
 * Avoids silent no-op predefined effects and uses explicit one-shot and waveform
 * timings with TOUCH / ASSISTANCE_SONIFICATION audio attributes so device policies
 * don't suppress the vibration.
 */
object HapticEngine {

    private const val TAG = "HapticEngine"
    private var appContext: Context? = null
    private var vibrator: Vibrator? = null
    private var prefs: EchoPreferences? = null

    private val touchAudioAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .build()
    }

    @Synchronized
    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        prefs = EchoPreferences(ctx)
        vibrator = obtainVibrator(ctx)
    }

    private fun obtainVibrator(ctx: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator ?: (ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve system vibrator: ${e.message}")
            null
        }
    }

    private fun getActiveVibrator(): Vibrator? {
        if (vibrator != null) return vibrator
        val ctx = appContext ?: return null
        vibrator = obtainVibrator(ctx)
        return vibrator
    }

    /**
     * Checks whether haptics are enabled by user settings in the app.
     */
    val isEnabled: Boolean
        get() = prefs?.hapticsEnabled ?: true

    /**
     * Executes a single tactile vibration pulse using hardware-level duration and attributes.
     */
    private fun vibrateOneShot(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (!isEnabled) return
        val vib = getActiveVibrator() ?: return
        try {
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = if (vib.hasAmplitudeControl() && amplitude in 1..255) {
                    amplitude
                } else {
                    VibrationEffect.DEFAULT_AMPLITUDE
                }
                val effect = VibrationEffect.createOneShot(durationMs, amp)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vib.vibrate(effect, attrs)
                } else {
                    vib.vibrate(effect, touchAudioAttributes)
                }
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Fallback for custom OEM security restrictions or deprecated APIs
            try {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            } catch (_: Exception) {}
        }
    }

    /**
     * Executes a patterned waveform vibration using hardware-level attributes.
     */
    private fun vibratePattern(timings: LongArray, amplitudes: IntArray? = null) {
        if (!isEnabled) return
        val vib = getActiveVibrator() ?: return
        try {
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (amplitudes != null && vib.hasAmplitudeControl() && amplitudes.size == timings.size) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vib.vibrate(effect, attrs)
                } else {
                    vib.vibrate(effect, touchAudioAttributes)
                }
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(timings, -1)
            }
        } catch (e: Exception) {
            try {
                @Suppress("DEPRECATION")
                vib.vibrate(timings, -1)
            } catch (_: Exception) {}
        }
    }

    /**
     * Tactile vibration triggered when ANY button is pressed ("düğmeler titreşsin").
     * Uses 48ms duration - perfectly tuned so both ERM and LRA vibration motors
     * produce a distinct, crisp tactile feedback without feeling sluggish.
     */
    fun triggerButtonClick() {
        vibrateOneShot(durationMs = 48L, amplitude = 220)
    }

    /**
     * Heavier vibration for primary CTA buttons (e.g. OYNA, Zafer, Sonraki Bölüm, Sandık Aç).
     */
    fun triggerButtonHeavyClick() {
        vibrateOneShot(durationMs = 75L, amplitude = 255)
    }

    /**
     * Double-pulse vibration for important actions like resetting or clearing echoes.
     */
    fun triggerButtonDoublePulse() {
        vibratePattern(
            timings = longArrayOf(0, 36, 40, 48),
            amplitudes = intArrayOf(0, 200, 0, 240)
        )
    }

    /**
     * Micro vibration when touching or connecting nodes in the web ("düğümler").
     * Tuned to 40ms so players feel an immediate tactile bump every time
     * they touch or link a node!
     */
    fun triggerNodeTouch() {
        vibrateOneShot(durationMs = 40L, amplitude = 200)
    }

    /**
     * Subtle pulse when player's stroke gets close to a previous echo.
     */
    fun triggerProximityPulse() {
        vibrateOneShot(durationMs = 26L, amplitude = 140)
    }

    /**
     * Sharp tactile vibration for errors (e.g. wrong start node, locked gate, deadlock).
     */
    fun triggerErrorFeedback() {
        vibratePattern(
            timings = longArrayOf(0, 55, 45, 70),
            amplitudes = intArrayOf(0, 220, 0, 255)
        )
    }

    /**
     * Celebratory tactile rhythm upon level victory.
     */
    fun triggerVictoryRhythm() {
        vibratePattern(
            timings = longArrayOf(0, 40, 50, 40, 50, 80),
            amplitudes = intArrayOf(0, 180, 0, 200, 0, 255)
        )
    }
}

/**
 * Higher-order helper for onClick lambdas that ensures device vibrates on button press ("düğmeler titreşsin").
 */
inline fun hapticClick(isHeavy: Boolean = false, crossinline action: () -> Unit): () -> Unit = {
    if (isHeavy) {
        HapticEngine.triggerButtonHeavyClick()
    } else {
        HapticEngine.triggerButtonClick()
    }
    action()
}
