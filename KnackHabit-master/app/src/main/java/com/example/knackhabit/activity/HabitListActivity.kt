package com.example.knackhabit.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knackhabit.App
import com.example.knackhabit.adapters.BasicHabitItemAdapter
import com.example.knackhabit.databinding.ActivityHabitListBinding
import com.example.knackhabit.db.MainViewModel
import com.example.knackhabit.db.MainViewModelFactory
import com.example.knackhabit.db.HabitEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitListActivity : AppCompatActivity(), BasicHabitItemAdapter.Listener {
    private lateinit var binding: ActivityHabitListBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((applicationContext as App).database)
    }
    private lateinit var adapter: BasicHabitItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout4) { v, insets ->
            val h = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updateLayoutParams<ConstraintLayout.LayoutParams> { topMargin = h }
            insets
        }

        adapter = BasicHabitItemAdapter(this)
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter

        // Устанавливаем ключ-дату (без изменений)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        viewModel.setDate(fmt.format(Date()))

        // Наблюдаем не за полным списком 오늘, а уже отфильтрованным по дню недели
        viewModel.scheduledHabitsToday.observe(this) { todayList ->
            adapter.submitList(todayList)
        }

        binding.imBack.setOnClickListener { finish() }
    }

    override fun onCompletionChanged(habit: HabitEntity, completionId: Int?, completed: Boolean) {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        viewModel.setCompletion(habit.id!!, fmt.format(Date()), completed)
    }
}