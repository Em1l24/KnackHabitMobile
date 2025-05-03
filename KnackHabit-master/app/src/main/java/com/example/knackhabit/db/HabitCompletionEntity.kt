package com.example.knackhabit.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completion",            // singular
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habit_id"),
        Index("date_completed")
    ]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "habit_id")
    val habitId: Int,

    /** формат YYYY-MM-DD */
    @ColumnInfo(name = "date_completed")
    val date: String
)