package com.example.knackhabit.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knackhabit.App
import com.example.knackhabit.adapters.PredefinedHabitAdapter
import com.example.knackhabit.databinding.ActivityHabitListBinding
import com.example.knackhabit.db.HabitEntity
import com.example.knackhabit.db.MainViewModel
import com.example.knackhabit.db.MainViewModelFactory

class HabitListActivity : AppCompatActivity(), PredefinedHabitAdapter.Listener {
    private lateinit var binding: ActivityHabitListBinding
    private lateinit var adapter: PredefinedHabitAdapter
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((applicationContext as App).database)
    }

    private val newSelected = mutableSetOf<String>()
    private var existingHabitsMap = mapOf<String, HabitEntity>()
    private lateinit var items: List<HabitEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout4) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updateLayoutParams<androidx.constraintlayout.widget.ConstraintLayout.LayoutParams> {
                topMargin = top
            }
            insets
        }
        binding.imBack.setOnClickListener { finish() }

        val titles = listOf(
            "Пить воду", "Утренняя зарядка", "Чтение",
            "Медитация", "Йога", "Посетить спортзал", "Заправить кровать", "Здоровый завтрак",
            "Употребление клетчатки", "Почистить зубы", "Упражнения для глаз",
            "Принять витамины", "Разминка кистей рук", "Контрастный душ",
            "Прослушивание расслабляющей музыки"

        )
        val defaultDays = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")
        val template = titles.map { title ->
            HabitEntity(
                id = null,
                title = title,
                daysOfWeek = defaultDays,
                reminderTime = null
            )
        }

        adapter = PredefinedHabitAdapter(this)
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter

        viewModel.allHabits.observe(this) { existing ->
            existingHabitsMap = existing.associateBy { it.title }
            items = template.map { t -> existingHabitsMap[t.title] ?: t }

            newSelected.clear()
            adapter.updateSelections(existingHabitsMap.keys, newSelected)
            adapter.submitList(items)
        }

        binding.btnNext.isEnabled = true
        binding.btnNext.setOnClickListener {
            items.filter { newSelected.contains(it.title) }
                .forEach { viewModel.upsertHabit(it) }
            startActivity(Intent(this, AddEditActivity::class.java))
        }
    }

    override fun onHabitClicked(habit: HabitEntity) {
        val title = habit.title
        if (existingHabitsMap.containsKey(title)) return
        if (!newSelected.add(title)) {
            newSelected.remove(title)
        }
        adapter.updateSelections(existingHabitsMap.keys, newSelected)
    }
}