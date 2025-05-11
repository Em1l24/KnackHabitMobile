package com.example.knackhabit.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.knackhabit.databinding.EditHabitsItemBinding
import com.example.knackhabit.db.HabitWithCompletion

class EditHabitItemAdapter(
    private val listener: Listener,
    private val todayAbbrev: String
) : ListAdapter<HabitWithCompletion, EditHabitItemAdapter.Holder>(Diff()) {

    interface Listener {
        fun editHabit(habitId: Int)
        fun onHabitToggled(habitId: Int, enabled: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(
            EditHabitsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), listener, todayAbbrev)
    }

    class Holder(private val b: EditHabitsItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(
            item: HabitWithCompletion,
            l: Listener,
            todayAbbrev: String
        ) = with(b) {
            // избавляемся от предыдущих слушателей
            chBox.setOnCheckedChangeListener(null)
            chBox.setOnTouchListener(null)

            chBox.text = item.habit.title
            chBox.isChecked = item.completed

            // доступна ли привычка сегодня
            val isScheduledToday = item.habit.daysOfWeek.contains(todayAbbrev)
            chBox.alpha = if (isScheduledToday) 1f else 0.5f

            // перехватываем касание, если не сегодня
            chBox.setOnTouchListener { _, event ->
                if (!isScheduledToday && event.action == MotionEvent.ACTION_UP) {
                    Toast.makeText(b.root.context,
                        "Привычка не запланирована на сегодня",
                        Toast.LENGTH_SHORT).show()
                    true
                } else {
                    false
                }
            }

            chBox.setOnCheckedChangeListener { _, checked ->
                if (isScheduledToday) {
                    l.onHabitToggled(item.habit.id!!, checked)
                }
            }

            imEdit.setOnClickListener {
                l.editHabit(item.habit.id!!)
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<HabitWithCompletion>() {
        override fun areItemsTheSame(
            a: HabitWithCompletion,
            b: HabitWithCompletion
        ) = a.habit.id == b.habit.id

        override fun areContentsTheSame(
            a: HabitWithCompletion,
            b: HabitWithCompletion
        ) = a == b
    }
}