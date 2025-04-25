package com.example.knackhabit.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.knackhabit.activity.HabitListActivity
import com.example.knackhabit.NotificationHelper
import com.example.knackhabit.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    //private lateinit var userNameTextView: TextView
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted.
                showNotification()
            } else {
                showNotificationPermissionDeniedMessage()
            }
        }
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //userNameTextView = findViewById(R.id.user_name_text_view)
        //val userProfileTextView: TextView = findViewById(R.id.link_to_settings)

        //userProfileTextView.setOnClickListener {
        //    val intent = Intent(this, HabitListActivity::class.java)
        //    startActivity(intent)
        //}

        // Устанавливаем отступ сверху в соответствии с статус-баром
        ViewCompat.setOnApplyWindowInsetsListener(binding.frameLayout) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = statusBarHeight
            }
            insets
        }

        binding.btnListHabits.setOnClickListener {
            val intent = Intent(this, HabitListActivity::class.java)
            startActivity(intent)
        }

        binding.btnEditHabits.setOnClickListener {
            val intent = Intent(this, AddEditActivity::class.java)
            startActivity(intent)
        }

        binding.btnWeeklyReports.setOnClickListener {
            val intent = Intent(this, ActivityWeeklyReport::class.java)
            startActivity(intent)
        }

        // 1. Получаем имя пользователя из SharedPreferences
        val userName = getUserName()

        // 2. Отображаем имя пользователя в TextView
        binding.userNameTextView.text = if (userName.isNotEmpty()) {
            "Добро пожаловать, $userName!"
        } else {
            "Без имени"
        }
        if (userName.isNotEmpty()) {
            requestNotificationPermission()
        }
    }


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showNotification()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationRationale()
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            showNotification()
        }
    }

    private fun showNotificationRationale() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Required")
            .setMessage("This app needs notification permission to show you important information.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showNotificationPermissionDeniedMessage() // Display the denied message
            }
            .show()
    }

    private fun showNotificationPermissionDeniedMessage() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("You have denied notification permission. Notifications will not be shown.")
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
        } else {
            Log.d("HomeActivity", "User name is empty, not showing notification")
        }
    }

    private fun getUserName(): String {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("userName", "") ?: ""
    }
}