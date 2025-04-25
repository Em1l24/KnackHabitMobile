package com.example.knackhabit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.knackhabit.db.MainDataBase

class App : Application() {

    val database by lazy {
        MainDataBase.getDataBase(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "login_channel",
                "Login Notifications",
                NotificationManager.IMPORTANCE_HIGH // Обязательно устанавливайте IMPORTANCE_HIGH, если нужен звук и всплывающие уведомления *всегда*
            ).apply {
                description = "Уведомления о входе в приложение"
                enableLights(true) // Включить подсветку (если поддерживается устройством)
                enableVibration(true) // Включить вибрацию
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}