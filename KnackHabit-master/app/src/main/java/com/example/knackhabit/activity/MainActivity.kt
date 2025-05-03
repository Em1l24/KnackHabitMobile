package com.example.knackhabit.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.knackhabit.R
import com.example.knackhabit.activity.SecondActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Получаем SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // 2. Проверяем, есть ли сохраненное имя пользователя
        val userName = sharedPreferences.getString("userName", "") ?: ""

        // 3. Если имя пользователя есть, сразу переходим в HomeActivity
        if (userName.isNotEmpty()) {
            // Имя пользователя есть, переходим в HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Закрываем MainActivity
        } else {
            // 4. Имени пользователя нет, показываем кнопку для перехода на SecondActivity
            val buttonGoToSecondActivity: Button = findViewById(R.id.button)
            buttonGoToSecondActivity.setOnClickListener {
                val intent = Intent(this, SecondActivity::class.java)
                startActivity(intent)
                finish() // Закрываем MainActivity
            }
        }
    }
}