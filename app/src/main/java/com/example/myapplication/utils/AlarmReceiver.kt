package com.example.myapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.model.AppEvent
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EVENT_TITLE") ?: "Напоминание"
        val isMonthly = intent.getBooleanExtra("IS_MONTHLY", false)
        val eventId = intent.getStringExtra("EVENT_ID") ?: ""
        val dateMillis = intent.getLongExtra("DATE_MILLIS", System.currentTimeMillis())

        // Передаем eventId в функцию
        showNotification(context, title, eventId)

        // Если событие ежемесячное, планируем его на следующий месяц
        if (isMonthly) {
            val nextCalendar = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                add(Calendar.MONTH, 1) // Прибавляем ровно 1 месяц
            }

            val nextEvent = AppEvent(
                id = eventId,
                title = title,
                dateMillis = nextCalendar.timeInMillis,
                isMonthlyRecurring = true,
                createdBy = ""
            )

            ReminderScheduler.scheduleReminder(context, nextEvent)
            android.util.Log.d("ALARM_TEST", "Ежемесячное событие перенесено на: ${nextCalendar.time}")
        }
    }

    // ДОБАВИЛИ ПАРАМЕТР eventId
    private fun showNotification(context: Context, title: String, eventId: String) {
        val channelId = "event_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Напоминания о событиях",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Напоминание!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Теперь eventId доступен
        notificationManager.notify(eventId.hashCode(), notification)
    }
}