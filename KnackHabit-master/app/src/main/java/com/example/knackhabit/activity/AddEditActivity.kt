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
import com.example.knackhabit.db.HabitEntity
import com.example.knackhabit.db.MainViewModel
import com.example.knackhabit.db.MainViewModelFactory

class AddEditActivity : AppCompatActivity(), EditHabitItemAdapter.Listener {

    private lateinit var binding: ActivityAddEditHabitBinding
    private lateinit var adapter: EditHabitItemAdapter
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((applicationContext as App).database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // отступ под статус-бар
        val insetViews = listOf(binding.cardView3, binding.imBack)
        insetViews.forEach { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                v.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = top
                }
                insets
            }
        }

        initRcView()
        observeHabits()

        binding.imBack.setOnClickListener { finish() }
        binding.imNewHabit.setOnClickListener {
            startActivity(Intent(this, CreateHabitActivity::class.java))
        }
    }

    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(this@AddEditActivity)
        adapter = EditHabitItemAdapter(this@AddEditActivity)
        rcView.adapter = adapter
    }

    private fun observeHabits() {
        mainViewModel.allHabits.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    // редактировать привычку — передаём в CreateHabitActivity
    override fun editHabit(habit: HabitEntity) {
        Intent(this, CreateHabitActivity::class.java).also {
            it.putExtra("update", habit)
            startActivity(it)
        }
    }
}
