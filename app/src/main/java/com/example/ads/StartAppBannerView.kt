package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

@Composable
fun StartAppBannerView(
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            var act: Activity? = context as? Activity
            if (act == null && context is ContextWrapper) {
                var cur: Context? = context
                while (cur is ContextWrapper && cur !is Activity) {
                    cur = cur.baseContext
                }
                act = cur as? Activity
            }
            val targetContext = act ?: context
            Banner(targetContext, object : BannerListener {
                override fun onReceiveAd(view: View) {
                    onAdLoaded?.invoke()
                }

                override fun onFailedToReceiveAd(view: View) {
                    onAdFailedToLoad?.invoke("Banner ad failed to receive")
                }

                override fun onImpression(view: View) {
                    onAdLoaded?.invoke()
                }

                override fun onClick(view: View) {}
            }).apply {
                id = com.example.R.id.startapp_banner_view
            }
        },
        onRelease = { banner ->
            try {
                banner.hideBanner()
            } catch (_: Exception) {
            }
        }
    )
}

