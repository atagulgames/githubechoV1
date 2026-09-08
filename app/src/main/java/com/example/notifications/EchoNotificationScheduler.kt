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
            title = "☀️ Günaydın Yankı Ustası!",
            message = "Güne zihnini açarak başla! 3 saatlik ödülün hazır, hemen kap!"
        ),
        EchoNotificationSlot(
            slotId = "SLOT_12_00",
            hour = 12,
            minute = 0,
            requestCode = 2002,
            title = "⚡ 3 Saatlik Ödülün Hazır!",
            message = "Ödül süren doldu! Yeni jetonlarını ve matkabını alıp rekor kır."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_13_00",
            hour = 13,
            minute = 0,
            requestCode = 2003,
            title = "🎯 Öğle Molası Zeka Turu!",
            message = "Kısa bir odaklanma molasına ne dersin? Günlük görevler seni bekliyor."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_16_00",
            hour = 16,
            minute = 0,
            requestCode = 2004,
            title = "✨ 3 Saatlik Ödülün Yenilendi!",
            message = "Yeni ödülün sandıkta seni bekliyor! Hemen giriş yap ve matkap lazerini al."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_17_00",
            hour = 17,
            minute = 0,
            requestCode = 2005,
            title = "🔥 İkindi Rezonansı & Liderlik!",
            message = "Liderlik tablosunda yükselme zamanı! Rakiplerini geride bırak ve zirveye çık."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_19_00",
            hour = 19,
            minute = 0,
            requestCode = 2006,
            title = "🎁 Akşam Ödülü Açıldı!",
            message = "3 saatlik ücretsiz ödülün tekrar hazır! Hemen kapıp maceraya devam et."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_19_30",
            hour = 19,
            minute = 30,
            requestCode = 2007,
            title = "💎 Özel Akşam Mücadelesi!",
            message = "Yankı labirentinde ustalaş! Akşam turunda yıldızları toplayıp kombonu yükselt."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_20_00",
            hour = 20,
            minute = 0,
            requestCode = 2008,
            title = "🎵 ECHO Melodi Vakti!",
            message = "Günün yorgunluğunu minimalist ses dalgaları ve harmonik bulmacalarla at."
        ),
        EchoNotificationSlot(
            slotId = "SLOT_21_00",
            hour = 21,
            minute = 0,
            requestCode = 2009,
            title = "🏆 Zirve Kapışması!",
            message = "Günün en yüksek skorunu yapmaya hazır mısın? Kupalarını 2'ye katla!"
        ),
        EchoNotificationSlot(
            slotId = "SLOT_22_00",
            hour = 22,
            minute = 0,
            requestCode = 2010,
            title = "🌙 Gece Huzuru & Son 3 Saatlik Ödül!",
            message = "Günün son 3 saatlik ödülünü al, rahatlatıcı ses dalgalarıyla günü tamamla."
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
