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

    // текущий день недели в виде аббревиатуры ("Пн", "Вт" и т.д.)
    private val todayAbbrev: String
        get() {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY    -> "Пн"
                Calendar.TUESDAY   -> "Вт"
                Calendar.WEDNESDAY -> "Ср"
                Calendar.THURSDAY  -> "Чт"
                Calendar.FRIDAY    -> "Пт"
                Calendar.SATURDAY  -> "Сб"
                Calendar.SUNDAY    -> "Вс"
                else               -> ""
            }
        }

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

        // установим дату для выборки в ViewModel
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        mainViewModel.setDate(fmt.format(Date()))

        // инициализируем адаптер, передаём "сегодня"
        adapter = EditHabitItemAdapter(this, todayAbbrev)
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter

        // подписываемся на все привычки с информацией о выполнении сегодня
        mainViewModel.habitsToday.observe(this) { listWithCompletion ->
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
        // при возврате на экран обновляем дату в ViewModel
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
        mainViewModel.setDate(todayStr)
    }
}
