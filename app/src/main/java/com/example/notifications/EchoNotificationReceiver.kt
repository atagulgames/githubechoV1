package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class EchoNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "echo_daily_notifications"
        const val EXTRA_TIME_SLOT = "extra_time_slot"

        const val SLOT_MORNING = "MORNING"
        const val SLOT_NOON = "NOON"
        const val SLOT_AFTERNOON = "AFTERNOON"
        const val SLOT_EVENING = "EVENING"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule alarms after device reboot
            EchoNotificationScheduler.scheduleAllDailyNotifications(context)
            return
        }

        val slot = intent?.getStringExtra(EXTRA_TIME_SLOT) ?: SLOT_MORNING
        val (title, message, notificationId) = when (slot) {
            SLOT_MORNING -> Triple(
                "☀️ Günaydın Yankı Ustası!",
                "Sabah zihnini açmak için 1 bölüm çöz, günlük bonusunu ve kupalarını kap!",
                1001
            )
            SLOT_NOON -> Triple(
                "✨ Öğle Molası & Yeni Rekorlar!",
                "Kısa bir zeka molasına ne dersin? Günlük görevlerin seni bekliyor, kombonu yükselt!",
                1002
            )
            SLOT_AFTERNOON -> Triple(
                "🎯 İkindi Rezonansı!",
                "Liderlik tablosunda yükselme zamanı! Rakiplerini geç ve zirvedeki yerini al.",
                1003
            )
            SLOT_EVENING -> Triple(
                "🌙 Akşam Huzuru & ECHO Melodileri",
                "Günün stresini minimalist ses dalgaları ve harmonik bulmacalarla geride bırak.",
                1004
            )
            else -> Triple(
                "🌟 ECHO Seni Bekliyor!",
                "Yeni seviyeleri keşfet ve eşsiz melodileri tamamla!",
                1000
            )
        }

        showNotification(context, title, message, notificationId)

        // Reschedule for next day to guarantee it triggers reliably
        EchoNotificationScheduler.scheduleSlot(context, slot)
    }

    private fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel if Android Oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ECHO Günlük Bildirimler",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sabah, öğle, ikindi ve akşam için oyun teşvik bildirimleri"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_echo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
