package com.example.knackhabit.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.knackhabit.databinding.ActivityWeeklyReportBinding

class ActivityWeeklyReport : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Устанавливаем отступ сверху в соответствии с статус-баром
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnBack) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = statusBarHeight
            }
            insets
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}