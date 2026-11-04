package cat.carlinapalius.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cat.carlinapalius.MainActivity
import cat.carlinapalius.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Cada dia a les 7 del matí avisa de quin residu toca treure.
 * Els dies sense recollida no es mostra cap notificació.
 */
object RecyclingReminder {
    private const val CHANNEL_ID = "recycling"
    private const val NOTIFICATION_ID = 7
    private val TIME: LocalTime = LocalTime.of(7, 0)

    /** Programa l'alarma per a les pròximes 7:00. Es crida en obrir l'app, en reiniciar i en actualitzar. */
    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(TIME)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, RecyclingReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            // Sense permís d'alarmes exactes: arribarà entre les 7:00 i les 7:15.
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 15 * 60 * 1000L, pending)
        }
    }

    fun notifyToday(context: Context) {
        val waste = Recycling.wasteOn(LocalDate.now()) ?: return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Reciclatge", NotificationManager.IMPORTANCE_DEFAULT),
        )
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Avui toca reciclar ${waste.label.lowercase()}")
            .setColor(waste.color.toInt())
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }
}

/** Salta a les 7:00 (notifica i programa el dia següent) i després de reiniciar o actualitzar l'app. */
class RecyclingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) RecyclingReminder.notifyToday(context)
        RecyclingReminder.schedule(context)
    }
}
