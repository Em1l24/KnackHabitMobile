package com.example.knackhabit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.knackhabit.R
import com.example.knackhabit.databinding.BasicHabitsItemBinding
import com.example.knackhabit.db.HabitEntity

class PredefinedHabitAdapter(
    private val listener: Listener
) : ListAdapter<HabitEntity, PredefinedHabitAdapter.Holder>(Diff()) {
    interface Listener { fun onHabitClicked(habit: HabitEntity) }

    private var existing = setOf<String>()
    private var selected = setOf<String>()

    fun updateSelections(exist: Set<String>, choose: Set<String>) {
        existing = exist
        selected = choose
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.basic_habits_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), listener, existing, selected)
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val b = BasicHabitsItemBinding.bind(view)
        fun bind(
            habit: HabitEntity,
            listener: Listener,
            exist: Set<String>,
            select: Set<String>
        ) = with(b) {
            chBox.setOnCheckedChangeListener(null)
            chBox.text = habit.title
            when {
                exist.contains(habit.title) -> {
                    chBox.isChecked = true
                    chBox.isEnabled = false
                    itemView.isEnabled = false
                }
                select.contains(habit.title) -> {
                    chBox.isChecked = true
                    chBox.isEnabled = true
                    itemView.isEnabled = true
                }
                else -> {
                    chBox.isChecked = false
                    chBox.isEnabled = true
                    itemView.isEnabled = true
                }
            }
            if (!exist.contains(habit.title)) {
                itemView.setOnClickListener { listener.onHabitClicked(habit) }
                chBox.setOnClickListener { listener.onHabitClicked(habit) }
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<HabitEntity>() {
        override fun areItemsTheSame(a: HabitEntity, b: HabitEntity) = a.title == b.title
        override fun areContentsTheSame(a: HabitEntity, b: HabitEntity) = a == b
    }
}