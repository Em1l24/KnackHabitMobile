package com.example.knackhabit.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knackhabit.R

class SecondActivity : AppCompatActivity() {

    private lateinit var editTextUserName: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        editTextUserName = findViewById(R.id.user_data)
        buttonSave = findViewById(R.id.button2)

        buttonSave.setOnClickListener {
            val userName = editTextUserName.text.toString()

            if (userName.isNotEmpty()) {
                // Сохраняем имя пользователя в SharedPreferences
                val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userName", userName)
                editor.apply()

                // Переходим в HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish() // Закрываем SecondActivity
            } else {
                // Показываем сообщение об ошибке, если имя пользователя не введено
                Toast.makeText(this, "Пожалуйста введите свое имя", Toast.LENGTH_SHORT).show()
            }
        }
    }
}