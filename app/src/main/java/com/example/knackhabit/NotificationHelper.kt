package com.example.knackhabit

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import com.example.knackhabit.activity.HomeActivity
import com.example.knackhabit.db.HabitEntity
import java.util.Calendar


object NotificationHelper {
    const val CHANNEL_ID = "login_channel" // Должен соответствовать ID из App.kt
    private const val NOTIFICATION_ID = 1 // Уникальный ID уведомления
    const val HABIT_CHANNEL_ID = "habit_channel"

    fun showLoginNotification(context: Context, userName: String) {
        Log.d(
            "NotificationHelper",
            "showLoginNotification() called with userName: $userName"
        )
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // ИЗМЕНЕНИЕ: Используйте CLEAR_TOP и SINGLE_TOP
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            ) //  Убедитесь, что флаг корректный

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle("С возвращением!")
            .setContentText("Привычки ждут вас, $userName! Приятно видеть вас снова.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        with(NotificationManagerCompat.from(context)) {
            try {
                Log.d(
                    "NotificationHelper",
                    "Attempting to show notification with ID: $NOTIFICATION_ID"
                )

                Log.d(
                    "NotificationHelper",
                    "Notification shown successfully"
                )
            } catch (e: Exception) {
                Log.e(
                    "NotificationHelper",
                    "Error showing notification: ${e.message}",
                    e
                )
            }
        }
    }

    private fun dayOfWeekFromAbbrev(abbrev: String): Int = when (abbrev) {
        "Пн" -> Calendar.MONDAY
        "Вт" -> Calendar.TUESDAY
        "Ср" -> Calendar.WEDNESDAY
        "Чт" -> Calendar.THURSDAY
        "Пт" -> Calendar.FRIDAY
        "Сб" -> Calendar.SATURDAY
        "Вс" -> Calendar.SUNDAY
        else -> throw IllegalArgumentException("Unknown day: $abbrev")
    }

    fun scheduleHabitReminder(context: Context, habit: HabitEntity) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val (hour, minute) = habit.reminderTime!!.split(":").map { it.toInt() }
        val now = Calendar.getInstance()
        val todayDow = now.get(Calendar.DAY_OF_WEEK)

        // Для каждого дня недели, выбранного в привычке
        habit.daysOfWeek.forEach { abbrev ->
            val targetDow = dayOfWeekFromAbbrev(abbrev)
            var daysUntil = (targetDow - todayDow + 7) % 7

            // Если сегодня и уже прошло время — отсрочить на неделю
            if (daysUntil == 0) {
                val nowTimeOnly = now.clone() as Calendar
                nowTimeOnly.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (nowTimeOnly.timeInMillis <= now.timeInMillis) {
                    daysUntil = 7
                }
            }

            // Календарь для этого дня
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, daysUntil)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Уникальный requestCode: комбинация id привычки и дня недели
            val requestCode = habit.id!! * 10 + targetDow
            val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
                putExtra("habitTitle", habit.title)
                putExtra("requestCode", requestCode)
            }
            val pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmInfo = AlarmManager.AlarmClockInfo(cal.timeInMillis, pi)
            alarmManager.setAlarmClock(alarmInfo, pi)

            Log.d(
                "NotificationHelper",
                "Scheduled '${habit.title}' на $abbrev ($targetDow) в $hour:$minute, rc=$requestCode"
            )
        }
    }

    fun cancelHabitReminders(context: Context, habit: HabitEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        habit.daysOfWeek.forEach { abbrev ->
            val day = dayOfWeekFromAbbrev(abbrev)
            val requestCode = habit.id!! * 10 + day
            val intent = Intent(context, HabitAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pi)
        }
    }
}
