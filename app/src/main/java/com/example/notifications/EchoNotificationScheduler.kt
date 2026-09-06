package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object EchoNotificationScheduler {
    private const val TAG = "EchoNotificationSched"

    // 4 daily prayer/day intervals as requested:
    // Sabah: 09:00, Öğle: 13:00, İkindi: 17:00, Akşam: 21:00
    private val SCHEDULE_HOURS = mapOf(
        EchoNotificationReceiver.SLOT_MORNING to 9,
        EchoNotificationReceiver.SLOT_NOON to 13,
        EchoNotificationReceiver.SLOT_AFTERNOON to 17,
        EchoNotificationReceiver.SLOT_EVENING to 21
    )

    fun scheduleAllDailyNotifications(context: Context) {
        SCHEDULE_HOURS.keys.forEach { slot ->
            scheduleSlot(context, slot)
        }
        Log.d(TAG, "All 4 daily notifications scheduled (Sabah 09:00, Öğle 13:00, İkindi 17:00, Akşam 21:00)")
    }

    fun scheduleSlot(context: Context, slot: String) {
        val targetHour = SCHEDULE_HOURS[slot] ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val requestCode = when (slot) {
            EchoNotificationReceiver.SLOT_MORNING -> 1001
            EchoNotificationReceiver.SLOT_NOON -> 1002
            EchoNotificationReceiver.SLOT_AFTERNOON -> 1003
            EchoNotificationReceiver.SLOT_EVENING -> 1004
            else -> 1000
        }

        val intent = Intent(context, EchoNotificationReceiver::class.java).apply {
            putExtra(EchoNotificationReceiver.EXTRA_TIME_SLOT, slot)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for $slot", e)
        }
    }
}
