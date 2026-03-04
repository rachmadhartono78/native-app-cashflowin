package com.example.cashflowin.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.TransactionItem
import com.example.cashflowin.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TransactionAdapter(
    private val onItemClick: ((TransactionItem) -> Unit)? = null
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactions: List<TransactionItem> = listOf()

    fun submitList(list: List<TransactionItem>) {
        transactions = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: TransactionItem) {
            binding.tvCategoryName.text = transaction.category?.name ?: transaction.description ?: "Unknown"
            
            // Format Date and Time
            val dateStr = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(transaction.date)
                if (date != null) outputFormat.format(date) else transaction.date
            } catch (e: Exception) {
                transaction.date
            }

            val timeStr = try {
                if (transaction.createdAt != null) {
                    // Typical API timestamp: 2023-10-27T14:30:00.000000Z
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val timeDate = isoFormat.parse(transaction.createdAt)
                    val outputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                        timeZone = TimeZone.getDefault()
                    }
                    if (timeDate != null) " • ${outputTimeFormat.format(timeDate)}" else ""
                } else ""
            } catch (e: Exception) {
                // Fallback for different format yyyy-MM-dd HH:mm:ss
                try {
                    val fallbackFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    val timeDate = fallbackFormat.parse(transaction.createdAt ?: "")
                    val outputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    if (timeDate != null) " • ${outputTimeFormat.format(timeDate)}" else ""
                } catch (e2: Exception) {
                    ""
                }
            }

            binding.tvDate.text = "$dateStr$timeStr"
            
            val amountValue = transaction.amount.toDoubleOrNull() ?: 0.0
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formattedAmount = format.format(amountValue)

            binding.tvAmount.text = formattedAmount
            
            if (transaction.type == "income") {
                binding.tvAmount.setTextColor(Color.parseColor("#10B981")) // Green
            } else {
                binding.tvAmount.setTextColor(Color.parseColor("#EF4444")) // Red
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(transaction)
            }
        }
    }
}
