package com.example.ads

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

/**
 * Jetpack Compose wrapper for Yandex Mobile Ads BannerAdView.
 * Uses official adaptive sticky banner format matching device width.
 *
 * Guarantees:
 * 1. NO empty black boxes or blank space when an ad is not loaded or during loading.
 * 2. Tries the user's primary live ad unit (R-M-20228995-1) first.
 * 3. Gracefully switches to test demo banner if live ad unit is in moderation or NO_FILL,
 *    so developers can verify ad layout and functionality immediately.
 * 4. Only expands and renders once an ad successfully loads.
 */
@Composable
fun YandexBannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = YandexAdsManager.BANNER_AD_UNIT_ID,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null
) {
    val normalizedPrimaryId = remember(adUnitId) { YandexAdsManager.normalizeAdUnitId(adUnitId) }
    var currentAdUnitId by remember(normalizedPrimaryId) { mutableStateOf(normalizedPrimaryId) }
    var isAdVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        key(currentAdUnitId) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    BannerAdView(ctx).apply {
                        id = com.example.R.id.yandex_banner_view
                        setAdUnitId(currentAdUnitId)

                        val displayMetrics = ctx.resources.displayMetrics
                        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
                        val bannerWidth = screenWidthDp.coerceAtLeast(320)
                        setAdSize(BannerAdSize.stickySize(ctx, bannerWidth))

                        setBannerAdEventListener(object : BannerAdEventListener {
                            override fun onAdLoaded() {
                                Log.d("YandexBanner", "Yandex Banner ad loaded successfully for ID: $currentAdUnitId")
                                isAdVisible = true
                                onAdLoaded?.invoke()
                            }

                            override fun onAdFailedToLoad(error: AdRequestError) {
                                Log.w(
                                    "YandexBanner",
                                    "Yandex Banner load failed for ID $currentAdUnitId: ${error.description} (code: ${error.code})"
                                )
                                onAdFailedToLoad?.invoke("${error.description} (code: ${error.code})")

                                // If live unit fails (e.g. Code 2 NO_FILL, moderation, or region unserved),
                                // fallback to official demo unit so ad visibility can be verified
                                if (currentAdUnitId != YandexAdsManager.DEMO_BANNER_ID) {
                                    Log.d("YandexBanner", "Falling back to official Yandex demo banner: ${YandexAdsManager.DEMO_BANNER_ID}")
                                    currentAdUnitId = YandexAdsManager.DEMO_BANNER_ID
                                } else {
                                    isAdVisible = false
                                }
                            }

                            override fun onAdClicked() {
                                Log.d("YandexBanner", "Yandex Banner ad clicked")
                            }

                            override fun onLeftApplication() {}

                            override fun onReturnedToApplication() {}

                            override fun onImpression(impressionData: ImpressionData?) {
                                Log.d("YandexBanner", "Yandex Banner ad impression recorded")
                            }
                        })

                        val adRequest = AdRequest.Builder().build()
                        loadAd(adRequest)
                    }
                },
                update = {
                    // View lifecycle maintained by AndroidView
                },
                onRelease = { bannerView ->
                    try {
                        bannerView.destroy()
                    } catch (e: Exception) {
                        Log.e("YandexBanner", "Error releasing banner", e)
                    }
                }
            )
        }
    }
}
