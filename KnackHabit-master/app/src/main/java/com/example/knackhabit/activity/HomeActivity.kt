package com.example.knackhabit.activity

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.knackhabit.NotificationHelper
import com.example.knackhabit.activity.HabitListActivity
import com.example.knackhabit.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val requestNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                showNotification()
                // После разрешения на POST_NOTIFICATIONS сразу проверим право на точные алармы
                requestExactAlarmPermission()
            } else {
                showNotificationPermissionDeniedMessage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Статус-бар
        ViewCompat.setOnApplyWindowInsetsListener(binding.frameLayout) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = statusBarHeight
            }
            insets
        }

        binding.btnListHabits.setOnClickListener {
            startActivity(Intent(this, HabitListActivity::class.java))
        }
        binding.btnEditHabits.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
        binding.btnWeeklyReports.setOnClickListener {
            startActivity(Intent(this, ActivityWeeklyReport::class.java))
        }

        // Отображаем имя
        val userName = getUserName()
        binding.userNameTextView.text = if (userName.isNotEmpty()) {
            "Добро пожаловать, $userName!"
        } else {
            "Без имени"
        }

        // Если имя есть — просим сначала уведомления, а затем точные алармы
        if (userName.isNotEmpty()) {
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showNotification()
                    requestExactAlarmPermission()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationRationale()
                }
                else -> {
                    requestNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            showNotification()
            requestExactAlarmPermission()
        }
    }

    private fun requestExactAlarmPermission() {
        // Android 12+ требует специального права для точных алармов
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (am != null && !am.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Точные будильники")
                    .setMessage("Чтобы напоминания приходили точно в назначенное время, разрешите приложению управлять точными будильниками.")
                    .setPositiveButton("Перейти в настройки") { _, _ ->
                        startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .apply {
                                    data = Uri.parse("package:$packageName")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                        )
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        }
    }

    private fun showNotificationRationale() {
        AlertDialog.Builder(this)
            .setTitle("Нужны уведомления")
            .setMessage("Приложению нужно разрешение на уведомления, чтобы напоминать о привычках.")
            .setPositiveButton("OK") { _, _ ->
                requestNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                showNotificationPermissionDeniedMessage()
            }
            .show()
    }

    private fun showNotificationPermissionDeniedMessage() {
        AlertDialog.Builder(this)
            .setTitle("Разрешение отклонено")
            .setMessage("Без разрешения на уведомления напоминания не будут работать.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showNotification() {
        Log.d("HomeActivity", "showNotification() called")
        val userName = getUserName()
        if (userName.isNotEmpty()) {
            Log.d("HomeActivity", "User name is: $userName")
            NotificationHelper.showLoginNotification(this, userName)
            Log.d("HomeActivity", "showLoginNotification() returned")
        }
    }

    private fun getUserName(): String {
        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        return prefs.getString("userName", "") ?: ""
    }
}