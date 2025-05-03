package com.example.knackhabit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.knackhabit.R
import com.example.knackhabit.databinding.BasicHabitsItemBinding
import com.example.knackhabit.db.HabitWithCompletion
import com.example.knackhabit.db.HabitEntity

class BasicHabitItemAdapter(
    private val listener: Listener
) : ListAdapter<HabitWithCompletion, BasicHabitItemAdapter.Holder>(Diff()) {

    interface Listener {
        fun onCompletionChanged(habit: HabitEntity, completionId: Int?, completed: Boolean)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        Holder(LayoutInflater.from(p.context).inflate(R.layout.basic_habits_item, p, false))

    override fun onBindViewHolder(h: Holder, pos: Int) = h.bind(getItem(pos), listener)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val b = BasicHabitsItemBinding.bind(view)
        fun bind(item: HabitWithCompletion, l: Listener) = with(b) {
            chBox.setOnCheckedChangeListener(null)
            chBox.text = item.habit.title
            chBox.isChecked = item.completed == true
            chBox.setOnCheckedChangeListener { _, c ->
                l.onCompletionChanged(item.habit, item.completionId, c)
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<HabitWithCompletion>() {
        override fun areItemsTheSame(a: HabitWithCompletion, b: HabitWithCompletion) =
            a.habit.id == b.habit.id
        override fun areContentsTheSame(a: HabitWithCompletion, b: HabitWithCompletion) = a == b
    }
}