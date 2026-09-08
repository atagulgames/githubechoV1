package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.StartIoManager
import com.example.ui.EchoGameScreen
import com.example.ui.components.PortraitRequiredOverlay
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EchoGameViewModel

class MainActivity : ComponentActivity() {
  private var activeGameViewModel: EchoGameViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    enableEdgeToEdge()

    // Initialize Start.io Ads SDK with App ID: 208838202 (Live Production Ads Mode)
    val echoPrefs = com.example.data.EchoPreferences(this)
    val testMode = echoPrefs.isTestAdsEnabled
    StartIoManager.initialize(this, testMode = testMode)
    com.example.audio.HarmonicAudioEngine.init(this)

    // Schedule 4 daily game motivation notifications (Sabah 09:00, Öğle 13:00, İkindi 17:00, Akşam 21:00)
    com.example.notifications.EchoNotificationScheduler.scheduleAllDailyNotifications(this)

    // Request notification permission on Android 13+ (API 33)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    setContent {
      MyApplicationTheme(darkTheme = false) {
        val gameViewModel: EchoGameViewModel = viewModel()
        activeGameViewModel = gameViewModel
        PortraitRequiredOverlay {
          EchoGameScreen(
            viewModel = gameViewModel,
            onShowRewardedAd = { type ->
              gameViewModel.setAdLoading(true, type)
              StartIoManager.showRewardedVideoAd(
                activity = this,
                onRewardEarned = {
                  gameViewModel.grantRewardedReward(type)
                },
                onAdDisplayed = {
                  gameViewModel.setAdLoading(false)
                },
                onAdClosed = {
                  gameViewModel.setAdLoading(false)
                },
                onAdUnavailable = { reason ->
                  gameViewModel.setAdLoading(false)
                  gameViewModel.onAdUnavailable(type, reason)
                }
              )
            },
            onShowInterstitialAd = { onAdFinished ->
              StartIoManager.showInterstitialAd(
                activity = this,
                onAdDisplayed = {
                  com.example.audio.HarmonicAudioEngine.pauseBgm()
                },
                onAdClosed = {
                  com.example.audio.HarmonicAudioEngine.resumeBgm()
                  onAdFinished()
                }
              )
            }
          )
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    com.example.audio.HarmonicAudioEngine.resumeBgm()
    StartIoManager.preloadAds(this)
  }

  override fun onPause() {
    super.onPause()
    com.example.audio.HarmonicAudioEngine.pauseBgm()
  }

  @Deprecated("Deprecated in Java")
  override fun onBackPressed() {
    StartIoManager.onBackPressed(this)
    super.onBackPressed()
  }
}
