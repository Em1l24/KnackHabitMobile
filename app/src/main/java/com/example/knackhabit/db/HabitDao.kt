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

    /** true, если для этой даты есть row в habit_completion */
    @ColumnInfo(name = "completed")
    val completed: Boolean,

    @ColumnInfo(name = "completion_id")
    val completionId: Int?
)

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert suspend fun insertHabit(habit: HabitEntity): Long
    @Update suspend fun updateHabit(habit: HabitEntity)
    @Delete suspend fun deleteHabit(habit: HabitEntity)

    @Query(
        """
        SELECT
          h.*,
          CASE WHEN c.id IS NOT NULL THEN 1 ELSE 0 END    AS completed,
          c.id                                           AS completion_id
        FROM habits h
        LEFT JOIN habit_completion c
          ON h.id = c.habit_id AND c.date_completed = :date
        """
    )
    fun getHabitsWithCompletionByDate(date: String): Flow<List<HabitWithCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(entry: HabitCompletionEntity)

    @Query("DELETE FROM habit_completion WHERE habit_id = :habitId AND date_completed = :date")
    suspend fun deleteCompletionForDate(habitId: Int, date: String)

    @Query("DELETE FROM habit_completion WHERE habit_id = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Int)

    @Query("SELECT * FROM habit_completion WHERE date_completed BETWEEN :startDate AND :endDate")
    suspend fun getCompletionsBetween(startDate: String, endDate: String): List<HabitCompletionEntity>
}