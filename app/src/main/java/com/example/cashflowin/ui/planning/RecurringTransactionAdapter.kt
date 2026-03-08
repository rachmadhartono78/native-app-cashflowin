package com.example.cashflowin.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.RecurringTransaction
import com.example.cashflowin.databinding.ItemRecurringTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RecurringTransactionAdapter(
    private var transactions: List<RecurringTransaction>,
    private val onPauseResumeClick: (RecurringTransaction) -> Unit
) : RecyclerView.Adapter<RecurringTransactionAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    
    // API Format: 2026-03-08 17:00:00 or 2026-03-08T17:00:00Z
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    inner class ViewHolder(private val binding: ItemRecurringTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: RecurringTransaction) {
            binding.apply {
                tvName.text = transaction.name
                tvCategory.text = transaction.category_name ?: "Tanpa Kategori"
                tvAmount.text = currencyFormat.format(transaction.amount)
                
                // Format Date
                val formattedDate = transaction.next_execution_date?.let { dateStr ->
                    try {
                        // Handle possible 'T' and 'Z' in ISO 8601 strings if they appear
                        val cleanedDate = dateStr.replace("T", " ").replace("Z", "")
                        val date = apiDateFormat.parse(cleanedDate)
                        date?.let { displayDateFormat.format(it) } ?: dateStr
                    } catch (e: Exception) {
                        dateStr
                    }
                } ?: "N/A"
                
                tvNextExecution.text = "Selanjutnya: $formattedDate"
                
                // Set type indicator with badge style
                tvType.text = when (transaction.type) {
                    "income" -> "Pemasukan"
                    "expense" -> "Pengeluaran"
                    else -> transaction.type.replaceFirstChar { it.uppercase() }
                }
                
                // Set frequency badge
                tvFrequency.text = when (transaction.frequency) {
                    "daily" -> "Harian"
                    "weekly" -> "Mingguan"
                    "monthly" -> "Bulanan"
                    "yearly" -> "Tahunan"
                    else -> transaction.frequency.replaceFirstChar { it.uppercase() }
                }
                
                // Set active status and button style
                if (transaction.is_active) {
                    root.alpha = 1.0f
                    btnPauseResume.text = "Pause"
                    btnPauseResume.setBackgroundColor(android.graphics.Color.parseColor("#EF4444")) // Red for pause
                } else {
                    root.alpha = 0.6f
                    btnPauseResume.text = "Resume"
                    btnPauseResume.setBackgroundColor(android.graphics.Color.parseColor("#10B981")) // Green for resume
                }
                
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
