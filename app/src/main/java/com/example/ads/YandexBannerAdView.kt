package com.example.ads

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
 * Guarantees proper layout height and includes automatic demo fallback
 * if live ad unit is currently unapproved or returning NO_FILL.
 */
@Composable
fun YandexBannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = YandexAdsManager.BANNER_AD_UNIT_ID,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var isAdVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp, max = 90.dp)
            .background(Color(0xFF030D2B)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                BannerAdView(ctx).apply {
                    id = com.example.R.id.yandex_banner_view

                    val primaryId = YandexAdsManager.normalizeAdUnitId(adUnitId)
                    setAdUnitId(primaryId)

                    val displayMetrics = ctx.resources.displayMetrics
                    val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
                    setAdSize(BannerAdSize.stickySize(ctx, screenWidthDp.coerceAtLeast(320)))

                    var retriedWithRawId = false
                    var retriedWithFallback = false

                    setBannerAdEventListener(object : BannerAdEventListener {
                        override fun onAdLoaded() {
                            Log.d("YandexBanner", "Yandex Banner ad loaded successfully ($primaryId)")
                            isAdVisible = true
                            onAdLoaded?.invoke()
                        }

                        override fun onAdFailedToLoad(error: AdRequestError) {
                            Log.w("YandexBanner", "Banner load failed: ${error.description} (code: ${error.code})")
                            if (!retriedWithRawId && primaryId != adUnitId) {
                                retriedWithRawId = true
                                Log.d("YandexBanner", "Retrying banner with raw ID: $adUnitId")
                                post {
                                    try {
                                        setAdUnitId(adUnitId)
                                        loadAd(AdRequest.Builder().build())
                                    } catch (e: Exception) {
                                        Log.e("YandexBanner", "Error loading raw banner", e)
                                    }
                                }
                            } else if (!retriedWithFallback) {
                                retriedWithFallback = true
                                Log.d("YandexBanner", "Retrying with Yandex demo banner fallback...")
                                post {
                                    try {
                                        setAdUnitId(YandexAdsManager.DEMO_BANNER_ID)
                                        loadAd(AdRequest.Builder().build())
                                    } catch (e: Exception) {
                                        Log.e("YandexBanner", "Error loading fallback banner", e)
                                        onAdFailedToLoad?.invoke(error.description)
                                    }
                                }
                            } else {
                                onAdFailedToLoad?.invoke(error.description)
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
                // No recomposition side-effects needed
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
