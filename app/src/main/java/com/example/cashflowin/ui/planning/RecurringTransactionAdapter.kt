package com.example.cashflowin.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.RecurringTransaction
import com.example.cashflowin.databinding.ItemRecurringTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RecurringTransactionAdapter(
    private var transactions: List<RecurringTransaction>,
    private val onPauseResumeClick: (RecurringTransaction) -> Unit
) : RecyclerView.Adapter<RecurringTransactionAdapter.ViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    inner class ViewHolder(private val binding: ItemRecurringTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: RecurringTransaction) {
            binding.apply {
                tvName.text = transaction.name
                tvCategory.text = transaction.category_name ?: "Unknown"
                tvAmount.text = format.format(transaction.amount)
                tvNextExecution.text = "Selanjutnya: ${transaction.next_execution_date ?: "N/A"}"
                
                // Set type indicator
                tvType.text = when (transaction.type) {
                    "income" -> "Pemasukan"
                    "expense" -> "Pengeluaran"
                    else -> transaction.type
                }
                
                // Set frequency badge
                tvFrequency.text = when (transaction.frequency) {
                    "daily" -> "Harian"
                    "weekly" -> "Mingguan"
                    "monthly" -> "Bulanan"
                    "yearly" -> "Tahunan"
                    else -> transaction.frequency
                }
                
                // Set active status and button
                if (transaction.is_active) {
                    root.alpha = 1.0f
                    btnPauseResume.text = "Pause"
                    btnPauseResume.setBackgroundColor(android.graphics.Color.parseColor("#EF4444")) // Red for pause
                } else {
                    root.alpha = 0.5f
                    btnPauseResume.text = "Resume"
                    btnPauseResume.setBackgroundColor(android.graphics.Color.parseColor("#10B981")) // Green for resume
                }
                
                // Set click listener for pause/resume button
                btnPauseResume.setOnClickListener {
                    onPauseResumeClick(transaction)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecurringTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newData: List<RecurringTransaction>) {
        transactions = newData
        notifyDataSetChanged()
    }
}
