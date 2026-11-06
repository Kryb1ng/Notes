package com.example.myapplication.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.myapplication.model.AppEvent
import java.util.Calendar

object ReminderScheduler {

    fun scheduleReminder(context: Context, event: AppEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EVENT_TITLE", event.title)
            putExtra("EVENT_ID", event.id)
            putExtra("IS_MONTHLY", event.isMonthlyRecurring)
            putExtra("DATE_MILLIS", event.dateMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Настраиваем время срабатывания
        val calendar = Calendar.getInstance().apply {
            timeInMillis = event.dateMillis
            // Для теста можно поставить время на 1 минуту вперед, чтобы сразу проверить:
            //add(Calendar.MINUTE, 1)
        }

        // Планируем точный будильник
        // Планируем точный будильник
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    android.util.Log.d(
                        "ALARM_TEST",
                        "Ставим точный будильник на ${calendar.timeInMillis}"
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                    )
                } else {
                    android.util.Log.e(
                        "ALARM_TEST",
                        "НЕТ РАЗРЕШЕНИЯ НА ТОЧНЫЕ БУДИЛЬНИКИ! Ставим неточный."
                    )
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                android.util.Log.d("ALARM_TEST", "Ставим точный будильник (Android < 12)")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                )
            }
        } catch (e: SecurityException) {
            android.util.Log.e("ALARM_TEST", "ОШИБКА БЕЗОПАСНОСТИ: ${e.message}")
            e.printStackTrace()
        }
    }
}