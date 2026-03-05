package com.example.cashflowin.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.Debt
import com.example.cashflowin.databinding.ItemDebtBinding
import java.text.NumberFormat
import java.util.Locale

class DebtsAdapter(
    private var debts: List<Debt>,
    private val onItemClick: (Debt) -> Unit
) : RecyclerView.Adapter<DebtsAdapter.DebtViewHolder>() {

    fun updateData(newDebts: List<Debt>) {
        debts = newDebts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val binding = ItemDebtBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DebtViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val debt = debts[position]
        holder.bind(debt)
        holder.itemView.setOnClickListener { onItemClick(debt) }
    }

    override fun getItemCount(): Int = debts.size

    class DebtViewHolder(private val binding: ItemDebtBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(debt: Debt) {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            
            binding.tvPersonName.text = debt.person_name
            binding.tvType.text = if (debt.type == "debt") "Hutang Saya" else "Piutang (Dihutangi)"
            
            val remaining = debt.remaining_amount ?: debt.amount
            binding.tvRemainingAmount.text = format.format(remaining)
            
            if (debt.due_date != null) {
                binding.tvDueDate.visibility = android.view.View.VISIBLE
                binding.tvDueDate.text = "Jatuh Tempo: ${debt.due_date}"
            } else {
                binding.tvDueDate.visibility = android.view.View.GONE
            }

            binding.tvStatus.text = debt.status.uppercase()
            if (debt.status == "paid") {
                binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#059669")) // Emerald 600
                binding.tvRemainingAmount.setTextColor(android.graphics.Color.parseColor("#059669"))
            } else {
                binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#DC2626")) // Red 600
                binding.tvRemainingAmount.setTextColor(android.graphics.Color.parseColor("#DC2626"))
            }
        }
    }
}
