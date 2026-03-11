package com.example.cashflowin.ui.planning

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.R
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
            val context = itemView.context
            // Optimasi Format: Tanpa desimal agar tidak terlalu panjang
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }
            
            binding.tvPersonName.text = debt.person_name
            
            if (debt.type == "debt") {
                binding.tvType.text = "Hutang Saya"
                binding.ivDebtIcon.setImageResource(R.drawable.ic_trending_up)
                binding.ivDebtIcon.rotation = 0f
                binding.ivDebtIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.expense))
                binding.iconContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
            } else {
                binding.tvType.text = "Piutang (Dihutangi)"
                binding.ivDebtIcon.setImageResource(R.drawable.ic_trending_up)
                binding.ivDebtIcon.rotation = 180f
                binding.ivDebtIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_green))
                binding.iconContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
            }
            
            val remaining = debt.remaining_amount ?: debt.amount
            binding.tvRemainingAmount.text = format.format(remaining)
            
            if (!debt.due_date.isNullOrEmpty()) {
                binding.tvDueDate.visibility = View.VISIBLE
                binding.tvDueDate.text = "Jatuh Tempo: ${debt.due_date}"
            } else {
                binding.tvDueDate.visibility = View.GONE
            }

            binding.tvStatus.text = debt.status.uppercase()
            
            if (debt.status == "paid") {
                val successColor = Color.parseColor("#059669")
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D1FAE5"))
                binding.tvStatus.setTextColor(successColor)
                binding.tvRemainingAmount.setTextColor(successColor)
            } else {
                val pendingColor = if (debt.type == "debt") Color.parseColor("#DC2626") else Color.parseColor("#2563EB")
                val bgPending = if (debt.type == "debt") Color.parseColor("#FEE2E2") else Color.parseColor("#DBEAFE")
                
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(bgPending)
                binding.tvStatus.setTextColor(pendingColor)
                binding.tvRemainingAmount.setTextColor(pendingColor)
            }
        }
    }
}