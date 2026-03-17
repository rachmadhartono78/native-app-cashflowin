package com.example.cashflowin.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
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

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
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
            
            // Perbaikan 1: Tampilkan keterangan transaksi
            binding.tvCategoryName.text = transaction.category?.name ?: "Lainnya"
            if (!transaction.description.isNullOrEmpty()) {
                binding.tvDescription.text = transaction.description
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.visibility = View.GONE
            }
            
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
            
            val amountValue = transaction.amount
            val formattedAmount = currencyFormat.format(amountValue)

            val categoryColor = transaction.category?.color
            val categoryIcon = transaction.category?.icon
            
            if (!categoryColor.isNullOrEmpty()) {
                try {
                    val color = Color.parseColor(categoryColor)
                    binding.cardIcon.setCardBackgroundColor(color)
                    binding.cardIcon.backgroundTintList = ColorStateList.valueOf(color).withAlpha(40)
                    binding.ivCategoryIcon.imageTintList = ColorStateList.valueOf(color)
                } catch (e: Exception) {
                    setDefaultStyle(transaction)
                }
            } else {
                setDefaultStyle(transaction)
            }

            if (!categoryIcon.isNullOrEmpty()) {
                val resId = context.resources.getIdentifier(categoryIcon, "drawable", context.packageName)
                if (resId != 0) {
                    binding.ivCategoryIcon.setImageResource(resId)
                }
            }

            if (transaction.type == "income") {
                binding.tvAmount.text = "+$formattedAmount"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income))
            } else {
                binding.tvAmount.text = "-$formattedAmount"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense))
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(transaction)
            }
        }

        private fun setDefaultStyle(transaction: TransactionItem) {
            val context = binding.root.context
            if (transaction.type == "income") {
                binding.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_green_light))
                binding.ivCategoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_green))
            } else {
                binding.cardIcon.setCardBackgroundColor(Color.parseColor("#F1F5F9"))
                binding.ivCategoryIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#64748B"))
            }
        }
    }
}
