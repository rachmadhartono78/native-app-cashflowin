package com.example.cashflowin.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.Goal
import com.example.cashflowin.databinding.ItemGoalBinding
import java.text.NumberFormat
import java.util.Locale

class GoalAdapter(
    private var goals: List<Goal>,
    private val onItemClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    fun updateData(newGoals: List<Goal>) {
        goals = newGoals
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.bind(goal)
        holder.itemView.setOnClickListener { onItemClick(goal) }
    }

    override fun getItemCount(): Int = goals.size

    class GoalViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: Goal) {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            
            binding.tvGoalName.text = goal.name
            binding.tvTargetAmount.text = format.format(goal.target_amount)
            binding.tvCurrentAmount.text = "Terkumpul: ${format.format(goal.current_amount)}"
            
            if (goal.deadline != null) {
                binding.tvDeadline.visibility = android.view.View.VISIBLE
                binding.tvDeadline.text = goal.deadline
            } else {
                binding.tvDeadline.visibility = android.view.View.GONE
            }
            
            val progress = if (goal.target_amount > 0) (goal.current_amount / goal.target_amount * 100).toInt() else 0
            binding.progressGoal.progress = progress
        }
    }
}