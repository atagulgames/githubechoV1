package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader

/**
 * Yandex Mobile Ads SDK Manager for ECHO FLUX.
 *
 * Configured with official Yandex Partner Ad Unit IDs:
 * - Banner: RM-20228995-1 (Normalized: R-M-20228995-1)
 * - Rewarded: RM-20228995-2 (Normalized: R-M-20228995-2)
 * - Interstitial: RM-20228995-3 (Normalized: R-M-20228995-3)
 *
 * Implements:
 * 1. Automatic Yandex ad unit normalization ("RM-" -> "R-M-")
 * 2. Real ad first priority with automated fallback to demo units if live unit
 *    is in moderation or has no fill in testing environments
 * 3. Robust preloading so Interstitial and Rewarded ads display instantly without delay
 */
object YandexAdsManager {
    private const val TAG = "YandexAdsManager"

    // Raw Ad Unit IDs configured in user's Yandex Partner interface
    const val BANNER_AD_UNIT_ID = "RM-20228995-1"
    const val REWARDED_AD_UNIT_ID = "RM-20228995-2"
    const val INTERSTITIAL_AD_UNIT_ID = "RM-20228995-3"

    // Official Yandex Demo IDs for guaranteed fill during testing/moderation
    const val DEMO_BANNER_ID = "demo-banner-yandex"
    const val DEMO_REWARDED_ID = "demo-rewarded-yandex"
    const val DEMO_INTERSTITIAL_ID = "demo-interstitial-yandex"

    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private var preloadedInterstitialAd: InterstitialAd? = null
    private var preloadedRewardedAd: RewardedAd? = null

    private var interstitialLoader: InterstitialAdLoader? = null
    private var rewardedLoader: RewardedAdLoader? = null

    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    /**
     * Normalizes "RM-XXXX-Y" to standard Yandex Partner format "R-M-XXXX-Y".
     */
    fun normalizeAdUnitId(rawId: String): String {
        return if (rawId.startsWith("RM-", ignoreCase = true)) {
            "R-" + rawId.substring(1)
        } else {
            rawId
        }
    }

    /**
     * Initializes Yandex Mobile Ads SDK.
     */
    fun initialize(context: Context, onInitialized: (() -> Unit)? = null) {
        if (isInitialized) {
            preloadAds(context)
            onInitialized?.invoke()
            return
        }

        try {
            Log.d(TAG, "Initializing Yandex Mobile Ads SDK...")
            MobileAds.initialize(context) {
                isInitialized = true
                Log.d(TAG, "Yandex Mobile Ads SDK initialized successfully")
                preloadAds(context)
                onInitialized?.invoke()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Yandex Mobile Ads SDK", e)
            onInitialized?.invoke()
        }
    }

    /**
     * Preloads both Interstitial and Rewarded ads so they are ready instantly.
     */
    fun preloadAds(context: Context) {
        preloadInterstitial(context)
        preloadRewarded(context)
    }

    /**
     * Preloads an Interstitial ad.
     * Tries live ad unit first; if unapproved or NO_FILL, falls back to demo unit.
     */
    fun preloadInterstitial(context: Context, useFallbackIfFailed: Boolean = true) {
        if (preloadedInterstitialAd != null || isInterstitialLoading) {
            return
        }

        try {
            isInterstitialLoading = true
            val loader = interstitialLoader ?: InterstitialAdLoader(context.applicationContext).also {
                interstitialLoader = it
            }

            val targetId = normalizeAdUnitId(INTERSTITIAL_AD_UNIT_ID)

            loader.setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    isInterstitialLoading = false
                    preloadedInterstitialAd = interstitialAd
                    Log.d(TAG, "Yandex Interstitial Ad loaded successfully ($targetId)")
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    isInterstitialLoading = false
                    preloadedInterstitialAd = null
                    Log.w(TAG, "Live Interstitial failed to load: ${error.description} (code: ${error.code})")

                    if (useFallbackIfFailed) {
                        Log.d(TAG, "Attempting demo interstitial fallback for verification...")
                        loadFallbackInterstitial(context)
                    }
                }
            })

