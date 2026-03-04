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
            
            // Format Date to more readable style
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(transaction.date)
                binding.tvDate.text = if (date != null) outputFormat.format(date) else transaction.date
            } catch (e: Exception) {
                binding.tvDate.text = transaction.date
            }
            
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
