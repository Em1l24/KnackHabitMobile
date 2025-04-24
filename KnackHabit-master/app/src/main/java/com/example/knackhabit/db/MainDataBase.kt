package com.example.knackhabit.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MainDataBase : RoomDatabase() {
    abstract fun getDao(): HabitDao

    companion object {
        @Volatile private var INSTANCE: MainDataBase? = null
        fun getDataBase(context: Context): MainDataBase =
            INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    MainDataBase::class.java,
                    "knack_habit.db"
                ).build()
                INSTANCE = inst
                inst
            }
    }
}