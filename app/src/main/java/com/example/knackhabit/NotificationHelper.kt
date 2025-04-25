package com.example.knackhabit

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import com.example.knackhabit.activity.HomeActivity

object NotificationHelper {
    private const val CHANNEL_ID = "login_channel" // Должен соответствовать ID из App.kt
    private const val NOTIFICATION_ID = 1 // Уникальный ID уведомления

    fun showLoginNotification(context: Context, userName: String) {
        Log.d(
            "NotificationHelper",
            "showLoginNotification() called with userName: $userName"
        )
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // ИЗМЕНЕНИЕ: Используйте CLEAR_TOP и SINGLE_TOP
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE) //  Убедитесь, что флаг корректный

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
                    "NotificationHelper", "Attempting to show notification with ID: $NOTIFICATION_ID")
                notify(NOTIFICATION_ID, builder.build())
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
}