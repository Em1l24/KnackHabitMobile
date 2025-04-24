package com.example.knackhabit.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable

@Entity(tableName = "habits")
@TypeConverters(Converters::class)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val daysOfWeek: List<String>,
    val reminderEnabled: Boolean,
    val reminderTime: String?
) : Serializable