            val adRequestConfig = AdRequestConfiguration.Builder(targetId).build()
            loader.loadAd(adRequestConfig)
        } catch (e: Exception) {
            isInterstitialLoading = false
            Log.e(TAG, "Error while preloading interstitial ad", e)
        }
    }

    private fun loadFallbackInterstitial(context: Context, retryWithRaw: Boolean = true) {
        val rawId = INTERSTITIAL_AD_UNIT_ID
        val normalizedId = normalizeAdUnitId(rawId)
        val idToTry = if (retryWithRaw && rawId != normalizedId) rawId else DEMO_INTERSTITIAL_ID
        try {
            val loader = interstitialLoader ?: InterstitialAdLoader(context.applicationContext).also {
                interstitialLoader = it
            }
            loader.setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    preloadedInterstitialAd = interstitialAd
                    Log.d(TAG, "Yandex Interstitial Ad loaded successfully ($idToTry)")
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.w(TAG, "Interstitial load failed for $idToTry: ${error.description}")
                    if (idToTry != DEMO_INTERSTITIAL_ID) {
                        loadFallbackInterstitial(context, retryWithRaw = false)
                    }
                }
            })
            val config = AdRequestConfiguration.Builder(idToTry).build()
            loader.loadAd(config)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading fallback interstitial", e)
        }
    }

    /**
     * Preloads a Rewarded ad.
     * Tries live ad unit first; if unapproved or NO_FILL, falls back to demo unit.
     */
    fun preloadRewarded(context: Context, useFallbackIfFailed: Boolean = true) {
        if (preloadedRewardedAd != null || isRewardedLoading) {
            return
        }

        try {
            isRewardedLoading = true
            val loader = rewardedLoader ?: RewardedAdLoader(context.applicationContext).also {
                rewardedLoader = it
            }

            val targetId = normalizeAdUnitId(REWARDED_AD_UNIT_ID)

            loader.setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    isRewardedLoading = false
                    preloadedRewardedAd = rewardedAd
                    Log.d(TAG, "Yandex Rewarded Ad loaded successfully ($targetId)")
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    isRewardedLoading = false
                    preloadedRewardedAd = null
                    Log.w(TAG, "Live Rewarded ad failed to load: ${error.description} (code: ${error.code})")

                    if (useFallbackIfFailed) {
                        Log.d(TAG, "Attempting demo rewarded fallback for verification...")
                        loadFallbackRewarded(context)
                    }
                }
            })

            val adRequestConfig = AdRequestConfiguration.Builder(targetId).build()
            loader.loadAd(adRequestConfig)
        } catch (e: Exception) {
            isRewardedLoading = false
            Log.e(TAG, "Error while preloading rewarded ad", e)
        }
    }

    private fun loadFallbackRewarded(context: Context, retryWithRaw: Boolean = true) {
        val rawId = REWARDED_AD_UNIT_ID
        val normalizedId = normalizeAdUnitId(rawId)
        val idToTry = if (retryWithRaw && rawId != normalizedId) rawId else DEMO_REWARDED_ID
        try {
            val loader = rewardedLoader ?: RewardedAdLoader(context.applicationContext).also {
                rewardedLoader = it
            }
            loader.setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    preloadedRewardedAd = rewardedAd
                    Log.d(TAG, "Yandex Rewarded Fallback Ad loaded successfully ($idToTry)")
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.w(TAG, "Rewarded load failed for $idToTry: ${error.description}")
                    if (idToTry != DEMO_REWARDED_ID) {
                        loadFallbackRewarded(context, retryWithRaw = false)
                    }
                }
            })
            val config = AdRequestConfiguration.Builder(idToTry).build()
            loader.loadAd(config)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading fallback rewarded", e)
        }
    }

    /**
     * Shows an Interstitial Ad at screen transitions.
     * Guaranteed to invoke onAdClosed so the game loop is never blocked.
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDisplayed: () -> Unit = {},
        onAdClosed: () -> Unit = {}
    ) {
        var closedCallbackFired = false
        fun fireClosedOnce() {
            if (!closedCallbackFired) {
                closedCallbackFired = true
                mainHandler.post { onAdClosed() }
            }
        }

        val ad = preloadedInterstitialAd
        if (ad != null) {
            preloadedInterstitialAd = null
            ad.setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {
                    Log.d(TAG, "Yandex Interstitial Ad shown")
                    mainHandler.post { onAdDisplayed() }
                }

                override fun onAdFailedToShow(adError: AdError) {
                    Log.w(TAG, "Yandex Interstitial Ad failed to show: ${adError.description}")
                    preloadInterstitial(activity)
                    fireClosedOnce()
                }

                override fun onAdDismissed() {
                    Log.d(TAG, "Yandex Interstitial Ad dismissed")
                    preloadInterstitial(activity)
                    fireClosedOnce()
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Yandex Interstitial Ad clicked")
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    Log.d(TAG, "Yandex Interstitial Ad impression recorded")
                }
            })

            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing Yandex Interstitial Ad", e)
                preloadInterstitial(activity)
                fireClosedOnce()
            }
        } else {
            // Not preloaded yet: attempt fast load with fallback
            Log.d(TAG, "Yandex Interstitial not preloaded, attempting immediate load...")
            val loader = InterstitialAdLoader(activity.applicationContext)
            var handled = false

            val timeoutRunnable = Runnable {
                if (!handled) {
                    handled = true
                    Log.w(TAG, "Interstitial on-demand load timed out, continuing game flow")
                    preloadInterstitial(activity)
                    fireClosedOnce()
                }
            }
            mainHandler.postDelayed(timeoutRunnable, 5000L)

            fun bindAndShow(freshAd: InterstitialAd) {
                freshAd.setAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdShown() {
                        mainHandler.post { onAdDisplayed() }
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        preloadInterstitial(activity)
                        fireClosedOnce()
                    }

                    override fun onAdDismissed() {
                        preloadInterstitial(activity)
                        fireClosedOnce()
                    }

                    override fun onAdClicked() {}
                    override fun onAdImpression(impressionData: ImpressionData?) {}
                })

                try {
                    freshAd.show(activity)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception showing fresh Interstitial", e)
                    preloadInterstitial(activity)
                    fireClosedOnce()
                }
            }

            loader.setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(freshAd: InterstitialAd) {
                    if (handled) return
                    handled = true
                    mainHandler.removeCallbacks(timeoutRunnable)
                    bindAndShow(freshAd)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.w(TAG, "On-demand live interstitial failed: ${error.description}, trying demo fallback...")
                    // Fallback to demo
                    val fallbackLoader = InterstitialAdLoader(activity.applicationContext)
                    fallbackLoader.setAdLoadListener(object : InterstitialAdLoadListener {
                        override fun onAdLoaded(demoAd: InterstitialAd) {
                            if (handled) return
                            handled = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            bindAndShow(demoAd)
                        }

                        override fun onAdFailedToLoad(demoError: AdRequestError) {
                            if (handled) return
                            handled = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            preloadInterstitial(activity)
                            fireClosedOnce()
                        }
                    })
                    try {
                        val demoConfig = AdRequestConfiguration.Builder(DEMO_INTERSTITIAL_ID).build()
                        fallbackLoader.loadAd(demoConfig)
                    } catch (e: Exception) {
                        if (!handled) {
                            handled = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            fireClosedOnce()
                        }
                    }
                }
            })

            try {
                val primaryId = normalizeAdUnitId(INTERSTITIAL_AD_UNIT_ID)
                val config = AdRequestConfiguration.Builder(primaryId).build()
                loader.loadAd(config)
            } catch (e: Exception) {
                if (!handled) {
                    handled = true
                    mainHandler.removeCallbacks(timeoutRunnable)
                    fireClosedOnce()
                }
            }
        }
    }

    /**
     * Shows a Rewarded Video Ad when the user requests it.
     * When completed, calls onRewardEarned.
     * If an ad cannot be loaded or shown, calls onAdUnavailable so user is notified.
     */
    fun showRewardedVideoAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDisplayed: () -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdUnavailable: (String) -> Unit = {}
    ) {
        var closedCallbackFired = false
        fun fireClosedOnce() {
            if (!closedCallbackFired) {
                closedCallbackFired = true
                mainHandler.post { onAdClosed() }
            }
        }

        fun attachAndShow(ad: RewardedAd) {
            ad.setAdEventListener(object : RewardedAdEventListener {
                override fun onRewarded(reward: Reward) {
                    Log.d(TAG, "Yandex Rewarded Ad reward earned! (${reward.amount} ${reward.type})")
                    mainHandler.post { onRewardEarned() }
                }

                override fun onAdShown() {
                    Log.d(TAG, "Yandex Rewarded Ad shown")
                    mainHandler.post { onAdDisplayed() }
                }

                override fun onAdFailedToShow(adError: AdError) {
                    Log.w(TAG, "Yandex Rewarded Ad failed to show: ${adError.description}")
                    preloadRewarded(activity)
                    mainHandler.post { onAdUnavailable("Reklam gösterilemedi: ${adError.description}") }
                    fireClosedOnce()
                }

                override fun onAdDismissed() {
                    Log.d(TAG, "Yandex Rewarded Ad dismissed")
                    preloadRewarded(activity)
                    fireClosedOnce()
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Yandex Rewarded Ad clicked")
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    Log.d(TAG, "Yandex Rewarded Ad impression recorded")
                }
            })

            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing Rewarded Ad", e)
                preloadRewarded(activity)
                mainHandler.post { onAdUnavailable("Reklam açılırken bir hata oluştu.") }
                fireClosedOnce()
            }
        }

        val ad = preloadedRewardedAd
        if (ad != null) {
            preloadedRewardedAd = null
            attachAndShow(ad)
        } else {
            // Not ready yet: attempt fast on-demand load with fallback
            Log.d(TAG, "Yandex Rewarded Ad not preloaded, attempting on-demand load...")
            val loader = RewardedAdLoader(activity.applicationContext)
            var handled = false

            val timeoutRunnable = Runnable {
                if (!handled) {
                    handled = true
                    Log.w(TAG, "Rewarded ad load timed out")
                    preloadRewarded(activity)
                    mainHandler.post { onAdUnavailable("Reklam yükleme zaman aşımına uğradı. Lütfen internet bağlantınızı kontrol edin.") }
                    fireClosedOnce()
                }
            }
            mainHandler.postDelayed(timeoutRunnable, 6000L)

            loader.setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(freshAd: RewardedAd) {
                    if (handled) return
                    handled = true
                    mainHandler.removeCallbacks(timeoutRunnable)
                    attachAndShow(freshAd)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.w(TAG, "Live rewarded ad load failed: ${error.description}, attempting demo fallback...")
                    // Attempt demo fallback immediately so user can see and earn reward
                    val fallbackLoader = RewardedAdLoader(activity.applicationContext)
                    fallbackLoader.setAdLoadListener(object : RewardedAdLoadListener {
                        override fun onAdLoaded(demoAd: RewardedAd) {
                            if (handled) return
                            handled = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            attachAndShow(demoAd)
                        }

                        override fun onAdFailedToLoad(demoError: AdRequestError) {
                            if (handled) return
                            handled = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            preloadRewarded(activity)
                            mainHandler.post { onAdUnavailable("Reklam yüklenemedi: ${demoError.description}") }
                            fireClosedOnce()
                        }
                    })

                    try {
                        val config = AdRequestConfiguration.Builder(DEMO_REWARDED_ID).build()
                        fallbackLoader.loadAd(config)
                    } catch (e: Exception) {
                        if (!handled) {
                            handled = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            fireClosedOnce()
                        }
                    }
                }
            })

            try {
                val primaryId = normalizeAdUnitId(REWARDED_AD_UNIT_ID)
                val config = AdRequestConfiguration.Builder(primaryId).build()
                loader.loadAd(config)
            } catch (e: Exception) {
                if (!handled) {
                    handled = true
                    mainHandler.removeCallbacks(timeoutRunnable)
                    mainHandler.post { onAdUnavailable("Reklam yüklenirken bir sorun oluştu.") }
                    fireClosedOnce()
                }
            }
        }
    }
}
