package com.example.knackhabit.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromDaysList(days: List<String>): String =
        gson.toJson(days)

    @TypeConverter
    fun toDaysList(json: String): List<String> =
        gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
}