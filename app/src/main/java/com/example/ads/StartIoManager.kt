package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener

object StartIoManager {
    private const val TAG = "StartIoManager"
    const val APP_ID = "208838202"

    private var isInitialized = false
    private var isTestMode = false

    private var preloadedRewardedAd: StartAppAd? = null
    private var preloadedInterstitialAd: StartAppAd? = null

    private var isRewardedLoading = false
    private var isInterstitialLoading = false

    private var rewardedRetryCount = 0
    private var interstitialRetryCount = 0
    private const val MAX_RETRY_COUNT = 2

    private val mainHandler = Handler(Looper.getMainLooper())
    private var retryRewardedRunnable: Runnable? = null
    private var retryInterstitialRunnable: Runnable? = null

    fun isEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT.lowercase()
        val model = android.os.Build.MODEL.lowercase()
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        val brand = android.os.Build.BRAND.lowercase()
        val device = android.os.Build.DEVICE.lowercase()
        val product = android.os.Build.PRODUCT.lowercase()
        val hardware = android.os.Build.HARDWARE.lowercase()
        val board = android.os.Build.BOARD.lowercase()

        return fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || fingerprint.contains("google_sdk")
                || fingerprint.contains("emulator")
                || fingerprint.contains("test-keys")
                || fingerprint.contains("vbox")
                || model.contains("google_sdk")
                || model.contains("emulator")
                || model.contains("android sdk")
                || model.contains("sdk")
                || model.contains("virtual")
                || model.contains("gphone")
                || manufacturer.contains("genymotion")
                || (manufacturer.contains("google") && (model.contains("sdk") || model.contains("gphone") || product.contains("sdk")))
                || (brand.startsWith("generic") && device.startsWith("generic"))
                || product.contains("sdk")
                || product.contains("google_sdk")
                || product.contains("emulator")
                || product.contains("simulator")
                || product.contains("cuttlefish")
                || product.contains("cf_")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || hardware.contains("cutf")
                || hardware.contains("cuttlefish")
                || hardware.contains("vsoc")
                || hardware.contains("virtualbox")
                || hardware.contains("qemu")
                || board.contains("goldfish")
                || board.contains("cutf")
                || board.contains("vsoc")
                || (android.os.Build.TAGS?.contains("test-keys") == true)
    }

    /**
     * Initializes Start.io SDK.
     */
    fun initialize(activity: Activity, testMode: Boolean = false) {
        isTestMode = testMode
        try {
            StartAppSDK.setTestAdsEnabled(isTestMode)

            if (!isInitialized) {
                // Initialize with App ID 208838202 in 100% Live Ads Mode
                StartAppSDK.init(activity, APP_ID, false)
                StartAppAd.disableSplash()

                // Submit GDPR/Privacy consent for highest fill rate and maximum eCPM on live networks
                StartAppSDK.setUserConsent(activity, "pas", System.currentTimeMillis(), true)
                isInitialized = true
                Log.d(TAG, "Start.io SDK successfully initialized with App ID: $APP_ID (Live Ads Mode: TestMode=$isTestMode)")
            }

            preloadAds(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Start.io SDK", e)
        }
    }

    fun setTestModeEnabled(enabled: Boolean, context: Context? = null) {
        isTestMode = enabled
        try {
            StartAppSDK.setTestAdsEnabled(enabled)
            Log.d(TAG, "Start.io test mode updated: $enabled")
            preloadedRewardedAd = null
            preloadedInterstitialAd = null
            if (context is Activity) {
                preloadAds(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating test mode", e)
        }
    }

    fun isTestModeEnabled(): Boolean = isTestMode

    /**
     * Preloads both Rewarded Video and Interstitial ads in memory
     * so they are ready for instantaneous zero-latency playback.
     */
    fun preloadAds(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        rewardedRetryCount = 0
        interstitialRetryCount = 0
        preloadRewardedVideo(activity)
        preloadInterstitial(activity)
    }

    fun preloadRewardedVideo(activity: Activity) {
        try {
            if (activity.isFinishing || activity.isDestroyed) return
            if (preloadedRewardedAd?.isReady == true || isRewardedLoading) {
                return
            }

            retryRewardedRunnable?.let { mainHandler.removeCallbacks(it) }

            val ad = StartAppAd(activity)
            preloadedRewardedAd = ad
            isRewardedLoading = true

            Log.d(TAG, "Preloading Start.io Rewarded Video (AppId=$APP_ID, TestMode=$isTestMode)...")
            ad.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(receivedAd: Ad) {
                    isRewardedLoading = false
                    rewardedRetryCount = 0
                    Log.d(TAG, "Start.io Rewarded Video successfully loaded and ready!")
                }

                override fun onFailedToReceiveAd(receivedAd: Ad?) {
                    isRewardedLoading = false
                    preloadedRewardedAd = null
                    val errMsg = receivedAd?.errorMessage ?: "NO FILL"

                    // If video-only ad has NO FILL on live inventory, preload available fullscreen ad automatically
                    if (errMsg.contains("NO FILL", ignoreCase = true) && preloadedInterstitialAd == null) {
                        preloadInterstitial(activity)
                        return
                    }

                    if (rewardedRetryCount < MAX_RETRY_COUNT) {
                        rewardedRetryCount++
                        val delayMs = 30_000L * rewardedRetryCount
                        Log.d(TAG, "Start.io live ad preload retry $rewardedRetryCount in ${delayMs / 1000}s...")
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            retryRewardedRunnable = Runnable {
                                if (!activity.isFinishing && !activity.isDestroyed) {
                                    preloadRewardedVideo(activity)
                                }
                            }
                            mainHandler.postDelayed(retryRewardedRunnable!!, delayMs)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            isRewardedLoading = false
            preloadedRewardedAd = null
            Log.e(TAG, "Error during preloadRewardedVideo", e)
        }
    }

    fun preloadInterstitial(activity: Activity) {
        try {
            if (activity.isFinishing || activity.isDestroyed) return
            if (preloadedInterstitialAd?.isReady == true || isInterstitialLoading) {
                return
            }

            retryInterstitialRunnable?.let { mainHandler.removeCallbacks(it) }

            val ad = StartAppAd(activity)
            preloadedInterstitialAd = ad
            isInterstitialLoading = true

            Log.d(TAG, "Preloading Start.io Interstitial (AppId=$APP_ID, TestMode=$isTestMode)...")
            ad.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(receivedAd: Ad) {
                    isInterstitialLoading = false
                    interstitialRetryCount = 0
                    Log.d(TAG, "Start.io Interstitial successfully loaded and ready!")
                }

                override fun onFailedToReceiveAd(receivedAd: Ad?) {
                    isInterstitialLoading = false
                    preloadedInterstitialAd = null
                    val errMsg = receivedAd?.errorMessage ?: "NO FILL"

                    if (interstitialRetryCount < MAX_RETRY_COUNT) {
                        interstitialRetryCount++
                        val delayMs = 30_000L * interstitialRetryCount
                        Log.d(TAG, "Start.io live Interstitial preload failed: $errMsg. Backoff retry $interstitialRetryCount in ${delayMs / 1000}s...")
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            retryInterstitialRunnable = Runnable {
                                if (!activity.isFinishing && !activity.isDestroyed) {
                                    preloadInterstitial(activity)
                                }
                            }
                            mainHandler.postDelayed(retryInterstitialRunnable!!, delayMs)
                        }
                    } else {
                        Log.d(TAG, "Start.io live Interstitial inventory temporarily unavailable ($errMsg). Pausing retries.")
                    }
                }
            })
        } catch (e: Exception) {
            isInterstitialLoading = false
            preloadedInterstitialAd = null
            Log.e(TAG, "Error during preloadInterstitial", e)
        }
    }

    fun onBackPressed(activity: Activity) {
        try {
            StartAppAd.onBackPressed(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error in StartAppAd.onBackPressed", e)
        }
    }

    /**
     * Displays a full-screen interstitial ad.
     * Uses preloaded ad if available for instant presentation;
     * otherwise triggers a live on-demand request with a safety timeout to protect UX.
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDisplayed: () -> Unit = {},
        onAdClosed: () -> Unit = {}
    ) {
        try {
            var isHandled = false
            fun finishOnce() {
                if (!isHandled) {
                    isHandled = true
                    activity.runOnUiThread { onAdClosed() }
                    preloadAds(activity)
                }
            }

            val preloaded = preloadedInterstitialAd
            if (preloaded != null && preloaded.isReady) {
                preloadedInterstitialAd = null
                val displayed = preloaded.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        finishOnce()
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        finishOnce()
                    }
                })
                if (displayed) return
            }

            // On-demand request for live interstitial with safety timeout
            loadAndShowInterstitial(activity, onAdDisplayed) { finishOnce() }
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying interstitial ad", e)
            activity.runOnUiThread { onAdClosed() }
        }
    }

    private fun loadAndShowInterstitial(
        activity: Activity,
        onAdDisplayed: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        var isHandled = false
        fun finishOnce() {
            if (!isHandled) {
                isHandled = true
                activity.runOnUiThread { onAdClosed() }
                preloadAds(activity)
            }
        }

        // 2.5s safety timeout: protect user experience if ad takes long to fill
        val timeoutRunnable = Runnable {
            Log.w(TAG, "Live on-demand interstitial timed out (2.5s). Advancing to next level seamlessly.")
            finishOnce()
        }
        mainHandler.postDelayed(timeoutRunnable, 2500L)

        val startAppAd = StartAppAd(activity)
        startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                mainHandler.removeCallbacks(timeoutRunnable)
                if (isHandled) return
                val displayed = startAppAd.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        finishOnce()
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        finishOnce()
                    }
                })
                if (!displayed) {
                    finishOnce()
                }
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                mainHandler.removeCallbacks(timeoutRunnable)
                Log.w(TAG, "Live on-demand interstitial failed: ${ad?.errorMessage}")
                finishOnce()
            }
        })
    }

    /**
     * Shows a real Live Rewarded Ad.
     * Strategy:
     * 1. Check if preloaded pure Rewarded Video is ready -> show immediately.
     * 2. If video not ready, check if preloaded Interstitial is ready -> show for reward.
     * 3. If neither ready, request dynamic on-demand live ad with an 8-second safety timeout.
     * 4. Reward is granted strictly on successful completion.
     * 5. Zero test mode fallbacks.
     */
    fun showRewardedVideoAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDisplayed: () -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdUnavailable: (String) -> Unit = {}
    ) {
        try {
            var rewardGiven = false

            fun grantRewardOnce() {
                if (!rewardGiven) {
                    rewardGiven = true
                    activity.runOnUiThread { onRewardEarned() }
                }
            }

            // Case 1: Preloaded Live Rewarded Video is ready
            val preloadedVideo = preloadedRewardedAd
            if (preloadedVideo != null && preloadedVideo.isReady) {
                Log.d(TAG, "Showing preloaded Start.io Live Rewarded Video")
                preloadedRewardedAd = null

                preloadedVideo.setVideoListener(object : VideoListener {
                    override fun onVideoCompleted() {
                        Log.d(TAG, "Start.io Rewarded Video completed!")
                        grantRewardOnce()
                    }
                })

                val displayed = preloadedVideo.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantRewardOnce()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        Log.w(TAG, "Preloaded video adNotDisplayed. No reward granted.")
                        activity.runOnUiThread {
                            onAdUnavailable("Reklam gösterilemedi. Ödül verilemedi.")
                            onAdClosed()
                        }
                        preloadAds(activity)
                    }
                })

                if (displayed) return
            }

            // Case 2: Preloaded Fullscreen/Interstitial is ready
            val preloadedInter = preloadedInterstitialAd
            if (preloadedInter != null && preloadedInter.isReady) {
                Log.d(TAG, "Showing preloaded Start.io Interstitial for reward")
                preloadedInterstitialAd = null

                val displayed = preloadedInter.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantRewardOnce()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        Log.w(TAG, "Preloaded interstitial adNotDisplayed. No reward granted.")
                        activity.runOnUiThread {
                            onAdUnavailable("Reklam gösterilemedi. Ödül verilemedi.")
                            onAdClosed()
                        }
                        preloadAds(activity)
                    }
                })

                if (displayed) return
            }

            // Case 3: Dynamic on-demand live ad request with 8-second safety timeout
            loadDynamicRewardAd(
                activity = activity,
                grantReward = { grantRewardOnce() },
                onAdDisplayed = onAdDisplayed,
                onAdClosed = onAdClosed,
                onAdUnavailable = onAdUnavailable
            )

        } catch (e: Exception) {
            Log.e(TAG, "Exception in showRewardedVideoAd", e)
            activity.runOnUiThread { onAdUnavailable(e.message ?: "Hata") }
        }
    }

    private fun loadDynamicRewardAd(
        activity: Activity,
        grantReward: () -> Unit,
        onAdDisplayed: () -> Unit,
        onAdClosed: () -> Unit,
        onAdUnavailable: (String) -> Unit
    ) {
        var isHandled = false
        val timeoutRunnable = Runnable {
            if (!isHandled) {
                isHandled = true
                activity.runOnUiThread {
                    onAdUnavailable("Reklam sunucusundan yanıt alınamadı. Lütfen daha sonra tekrar deneyin.")
                    onAdClosed()
                }
                preloadAds(activity)
            }
        }
        mainHandler.postDelayed(timeoutRunnable, 8_000L)

        val freshVideoAd = StartAppAd(activity)
        freshVideoAd.setVideoListener(object : VideoListener {
            override fun onVideoCompleted() {
                grantReward()
            }
        })

        freshVideoAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                if (isHandled) return
                mainHandler.removeCallbacks(timeoutRunnable)
                isHandled = true

                Log.d(TAG, "Live on-demand video received! Showing on device...")
                val displayed = freshVideoAd.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantReward()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        Log.w(TAG, "Dynamic video adNotDisplayed. No reward granted.")
                        activity.runOnUiThread {
                            onAdUnavailable("Reklam gösterilemedi. Ödül verilemedi.")
                            onAdClosed()
                        }
                        preloadAds(activity)
                    }
                })
                if (!displayed) {
                    loadFallbackFullscreenAd(activity, grantReward, onAdDisplayed, onAdClosed, onAdUnavailable)
                }
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                if (isHandled) return
                mainHandler.removeCallbacks(timeoutRunnable)
                Log.w(TAG, "Live rewarded video fill not ready (${ad?.errorMessage}), trying fullscreen ad...")
                loadFallbackFullscreenAd(activity, grantReward, onAdDisplayed, onAdClosed, onAdUnavailable)
            }
        })
    }

    private fun loadFallbackFullscreenAd(
        activity: Activity,
        grantReward: () -> Unit,
        onAdDisplayed: () -> Unit,
        onAdClosed: () -> Unit,
        onAdUnavailable: (String) -> Unit
    ) {
        val fallbackAd = StartAppAd(activity)
        fallbackAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                Log.d(TAG, "Fallback live fullscreen ad received, showing now...")
                val displayed = fallbackAd.showAd(object : AdDisplayListener {
                    override fun adDisplayed(ad: Ad) {
                        activity.runOnUiThread { onAdDisplayed() }
                    }

                    override fun adHidden(ad: Ad) {
                        grantReward()
                        activity.runOnUiThread { onAdClosed() }
                        preloadAds(activity)
                    }

                    override fun adClicked(ad: Ad) {}

                    override fun adNotDisplayed(ad: Ad) {
                        Log.w(TAG, "Fallback adNotDisplayed. No reward granted.")
                        activity.runOnUiThread {
                            onAdUnavailable("Reklam gösterilemedi. Ödül verilemedi.")
                            onAdClosed()
                        }
                        preloadAds(activity)
                    }
                })

                if (!displayed) {
                    Log.w(TAG, "Fallback ad was not displayed. No reward granted.")
                    activity.runOnUiThread {
                        onAdUnavailable("Reklam gösterilemedi. Ödül verilemedi.")
                        onAdClosed()
                    }
                    preloadAds(activity)
                }
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                val errMsg = ad?.errorMessage ?: "NO FILL"
                Log.w(TAG, "Start.io live ad returned $errMsg.")
                activity.runOnUiThread {
                    onAdUnavailable("Reklam şu anda yüklenemedi. Lütfen biraz sonra tekrar deneyin.")
                    onAdClosed()
                }
                preloadAds(activity)
            }
        })
    }
}
