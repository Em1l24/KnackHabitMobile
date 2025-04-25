package com.example.knackhabit.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.knackhabit.App
import com.example.knackhabit.databinding.ActivityCreateHabitBinding
import com.example.knackhabit.databinding.SelectCustomTimeDialogBinding
import com.example.knackhabit.db.HabitEntity
import com.example.knackhabit.db.MainViewModel
import com.example.knackhabit.db.MainViewModelFactory
import java.util.Calendar

class CreateHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateHabitBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((applicationContext as App).database)
    }

    private var habitToUpdate: HabitEntity? = null
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout) { v, insets ->
            val h = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updateLayoutParams<ConstraintLayout.LayoutParams> { topMargin = h }
            insets
        }

        binding.imBack.setOnClickListener { finish() }
        binding.tvTime.setOnClickListener { showTimeDialog() }

        habitToUpdate = intent.getSerializableExtra("update") as? HabitEntity
        if (habitToUpdate == null) {
            binding.btnDelete.visibility = View.GONE
            binding.btnSave.text = "Создать"
        } else {
            habitToUpdate!!.let { h ->
                binding.edHabit.setText(h.title)
                binding.daysView.setSelectedDays(h.daysOfWeek)
                binding.tvTitle.text = h.title
                binding.edHabit.hint = "Впишите новую привычку"
                // если есть время — считаем, что напоминание включено
                binding.switch1.isChecked = (h.reminderTime != null)
                h.reminderTime?.let { binding.tvTime.text = it }
            }
            binding.btnSave.text = "Сохранить"
            binding.btnDelete.visibility = View.VISIBLE
        }

        binding.btnSave.setOnClickListener { saveHabit() }
        binding.btnDelete.setOnClickListener { deleteHabit() }
        binding.imReset.setOnClickListener { resetData() }
        binding.tvReset.setOnClickListener { resetData() }
    }

    private fun saveHabit() {
        val title = binding.edHabit.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show()
            return
        }
        val days = binding.daysView.getSelectedDays()
        if (days.isEmpty()) {
            Toast.makeText(this, "Выберите дни", Toast.LENGTH_SHORT).show()
            return
        }
        val reminderEnabled = binding.switch1.isChecked
        val time = if (reminderEnabled) binding.tvTime.text.toString() else null

        val entity = HabitEntity(
            id = habitToUpdate?.id,
            title = title,
            daysOfWeek = days,
            reminderTime = time
        )
        viewModel.upsertHabit(entity)
        finish()
    }

    private fun deleteHabit() {
        habitToUpdate?.let {
            viewModel.deleteHabit(it)
            finish()
        }
    }

    private fun resetData() {
        binding.edHabit.text?.clear()
        binding.daysView.setSelectedDays(emptyList())
        binding.switch1.isChecked = false
        binding.tvTime.text = "время"
        selectedHour = null
        selectedMinute = null
    }

    private fun showTimeDialog() {
        val dlgB = SelectCustomTimeDialogBinding.inflate(layoutInflater)
        val dlg = AlertDialog.Builder(this)
            .setView(dlgB.root)
            .create()
            .apply {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                show()
            }

        val cal = Calendar.getInstance()

        dlgB.numberPickerHours.apply {
            minValue = 0
            maxValue = 23
            wrapSelectorWheel = true
            setFormatter { String.format("%02d", it) }
            value = selectedHour ?: cal.get(Calendar.HOUR_OF_DAY)
        }

        dlgB.numberPickerMinutes.apply {
            minValue = 0
            maxValue = 59
            wrapSelectorWheel = true
            setFormatter { String.format("%02d", it) }
            value = selectedMinute ?: cal.get(Calendar.MINUTE)
        }

        dlgB.btnApply.setOnClickListener {
            val h = dlgB.numberPickerHours.value
            val m = dlgB.numberPickerMinutes.value
            selectedHour = h
            selectedMinute = m
            binding.tvTime.text = String.format("%02d:%02d", h, m)
            dlg.dismiss()
        }
    }
}