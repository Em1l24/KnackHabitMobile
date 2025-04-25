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

    // в БД столбец называется name
    @ColumnInfo(name = "name")
    val title: String,

    // дни недели → frequency
    @ColumnInfo(name = "frequency")
    val daysOfWeek: List<String>,

    // если напоминание включено, сюда пишем время, иначе null
    @ColumnInfo(name = "reminder_time")
    val reminderTime: String?
) : Serializable