package com.example.knackhabit

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.knackhabit.activity.AddEditActivity

class HabitAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitTitle = intent.getStringExtra("habitTitle") ?: "Привычка"
        // получаем requestCode, чтобы каждый раз одно и то же уведомление перезаписывалось
        val requestCode = intent.getIntExtra("requestCode", 0)

        // Открывать HomeActivity по клику на уведомление
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, AddEditActivity::class.java)
                .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.HABIT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle("Напоминание")
            .setContentText("Пора выполнить привычку: $habitTitle")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        NotificationManagerCompat.from(context)
            .notify(requestCode, builder.build())
    }
}