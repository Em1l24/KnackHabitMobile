package com.example.knackhabit.pdf

data class HabitReportItem(
    val title: String,
    val scheduled: List<Boolean>,
    val checks: List<Boolean>,
    val percent: Int,
    val barColor: Int
)