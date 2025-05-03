package com.example.knackhabit.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knackhabit.App
import com.example.knackhabit.adapters.EditHabitItemAdapter
import com.example.knackhabit.databinding.ActivityAddEditHabitBinding
import com.example.knackhabit.db.HabitWithCompletion
import com.example.knackhabit.db.MainViewModel
import com.example.knackhabit.db.MainViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddEditActivity : AppCompatActivity(), EditHabitItemAdapter.Listener {

    private lateinit var binding: ActivityAddEditHabitBinding
    private lateinit var adapter: EditHabitItemAdapter
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((applicationContext as App).database)
    }

    // сохраняем последний список, чтобы по id найти HabitWithCompletion
    private var habitsList: List<HabitWithCompletion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // статус-бар отступы
        listOf(binding.cardView3, binding.imBack).forEach { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                v.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = top
                }
                insets
            }
        }

        // установим дату для выборки
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        mainViewModel.setDate(fmt.format(Date()))

        adapter = EditHabitItemAdapter(this)
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter

        mainViewModel.scheduledHabitsToday.observe(this) { listWithCompletion ->
            habitsList = listWithCompletion
            adapter.submitList(listWithCompletion)
        }

        binding.imBack.setOnClickListener { finish() }
        binding.imNewHabit.setOnClickListener {
            startActivity(Intent(this, CreateHabitActivity::class.java))
        }
    }

    override fun editHabit(habitId: Int) {
        val item = habitsList.find { it.habit.id == habitId } ?: return
        Intent(this, CreateHabitActivity::class.java).also {
            it.putExtra("update", item.habit)
            startActivity(it)
        }
    }

    override fun onHabitToggled(habitId: Int, enabled: Boolean) {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormatter.format(Date())
        mainViewModel.setCompletion(
            habitId = habitId,
            date = dateStr,
            completed = enabled
        )
    }

    override fun onResume() {
        super.onResume()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
        mainViewModel.setDate(todayStr)
    }
}
