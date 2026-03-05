package com.example.cashflowin.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.R
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

    // Formatter dipindahkan ke luar agar lebih efisien
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

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
            val context = binding.root.context
            binding.tvCategoryName.text = transaction.category?.name ?: transaction.description ?: "Unknown"
            
            // Format Date and Time
            val dateStr = try {
                val date = inputDateFormat.parse(transaction.date)
                if (date != null) outputDateFormat.format(date) else transaction.date
            } catch (e: Exception) {
                transaction.date
            }

            val timeStr = try {
                if (transaction.createdAt != null) {
                    val timeDate = isoFormat.parse(transaction.createdAt)
                    if (timeDate != null) " • ${outputTimeFormat.format(timeDate)}" else ""
                } else ""
            } catch (e: Exception) {
                ""
            }

            binding.tvDate.text = "$dateStr$timeStr"
            
            val amountValue = (transaction.amount.toDoubleOrNull() ?: 0.0)
            var formattedAmount = currencyFormat.format(amountValue)

            if (transaction.type == "income") {
                formattedAmount = "+ $formattedAmount"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income))
                binding.viewIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.income))
            } else {
                formattedAmount = "- $formattedAmount"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense))
                binding.viewIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.expense))
            }
            binding.tvAmount.text = formattedAmount

            binding.root.setOnClickListener {
                onItemClick?.invoke(transaction)
            }
        }
    }
}
