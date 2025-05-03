package com.example.knackhabit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.knackhabit.db.MainDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class App : Application() {

    // ваша БД
    val database by lazy { MainDataBase.getDataBase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // создаём scope для фоновой работы
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // подписываемся на все привычки
        database.getDao().getAllHabits()
            .onEach { list ->
                list.forEach { habit ->
                    // отменяем старые алармы
                    NotificationHelper.cancelHabitReminders(this, habit)
                    // и если стоит время — снова планируем
                    habit.reminderTime?.takeIf { it.isNotBlank() }
                    ?.let {
                        NotificationHelper.scheduleHabitReminder(this, habit)
                    }

                }
            }
            .launchIn(scope)  // стартуем сборку Flow в нашем scope
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val loginChannel = NotificationChannel(
                NotificationHelper.CHANNEL_ID,
                "Login Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о входе в приложение"
            }
            notificationManager.createNotificationChannel(loginChannel)

            val habitChannel = NotificationChannel(
                NotificationHelper.HABIT_CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Напоминания о привычках"
            }
            notificationManager.createNotificationChannel(habitChannel)
        }
    }
}