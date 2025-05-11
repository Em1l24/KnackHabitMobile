package com.example.knackhabit.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.knackhabit.databinding.ActivityWeeklyReportBinding
import com.example.knackhabit.pdf.HabitReportItem
import com.example.knackhabit.pdf.PDFReportGenerator
import com.example.knackhabit.pdf.PeriodReport
import android.graphics.Color
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.knackhabit.db.HabitCompletionEntity
import com.example.knackhabit.db.HabitEntity
import com.example.knackhabit.db.MainDataBase
import com.example.knackhabit.db.MainViewModel
import com.example.knackhabit.db.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.mapIndexed

class ActivityWeeklyReport : AppCompatActivity() {
    private lateinit var binding: ActivityWeeklyReportBinding
    private lateinit var viewModel: MainViewModel

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

        // ViewModel
        val db = MainDataBase.getDataBase(applicationContext)
        viewModel = MainViewModelFactory(db).create(MainViewModel::class.java)

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Download PDF
        binding.btnDownloadPDF.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val reports = generateReportsForAllWeeks()
                val saved = PDFReportGenerator.createHabitReportPdf(
                    this@ActivityWeeklyReport,
                    "HabitReport",
                    reports
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ActivityWeeklyReport,
                        if (saved) "PDF сохранён в Загрузки" else "Ошибка сохранения PDF",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun generateReportsForAllWeeks(): List<PeriodReport> {
        val dao = MainDataBase.getDataBase(applicationContext).getDao()
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completions = dao.getCompletionsBetween("2000-01-01", todayKey)
        val habits = dao.getAllHabits().first()

        if (completions.isEmpty()) return listOf(createPeriodForWeek(Date(), habits, completions))

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val minDateStr = completions.minByOrNull { sdf.parse(it.date)!! }!!.date
        val startMonday = Calendar.getInstance().apply {
            time = sdf.parse(minDateStr)!!
            firstDayOfWeek = Calendar.MONDAY
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) add(Calendar.DATE, -1)
        }.time
        val currentMonday = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) add(Calendar.DATE, -1)
        }.time

        val periodReports = mutableListOf<PeriodReport>()
        val weekCal = Calendar.getInstance().apply { time = startMonday }
        while (!weekCal.time.after(currentMonday)) {
            periodReports.add(createPeriodForWeek(weekCal.time, habits, completions))
            weekCal.add(Calendar.DATE, 7)
        }
        return periodReports
    }

    private fun createPeriodForWeek(
        startDate: Date,
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>
    ): PeriodReport {
        val fmtKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fmtLabel = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val days = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")
        val cal = Calendar.getInstance().apply { time = startDate }
        val labelStart = fmtLabel.format(cal.time)
        cal.add(Calendar.DATE, 6)
        val labelEnd = fmtLabel.format(cal.time)
        val periodLabel = "$labelStart – $labelEnd"

        val items = habits.mapIndexed { index, habit ->
            val scheduled = mutableListOf<Boolean>()
            val checks = mutableListOf<Boolean>()
            val dayCal = Calendar.getInstance().apply { time = startDate }
            repeat(7) {
                val dow = dayCal.get(Calendar.DAY_OF_WEEK)
                val idx = when (dow) {
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    Calendar.SUNDAY -> 6
                    else -> 0
                }
                scheduled.add(habit.daysOfWeek.contains(days[idx]))
                val key = fmtKey.format(dayCal.time)
                checks.add(completions.any { it.habitId == habit.id && it.date == key })
                dayCal.add(Calendar.DATE, 1)
            }
            val totalScheduled = scheduled.count { it }
            val doneCount = checks.zip(scheduled).count { it.first && it.second }
            val percent = if (totalScheduled > 0) doneCount * 100 / totalScheduled else 0
            val color = Color.HSVToColor(floatArrayOf((index * 40f) % 360f, 0.8f, 0.8f))
            HabitReportItem(habit.title, scheduled, checks, percent, color)
        }
        return PeriodReport(periodLabel, items)
    }
}
