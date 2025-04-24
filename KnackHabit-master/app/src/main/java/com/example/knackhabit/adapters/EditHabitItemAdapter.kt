package com.example.knackhabit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.knackhabit.R
import com.example.knackhabit.databinding.EditHabitsItemBinding
import com.example.knackhabit.db.HabitEntity

class EditHabitItemAdapter(
    private val listener: Listener
) : ListAdapter<HabitEntity, EditHabitItemAdapter.Holder>(Diff()) {

    interface Listener { fun editHabit(h: HabitEntity) }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        Holder(LayoutInflater.from(p.context).inflate(R.layout.edit_habits_item, p, false))

    override fun onBindViewHolder(h: Holder, pos: Int) = h.bind(getItem(pos), listener)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val b = EditHabitsItemBinding.bind(view)
        fun bind(habit: HabitEntity, l: Listener) = with(b) {
            tvName.text = habit.title
            imEdit.setOnClickListener { l.editHabit(habit) }
        }
    }

    class Diff : DiffUtil.ItemCallback<HabitEntity>() {
        override fun areItemsTheSame(a: HabitEntity, b: HabitEntity) = a.id == b.id
        override fun areContentsTheSame(a: HabitEntity, b: HabitEntity) = a == b
    }
}