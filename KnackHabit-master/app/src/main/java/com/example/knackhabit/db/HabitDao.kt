package com.example.knackhabit.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class HabitWithCompletion(
    @Embedded val habit: HabitEntity,
    @ColumnInfo(name = "completed") val completed: Boolean?,
    @ColumnInfo(name = "completion_id") val completionId: Int?
)

@Dao
interface HabitDao {
    // metadata
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert suspend fun insertHabit(habit: HabitEntity): Long
    @Update suspend fun updateHabit(habit: HabitEntity)
    @Delete suspend fun deleteHabit(habit: HabitEntity)

    // for a given date
    @Query(
        "SELECT h.*, c.completed AS completed, c.id AS completion_id " +
                "FROM habits h LEFT JOIN habit_completions c " +
                "ON h.id = c.habit_id AND c.date = :date"
    )
    fun getHabitsWithCompletionByDate(date: String): Flow<List<HabitWithCompletion>>

    // upsert completion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(entry: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habit_id = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Int)

    @Query(
        "DELETE FROM habit_completions " +
                "WHERE habit_id = :habitId AND date = :date"
    )
    suspend fun deleteCompletionForDate(habitId: Int, date: String)

    @Query(
        "SELECT * FROM habit_completions WHERE date BETWEEN :startDate AND :endDate"
    )
    suspend fun getCompletionsBetween(startDate: String, endDate: String): List<HabitCompletionEntity>
}