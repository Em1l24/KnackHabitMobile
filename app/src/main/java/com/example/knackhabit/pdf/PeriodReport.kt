package com.example.knackhabit.pdf

data class PeriodReport(
    val label: String,
    val items: List<HabitReportItem>
)