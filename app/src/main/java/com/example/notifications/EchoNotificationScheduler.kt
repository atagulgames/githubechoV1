package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

data class EchoNotificationSlot(
    val slotId: String,
    val hour: Int,
    val minute: Int,
    val requestCode: Int,
    val title: String,
    val message: String
)

object EchoNotificationScheduler {
    private const val TAG = "EchoNotificationSched"

    // 10 daily notification times as requested:
    // 09:00, 12:00, 13:00, 16:00, 17:00, 19:00, 19:30, 20:00, 21:00, 22:00
    val ALL_SLOTS = listOf(
        EchoNotificationSlot(
            slotId = "SLOT_09_00",
            hour = 9,
            minute = 0,
            requestCode = 2001,
            title = "☀️ Good Morning Echo Master!",
            message = "Start your day sharpening your mind! Your 3-hour reward is ready, grab it now!"
        ),
        EchoNotificationSlot(
            slotId = "SLOT_12_00",
            hour = 12,
            minute = 0,
            requestCode = 2002,
            title = "⚡ Your 3-Hour Reward is Ready!",
            message = "Cooldown expired! Claim your new tokens and drill to set a new record."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_13_00",
            hour = 13,
            minute = 0,
            requestCode = 2003,
            title = "🎯 Lunch Break Brain Teaser!",
            message = "How about a quick focus break? Daily quests are waiting for you."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_16_00",
            hour = 16,
            minute = 0,
            requestCode = 2004,
            title = "✨ 3-Hour Reward Refreshed!",
            message = "A fresh reward awaits in the chest! Jump in and grab your drill laser."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_17_00",
            hour = 17,
            minute = 0,
            requestCode = 2005,
            title = "🔥 Afternoon Resonance & Leaderboard!",
            message = "Time to climb the ranks! Outperform rivals and reach the summit."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_19_00",
            hour = 19,
            minute = 0,
            requestCode = 2006,
            title = "🎁 Evening Reward Unlocked!",
            message = "Your 3-hour free reward is ready again! Claim it and continue your journey."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_19_30",
            hour = 19,
            minute = 30,
            requestCode = 2007,
            title = "💎 Special Evening Challenge!",
            message = "Master the echo maze! Collect stars and boost your combo in tonight's round."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_20_00",
            hour = 20,
            minute = 0,
            requestCode = 2008,
            title = "🎵 ECHO Melody Time!",
            message = "Unwind with minimalist harmonic soundscapes and relaxing puzzles."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_21_00",
            hour = 21,
            minute = 0,
            requestCode = 2009,
            title = "🏆 Summit Showdown!",
            message = "Ready to set today's high score? Double your trophies tonight!"
        ),
        EchoNotificationSlot(
            slotId = "SLOT_22_00",
            hour = 22,
            minute = 0,
            requestCode = 2010,
            title = "🌙 Night Serenity & Final 3-Hour Reward!",
            message = "Claim your final 3-hour reward of the day and relax with soothing waves."
        )
    )

    fun getSlotById(slotId: String): EchoNotificationSlot {
        return ALL_SLOTS.find { it.slotId == slotId } ?: ALL_SLOTS[0]
    }

    fun scheduleAllDailyNotifications(context: Context) {
        ALL_SLOTS.forEach { slot ->
            scheduleSlot(context, slot)
        }
        Log.d(TAG, "All 10 daily notifications scheduled successfully (09:00, 12:00, 13:00, 16:00, 17:00, 19:00, 19:30, 20:00, 21:00, 22:00)")
    }

    fun scheduleSlot(context: Context, slot: EchoNotificationSlot) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, slot.hour)
            set(Calendar.MINUTE, slot.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, EchoNotificationReceiver::class.java).apply {
            putExtra(EchoNotificationReceiver.EXTRA_TIME_SLOT, slot.slotId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slot.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    targetTime.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for ${slot.slotId} at ${slot.hour}:${slot.minute}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for ${slot.slotId}", e)
        }
    }

    fun scheduleSlotById(context: Context, slotId: String) {
        val slot = getSlotById(slotId)
        scheduleSlot(context, slot)
    }
}
