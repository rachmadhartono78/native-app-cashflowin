package com.example.cashflowin.ui.planning

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.Budget
import com.example.cashflowin.databinding.ItemBudgetBinding
import java.text.NumberFormat
import java.util.Locale

class BudgetAdapter(
    private var budgets: List<Budget>,
    private val onItemClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    fun updateData(newBudgets: List<Budget>) {
        budgets = newBudgets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        holder.bind(budget)
        holder.itemView.setOnClickListener { onItemClick(budget) }
    }

    override fun getItemCount(): Int = budgets.size

    class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: Budget) {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val spent = budget.spent ?: 0.0
            
            binding.tvCategoryName.text = budget.category?.name ?: "Unknown Category"
            binding.tvBudgetAmount.text = format.format(budget.amount)
            binding.tvSpentAmount.text = "Terpakai: ${format.format(spent)}"
            
            val remaining = budget.amount - spent
            binding.tvRemainingBudget.text = "Sisa: ${format.format(remaining)}"
            
            val progress = if (budget.amount > 0) (spent / budget.amount * 100).toInt() else 0
            binding.progressBudget.progress = progress
            
            if (spent > budget.amount) {
                binding.progressBudget.setIndicatorColor(Color.parseColor("#EF4444")) // Red
                binding.tvRemainingBudget.setTextColor(Color.parseColor("#EF4444"))
            } else {
                binding.progressBudget.setIndicatorColor(Color.parseColor("#059669")) // Emerald
                binding.tvRemainingBudget.setTextColor(Color.parseColor("#059669"))
            }
        }
    }
}