package com.example.knackhabit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.knackhabit.databinding.EditHabitsItemBinding
import com.example.knackhabit.db.HabitWithCompletion

class EditHabitItemAdapter(
    private val listener: Listener
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

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(getItem(position), listener)

    class Holder(private val b: EditHabitsItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: HabitWithCompletion, l: Listener) = with(b) {
            // Сначала отписываемся, чтобы не сработал наш же setChecked
            chBox.setOnCheckedChangeListener(null)

            chBox.text = item.habit.title
            // теперь ставим исходное состояние из item.completed
            chBox.isChecked = item.completed

            chBox.setOnCheckedChangeListener { _, checked ->
                l.onHabitToggled(item.habit.id!!, checked)
            }
            imEdit.setOnClickListener {
                l.editHabit(item.habit.id!!)
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<HabitWithCompletion>() {
        override fun areItemsTheSame(a: HabitWithCompletion, b: HabitWithCompletion) =
            a.habit.id == b.habit.id

        override fun areContentsTheSame(a: HabitWithCompletion, b: HabitWithCompletion) =
            a == b
    }
}
