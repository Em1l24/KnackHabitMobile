package com.example.knackhabit.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable

@Entity(tableName = "habits")
@TypeConverters(Converters::class)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "name")
    val title: String,

    @ColumnInfo(name = "frequency")
    val daysOfWeek: List<String>,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String?
) : Serializable