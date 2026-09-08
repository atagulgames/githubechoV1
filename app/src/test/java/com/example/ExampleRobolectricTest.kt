package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EchoPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var prefs: EchoPreferences

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    prefs = EchoPreferences(context)
    prefs.resetAllProgress()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("ECHO FLUX", appName)
  }

  @Test
  fun `daily chest availability test`() {
    val today = "2026-03-30"
    assertTrue(prefs.isFreeChestAvailable(today))

    prefs.lastFreeChestDate = today
    assertFalse(prefs.isFreeChestAvailable(today))

    val tomorrow = "2026-03-31"
    assertTrue(prefs.isFreeChestAvailable(tomorrow))
  }

  @Test
  fun `login streak and claim test`() {
    val today = "2026-03-30"
    assertEquals(1, prefs.loginStreak)
    assertFalse(prefs.isLoginRewardClaimedToday(today))

    prefs.lastLoginClaimDate = today
    prefs.loginStreak = 2
    assertEquals(2, prefs.loginStreak)
    assertTrue(prefs.isLoginRewardClaimedToday(today))
  }

  @Test
  fun `daily quest metrics tracking test`() {
    val today = "2026-03-30"
    prefs.checkAndResetDailyQuests(today)
    assertEquals(0, prefs.levelsCompletedToday)
    assertEquals(0, prefs.starsEarnedToday)
    assertEquals(0, prefs.flawlessLevelsToday)

    prefs.levelsCompletedToday += 3
    prefs.starsEarnedToday += 9
    prefs.flawlessLevelsToday += 2

    assertEquals(3, prefs.levelsCompletedToday)
    assertEquals(9, prefs.starsEarnedToday)
    assertEquals(2, prefs.flawlessLevelsToday)

    assertFalse(prefs.isQuestClaimed(1))
    prefs.setQuestClaimed(1, true)
    assertTrue(prefs.isQuestClaimed(1))
  }

  @Test
  fun `authentication and session persistence test`() {
    assertFalse(prefs.isAuthenticated)
    prefs.setAuthenticatedUser("TestOyuncu", remember = true)
    assertTrue(prefs.isAuthenticated)
    assertEquals("TestOyuncu", prefs.authenticatedUsername)
    assertTrue(prefs.rememberMe)

    prefs.clearAuthentication()
    assertFalse(prefs.isAuthenticated)
    assertEquals("TestOyuncu", prefs.authenticatedUsername)
  }
}
