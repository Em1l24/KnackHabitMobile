package com.example.knackhabit.db

import androidx.lifecycle.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(private val dao: HabitDao) : ViewModel() {
    val allHabits: LiveData<List<HabitEntity>> = dao.getAllHabits().asLiveData()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayKey = MutableLiveData<String>(dateFormat.format(Date()))

    val habitsToday: LiveData<List<HabitWithCompletion>> =
        todayKey.switchMap { date -> dao.getHabitsWithCompletionByDate(date).asLiveData() }

    val scheduledHabitsToday: LiveData<List<HabitWithCompletion>> =
        todayKey.switchMap { date ->
            dao.getHabitsWithCompletionByDate(date)
                .map { list ->
                    // Парсим дату и крутилкой Calendar вычисляем день недели
                    val cal = Calendar.getInstance().apply {
                        time = dateFormat.parse(date)!!
                    }
                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    // Сопоставляем с вашими строками
                    val todayAbbrev = when (dow) {
                        Calendar.MONDAY    -> "Пн"
                        Calendar.TUESDAY   -> "Вт"
                        Calendar.WEDNESDAY -> "Ср"
                        Calendar.THURSDAY  -> "Чт"
                        Calendar.FRIDAY    -> "Пт"
                        Calendar.SATURDAY  -> "Сб"
                        Calendar.SUNDAY    -> "Вс"
                        else               -> ""
                    }
                    list.filter { hwc ->
                        hwc.habit.daysOfWeek.contains(todayAbbrev)
                    }
                }
                .asLiveData()
        }

    fun setDate(date: String) { todayKey.value = date }

    fun upsertHabit(h: HabitEntity) = viewModelScope.launch {
        if (h.id == null) dao.insertHabit(h) else dao.updateHabit(h)
    }

    fun deleteHabit(h: HabitEntity) = viewModelScope.launch {
        dao.deleteHabit(h)
    }

    fun setCompletion(habitId: Int, date: String, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            // ставим или обновляем «выполнено»
            dao.upsertCompletion(
                HabitCompletionEntity(
                    id       = null,
                    habitId  = habitId,
                    date     = date,
                    completed = true
                )
            )
        } else {
            // снимаем галочку — удаляем запись именно за этот день
            dao.deleteCompletionForDate(habitId, date)
        }
    }

    suspend fun getCompletionsBetween(start: String, end: String) =
        dao.getCompletionsBetween(start, end)
}

class MainViewModelFactory(private val db: MainDataBase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            MainViewModel(db.getDao()) as T
        } else throw IllegalArgumentException("Unknown ViewModel class")
}